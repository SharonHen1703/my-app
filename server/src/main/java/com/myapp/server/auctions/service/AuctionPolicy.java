package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCategory;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Contains business logic and validation rules for auctions.
 * Pure business rules without dependencies on external services.
 */
@Component
public class AuctionPolicy {

    /**
     * Validates a create auction request
     */
    public void validateCreateAuctionRequest(CreateAuctionRequest request) {
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        
        if (request.minPrice() == null || request.minPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Min price must be greater than 0");
        }
        
        if (request.bidIncrement() == null || request.bidIncrement().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Bid increment must be greater than 0");
        }
        
        if (request.endDate() == null || request.endDate().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("End date must be in the future");
        }
        
        validateCondition(request.condition());
        validateCategories(request.categories());
    }

    /**
     * Validates if condition is valid
     */
    public void validateCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition is required");
        }
        
        try {
            AuctionCondition.fromValue(condition);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid condition: " + condition);
        }
    }

    /**
     * Validates if categories are valid
     */
    public void validateCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }
        
        for (String categoryCode : categories) {
            if (!AuctionCategory.isValidCode(categoryCode)) {
                throw new IllegalArgumentException("Invalid category: " + categoryCode);
            }
        }
    }

    /**
     * Validates if auction can be updated
     */
    public void validateAuctionCanBeUpdated(Auction auction) {
        if (auction.getBidsCount() > 0) {
            throw new IllegalStateException("Cannot update auction with existing bids");
        }
    }

    /**
     * Validates if auction can be deleted
     */
    public void validateAuctionCanBeDeleted(Auction auction) {
        if (auction.getBidsCount() > 0) {
            throw new IllegalStateException("Cannot delete auction with existing bids");
        }
    }

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
     * Validates if conditions filter is properly formatted and converts to safe list.
     */
    public List<AuctionCondition> validateAndParseConditions(List<String> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }
        
        List<AuctionCondition> cleaned = conditions.stream()
            .filter(c -> c != null && !c.trim().isEmpty())
            .map(String::trim)
            .map(this::parseCondition)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
            
        return cleaned.isEmpty() ? null : cleaned;
    }

    /**
     * Validates search text and returns safe version or null.
     */
    public String validateSearchText(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return null;
        }
        String trimmed = searchText.trim();
        return trimmed.length() > 100 ? trimmed.substring(0, 100) : trimmed;
    }

    /**
     * Validates category filter.
     */
    public String validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        return category.trim();
    }

    /**
     * Helper method to parse condition string to AuctionCondition enum.
     */
    private AuctionCondition parseCondition(String conditionStr) {
        if (conditionStr == null || conditionStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            for (AuctionCondition condition : AuctionCondition.values()) {
                if (condition.getValue().equals(conditionStr.trim())) {
                    return condition;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to parse condition: " + conditionStr + ", error: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Validates and converts condition string to enum (moved from AuctionMapper).
     */
    public AuctionCondition validateAndParseCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Condition is required");
        }
        
        try {
            return AuctionCondition.fromValue(condition);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid condition: " + condition);
        }
    }

    /**
     * Validates and converts status string to enum (moved from AuctionMapper).
     */
    public AuctionStatus validateAndParseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        
        try {
            return AuctionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    /**
     * Validates categories list and throws on invalid entries (moved from AuctionMapper).
     */
    public void validateCategoriesStrict(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("At least one category is required");
        }
        
        for (String categoryCode : categories) {
            if (!AuctionCategory.isValidCode(categoryCode)) {
                throw new IllegalArgumentException("Invalid category: " + categoryCode);
            }
        }
    }

    /**
     * Computes minimum bid to place for auction (business rule).
     */
    public BigDecimal computeMinBidToPlace(BigDecimal currentBidAmount, BigDecimal bidIncrement) {
        return currentBidAmount.add(bidIncrement);
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
