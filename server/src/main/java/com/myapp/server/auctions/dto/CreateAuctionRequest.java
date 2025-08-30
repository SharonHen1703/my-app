package com.myapp.server.auctions.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CreateAuctionRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 5, message = "Title must be at least 5 characters long")
    String title,
    
    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters long")
    String description,
    
    @NotBlank(message = "Condition is required")
    String condition,
    
    @NotNull(message = "Categories are required")
    @Size(min = 1, message = "At least one category is required")
    List<String> categories,
    
    @NotNull(message = "Min price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Min price must be positive")
    @Digits(integer = 12, fraction = 3, message = "Invalid price format")
    BigDecimal minPrice,
    
    @NotNull(message = "Bid increment is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Bid increment must be positive")
    @Digits(integer = 12, fraction = 3, message = "Invalid bid increment format")
    BigDecimal bidIncrement,
    
    @NotNull(message = "Start date is required")
    OffsetDateTime startDate,
    
    @NotNull(message = "End date is required")
    OffsetDateTime endDate,
    
    @NotBlank(message = "Status is required")
    String status,
    
    @NotNull(message = "Bids count is required")
    @Min(value = 0, message = "Bids count cannot be negative")
    Integer bidsCount
) {}
