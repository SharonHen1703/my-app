package com.myapp.server.auctions.entity;

import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.converter.AuctionConditionConverter;
import com.myapp.server.auctions.converter.AuctionStatusConverter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "auctions")
public class Auction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "condition", nullable = false)
    @Convert(converter = AuctionConditionConverter.class)
    private AuctionCondition condition;
    
    @Column(name = "categories", columnDefinition = "text")
    private String categories;
    
    @Column(name = "image_urls", columnDefinition = "text")
    private String imageUrls;
    
    @Column(name = "min_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal minPrice;
    
    @Column(name = "bid_increment", nullable = false, precision = 12, scale = 2)
    private BigDecimal bidIncrement;
    
    @Column(name = "current_bid_amount", precision = 12, scale = 2)
    private BigDecimal currentBidAmount;
    
    @Column(name = "highest_max_bid", precision = 12, scale = 2)
    private BigDecimal highestMaxBid;
    
    @Column(name = "highest_user_id")
    private Long highestUserId;
    
    @Column(name = "bids_count", nullable = false)
    private Integer bidsCount = 0;
    
    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "status", nullable = false)
    @Convert(converter = AuctionStatusConverter.class)
    private AuctionStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    // Constructors
    public Auction() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public AuctionCondition getCondition() { return condition; }
    public void setCondition(AuctionCondition condition) { this.condition = condition; }
    
    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }
    
    public String getImageUrls() { return imageUrls; }
    public void setImageUrls(String imageUrls) { this.imageUrls = imageUrls; }
    
    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
    
    public BigDecimal getBidIncrement() { return bidIncrement; }
    public void setBidIncrement(BigDecimal bidIncrement) { this.bidIncrement = bidIncrement; }
    
    public BigDecimal getCurrentBidAmount() { return currentBidAmount; }
    public void setCurrentBidAmount(BigDecimal currentBidAmount) { this.currentBidAmount = currentBidAmount; }
    
    public BigDecimal getHighestMaxBid() { return highestMaxBid; }
    public void setHighestMaxBid(BigDecimal highestMaxBid) { this.highestMaxBid = highestMaxBid; }
    
    public Long getHighestUserId() { return highestUserId; }
    public void setHighestUserId(Long highestUserId) { this.highestUserId = highestUserId; }
    
    public Integer getBidsCount() { return bidsCount; }
    public void setBidsCount(Integer bidsCount) { this.bidsCount = bidsCount; }
    
    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }
    
    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }
    
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
