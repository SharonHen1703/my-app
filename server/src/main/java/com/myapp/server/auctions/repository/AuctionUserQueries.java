package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.dto.AuctionListItem;

import java.util.List;

/**
 * Repository fragment for user-specific auction queries.
 */
public interface AuctionUserQueries {

    /**
     * Find auctions by seller ID.
     */
    List<AuctionListItem> findBySellerId(Long sellerId);

    /**
     * Find auctions with bids by user ID.
     */
    List<AuctionListItem> findAuctionsWithBidsByUserId(Long userId);

    /**
     * Find auctions by seller ID ordered by creation date
     */
    List<AuctionRepository.UserAuctionProjection> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
