package com.myapp.server.auctions.mapper;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.Auction;
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
     * Maps Auction entity to AuctionListItem DTO (pure field mapping).
     * Computes minBidToPlace using simple logic.
     */
    public AuctionListItem toAuctionListItem(Auction auction) {
        List<String> imageUrls = parseImageUrls(auction.getImageUrls());
        
        // Simple minBidToPlace calculation
        BigDecimal minBidToPlace = auction.getCurrentBidAmount() != null 
            ? auction.getCurrentBidAmount().add(auction.getBidIncrement())
            : auction.getMinPrice();
        
        return new AuctionListItem(
            auction.getId(),
            auction.getTitle(),
            auction.getDescription(),
            auction.getCondition().getValue(),
            auction.getCategories(),
            auction.getMinPrice(),
            auction.getBidIncrement(),
            auction.getCurrentBidAmount() != null ? auction.getCurrentBidAmount() : auction.getMinPrice(),
            auction.getBidsCount(),
            minBidToPlace,
            auction.getEndDate(),
            imageUrls
        );
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
            projection.getCurrentBidAmount() != null ? projection.getCurrentBidAmount() : projection.getMinPrice(),
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
    
    /**
     * Alternative parsing method for simpler JSON format (used by UserAuctionsQueries).
     * Handles manual JSON parsing without Jackson for simple array strings.
     */
    public List<String> parseImageUrlsSimple(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            if (imageUrlsJson.startsWith("[") && imageUrlsJson.endsWith("]")) {
                String content = imageUrlsJson.substring(1, imageUrlsJson.length() - 1);
                if (content.trim().isEmpty()) {
                    return List.of();
                }
                return List.of(content.split(","))
                    .stream()
                    .map(String::trim)
                    .map(s -> s.replaceAll("^\"|\"$", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
            }
            return List.of(imageUrlsJson);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    /**
     * Calculate minimum bid to place based on current bid and increment.
     */
    public BigDecimal calculateMinBidToPlace(BigDecimal currentBid, BigDecimal bidIncrement, BigDecimal minPrice) {
        if (currentBid != null && currentBid.compareTo(BigDecimal.ZERO) > 0) {
            return currentBid.add(bidIncrement != null ? bidIncrement : BigDecimal.ONE);
        }
        return minPrice != null ? minPrice : BigDecimal.ZERO;
    }
}
