package com.myapp.server.bids.service;

import com.myapp.server.bids.repository.BidsDao;
import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.dto.PlaceBidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class BidsService {

    private final BidsDao dao;

    @Transactional
    public PlaceBidResponse placeBid(long auctionId, PlaceBidRequest req, Long currentUserId) {
        Long bidderId = currentUserId; // ignore client userId; trust session
        var a = dao.lockAuctionForUpdate(auctionId); // row lock

        // A) Validation
        if (!"active".equalsIgnoreCase(a.status())) {
            throw new ResponseStatusException(CONFLICT, "המכרז אינו פעיל");
        }
        if (a.endDate() != null && a.endDate().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(CONFLICT, "המכרז הסתיים");
        }
        if (bidderId.equals(a.sellerId())) {
            throw new ResponseStatusException(FORBIDDEN, "לא ניתן להגיש הצעה למכרז של עצמך");
        }

        // Determine current public minimum required
        BigDecimal minToPlace = (a.bidsCount() == 0)
                ? a.minPrice()
                : a.currentBid().add(a.bidIncrement());

        // Fetch bidder's previous max (if any) and whether they lead
        BigDecimal userPrevMax = dao.getUserPrevMax(auctionId, bidderId);
        boolean userIsLeader = a.highestUserId() != null && a.highestUserId().equals(bidderId);

        // Specific min-bid rules per leader/challenger
        if (a.bidsCount() == 0) {
            if (req.maxBid().compareTo(a.minPrice()) < 0) {
                throw new ResponseStatusException(BAD_REQUEST, "ההצעה נמוכה מהמחיר ההתחלתי");
            }
        } else if (userIsLeader) {
            // Leader raising own max: strictly greater than previous
            // Leader doesn't need to meet the public minimum (current+increment)
            if (userPrevMax == null || req.maxBid().compareTo(userPrevMax) <= 0) {
                throw new ResponseStatusException(BAD_REQUEST, "עליך להעלות מעל ההצעה הקודמת שלך");
            }
            // Note: Leader doesn't need to check minToPlace - they just need to beat their own previous bid
        } else {
            // Challenger must be >= current+increment
            if (req.maxBid().compareTo(minToPlace) < 0) {
                throw new ResponseStatusException(BAD_REQUEST, "ההצעה נמוכה מהמינימום המותר");
            }
        }

        // C) Writes begin
        OffsetDateTime now = OffsetDateTime.now();

        // 1) Upsert/insert bidder's maxBid (keep bidId for history linking)
        Long bidId = dao.insertBidReturningId(auctionId, bidderId, req.maxBid());

        // 2) Recompute leader & runner-up (top-2)
        var top = dao.getTopBids(auctionId, 2);
        // If same user inserted twice (should be single row due to upsert), top may have one row
        Long leaderUserId = null;
        BigDecimal leaderMax = null;
        Long runnerUserId = null;
        BigDecimal runnerMax = null;
        if (!top.isEmpty()) {
            leaderUserId = top.get(0).userId();
            leaderMax = top.get(0).maxBid();
            if (top.size() > 1 && top.get(1).userId() != leaderUserId) {
                runnerUserId = top.get(1).userId();
                runnerMax = top.get(1).maxBid();
            }
        }

        // B) Second-Price current calculation
        BigDecimal newCurrent;
        if (runnerUserId == null) {
            newCurrent = a.minPrice();
        } else {
            BigDecimal candidate = runnerMax.add(a.bidIncrement());
            newCurrent = candidate.min(leaderMax);
        }

        // C4) Public Bid History with deterministic ordering
        // Decide if challenger below leader, tie, or new highest
    boolean currentChanged = a.currentBid() == null || newCurrent.compareTo(a.currentBid()) != 0;
    // Re-evaluate userIsLeader against recomputed leader
    userIsLeader = leaderUserId != null && leaderUserId.equals(bidderId);

        // We will write history according to rules:
        if (runnerUserId == null) {
            // First bid: public current remains min_price. Create history row for first bid.
            dao.insertBidSnapshot(auctionId, bidId, bidderId, newCurrent, "USER_BID", now);
        } else {
            if (!currentChanged) {
                // Leader raised own max without public price change → no history
                if (!userIsLeader) {
                    // Challenger placed but didn't change current (shouldn't happen with our validations), skip
                }
            } else {
                if (leaderUserId != null && leaderUserId.equals(bidderId)) {
                    // Bidder is (or remains) leader and public changed (e.g., tie promoted current to leaderMax)
                    dao.insertBidSnapshot(auctionId, bidId, bidderId, newCurrent, "USER_BID", now);
                } else {
                    // Challenger below leader or overtakes
                    // Case D explicit: challengerMax below leaderMax → two rows
                    if (leaderMax != null && req.maxBid().compareTo(leaderMax) < 0) {
                        // USER_BID then AUTO_RAISE; AUTO_RAISE gets slightly later timestamp for chronological order
                        dao.insertBidSnapshot(auctionId, bidId, bidderId, req.maxBid(), "USER_BID", now);
                        dao.insertBidSnapshot(auctionId, bidId, leaderUserId, newCurrent, "AUTO_RAISE", now.plusNanos(1000000));
                    } else if (leaderMax != null && req.maxBid().compareTo(leaderMax) == 0 && leaderUserId != null && !leaderUserId.equals(bidderId)) {
                        // Tie on max → USER_BID + TIE_AUTO; TIE_AUTO gets slightly later timestamp for chronological order
                        dao.insertBidSnapshot(auctionId, bidId, bidderId, leaderMax, "USER_BID", now);
                        dao.insertBidSnapshot(auctionId, bidId, leaderUserId, leaderMax, "TIE_AUTO", now.plusNanos(1000000));
                    } else {
                        // New highest leader; public current becomes min(leaderMax, runner+inc)
                        dao.insertBidSnapshot(auctionId, bidId, bidderId, newCurrent, "USER_BID", now);
                        // No AUTO_RAISE because bidder became leader and public price is attributable to challenger+inc cap
                    }
                }
            }
        }

    // 3) Update auctions atomically – increment bids_count per attempt
    dao.updateAuctionAfterBid(auctionId, leaderUserId != null ? leaderUserId : bidderId, leaderMax != null ? leaderMax : req.maxBid(), newCurrent);

        // E) Response: only public values
    boolean youAreLeading = leaderUserId != null ? leaderUserId.equals(bidderId) : true;
    BigDecimal minNextBid = newCurrent.add(a.bidIncrement());

    // Fetch updated public auction snapshot
    var updatedAuction = dao.getAuction(auctionId);
    int bidsCountPublic = updatedAuction != null ? updatedAuction.bidsCount() : a.bidsCount() + 1;
        return new PlaceBidResponse(
                auctionId,
                leaderUserId != null ? leaderUserId : bidderId,
                null, // do not expose highestMaxBid
                newCurrent,
                bidsCountPublic,
                minNextBid,
                youAreLeading,
                updatedAuction != null ? updatedAuction.endDate() : a.endDate()
        );
    }

    // Bid history implementation
    public record BidHistoryItem(Long snapshotId, Long bidId, Long bidderId,
                                  BigDecimal displayedBid, OffsetDateTime snapshotTime, String kind) {}

    public List<BidHistoryItem> getHistory(long auctionId) {
        var rows = dao.getBidHistory(auctionId);
        return rows.stream()
                .map(row -> new BidHistoryItem(
                        row.snapshotId(),
                        row.bidId(),
                        row.bidderId(),
                        row.displayedBid(),
                        row.snapshotTime(),
                        row.kind()
                ))
                .toList();
    }

    public List<com.myapp.server.bids.dto.UserBidSummaryItem> getUserBidsSummary(Long userId) {
        List<BidsDao.UserBidSummaryRow> rows = dao.getUserBidsSummary(userId);
        
        return rows.stream()
                .map(row -> new com.myapp.server.bids.dto.UserBidSummaryItem(
                        row.auctionId(),
                        row.auctionTitle(),
                        row.currentPrice(),
                        row.yourMax(),
                        row.endDate(),
                        row.leading(),
                        row.status()
                ))
                .toList();
    }
}
