package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    
    /**
     * מוצא מכרזים פעילים עם מיון לפי תאריך סיום
     */
    Page<Auction> findByStatusOrderByEndDate(AuctionStatus status, Pageable pageable);
    
    /**
     * סופר מכרזים פעילים
     */
    long countByStatus(AuctionStatus status);
    
    /**
     * שאילתה מותאמת אישית לחישוב מינימום הצעה הבאה
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.minPrice as minPrice,
               a.bidIncrement as bidIncrement,
               a.currentBidAmount as currentBidAmount,
               a.bidsCount as bidsCount,
               CASE 
                   WHEN a.bidsCount = 0 THEN a.minPrice
                   ELSE a.currentBidAmount + a.bidIncrement
               END as minBidToPlace,
               a.endDate as endDate,
               a.imageUrls as imageUrls
        FROM Auction a 
        WHERE a.status = :status 
        ORDER BY a.endDate
        """)
    Page<AuctionProjection> findActiveAuctionsWithMinBid(AuctionStatus status, Pageable pageable);
    
    /**
     * Projection interface for auction list items
     */
    interface AuctionProjection {
        Long getId();
        String getTitle();
        String getDescription();
        java.math.BigDecimal getMinPrice();
        java.math.BigDecimal getBidIncrement();
        java.math.BigDecimal getCurrentBidAmount();
        Integer getBidsCount();
        java.math.BigDecimal getMinBidToPlace();
        java.time.OffsetDateTime getEndDate();
        String getImageUrls();
    }
}
