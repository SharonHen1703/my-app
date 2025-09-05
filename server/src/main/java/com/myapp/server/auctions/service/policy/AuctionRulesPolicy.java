package com.myapp.server.auctions.service.policy;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.common.exception.BusinessRuleViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handles business rules and state validation for auction operations.
 * Focused on auction state checks and business constraints.
 */
@Component
public class AuctionRulesPolicy {

    /**
     * Validates if auction can be updated
     */
    public void validateAuctionCanBeUpdated(Auction auction) {
        if (auction.getBidsCount() > 0) {
            throw new BusinessRuleViolationException(HttpStatus.CONFLICT, "Cannot update auction with existing bids");
        }
    }

    /**
     * Validates if auction can be deleted
     */
    public void validateAuctionCanBeDeleted(Auction auction) {
        if (auction.getBidsCount() > 0) {
            throw new BusinessRuleViolationException(HttpStatus.CONFLICT, "Cannot delete auction with existing bids");
        }
    }

    /**
     * Computes minimum bid to place for auction (business rule).
     */
    public BigDecimal computeMinBidToPlace(BigDecimal currentBidAmount, BigDecimal bidIncrement) {
        return currentBidAmount.add(bidIncrement);
    }
}
