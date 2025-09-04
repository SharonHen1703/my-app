package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles filtered search operations for active auctions with pagination.
 * Extracted from AuctionRepositoryImpl to focus on search functionality.
 */
@Component
@RequiredArgsConstructor
public class ActiveAuctionsQueries {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;

    /**
     * Find active auctions with pagination.
     */
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable) {
        var projections = auctionRepository.findActiveAuctionsProjection(pageable);
        return projections.map(auctionMapper::toAuctionListItem);
    }

    /**
     * Find active auctions by category with pagination.
     */
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable, String category) {
        if (category == null || category.trim().isEmpty()) {
            return findActiveAuctions(pageable);
        }
        
        var projections = auctionRepository.findActiveAuctionsByCategory(category, pageable);
        return projections.map(auctionMapper::toAuctionListItem);
    }

    /**
     * Find active auctions with comprehensive filtering.
     */
    public Page<AuctionListItem> findActiveAuctions(
        Pageable pageable,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText
    ) {
        return findActiveAuctions(
            pageable, category, minPrice, maxPrice, conditions, searchText, null
        );
    }

    /**
     * Find active auctions with comprehensive filtering, excluding specific seller.
     */
    public Page<AuctionListItem> findActiveAuctions(
        Pageable pageable,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText,
        Long excludeSellerId
    ) {
        // Build filter criteria
        var filterCriteria = buildFilterCriteria(
            category, minPrice, maxPrice, conditions, searchText, excludeSellerId
        );
        
        // Execute filtered query
        var projections = executeFilteredQuery(filterCriteria, pageable);
        
        return projections.map(auctionMapper::toAuctionListItem);
    }

    /**
     * Count total active auctions.
     */
    public long countActiveAuctions() {
        return auctionRepository.countByStatusAndEndDateAfter(
            AuctionStatus.ACTIVE,
            OffsetDateTime.now()
        );
    }

    /**
     * Build filter criteria for complex queries.
     */
    private FilterCriteria buildFilterCriteria(
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText,
        Long excludeSellerId
    ) {
        return FilterCriteria.builder()
            .category(category)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .conditions(conditions != null ? conditions : new ArrayList<>())
            .searchText(searchText)
            .excludeSellerId(excludeSellerId)
            .build();
    }

    /**
     * Execute filtered query based on criteria complexity.
     */
    private Page<AuctionRepository.AuctionProjection> executeFilteredQuery(
        FilterCriteria criteria, Pageable pageable
    ) {
        // Simple category filter
        if (isSimpleCategoryFilter(criteria)) {
            return auctionRepository.findActiveAuctionsByCategory(criteria.category, pageable);
        }
        
        // Complex filtering required
        return executeComplexFilter(criteria, pageable);
    }

    /**
     * Check if this is a simple category-only filter.
     */
    private boolean isSimpleCategoryFilter(FilterCriteria criteria) {
        return criteria.category != null &&
               criteria.minPrice == null &&
               criteria.maxPrice == null &&
               (criteria.conditions == null || criteria.conditions.isEmpty()) &&
               (criteria.searchText == null || criteria.searchText.trim().isEmpty()) &&
               criteria.excludeSellerId == null;
    }

    /**
     * Execute complex filtered query with multiple criteria.
     */
    private Page<AuctionRepository.AuctionProjection> executeComplexFilter(
        FilterCriteria criteria, Pageable pageable
    ) {
        return auctionRepository.findActiveAuctionsWithFilters(
            criteria.category,
            criteria.minPrice,
            criteria.maxPrice,
            criteria.conditions,
            criteria.searchText,
            criteria.excludeSellerId,
            pageable
        );
    }

    /**
     * Internal filter criteria holder.
     */
    private static class FilterCriteria {
        String category;
        BigDecimal minPrice;
        BigDecimal maxPrice;
        List<AuctionCondition> conditions;
        String searchText;
        Long excludeSellerId;

        static Builder builder() {
            return new Builder();
        }

        static class Builder {
            private final FilterCriteria criteria = new FilterCriteria();

            Builder category(String category) {
                criteria.category = category;
                return this;
            }

            Builder minPrice(BigDecimal minPrice) {
                criteria.minPrice = minPrice;
                return this;
            }

            Builder maxPrice(BigDecimal maxPrice) {
                criteria.maxPrice = maxPrice;
                return this;
            }

            Builder conditions(List<AuctionCondition> conditions) {
                criteria.conditions = conditions;
                return this;
            }

            Builder searchText(String searchText) {
                criteria.searchText = searchText;
                return this;
            }

            Builder excludeSellerId(Long excludeSellerId) {
                criteria.excludeSellerId = excludeSellerId;
                return this;
            }

            FilterCriteria build() {
                return criteria;
            }
        }
    }
}
