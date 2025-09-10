package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;

/**
 * Compact search/filter engine for active auctions.
 * All WHERE/filter/search/excludeSeller variants with parameter binding.
 */
@Component
@RequiredArgsConstructor
public class ActiveAuctionsSearchQueries {

    private final EntityManager entityManager;

    public List<Auction> findActiveAuctionsFilteredNoSearchExcludeSeller(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions, Long excludeSellerId, int offset, int limit) {
        return executeFilteredQuery(categoryPattern, minPrice, maxPrice, conditions, null, excludeSellerId, offset, limit);
    }

    public long countActiveAuctionsFilteredNoSearchExcludeSeller(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions, Long excludeSellerId) {
        return executeFilteredCountQuery(categoryPattern, minPrice, maxPrice, conditions, null, excludeSellerId);
    }

    public List<Auction> findActiveAuctionsFilteredWithSearchExcludeSeller(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions, Long excludeSellerId, String searchPattern, int offset, int limit) {
        return executeFilteredQuery(categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId, offset, limit);
    }

    public long countActiveAuctionsFilteredWithSearchExcludeSeller(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions, Long excludeSellerId, String searchPattern) {
        return executeFilteredCountQuery(categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId);
    }

    // Core filtered query execution
    private List<Auction> executeFilteredQuery(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice,
        List<AuctionCondition> conditions, String searchPattern, Long excludeSellerId, int offset, int limit) {
        return executeQuery(buildQuery(categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId), 
            offset, limit, q -> setFilterParams(q, categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId));
    }

    private long executeFilteredCountQuery(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice,
        List<AuctionCondition> conditions, String searchPattern, Long excludeSellerId) {
        return executeCountQuery(buildCountQuery(categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId),
            q -> setFilterParams(q, categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId));
    }

    private String buildQuery(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice, 
        List<AuctionCondition> conditions, String searchPattern, Long excludeSellerId) {
        return buildQueryBase("SELECT a FROM Auction a", categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId) + " ORDER BY a.endDate ASC";
    }

    private String buildCountQuery(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice, 
        List<AuctionCondition> conditions, String searchPattern, Long excludeSellerId) {
        return buildQueryBase("SELECT COUNT(a) FROM Auction a", categoryPattern, minPrice, maxPrice, conditions, searchPattern, excludeSellerId);
    }

    private String buildQueryBase(String selectClause, String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice, 
        List<AuctionCondition> conditions, String searchPattern, Long excludeSellerId) {
        StringBuilder jpql = new StringBuilder(selectClause + " WHERE a.status = :status");
        if (categoryPattern != null) jpql.append(" AND a.categories LIKE :categoryPattern");
        // Use COALESCE to handle null currentBidAmount (fallback to minPrice for auctions without bids)
        if (minPrice != null) jpql.append(" AND COALESCE(a.currentBidAmount, a.minPrice) >= :minPrice");
        if (maxPrice != null) jpql.append(" AND COALESCE(a.currentBidAmount, a.minPrice) <= :maxPrice");
        if (conditions != null && !conditions.isEmpty()) jpql.append(" AND a.condition IN :conditions");
        if (excludeSellerId != null) jpql.append(" AND a.sellerId != :excludeSellerId");
        if (searchPattern != null) jpql.append(" AND (a.title LIKE :searchPattern OR a.description LIKE :searchPattern)");
        return jpql.toString();
    }

    private void setFilterParams(TypedQuery<?> q, String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice,
        List<AuctionCondition> conditions, String searchPattern, Long excludeSellerId) {
        q.setParameter("status", AuctionStatus.ACTIVE);
        if (categoryPattern != null) q.setParameter("categoryPattern", "%" + categoryPattern + "%");
        if (minPrice != null) q.setParameter("minPrice", minPrice);
        if (maxPrice != null) q.setParameter("maxPrice", maxPrice);
        if (conditions != null && !conditions.isEmpty()) q.setParameter("conditions", conditions);
        if (excludeSellerId != null) q.setParameter("excludeSellerId", excludeSellerId);
        if (searchPattern != null) q.setParameter("searchPattern", "%" + searchPattern + "%");
    }

    private List<Auction> executeQuery(String jpql, int offset, int limit, QueryParameterSetter setter) {
        TypedQuery<Auction> query = entityManager.createQuery(jpql, Auction.class);
        setter.setParameters(query);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    private long executeCountQuery(String jpql, QueryParameterSetter setter) {
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        setter.setParameters(query);
        return query.getSingleResult();
    }

    @FunctionalInterface
    private interface QueryParameterSetter { void setParameters(TypedQuery<?> query); }
}
