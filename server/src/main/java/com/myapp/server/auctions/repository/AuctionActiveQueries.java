package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.entity.Auction;
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
     * Find active auctions with comprehensive filtering, excluding specific seller - returns domain entities.
     */
    Page<Auction> findActiveAuctionsDomain(
        Pageable pageable,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText,
        Long excludeSellerId
    );
}
