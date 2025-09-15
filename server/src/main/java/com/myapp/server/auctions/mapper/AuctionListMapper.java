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
