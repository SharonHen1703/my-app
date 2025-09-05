package com.myapp.server.auctions.service.policy;

import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.entity.enums.AuctionCategory;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.common.exception.BusinessRuleViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Input validation and normalization for auction operations.
 */
@Component
public class AuctionValidationPolicy {

    public void validateCreateAuctionRequest(CreateAuctionRequest request) {
        if (request.title() == null || request.title().trim().isEmpty()) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Title cannot be empty");
        }
        if (request.minPrice() == null || request.minPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Min price must be greater than 0");
        }
        if (request.bidIncrement() == null || request.bidIncrement().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Bid increment must be greater than 0");
        }
        if (request.endDate() == null || request.endDate().isBefore(OffsetDateTime.now())) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "End date must be in the future");
        }
        validateCondition(request.condition());
        validateCategories(request.categories());
    }

    public void validateCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Condition is required");
        }
        try {
            AuctionCondition.fromValue(condition);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Invalid condition: " + condition);
        }
    }

    public void validateCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "At least one category is required");
        }
        for (String categoryCode : categories) {
            if (!AuctionCategory.isValidCode(categoryCode)) {
                throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Invalid category: " + categoryCode);
            }
        }
    }

    public void validateCategoriesStrict(List<String> categories) {
        validateCategories(categories);
    }

    public AuctionCondition validateAndParseCondition(String condition) {
        if (condition == null || condition.trim().isEmpty()) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Condition is required");
        }
        try {
            return AuctionCondition.fromValue(condition);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Invalid condition: " + condition);
        }
    }

    public AuctionStatus validateAndParseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Status is required");
        }
        try {
            return AuctionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
        }
    }

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

    public String validateSearchText(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return null;
        }
        String trimmed = searchText.trim();
        return trimmed.length() > 100 ? trimmed.substring(0, 100) : trimmed;
    }

    public String validateCategory(String category) {
        return category == null || category.trim().isEmpty() ? null : category.trim();
    }

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
}
