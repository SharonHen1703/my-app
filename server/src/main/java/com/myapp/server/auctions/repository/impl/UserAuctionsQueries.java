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
    @Override public BigDecimal getCurrentPrice() { return auction.getCurrentBidAmount(); }
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
    // Removed AuctionListMapper to break circular dependency

    /**
     * Find auctions by seller ID.
     * Uses direct JPQL query via EntityManager to avoid circular dependencies.
     */
    public List<AuctionListItem> findBySellerId(Long sellerId) {
        String jpql = "SELECT a FROM Auction a WHERE a.sellerId = :sellerId ORDER BY a.createdAt DESC";
        TypedQuery<Auction> query = entityManager.createQuery(jpql, Auction.class);
        query.setParameter("sellerId", sellerId);
        
        List<Auction> auctions = query.getResultList();
        
        return auctions.stream()
            .map(this::convertToAuctionListItem)
            .toList();
    }

    /**
     * Find auctions with bids by user ID.
     * TODO: Implement when bid tracking is available
     */
    public List<AuctionListItem> findAuctionsWithBidsByUserId(Long userId) {
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
    
    /**
     * Convert auction entity to AuctionListItem DTO inline to avoid circular dependency
     */
    private AuctionListItem convertToAuctionListItem(Auction auction) {
        List<String> imageUrls = parseImageUrls(auction.getImageUrls());
        
        return new AuctionListItem(
            auction.getId(),
            auction.getTitle(),
            auction.getDescription(),
            auction.getCondition() != null ? auction.getCondition().name() : null,
            auction.getCategories(),
            auction.getMinPrice(),
            auction.getBidIncrement(),
            auction.getCurrentBidAmount(),
            auction.getBidsCount(),
            calculateMinBidToPlace(auction.getCurrentBidAmount(), auction.getBidIncrement(), auction.getMinPrice()),
            auction.getEndDate(),
            imageUrls
        );
    }
    
    private List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            if (imageUrlsJson.startsWith("[") && imageUrlsJson.endsWith("]")) {
                String content = imageUrlsJson.substring(1, imageUrlsJson.length() - 1);
                if (content.trim().isEmpty()) {
                    return List.of();
                }
                return List.of(content.split(","))
                    .stream()
                    .map(String::trim)
                    .map(s -> s.replaceAll("^\"|\"$", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
            }
            return List.of(imageUrlsJson);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    private BigDecimal calculateMinBidToPlace(BigDecimal currentBid, BigDecimal bidIncrement, BigDecimal minPrice) {
        if (currentBid != null && currentBid.compareTo(BigDecimal.ZERO) > 0) {
            return currentBid.add(bidIncrement != null ? bidIncrement : BigDecimal.ONE);
        }
        return minPrice != null ? minPrice : BigDecimal.ZERO;
    }
}
