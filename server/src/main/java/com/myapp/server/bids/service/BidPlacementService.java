package com.myapp.server.bids.service;

import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.dto.PlaceBidResponse;
import com.myapp.server.bids.repository.BidsDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Handles the complex orchestration of bid placement.
 * Manages transactions, locking, validations, and bid processing.
 */
@Service
@RequiredArgsConstructor
public class BidPlacementService {
    
    private final BidsDao dao;
    private final BiddingPolicy biddingPolicy;
    
    @Transactional
    public PlaceBidResponse placeBid(long auctionId, PlaceBidRequest req, Long currentUserId) {
        Long bidderId = currentUserId; // trust session, ignore client userId
        var auction = dao.lockAuctionForUpdate(auctionId); // row lock for concurrency
        
        // A) Basic validations
        biddingPolicy.validateAuctionState(auction, bidderId);
        
        // Determine current public minimum required
        BigDecimal minToPlace = biddingPolicy.calculateMinBidForChallenger(
            auction.currentBid(), auction.minPrice(), auction.bidIncrement(), auction.bidsCount()
        );
        
        // B) Validate bid amount - all bids must meet the minimum requirement
        if (auction.bidsCount() == 0) {
            // First bid: must be at least starting price
            biddingPolicy.validateFirstBid(req.maxBid(), auction.minPrice());
        } else {
            // Check if bidder is current leader
            BigDecimal userPrevMax = dao.getUserPrevMax(auctionId, bidderId);
            boolean isCurrentLeader = auction.highestUserId() != null && 
                                    auction.highestUserId().equals(bidderId);
            
            if (isCurrentLeader && userPrevMax != null) {
                // Leader bidding again: must increase their own max AND meet general minimum
                biddingPolicy.validateLeaderBidIncrease(req.maxBid(), userPrevMax);
                biddingPolicy.validateChallengerBid(req.maxBid(), minToPlace);
            } else {
                // Non-leader (challenger): must be at least current price + increment
                biddingPolicy.validateChallengerBid(req.maxBid(), minToPlace);
            }
        }
        
        // C) Execute bid placement
        return executeBidPlacement(auctionId, bidderId, req.maxBid(), auction);
    }
    
    private PlaceBidResponse executeBidPlacement(long auctionId, Long bidderId, BigDecimal maxBid, 
                                               BidsDao.AuctionRow auction) {
        OffsetDateTime now = OffsetDateTime.now();
        
        // 1) Insert/upsert bidder's max bid
        Long bidId = dao.insertBidReturningId(auctionId, bidderId, maxBid);
        
        // 2) Recompute leader & runner-up (top-2)
        var top = dao.getTopBids(auctionId, 2);
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
        
        // 3) Calculate new current price using second-price rules
        BigDecimal newCurrent = biddingPolicy.calculateNewCurrentPrice(
            auction.minPrice(), auction.bidIncrement(), leaderMax, runnerMax
        );
        
        // 4) Create bid history snapshots
        createBidHistorySnapshots(auctionId, bidId, bidderId, maxBid, auction, 
                                leaderUserId, leaderMax, runnerUserId, runnerMax, newCurrent, now);
        
        // 5) Update auction state
        dao.updateAuctionAfterBid(auctionId, 
                                leaderUserId != null ? leaderUserId : bidderId, 
                                leaderMax != null ? leaderMax : maxBid, 
                                newCurrent);
        
        // 6) Build response
        return buildPlaceBidResponse(auctionId, bidderId, leaderUserId, newCurrent, auction);
    }
    
    private void createBidHistorySnapshots(long auctionId, Long bidId, Long bidderId, BigDecimal maxBid,
                                         BidsDao.AuctionRow auction, Long leaderUserId, BigDecimal leaderMax,
                                         Long runnerUserId, BigDecimal runnerMax, BigDecimal newCurrent,
                                         OffsetDateTime now) {
        
        boolean currentChanged = auction.currentBid() == null || newCurrent.compareTo(auction.currentBid()) != 0;
        boolean userIsLeader = leaderUserId != null && leaderUserId.equals(bidderId);
        
        // Only create history snapshots when the public price changes
        if (currentChanged) {
            if (runnerUserId == null) {
                // First bid with price change
                dao.insertBidSnapshot(auctionId, bidId, bidderId, newCurrent, "USER_BID", "ידני", now);
            } else {
                if (userIsLeader) {
                    // Bidder is/remains leader and public price changed
                    dao.insertBidSnapshot(auctionId, bidId, bidderId, newCurrent, "USER_BID", "ידני", now);
                } else {
                    // Challenger scenarios
                    if (leaderMax != null && maxBid.compareTo(leaderMax) < 0) {
                        // Challenger below leader → USER_BID then AUTO_RAISE
                        dao.insertBidSnapshot(auctionId, bidId, bidderId, maxBid, "USER_BID", "ידני", now);
                        dao.insertBidSnapshot(auctionId, bidId, leaderUserId, newCurrent, "AUTO_RAISE", "אוטומטי", now.plusNanos(1000000));
                    } else if (leaderMax != null && maxBid.compareTo(leaderMax) == 0 && 
                              leaderUserId != null && !leaderUserId.equals(bidderId)) {
                        // Tie on max → USER_BID + TIE_AUTO
                        dao.insertBidSnapshot(auctionId, bidId, bidderId, leaderMax, "USER_BID", "ידני", now);
                        dao.insertBidSnapshot(auctionId, bidId, leaderUserId, leaderMax, "TIE_AUTO", "אוטומטי", now.plusNanos(1000000));
                    } else {
                        // New highest leader
                        dao.insertBidSnapshot(auctionId, bidId, bidderId, newCurrent, "USER_BID", "ידני", now);
                    }
                }
            }
        }
        // If !currentChanged, no history is created (leader raised max without public price change)
    }
    
    private PlaceBidResponse buildPlaceBidResponse(long auctionId, Long bidderId, Long leaderUserId, 
                                                 BigDecimal newCurrent, BidsDao.AuctionRow auction) {
        boolean youAreLeading = leaderUserId != null ? leaderUserId.equals(bidderId) : true;
        BigDecimal minNextBid = biddingPolicy.calculateNextMinBid(newCurrent, auction.bidIncrement());
        
        // Get the accurate count from bid_history_snapshots table
        int bidsCountPublic = dao.countBidHistorySnapshots(auctionId);
        
        // Fetch updated auction state for endDate
        var updatedAuction = dao.getAuction(auctionId);
        
        return new PlaceBidResponse(
                auctionId,
                leaderUserId != null ? leaderUserId : bidderId,
                null, // do not expose highestMaxBid
                newCurrent,
                bidsCountPublic,
                minNextBid,
                youAreLeading,
                updatedAuction != null ? updatedAuction.endDate() : auction.endDate()
        );
    }
}
