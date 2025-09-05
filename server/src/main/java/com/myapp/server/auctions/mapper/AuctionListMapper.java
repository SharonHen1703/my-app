package com.myapp.server.auctions.mapper;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.repository.AuctionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pure mapper for auction list operations - no business logic, only field-to-field mapping.
 */
@Component
public class AuctionListMapper {
    
    private final ObjectMapper objectMapper;
    
    public AuctionListMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Maps Projection to AuctionListItem DTO (pure field mapping).
     */
    public AuctionListItem toAuctionListItem(AuctionRepository.AuctionProjection projection) {
        List<String> imageUrls = parseImageUrls(projection.getImageUrls());
        
        return new AuctionListItem(
            projection.getId(),
            projection.getTitle(),
            projection.getDescription(),
            projection.getCondition(),
            projection.getCategories(),
            projection.getMinPrice(),
            projection.getBidIncrement(),
            projection.getCurrentBidAmount(),
            projection.getBidsCount(),
            projection.getMinBidToPlace(),
            projection.getEndDate(),
            imageUrls
        );
    }
    
    /**
     * Maps UserAuctionProjection to AuctionListItem DTO with provided default values.
     * Business logic for defaults should be applied by caller.
     */
    public AuctionListItem toAuctionListItem(
            AuctionRepository.UserAuctionProjection projection,
            String description,
            String condition,
            String categories,
            BigDecimal bidIncrement,
            BigDecimal minBidToPlace,
            List<String> imageUrls) {
        
        return new AuctionListItem(
            projection.getId(),
            projection.getTitle(),
            description,
            condition,
            categories,
            projection.getMinPrice(),
            bidIncrement,
            projection.getCurrentPrice() != null ? projection.getCurrentPrice() : projection.getMinPrice(),
            projection.getBidsCount(),
            minBidToPlace,
            projection.getEndDate(),
            imageUrls
        );
    }
    
    /**
     * Parses JSON string to image URLs list (pure utility).
     */
    public List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.equals("[]")) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
