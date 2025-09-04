package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionCategory;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles all read-only auction operations.
 * Responsible for queries, searches, and data retrieval.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryService {
    
    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final AuctionPolicy auctionPolicy;
    
    /**
     * מוצא מכרזים פעילים עם paging
     */
    /**
     * מוצא מכרזים פעילים עם paging
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size) {
        return findActiveAuctions(page, size, null, null, null, null, null, null);
    }

    /**
     * מוצא מכרזים פעילים עם paging וסינון לפי קטגוריה
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category) {
        return findActiveAuctions(page, size, category, null, null, null, null, null);
    }
    /**
     * מוצא מכרזים פעילים עם paging, סינון ומחיר
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category, 
                                                   BigDecimal minPrice, BigDecimal maxPrice) {
        return findActiveAuctions(page, size, category, minPrice, maxPrice, null, null, null);
    }
    
    /**
     * מוצא מכרזים פעילים עם כל הפילטרים
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category, 
                                                   BigDecimal minPrice, BigDecimal maxPrice, 
                                                   List<String> conditions, String searchText) {
        return findActiveAuctions(page, size, category, minPrice, maxPrice, conditions, searchText, null);
    }
    
    /**
     * מוצא מכרזים פעילים עם כל הפילטרים והחרגת מוכר
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category, 
                                                   BigDecimal minPrice, BigDecimal maxPrice, 
                                                   List<String> conditions, String searchText, 
                                                   Long excludeSellerId) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Validation and processing
        String safeCategory = auctionPolicy.validateCategory(category);
        String categoryPattern = (safeCategory != null) ? "%" + safeCategory + "%" : null;
        
        BigDecimal safeMin = (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) ? minPrice : null;
        BigDecimal safeMax = (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) ? maxPrice : null;
        
        var safeConditions = auctionPolicy.validateAndParseConditions(conditions);
        String safeSearchText = auctionPolicy.validateSearchText(searchText);
        
        // Repository calls based on seller exclusion
        Page<AuctionRepository.AuctionProjection> projections;
        
        if (excludeSellerId == null) {
            // Use original methods
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

        return projections.map(auctionMapper::toAuctionListItem);
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
        
        return auctionMapper.toAuctionDetail(projection);
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
        
        return auctionMapper.toAuctionDetail(projection);
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
    public Map<String, String> getCategoryMapping() {
        return Arrays.stream(AuctionCategory.values())
                .collect(Collectors.toMap(
                    AuctionCategory::getCode,
                    AuctionCategory::getDisplayName
                ));
    }
    
    /**
     * מוצא מכרזים של משתמש ספציפי (בהתבסס על sellerId)
     */
    public List<com.myapp.server.users.dto.UserAuctionItem> getUserAuctions(Long userId) {
        List<AuctionRepository.UserAuctionProjection> projections = 
            auctionRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        
        return projections.stream()
            .map(auctionMapper::toUserAuctionItem)
            .collect(Collectors.toList());
    }
}
