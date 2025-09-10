package com.myapp.server.auctions.repository.impl;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.repository.AuctionRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Simple projection implementation for temporary use during @Query migration
 */
public class AuctionProjectionImpl implements AuctionRepository.AuctionProjection {
    private final Auction auction;
    
    public AuctionProjectionImpl(Auction auction) {
        this.auction = auction;
    }
    
    @Override public Long getId() { return auction.getId(); }
    @Override public String getTitle() { return auction.getTitle(); }
    @Override public String getDescription() { return auction.getDescription(); }
    @Override public String getCondition() { return auction.getCondition().name(); }
    @Override public String getCategories() { return auction.getCategories(); }
    @Override public BigDecimal getMinPrice() { return auction.getMinPrice(); }
    @Override public BigDecimal getBidIncrement() { return auction.getBidIncrement(); }
    @Override public BigDecimal getCurrentBidAmount() { return auction.getCurrentBidAmount(); }
    @Override public Integer getBidsCount() { return auction.getBidsCount(); }
    @Override public BigDecimal getMinBidToPlace() { 
        return getCurrentBidAmount() != null ? 
            getCurrentBidAmount().add(getBidIncrement()) : getMinPrice(); 
    }
    @Override public OffsetDateTime getEndDate() { return auction.getEndDate(); }
    @Override public String getImageUrls() { return auction.getImageUrls(); }
    @Override public Long getSellerId() { return auction.getSellerId(); }
    @Override public String getStatus() { return auction.getStatus().name(); }
}
