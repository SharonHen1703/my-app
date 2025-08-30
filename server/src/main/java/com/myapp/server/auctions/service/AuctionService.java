package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.entity.enums.AuctionCategory;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.repository.AuctionRepository;
import com.myapp.server.auctions.utils.AuctionStatusTranslator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import java.time.Duration;

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
        return findActiveAuctions(page, size, null, null, null, null);
    }
    
    /**
     * מוצא מכרזים פעילים עם paging וסינון לפי קטגוריה
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category) {
        return findActiveAuctions(page, size, category, null, null, null, null);
    }

    /**
     * מוצא מכרזים פעילים עם paging וסינון לפי: קטגוריה, טווח מחיר, ומצב (רשימה)
     */
    public Page<AuctionListItem> findActiveAuctions(
            int page,
            int size,
            String category,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            List<String> conditions
    ) {
        return findActiveAuctions(page, size, category, minPrice, maxPrice, conditions, null);
    }

    /**
     * מוצא מכרזים פעילים עם paging וסינון לפי: קטגוריה, טווח מחיר, מצב וחיפוש טקסט
     */
    public Page<AuctionListItem> findActiveAuctions(
            int page,
            int size,
            String category,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            List<String> conditions,
            String searchText
    ) {
        return findActiveAuctions(page, size, category, minPrice, maxPrice, conditions, searchText, null);
    }

    /**
     * מוצא מכרזים פעילים עם paging וסינון לפי: קטגוריה, טווח מחיר, מצב, חיפוש טקסט ומוציא מכרזים של מוכר מסוים
     */
    public Page<AuctionListItem> findActiveAuctions(
            int page,
            int size,
            String category,
            java.math.BigDecimal minPrice,
            java.math.BigDecimal maxPrice,
            List<String> conditions,
            String searchText,
            Long excludeSellerId
    ) {
    Pageable pageable = PageRequest.of(page, size);

        String safeCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : null;
        // Build LIKE pattern once to avoid CONCAT in JPQL (prevents varchar ~~ bytea in Postgres)
        String categoryPattern = (safeCategory == null) ? null : "%\"" + safeCategory + "\"%";
        java.math.BigDecimal safeMin = (minPrice != null && minPrice.signum() >= 0) ? minPrice : null;
        java.math.BigDecimal safeMax = (maxPrice != null && maxPrice.signum() >= 0) ? maxPrice : null;
        
        List<com.myapp.server.auctions.entity.enums.AuctionCondition> safeConditions = null;
        if (conditions != null && !conditions.isEmpty()) {
            List<com.myapp.server.auctions.entity.enums.AuctionCondition> cleaned = conditions.stream()
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(String::trim)
                .map(this::parseCondition)
                .filter(java.util.Objects::nonNull)
                .toList();
            if (!cleaned.isEmpty()) {
                safeConditions = cleaned;
            }
        }

        System.out.println("AuctionService.findActiveAuctions - Debug Info:");
        System.out.println("  Original conditions: " + conditions);
        System.out.println("  Processed safeConditions: " + safeConditions);
        System.out.println("  Category pattern: " + categoryPattern);
        System.out.println("  Price range: " + safeMin + " - " + safeMax);
        System.out.println("  Exclude seller ID: " + excludeSellerId);        // Process search text - ensure empty strings become null
        String safeSearchText = null;
        if (searchText != null && !searchText.trim().isEmpty()) {
            safeSearchText = searchText.trim();
        }

        Page<AuctionRepository.AuctionProjection> projections;
        
        if (excludeSellerId == null) {
            // Use original methods when no seller exclusion needed
            if (safeSearchText == null) {
                projections = auctionRepository.findActiveAuctionsFilteredNoSearch(
                    categoryPattern,
                    safeMin,
                    safeMax,
                    safeConditions,
                    pageable
                );
            } else {
                String searchPattern = "%" + safeSearchText + "%";
                projections = auctionRepository.findActiveAuctionsFilteredWithSearch(
                    categoryPattern,
                    safeMin,
                    safeMax,
                    safeConditions,
                    searchPattern,
                    pageable
                );
            }
        } else {
            // Use new methods that exclude seller
            if (safeSearchText == null) {
                projections = auctionRepository.findActiveAuctionsFilteredNoSearchExcludeSeller(
                    categoryPattern,
                    safeMin,
                    safeMax,
                    safeConditions,
                    excludeSellerId,
                    pageable
                );
            } else {
                String searchPattern = "%" + safeSearchText + "%";
                projections = auctionRepository.findActiveAuctionsFilteredWithSearchExcludeSeller(
                    categoryPattern,
                    safeMin,
                    safeMax,
                    safeConditions,
                    excludeSellerId,
                    searchPattern,
                    pageable
                );
            }
        }

        return projections.map(this::convertToAuctionListItem);
    }
    
    /**
     * סופר מכרזים פעילים שלא הסתיימו
     */
    public long countActiveAuctions() {
        return auctionRepository.countActiveAuctions(AuctionStatus.ACTIVE);
    }
    
    /**
     * מוצא פרטי מכרז בודד (רק פעילים)
     */
    public AuctionDetail findAuctionDetail(Long id) {
        AuctionRepository.AuctionProjection projection = 
            auctionRepository.findAuctionDetailById(id, AuctionStatus.ACTIVE);
        
        if (projection == null) {
            throw new RuntimeException("Auction not found or not active");
        }
        
        return convertToAuctionDetail(projection);
    }
    
    /**
     * מוצא פרטי מכרז בודד ללא קשר לסטטוס
     */
    public AuctionDetail findAuctionDetailAnyStatus(Long id) {
        AuctionRepository.AuctionProjection projection = 
            auctionRepository.findAuctionDetailByIdAnyStatus(id);
        
        if (projection == null) {
            throw new RuntimeException("Auction not found");
        }
        
        return convertToAuctionDetail(projection);
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
     * ממיר Projection ל-AuctionDetail DTO
     */
    private AuctionDetail convertToAuctionDetail(AuctionRepository.AuctionProjection projection) {
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
     * מחזיר רשימת כל הקטגוריות הקבועות
     */
    public List<String> getAllCategories() {
        return Arrays.stream(AuctionCategory.values())
                .map(AuctionCategory::getDisplayName)
                .sorted()
                .toList();
    }
    
    /**
     * מחזיר רשימת קודי הקטגוריות הקבועות
     */
    public List<String> getAllCategoryCodes() {
        return Arrays.stream(AuctionCategory.values())
                .map(AuctionCategory::getCode)
                .sorted()
                .toList();
    }
    
    /**
     * מחזיר מפה של קטגוריות (קוד -> שם בעברית)
     */
    public java.util.Map<String, String> getCategoriesMap() {
        return Arrays.stream(AuctionCategory.values())
                .collect(java.util.stream.Collectors.toMap(
                    AuctionCategory::getCode,
                    AuctionCategory::getDisplayName
                ));
    }

    /**
     * יוצר מכרז חדש
     */
    @Transactional
    public CreateAuctionResponse createAuction(CreateAuctionRequest request, Long currentUserId) {
        // Validate end date is after start date
        if (!request.endDate().isAfter(request.startDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Validate condition
        AuctionCondition condition;
        try {
            condition = AuctionCondition.fromValue(request.condition());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid condition: " + request.condition());
        }

        // Validate categories
        for (String categoryCode : request.categories()) {
            if (!AuctionCategory.isValidCode(categoryCode)) {
                throw new IllegalArgumentException("Invalid category: " + categoryCode);
            }
        }

        // Validate status
        AuctionStatus status;
        try {
            status = AuctionStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.status());
        }

        // Create auction entity
        Auction auction = new Auction();
        auction.setTitle(request.title());
        auction.setDescription(request.description());
        auction.setCondition(condition);
        
        // Convert categories list to JSON string
        try {
            String categoriesJson = objectMapper.writeValueAsString(request.categories());
            auction.setCategories(categoriesJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize categories", e);
        }
        
        auction.setImageUrls("[]"); // Empty array for now
        auction.setMinPrice(request.minPrice());
        auction.setBuyNowPrice(null); // Not supported yet
        auction.setBidIncrement(request.bidIncrement());
        auction.setCurrentBidAmount(request.minPrice()); // Initialize with min_price
        auction.setHighestMaxBid(null);
        auction.setHighestUserId(null);
        auction.setBidsCount(request.bidsCount());
        auction.setStartDate(request.startDate());
        auction.setEndDate(request.endDate());
        auction.setSellerId(currentUserId); // Use authenticated user instead of request payload
        auction.setStatus(status);

        // Save auction
        Auction savedAuction = auctionRepository.save(auction);

        return new CreateAuctionResponse(
            savedAuction.getId(),
            savedAuction.getTitle(),
            "Auction created successfully"
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
    
    /**
     * מוצא מכרזים של משתמש ספציפי (בהתבסס על sellerId)
     */
    public List<com.myapp.server.users.dto.UserAuctionItem> getUserAuctions(Long userId) {
        List<AuctionRepository.UserAuctionProjection> projections = 
            auctionRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        
        return projections.stream()
            .map(projection -> new com.myapp.server.users.dto.UserAuctionItem(
                projection.getId(),
                projection.getTitle(),
                projection.getCurrentPrice() != null ? projection.getCurrentPrice() : projection.getMinPrice(),
                AuctionStatusTranslator.translateToHebrew(projection.getAuctionStatus()),
                projection.getBidsCount(),
                projection.getEndDate().toString() // שולח את תאריך הסיום כמו שהוא
            ))
            .collect(Collectors.toList());
    }
    
    /**
     * Helper method to parse condition string to AuctionCondition enum
     */
    private com.myapp.server.auctions.entity.enums.AuctionCondition parseCondition(String conditionStr) {
        if (conditionStr == null || conditionStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            for (com.myapp.server.auctions.entity.enums.AuctionCondition condition : 
                 com.myapp.server.auctions.entity.enums.AuctionCondition.values()) {
                if (condition.getValue().equals(conditionStr.trim())) {
                    return condition;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to parse condition: " + conditionStr + ", error: " + e.getMessage());
        }
        
        return null;
    }
}
