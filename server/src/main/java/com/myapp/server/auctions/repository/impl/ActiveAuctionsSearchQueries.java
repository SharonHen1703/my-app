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

    public List<Auction> findActiveAuctionsWithMinBid(AuctionStatus status, int offset, int limit) {
        return executeQuery("SELECT a FROM Auction a WHERE a.status = :status ORDER BY a.endDate ASC",
            offset, limit, q -> q.setParameter("status", status));
    }

    public long countActiveAuctionsWithMinBid(AuctionStatus status) {
        return executeCountQuery("SELECT COUNT(a) FROM Auction a WHERE a.status = :status",
            q -> q.setParameter("status", status));
    }

    public List<Auction> findActiveAuctionsByCategory(AuctionStatus status, String categoryPattern, int offset, int limit) {
        String jpql = "SELECT a FROM Auction a WHERE a.status = :status" + 
                     (categoryPattern != null ? " AND a.categories LIKE :categoryPattern" : "") + " ORDER BY a.endDate ASC";
        return executeQuery(jpql, offset, limit, q -> {
            q.setParameter("status", status);
            if (categoryPattern != null) q.setParameter("categoryPattern", "%" + categoryPattern + "%");
        });
    }

    public long countActiveAuctionsByCategory(AuctionStatus status, String categoryPattern) {
        String jpql = "SELECT COUNT(a) FROM Auction a WHERE a.status = :status" + 
                     (categoryPattern != null ? " AND a.categories LIKE :categoryPattern" : "");
        return executeCountQuery(jpql, q -> {
            q.setParameter("status", status);
            if (categoryPattern != null) q.setParameter("categoryPattern", "%" + categoryPattern + "%");
        });
    }

    public List<Auction> findActiveAuctionsFilteredNoSearch(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions, int offset, int limit) {
        return executeFilteredQuery(categoryPattern, minPrice, maxPrice, conditions, null, null, offset, limit);
    }

    public long countActiveAuctionsFilteredNoSearch(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions) {
        return executeFilteredCountQuery(categoryPattern, minPrice, maxPrice, conditions, null, null);
    }

    public List<Auction> findActiveAuctionsFilteredWithSearch(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions, String searchPattern, int offset, int limit) {
        return executeFilteredQuery(categoryPattern, minPrice, maxPrice, conditions, searchPattern, null, offset, limit);
    }

    public long countActiveAuctionsFilteredWithSearch(String categoryPattern, BigDecimal minPrice, 
        BigDecimal maxPrice, List<AuctionCondition> conditions, String searchPattern) {
        return executeFilteredCountQuery(categoryPattern, minPrice, maxPrice, conditions, searchPattern, null);
    }

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

    public long countActiveAuctions() {
        return executeCountQuery("SELECT COUNT(a) FROM Auction a WHERE a.status = :status",
            q -> q.setParameter("status", AuctionStatus.ACTIVE));
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
        if (minPrice != null) jpql.append(" AND a.currentBidAmount >= :minPrice");
        if (maxPrice != null) jpql.append(" AND a.currentBidAmount <= :maxPrice");
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
