package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCategory;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import com.myapp.server.users.entity.User;
import com.myapp.server.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles all write operations for auctions.
 * Responsible for creating, updating, and managing auction lifecycle.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCommandService {
    
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final AuctionMapper auctionMapper;
    private final AuctionPolicy auctionPolicy;
    
    /**
     * יוצר מכרז חדש
     */
    public CreateAuctionResponse createAuction(CreateAuctionRequest request, Long sellerId) {
        // Validate user exists
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate business rules
        auctionPolicy.validateCreateAuctionRequest(request);
        
        // Build auction entity
        Auction auction = buildAuctionFromRequest(request, seller);
        
        // Save auction
        Auction savedAuction = auctionRepository.save(auction);
        
        // Map to response
        return auctionMapper.toCreateAuctionResponse(savedAuction);
    }
    
    /**
     * בונה entity של מכרז מה-DTO
     */
    private Auction buildAuctionFromRequest(CreateAuctionRequest request, User seller) {
        Auction auction = new Auction();
        
        // Set basic fields
        auction.setTitle(request.getTitle());
        auction.setDescription(request.getDescription());
        auction.setStartingPrice(request.getStartingPrice());
        auction.setCurrentPrice(request.getStartingPrice());
        auction.setMinBidIncrement(request.getMinBidIncrement());
        auction.setEndTime(request.getEndTime());
        auction.setBuyItNowPrice(request.getBuyItNowPrice());
        auction.setCondition(request.getCondition());
        auction.setShippingCost(request.getShippingCost());
        auction.setImageUrls(auctionPolicy.formatImageUrls(request.getImageUrls()));
        
        // Set category using validation
        AuctionCategory category = auctionPolicy.parseCategory(request.getCategory());
        auction.setCategory(category);
        
        // Set derived fields
        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCreatedAt(LocalDateTime.now());
        auction.setTotalBids(0);
        
        return auction;
    }
    
    /**
     * מעדכן מכרז קיים (עדכונים בסיסיים בלבד - לא מחיר או סטטוס)
     */
    public void updateAuctionBasicInfo(Long auctionId, String title, String description, Long sellerId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        // Verify ownership
        if (!auction.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Not authorized to update this auction");
        }
        
        // Verify auction is still active
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new RuntimeException("Cannot update inactive auction");
        }
        
        // Update allowed fields
        if (title != null && !title.trim().isEmpty()) {
            auction.setTitle(title.trim());
        }
        if (description != null) {
            auction.setDescription(description);
        }
        
        auctionRepository.save(auction);
    }
    
    /**
     * מסמן מכרז כמסתיים (לשימוש פנימי)
     */
    public void markAuctionEnded(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        if (auction.getStatus() == AuctionStatus.ACTIVE) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);
        }
    }
    
    /**
     * מבטל מכרז (רק אם אין הצעות)
     */
    public void cancelAuction(Long auctionId, Long sellerId) {
        Auction auction = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new RuntimeException("Auction not found"));
        
        // Verify ownership
        if (!auction.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Not authorized to cancel this auction");
        }
        
        // Verify no bids
        if (auction.getTotalBids() > 0) {
            throw new RuntimeException("Cannot cancel auction with existing bids");
        }
        
        // Cancel auction
        auction.setStatus(AuctionStatus.ENDED);
        auctionRepository.save(auction);
    }
}
