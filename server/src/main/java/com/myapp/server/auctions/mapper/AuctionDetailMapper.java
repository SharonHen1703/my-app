package com.myapp.server.auctions.mapper;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.repository.AuctionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pure mapper for auction detail operations - no business logic, only field-to-field mapping.
 */
@Component
public class AuctionDetailMapper {
    
    private final ObjectMapper objectMapper;
    
    public AuctionDetailMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Maps Entity to AuctionDetail DTO with computed minBidToPlace (pure field mapping).
     */
    public AuctionDetail toAuctionDetail(Auction auction, BigDecimal minBidToPlace) {
        List<String> imageUrls = parseImageUrls(auction.getImageUrls());
        
        return new AuctionDetail(
            auction.getId(),
            auction.getTitle(),
            auction.getDescription(),
            auction.getCondition().getValue(),
            auction.getMinPrice(),
            auction.getBidIncrement(),
            auction.getCurrentBidAmount(),
            auction.getBidsCount(),
            minBidToPlace,
            auction.getEndDate(),
            imageUrls,
            auction.getSellerId(),
            auction.getStatus().name()
        );
    }
    
    /**
     * Maps Projection to AuctionDetail DTO (pure field mapping).
     */
    public AuctionDetail toAuctionDetail(AuctionRepository.AuctionProjection projection) {
        List<String> imageUrls = parseImageUrls(projection.getImageUrls());
        
        return new AuctionDetail(
            projection.getId(),
            projection.getTitle(),
            projection.getDescription(),
            projection.getCondition(),
            projection.getMinPrice(),
            projection.getBidIncrement(),
            projection.getCurrentBidAmount(),
            projection.getBidsCount(),
            projection.getMinBidToPlace(),
            projection.getEndDate(),
            imageUrls,
            projection.getSellerId(),
            projection.getStatus()
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
