package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Custom repository interface for complex auction queries.
 * Extended by AuctionRepository to provide custom query capabilities
 * while maintaining Spring Data JPA's standard functionality.
 */
public interface AuctionRepositoryCustom {

    /**
     * Find active auctions with pagination.
     */
    Page<AuctionListItem> findActiveAuctions(Pageable pageable);

    /**
     * Find active auctions by category with pagination.
     */
    Page<AuctionListItem> findActiveAuctions(Pageable pageable, String category);

    /**
     * Find active auctions with comprehensive filtering.
     */
    Page<AuctionListItem> findActiveAuctions(
        Pageable pageable,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText
    );

    /**
     * Find active auctions with comprehensive filtering, excluding specific seller.
     */
    Page<AuctionListItem> findActiveAuctions(
        Pageable pageable,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText,
        Long excludeSellerId
    );

    /**
     * Count total active auctions.
     */
    long countActiveAuctions();

    /**
     * Find auctions by seller ID.
     */
    List<AuctionListItem> findBySellerId(Long sellerId);

    /**
     * Find auctions with bids by user ID.
     */
    List<AuctionListItem> findAuctionsWithBidsByUserId(Long userId);

    /**
     * Get auction detail by ID with specific status filtering.
     */
    com.myapp.server.auctions.dto.AuctionDetail getAuctionDetailById(Long id);

    /**
     * Check if auction exists and is in valid status for bidding.
     */
    boolean isAuctionValidForBidding(Long auctionId);
}
