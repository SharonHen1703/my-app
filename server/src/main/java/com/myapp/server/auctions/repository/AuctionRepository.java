package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.OffsetDateTime;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long>, 
                                          AuctionActiveQueries,
                                          AuctionDetailQueriesFragment,
                                          AuctionUserQueries {
     /**
     * מוצא מכרזים לפי סטטוס ותאריך סיום
     */
    List<Auction> findByStatusAndEndDateBefore(AuctionStatus status, OffsetDateTime endDate);

    /**
     * Projection interface for user auction items
     */
    interface UserAuctionProjection {
        Long getId();
        String getTitle();
        AuctionStatus getAuctionStatus();
        Integer getBidsCount();
        java.time.OffsetDateTime getEndDate();
        java.math.BigDecimal getCurrentPrice();
        java.math.BigDecimal getMinPrice();
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
