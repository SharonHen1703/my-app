package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.OffsetDateTime;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    
    /**
     * מוצא מכרזים פעילים עם מיון לפי תאריך סיום
     */
    Page<Auction> findByStatusOrderByEndDate(AuctionStatus status, Pageable pageable);
    
    /**
     * מוצא מכרזים לפי סטטוס ותאריך סיום
     */
    List<Auction> findByStatusAndEndDateBefore(AuctionStatus status, OffsetDateTime endDate);
    
    /**
     * סופר מכרזים פעילים שלא הסתיימו
     */
    @Query("SELECT COUNT(a) FROM Auction a WHERE a.status = :status AND a.endDate > CURRENT_TIMESTAMP")
    long countActiveAuctions(AuctionStatus status);
    
    /**
     * שאילתה מותאמת אישית לחישוב מינימום הצעה הבאה - רק מכרזים שלא הסתיימו
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
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
        WHERE a.status = :status AND a.endDate > CURRENT_TIMESTAMP
        ORDER BY a.endDate
        """)
    Page<AuctionProjection> findActiveAuctionsWithMinBid(AuctionStatus status, Pageable pageable);
    
    /**
     * שאילתה מותאמת אישית לחישוב מינימום הצעה הבאה עם סינון לפי קטגוריה
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
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
        AND a.endDate > CURRENT_TIMESTAMP
        AND a.categories LIKE :categoryPattern
        ORDER BY a.endDate
        """)
    Page<AuctionProjection> findActiveAuctionsByCategory(AuctionStatus status, String categoryPattern, Pageable pageable);

    /**
     * Active auctions with optional filters - avoiding CONCAT issues with null handling
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
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
        WHERE a.status = 'active' 
            AND a.endDate > CURRENT_TIMESTAMP
            AND (:categoryPattern IS NULL OR a.categories LIKE :categoryPattern)
            AND (:minPrice IS NULL OR a.currentBidAmount >= :minPrice)
            AND (:maxPrice IS NULL OR a.currentBidAmount <= :maxPrice)
            AND (:conditions IS NULL OR a.condition IN :conditions)
        ORDER BY a.endDate
        """)
    Page<AuctionProjection> findActiveAuctionsFilteredNoSearch(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        Pageable pageable
    );

    /**
     * Active auctions with optional filters including text search
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
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
        WHERE a.status = 'active' 
            AND a.endDate > CURRENT_TIMESTAMP
            AND (:categoryPattern IS NULL OR a.categories LIKE :categoryPattern)
            AND (:minPrice IS NULL OR a.currentBidAmount >= :minPrice)
            AND (:maxPrice IS NULL OR a.currentBidAmount <= :maxPrice)
            AND (:conditions IS NULL OR a.condition IN :conditions)
            AND (a.title LIKE :searchPattern OR a.description LIKE :searchPattern)
        ORDER BY a.endDate
        """)
    Page<AuctionProjection> findActiveAuctionsFilteredWithSearch(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        String searchPattern,
        Pageable pageable
    );

    /**
     * Active auctions with optional filters excluding specific seller
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
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
        WHERE a.status = 'active' 
            AND a.endDate > CURRENT_TIMESTAMP
            AND (:categoryPattern IS NULL OR a.categories LIKE :categoryPattern)
            AND (:minPrice IS NULL OR a.currentBidAmount >= :minPrice)
            AND (:maxPrice IS NULL OR a.currentBidAmount <= :maxPrice)
            AND (:conditions IS NULL OR a.condition IN :conditions)
            AND (:excludeSellerId IS NULL OR a.sellerId != :excludeSellerId)
        ORDER BY a.endDate
        """)
    Page<AuctionProjection> findActiveAuctionsFilteredNoSearchExcludeSeller(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        Long excludeSellerId,
        Pageable pageable
    );

    /**
     * Active auctions with optional filters including text search and excluding specific seller
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
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
        WHERE a.status = 'active' 
            AND a.endDate > CURRENT_TIMESTAMP
            AND (:categoryPattern IS NULL OR a.categories LIKE :categoryPattern)
            AND (:minPrice IS NULL OR a.currentBidAmount >= :minPrice)
            AND (:maxPrice IS NULL OR a.currentBidAmount <= :maxPrice)
            AND (:conditions IS NULL OR a.condition IN :conditions)
            AND (:excludeSellerId IS NULL OR a.sellerId != :excludeSellerId)
            AND (a.title LIKE :searchPattern OR a.description LIKE :searchPattern)
        ORDER BY a.endDate
        """)
    Page<AuctionProjection> findActiveAuctionsFilteredWithSearchExcludeSeller(
        String categoryPattern,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> conditions,
        Long excludeSellerId,
        String searchPattern,
        Pageable pageable
    );

    /**
     * מחזיר רשימת כל הקטגוריות הייחודיות - שאילתה פשוטה יותר
     */
    @Query(value = """
        SELECT DISTINCT a.categories
        FROM auctions a 
        WHERE a.status = 'ACTIVE' 
        AND a.end_date > CURRENT_TIMESTAMP
        AND a.categories IS NOT NULL
        AND a.categories != ''
        AND a.categories != '[]'
        """, nativeQuery = true)
    List<String> findAllDistinctCategories();
    
    /**
     * שאילתה לקבלת פרטי מכרז בודד - רק אם לא הסתיים
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
               a.minPrice as minPrice,
               a.bidIncrement as bidIncrement,
               a.currentBidAmount as currentBidAmount,
               a.bidsCount as bidsCount,
               CASE 
                   WHEN a.bidsCount = 0 THEN a.minPrice
                   ELSE (a.currentBidAmount + a.bidIncrement)
               END as minBidToPlace,
               a.endDate as endDate,
               a.imageUrls as imageUrls,
               a.sellerId as sellerId,
               a.status as status
        FROM Auction a 
        WHERE a.id = :id AND a.status = :status AND a.endDate > CURRENT_TIMESTAMP
        """)
    AuctionProjection findAuctionDetailById(Long id, AuctionStatus status);

    /**
     * מוצא פרטי מכרז לפי ID ללא קשר לסטטוס או תאריך סיום
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.description as description,
               a.condition as condition,
               a.categories as categories,
               a.minPrice as minPrice,
               a.bidIncrement as bidIncrement,
               a.currentBidAmount as currentBidAmount,
               a.bidsCount as bidsCount,
               CASE 
                   WHEN a.bidsCount = 0 THEN a.minPrice
                   ELSE (a.currentBidAmount + a.bidIncrement)
               END as minBidToPlace,
               a.endDate as endDate,
               a.imageUrls as imageUrls,
               a.sellerId as sellerId,
               a.status as status
        FROM Auction a 
        WHERE a.id = :id
        """)
    AuctionProjection findAuctionDetailByIdAnyStatus(Long id);

    /**
     * מוצא מכרזים של משתמש ספציפי
     */
    @Query("""
        SELECT a.id as id,
               a.title as title,
               a.currentBidAmount as currentPrice,
               a.status as auctionStatus,
               a.bidsCount as bidsCount,
               a.endDate as endDate
        FROM Auction a 
        WHERE a.sellerId = :sellerId
        ORDER BY a.createdAt DESC
        """)
    List<UserAuctionProjection> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    /**
     * Projection interface for user auction items
     */
    interface UserAuctionProjection {
        Long getId();
        String getTitle();
        java.math.BigDecimal getCurrentPrice();
        java.math.BigDecimal getMinPrice();
        AuctionStatus getAuctionStatus();
        Integer getBidsCount();
        java.time.OffsetDateTime getEndDate();
    }

    /**
     * Projection interface for auction list items
     */
    interface AuctionProjection {
        Long getId();
        String getTitle();
        String getDescription();
        String getCondition();
        String getCategories();
        java.math.BigDecimal getMinPrice();
        java.math.BigDecimal getBidIncrement();
        java.math.BigDecimal getCurrentBidAmount();
        Integer getBidsCount();
        java.math.BigDecimal getMinBidToPlace();
        java.time.OffsetDateTime getEndDate();
        String getImageUrls();
        Long getSellerId();
        String getStatus();
    }
}
