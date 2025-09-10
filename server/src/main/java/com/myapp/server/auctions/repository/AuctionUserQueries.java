package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.entity.Auction;

import java.util.List;

/**
 * Repository fragment for user-specific auction queries.
 */
public interface AuctionUserQueries {
    
    // === Domain methods (return entities/projections) ===
    
    /**
     * Find auctions by seller ID - returns domain entities.
     */
    List<Auction> findBySellerIdDomain(Long sellerId);

    /**
     * Find auctions with bids by user ID - returns domain entities.
     */
    List<Auction> findAuctionsWithBidsByUserIdDomain(Long userId);

    /**
     * Find auctions by seller ID ordered by creation date
     */
    List<AuctionRepository.UserAuctionProjection> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
