package com.myapp.server.auctions.mapper;

import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Pure mapper for auction form operations - no validation/business logic, only field-to-field mapping.
 * Validation and defaults should be applied by policy/service layers before calling these methods.
 */
@Component
public class AuctionFormMapper {
    
    private final ObjectMapper objectMapper;
    
    public AuctionFormMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Creates CreateAuctionResponse from saved Auction entity (pure field mapping).
     */
    public CreateAuctionResponse toCreateAuctionResponse(Auction auction, String message) {
        return new CreateAuctionResponse(
            auction.getId(),
            auction.getTitle(),
            message
        );
    }
    
    /**
     * Maps CreateAuctionRequest to Auction entity (pure field mapping).
     * Assumes all validation and enum conversion has been done by caller.
     */
    public Auction fromCreateAuctionRequest(
            CreateAuctionRequest request, 
            Long sellerId,
            AuctionCondition validatedCondition,
            AuctionStatus validatedStatus,
            String categoriesJson,
            String imageUrlsJson) {
        
        Auction auction = new Auction();
        auction.setTitle(request.title());
        auction.setDescription(request.description());
        auction.setCondition(validatedCondition);
        auction.setCategories(categoriesJson);
        auction.setImageUrls(imageUrlsJson);
        auction.setMinPrice(request.minPrice());
        auction.setBuyNowPrice(null); // Business rule: not supported yet
        auction.setBidIncrement(request.bidIncrement());
        auction.setCurrentBidAmount(null); // Business rule: no current bid until first bid is placed
        auction.setHighestMaxBid(null); // Business rule: no bids yet
        auction.setHighestUserId(null); // Business rule: no bids yet
        auction.setBidsCount(request.bidsCount());
        auction.setStartDate(request.startDate());
        auction.setEndDate(request.endDate());
        auction.setSellerId(sellerId);
        auction.setStatus(validatedStatus);

        return auction;
    }
    
    /**
     * Serializes categories list to JSON string (pure utility).
     */
    public String serializeCategories(java.util.List<String> categories) {
        try {
            return objectMapper.writeValueAsString(categories);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize categories", e);
        }
    }
}
