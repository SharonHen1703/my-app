package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Simple projection implementation for user auctions as AuctionProjection
 */
class UserAuctionProjectionImpl implements AuctionRepository.AuctionProjection {
    private final Auction auction;
    
    public UserAuctionProjectionImpl(Auction auction) {
        this.auction = auction;
    }
    
    @Override public Long getId() { return auction.getId(); }
    @Override public String getTitle() { return auction.getTitle(); }
    @Override public String getDescription() { return auction.getDescription(); }
    @Override public String getCondition() { return auction.getCondition().toString(); }
    @Override public String getCategories() { return auction.getCategories(); }
    @Override public BigDecimal getMinPrice() { return auction.getMinPrice(); }
    @Override public BigDecimal getBidIncrement() { return auction.getBidIncrement(); }
    @Override public BigDecimal getCurrentBidAmount() { return auction.getCurrentBidAmount(); }
    @Override public Integer getBidsCount() { return auction.getBidsCount(); }
    @Override public BigDecimal getMinBidToPlace() { 
        // Simple calculation for now - ideally would use AuctionPolicy
        return auction.getCurrentBidAmount() != null 
            ? auction.getCurrentBidAmount().add(auction.getBidIncrement())
            : auction.getMinPrice();
    }
    @Override public OffsetDateTime getEndDate() { return auction.getEndDate(); }
    @Override public String getImageUrls() { return auction.getImageUrls(); }
    @Override public Long getSellerId() { return auction.getSellerId(); }
    @Override public String getStatus() { return auction.getStatus().toString(); }
}

/**
 * Separate projection implementation for UserAuctionProjection interface
 */
class UserAuctionProjectionForUser implements AuctionRepository.UserAuctionProjection {
    private final Auction auction;
    
    public UserAuctionProjectionForUser(Auction auction) {
        this.auction = auction;
    }
    
    @Override public Long getId() { return auction.getId(); }
    @Override public String getTitle() { return auction.getTitle(); }
    @Override public AuctionStatus getAuctionStatus() { return auction.getStatus(); }
    @Override public Integer getBidsCount() { return auction.getBidsCount(); }
    @Override public OffsetDateTime getEndDate() { return auction.getEndDate(); }
    @Override public BigDecimal getCurrentPrice() { 
        return auction.getCurrentBidAmount() != null ? auction.getCurrentBidAmount() : auction.getMinPrice(); 
    }
    @Override public BigDecimal getMinPrice() { return auction.getMinPrice(); }
}

/**
 * Handles user-specific auction queries.
 * Extracted from AuctionRepositoryImpl to focus on user-related operations.
 * Uses direct JPQL queries via EntityManager to avoid circular dependencies.
 */
@Component
@RequiredArgsConstructor
public class UserAuctionsQueries {

    private final EntityManager entityManager;

    /**
     * Find auctions by seller ID - returns domain entities.
     * Uses direct JPQL query via EntityManager.
     */
    public List<Auction> findBySellerIdDomain(Long sellerId) {
        String jpql = "SELECT a FROM Auction a WHERE a.sellerId = :sellerId ORDER BY a.createdAt DESC";
        TypedQuery<Auction> query = entityManager.createQuery(jpql, Auction.class);
        query.setParameter("sellerId", sellerId);
        
        return query.getResultList();
    }

    /**
     * Find auctions with bids by user ID - returns domain entities.
     * TODO: Implement when bid tracking is available
     */
    public List<Auction> findAuctionsWithBidsByUserIdDomain(Long userId) {
        // Placeholder implementation - would require bids table join
        return List.of();
    }

    // === EXACT SIGNATURE DELEGATION METHODS (352→Custom migration) ===
    
    public java.util.List<AuctionRepository.UserAuctionProjection> findBySellerIdOrderByCreatedAtDesc(Long sellerId) {
        // Direct JPQL implementation to avoid circular dependencies
        String jpql = "SELECT a FROM Auction a WHERE a.sellerId = :sellerId ORDER BY a.createdAt DESC";
        TypedQuery<Auction> query = entityManager.createQuery(jpql, Auction.class);
        query.setParameter("sellerId", sellerId);
        
        List<Auction> auctions = query.getResultList();
        return auctions.stream()
            .map(UserAuctionProjectionForUser::new)
            .collect(java.util.stream.Collectors.toList());
    }
}
