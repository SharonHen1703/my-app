package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Handles auction detail lookups by ID with status filtering.
 * Extracted from AuctionRepositoryImpl to focus on detail operations.
 */
@Component
@RequiredArgsConstructor
public class AuctionDetailQueries {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;

    /**
     * Get auction detail by ID with status validation.
     */
    public AuctionDetail getAuctionDetailById(Long id) {
        var auction = auctionRepository.findById(id).orElse(null);
        if (auction == null) {
            return null;
        }
        
        // Check if auction is viewable (not draft or deleted)
        if (!isAuctionViewable(auction.getStatus())) {
            return null;
        }
        
        return auctionMapper.toAuctionDetail(auction);
    }

    /**
     * Check if auction exists and is in valid status for bidding.
     */
    public boolean isAuctionValidForBidding(Long auctionId) {
        var auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null) {
            return false;
        }
        
        // Must be active and not expired
        return auction.getStatus() == AuctionStatus.ACTIVE &&
               auction.getEndDate().isAfter(OffsetDateTime.now());
    }

    /**
     * Check if auction status allows viewing.
     */
    private boolean isAuctionViewable(AuctionStatus status) {
        return status == AuctionStatus.ACTIVE ||
               status == AuctionStatus.ENDED ||
               status == AuctionStatus.COMPLETED;
    }
}
