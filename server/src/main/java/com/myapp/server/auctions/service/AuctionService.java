package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.repository.AuctionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuctionService {
    
    private final AuctionRepository auctionRepository;
    private final ObjectMapper objectMapper;
    
    public AuctionService(AuctionRepository auctionRepository, ObjectMapper objectMapper) {
        this.auctionRepository = auctionRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * מוצא מכרזים פעילים עם paging
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionRepository.AuctionProjection> projections = 
            auctionRepository.findActiveAuctionsWithMinBid(AuctionStatus.ACTIVE, pageable);
        
        return projections.map(this::convertToAuctionListItem);
    }
    
    /**
     * סופר מכרזים פעילים
     */
    public long countActiveAuctions() {
        return auctionRepository.countByStatus(AuctionStatus.ACTIVE);
    }
    
    /**
     * ממיר Projection ל-AuctionListItem DTO
     */
    private AuctionListItem convertToAuctionListItem(AuctionRepository.AuctionProjection projection) {
        List<String> imageUrls = parseImageUrls(projection.getImageUrls());
        
        return new AuctionListItem(
            projection.getId(),
            projection.getTitle(),
            projection.getDescription(),
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
     * ממיר JSON string למערך תמונות
     */
    private List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.equals("[]")) {
            return List.of();
        }
        
        try {
            return objectMapper.readValue(imageUrlsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // If parsing fails, return empty list
            return List.of();
        }
    }
}
