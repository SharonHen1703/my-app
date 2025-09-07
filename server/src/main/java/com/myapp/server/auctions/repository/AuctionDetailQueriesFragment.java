package com.myapp.server.auctions.repository;

/**
 * Repository fragment for auction detail queries by ID.
 */
public interface AuctionDetailQueriesFragment {

    /**
     * Get auction detail by ID with specific status filtering.
     */
    com.myapp.server.auctions.dto.AuctionDetail getAuctionDetailById(Long id);

    /**
     * Check if auction exists and is in valid status for bidding.
     */
    boolean isAuctionValidForBidding(Long auctionId);
    
    /**
     * Find auction detail by ID any status
     */
    AuctionRepository.AuctionProjection findAuctionDetailByIdAnyStatus(Long id);
}
