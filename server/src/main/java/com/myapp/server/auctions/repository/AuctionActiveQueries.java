package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository fragment for active auction queries, searches, and pagination.
 */
public interface AuctionActiveQueries {

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
     * Count active auctions (exact signature match)
     */
    long countActiveAuctions(com.myapp.server.auctions.entity.enums.AuctionStatus status);
    
    /**
     * Find active auctions with min bid calculation
     */
    org.springframework.data.domain.Page<AuctionRepository.AuctionProjection> findActiveAuctionsWithMinBid(
        com.myapp.server.auctions.entity.enums.AuctionStatus status, 
        Pageable pageable
    );
    
    /**
     * Find active auctions by category
     */
    org.springframework.data.domain.Page<AuctionRepository.AuctionProjection> findActiveAuctionsByCategory(
        com.myapp.server.auctions.entity.enums.AuctionStatus status, 
        String categoryPattern, 
        Pageable pageable
    );
    
    /**
     * Find active auctions filtered without search
     */
    org.springframework.data.domain.Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredNoSearch(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        Pageable pageable
    );
    
    /**
     * Find active auctions filtered with search
     */
    org.springframework.data.domain.Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredWithSearch(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        String searchPattern,
        Pageable pageable
    );
    
    /**
     * Find active auctions filtered without search, excluding seller
     */
    org.springframework.data.domain.Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredNoSearchExcludeSeller(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        Long excludeSellerId,
        Pageable pageable
    );
    
    /**
     * Find active auctions filtered with search, excluding seller
     */
    org.springframework.data.domain.Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredWithSearchExcludeSeller(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        Long excludeSellerId,
        String searchPattern,
        Pageable pageable
    );
}
