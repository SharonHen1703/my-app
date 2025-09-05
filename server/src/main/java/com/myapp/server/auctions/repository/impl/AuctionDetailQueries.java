package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionDetailMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Handles auction detail lookups by ID with status filtering.
 * Extracted from AuctionRepositoryImpl to focus on detail operations.
 * Uses direct JPQL queries via EntityManager to avoid circular dependencies.
 */
@Component
@RequiredArgsConstructor
public class AuctionDetailQueries {

    private final EntityManager entityManager;
    private final AuctionDetailMapper auctionDetailMapper;

    /**
     * Get auction detail by ID with status validation.
     */
    public AuctionDetail getAuctionDetailById(Long id) {
        var auction = entityManager.find(Auction.class, id);
        if (auction == null) {
            return null;
        }
        
        // Check if auction is viewable (not draft or deleted)
        if (!isAuctionViewable(auction.getStatus())) {
            return null;
        }
        
        // Calculate minBidToPlace directly without policy to avoid circular dependency
        BigDecimal minBidToPlace = auction.getCurrentBidAmount() != null 
            ? auction.getCurrentBidAmount().add(auction.getBidIncrement())
            : auction.getMinPrice();
        
        return auctionDetailMapper.toAuctionDetail(auction, minBidToPlace);
    }

    /**
     * Check if auction exists and is in valid status for bidding.
     */
    public boolean isAuctionValidForBidding(Long auctionId) {
        var auction = entityManager.find(Auction.class, auctionId);
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
               status == AuctionStatus.SOLD ||
               status == AuctionStatus.UNSOLD;
    }

    // === EXACT SIGNATURE DELEGATION METHODS (352→Custom migration) ===
    
    public AuctionRepository.AuctionProjection findAuctionDetailById(Long id, com.myapp.server.auctions.entity.enums.AuctionStatus status) {
        var auction = entityManager.find(Auction.class, id);
        if (auction != null && auction.getStatus() == status) {
            return new AuctionProjectionImpl(auction);
        }
        return null;
    }
    
    public AuctionRepository.AuctionProjection findAuctionDetailByIdAnyStatus(Long id) {
        var auction = entityManager.find(Auction.class, id);
        if (auction != null) {
            return new AuctionProjectionImpl(auction);
        }
        return null;
    }
}
