package com.myapp.server.auctions.service.policy;

import com.myapp.server.auctions.entity.Auction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Handles default value application for auction operations.
 * Focused on setting appropriate defaults during create/update flows.
 */
@Component
public class AuctionDefaults {

    /**
     * Applies creation defaults to new auction
     */
    public void applyCreationDefaults(Auction auction) {
        // Set default values that are not in the request
        if (auction.getCurrentBidAmount() == null) {
            auction.setCurrentBidAmount(auction.getMinPrice());
        }
        if (auction.getBidsCount() == null) {
            auction.setBidsCount(0);
        }
    }

    /**
     * Applies update defaults to auction
     */
    public void applyUpdateDefaults(Auction auction) {
        // Add any update-specific business rules here
    }

    /**
     * Provides default empty image URLs JSON (business rule).
     */
    public String getDefaultImageUrls() {
        return "[]";
    }

    /**
     * Provides default values for UserAuctionProjection mapping (business rules).
     */
    public static class UserAuctionDefaults {
        public static final String DESCRIPTION = "";
        public static final String CONDITION = "";
        public static final String CATEGORIES = "";
        public static final BigDecimal BID_INCREMENT = BigDecimal.ZERO;
        public static final List<String> IMAGE_URLS = List.of();
        
        public static BigDecimal approximateMinBidToPlace(BigDecimal currentPrice) {
            return currentPrice != null ? currentPrice.add(BigDecimal.ONE) : BigDecimal.ONE;
        }
    }
}
