package com.myapp.server.bids.service;

import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.dto.PlaceBidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Facade service for bid operations.
 * Delegates to specialized services for clear separation of concerns.
 */
@Service
@RequiredArgsConstructor
public class BidsService {

    private final BidPlacementService bidPlacementService;
    private final BidQueryService bidQueryService;

    /**
     * Places a bid on an auction.
     * Delegates to BidPlacementService for complex orchestration.
     */
    public PlaceBidResponse placeBid(long auctionId, PlaceBidRequest req, Long currentUserId) {
        return bidPlacementService.placeBid(auctionId, req, currentUserId);
    }

    /**
     * Gets bid history for an auction.
     * Delegates to BidQueryService for read operations.
     */
    public List<BidHistoryItem> getHistory(long auctionId) {
        return bidQueryService.getHistory(auctionId);
    }

    /**
     * Gets user's bid summary across all auctions.
     * Delegates to BidQueryService for read operations.
     */
    public List<com.myapp.server.bids.dto.UserBidSummaryItem> getUserBidsSummary(Long userId) {
        return bidQueryService.getUserBidsSummary(userId);
    }

    // Keep the BidHistoryItem record here for backward compatibility
    public record BidHistoryItem(Long snapshotId, Long bidId, Long bidderId,
                                 BigDecimal displayedBid, OffsetDateTime snapshotTime, String kind, String bidType) {}
}
