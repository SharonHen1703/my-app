package com.myapp.server.bids.service;

import com.myapp.server.bids.repository.BidsDao;
import com.myapp.server.common.exception.BusinessRuleViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Pure business logic for bid calculations and validations.
 * Contains no side effects - only pure functions for bid rules.
 */
@Component
public class BiddingPolicy {
    
    /**
     * Calculates the minimum bid required for a challenger (non-leader).
     */
    public BigDecimal calculateMinBidForChallenger(BigDecimal currentBid, BigDecimal minPrice, BigDecimal bidIncrement, int bidsCount) {
        if (bidsCount == 0 || currentBid == null) {
            return minPrice;
        }
        return currentBid.add(bidIncrement);
    }
    
    /**
     * Calculates the new current price using second-price auction rules.
     */
    public BigDecimal calculateNewCurrentPrice(BigDecimal minPrice, BigDecimal bidIncrement, 
                                             BigDecimal leaderMax, BigDecimal runnerMax) {
        if (runnerMax == null) {
            return minPrice;
        } else {
            BigDecimal candidate = runnerMax.add(bidIncrement);
            return candidate.min(leaderMax);
        }
    }
    
    /**
     * Calculates the next minimum bid amount.
     */
    public BigDecimal calculateNextMinBid(BigDecimal currentPrice, BigDecimal bidIncrement) {
        return currentPrice.add(bidIncrement);
    }
    
    /**
     * Validates if a bid amount is sufficient for the first bid.
     */
    public void validateFirstBid(BigDecimal bidAmount, BigDecimal minPrice) {
        if (bidAmount.compareTo(minPrice) < 0) {
            throw new BusinessRuleViolationException(
                HttpStatus.BAD_REQUEST, 
                "ההצעה נמוכה מהמחיר ההתחלתי"
            );
        }
    }
    
    /**
     * Validates if a leader can raise their own bid.
     */
    public void validateLeaderBidIncrease(BigDecimal newBid, BigDecimal previousMax) {
        if (previousMax == null || newBid.compareTo(previousMax) <= 0) {
            throw new BusinessRuleViolationException(
                HttpStatus.BAD_REQUEST,
                "עליך להעלות מעל ההצעה הקודמת שלך"
            );
        }
    }
    
    /**
     * Validates if a challenger's bid meets the minimum requirements.
     */
    public void validateChallengerBid(BigDecimal bidAmount, BigDecimal minRequired) {
        if (bidAmount.compareTo(minRequired) < 0) {
            throw new BusinessRuleViolationException(
                HttpStatus.BAD_REQUEST,
                "ההצעה נמוכה מהמינימום המותר"
            );
        }
    }
    
    /**
     * Validates basic auction state before bid placement.
     */
    public void validateAuctionState(BidsDao.AuctionRow auction, Long bidderId) {
        if (!"active".equalsIgnoreCase(auction.status())) {
            throw new BusinessRuleViolationException(
                HttpStatus.CONFLICT, 
                "המכרז אינו פעיל"
            );
        }
        
        if (auction.endDate() != null && auction.endDate().isBefore(java.time.OffsetDateTime.now())) {
            throw new BusinessRuleViolationException(
                HttpStatus.CONFLICT, 
                "המכרז הסתיים"
            );
        }
        
        if (bidderId.equals(auction.sellerId())) {
            throw new BusinessRuleViolationException(
                HttpStatus.FORBIDDEN, 
                "לא ניתן להגיש הצעה למכרז של עצמך"
            );
        }
    }
}
