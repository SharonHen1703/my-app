package com.myapp.server.auctions.mapper;

import com.myapp.server.auctions.repository.AuctionRepository;
import com.myapp.server.auctions.service.policy.AuctionDefaults;
import com.myapp.server.users.dto.UserAuctionItem;
import org.springframework.stereotype.Component;

/**
 * Pure mapper for user auction operations - no business logic, only field-to-field mapping.
 */
// @Component - Temporarily disabled to debug circular dependency
public class UserAuctionMapper {
    
    private final AuctionListMapper listMapper;
    
    public UserAuctionMapper(AuctionListMapper listMapper) {
        this.listMapper = listMapper;
    }
    
    /**
     * Maps UserAuctionProjection to UserAuctionItem DTO with provided status translation.
     * Status translation should be done by caller (e.g., via AuctionStatusTranslator).
     */
    public UserAuctionItem toUserAuctionItem(
            AuctionRepository.UserAuctionProjection projection,
            String translatedStatus) {
        return new UserAuctionItem(
            projection.getId(),
            projection.getTitle(),
            projection.getCurrentPrice() != null ? projection.getCurrentPrice() : projection.getMinPrice(),
            translatedStatus,
            projection.getBidsCount(),
            projection.getEndDate().toString()
        );
    }
    
    /**
     * Maps UserAuctionProjection to AuctionListItem DTO using policy defaults.
     */
    public com.myapp.server.auctions.dto.AuctionListItem toAuctionListItem(
            AuctionRepository.UserAuctionProjection projection) {
        
        return listMapper.toAuctionListItem(
            projection,
            AuctionDefaults.UserAuctionDefaults.DESCRIPTION,
            AuctionDefaults.UserAuctionDefaults.CONDITION,
            AuctionDefaults.UserAuctionDefaults.CATEGORIES,
            AuctionDefaults.UserAuctionDefaults.BID_INCREMENT,
            AuctionDefaults.UserAuctionDefaults.approximateMinBidToPlace(projection.getCurrentPrice()),
            AuctionDefaults.UserAuctionDefaults.IMAGE_URLS
        );
    }
}
