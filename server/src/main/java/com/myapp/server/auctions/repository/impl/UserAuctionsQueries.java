package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.mapper.AuctionMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles user-specific auction queries.
 * Extracted from AuctionRepositoryImpl to focus on user-related operations.
 */
@Component
@RequiredArgsConstructor
public class UserAuctionsQueries {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;

    /**
     * Find auctions by seller ID.
     */
    public List<AuctionListItem> findBySellerId(Long sellerId) {
        var projections = auctionRepository.findBySellerIdProjection(sellerId);
        return projections.stream()
            .map(auctionMapper::toAuctionListItem)
            .toList();
    }

    /**
     * Find auctions with bids by user ID.
     */
    public List<AuctionListItem> findAuctionsWithBidsByUserId(Long userId) {
        var projections = auctionRepository.findAuctionsWithBidsByUserIdProjection(userId);
        return projections.stream()
            .map(auctionMapper::toAuctionListItem)
            .toList();
    }
}
