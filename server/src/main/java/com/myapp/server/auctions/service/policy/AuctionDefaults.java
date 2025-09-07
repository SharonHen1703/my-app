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
        // Note: currentBidAmount should remain null until first bid is placed
        // This ensures minBidToPlace calculation works correctly for first bidder
        if (auction.getBidsCount() == null) {
            auction.setBidsCount(0);
        }
    }

    /**
     * Provides default empty image URLs JSON (business rule).
     */
    public String getDefaultImageUrls() {
        return "[]";
    }
}
