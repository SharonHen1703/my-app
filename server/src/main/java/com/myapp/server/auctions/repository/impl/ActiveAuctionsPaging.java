package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles pagination utilities and Page<T> construction for auction queries.
 * Focuses on converting entity lists to paginated results with proper metadata.
 * Extracted from ActiveAuctionsQueries to separate pagination concerns.
 */
@Component
@RequiredArgsConstructor
public class ActiveAuctionsPaging {

    /**
     * Create a Page<AuctionProjection> from auction entities and pagination metadata.
     */
    public Page<AuctionRepository.AuctionProjection> createProjectionPage(
        List<Auction> auctions, 
        Pageable pageable, 
        long totalElements
    ) {
        List<AuctionRepository.AuctionProjection> projections = new ArrayList<>();
        for (Auction auction : auctions) {
            projections.add(new AuctionProjectionImpl(auction));
        }
        return new PageImpl<>(projections, pageable, totalElements);
    }
    
    /**
     * Create a Page<Auction> from auction entities and pagination metadata.
     * Used by Domain methods to avoid Projection wrapper overhead.
     */
    public Page<Auction> createEntityPage(
        List<Auction> auctions, 
        Pageable pageable, 
        long totalElements
    ) {
        return new PageImpl<>(auctions, pageable, totalElements);
    }

    /**
     * Calculate offset from Pageable for query execution.
     */
    public int getOffset(Pageable pageable) {
        return (int) pageable.getOffset();
    }

    /**
     * Get page size from Pageable for query execution.
     */
    public int getPageSize(Pageable pageable) {
        return pageable.getPageSize();
    }

    /**
     * Create Page<AuctionProjection> with query execution helper.
     * Combines auction retrieval and counting into a single paginated result.
     */
    public Page<AuctionRepository.AuctionProjection> executePagedQuery(
        List<Auction> auctions,
        long totalCount,
        Pageable pageable
    ) {
        return createProjectionPage(auctions, pageable, totalCount);
    }

    /**
     * Helper to validate pagination parameters.
     */
    public void validatePagination(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        if (pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }
    }

    /**
     * Create empty page when no results are found.
     */
    public Page<AuctionRepository.AuctionProjection> createEmptyPage(Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    /**
     * Check if pagination parameters require database queries.
     * Returns false if the requested page is beyond available data.
     */
    public boolean shouldExecuteQuery(Pageable pageable, long totalElements) {
        return pageable.getOffset() < totalElements;
    }
}
