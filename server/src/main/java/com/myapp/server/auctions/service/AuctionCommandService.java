package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionFormMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import com.myapp.server.auctions.service.policy.AuctionDefaults;
import com.myapp.server.auctions.service.policy.AuctionValidationPolicy;
import com.myapp.server.auth.entity.User;
import com.myapp.server.auth.repository.UserRepository;
import com.myapp.server.common.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles all write operations for auctions.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCommandService {
    
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final AuctionFormMapper auctionFormMapper;
    private final AuctionValidationPolicy validationPolicy;
    private final AuctionDefaults auctionDefaults;
    
    /**
     * יוצר מכרז חדש
     */
    public CreateAuctionResponse createAuction(CreateAuctionRequest request, Long sellerId) {
        // Validate user exists
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new BusinessRuleViolationException(HttpStatus.NOT_FOUND, "User not found"));
        
        // Validate business rules
        validationPolicy.validateCreateAuctionRequest(request);
        
        // Build auction entity
        Auction auction = buildAuctionFromRequest(request, seller);
        
        // Save auction
        Auction savedAuction = auctionRepository.save(auction);
        
        // Map to response
        return auctionFormMapper.toCreateAuctionResponse(savedAuction, "Auction created successfully");
    }
    
    /**
     * בונה entity של מכרז מה-DTO
     */
    private Auction buildAuctionFromRequest(CreateAuctionRequest request, User seller) {
        // Validate and convert business data using policy
        AuctionCondition validatedCondition = validationPolicy.validateAndParseCondition(request.condition());
        AuctionStatus validatedStatus = validationPolicy.validateAndParseStatus(request.status());
        validationPolicy.validateCategoriesStrict(request.categories());
        
        // Serialize categories to JSON
        String categoriesJson = auctionFormMapper.serializeCategories(request.categories());
        String imageUrlsJson = auctionDefaults.getDefaultImageUrls();
        
        // Use pure mapper with validated data
        return auctionFormMapper.fromCreateAuctionRequest(
            request, 
            seller.getId(),
            validatedCondition,
            validatedStatus,
            categoriesJson,
            imageUrlsJson
        );
    }
}
