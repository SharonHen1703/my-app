package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Main auction service facade that delegates to specialized services.
 * Provides a unified interface for auction operations while maintaining
 * clear separation of concerns through delegation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionService {
    
    private final AuctionQueryService auctionQueryService;
    private final AuctionCommandService auctionCommandService;
    
    /**
     * מוצא מכרזים פעילים עם paging
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size) {
        return auctionQueryService.findActiveAuctions(page, size);
    }
    
    /**
     * מוצא מכרזים פעילים עם paging וסינון לפי קטגוריה
     */
    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category) {
        return auctionQueryService.findActiveAuctions(page, size, category);
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
        return auctionQueryService.findActiveAuctions(page, size, category, minPrice, maxPrice, conditions, null);
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
        return auctionQueryService.findActiveAuctions(page, size, category, minPrice, maxPrice, conditions, searchText);
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
        return auctionQueryService.findActiveAuctions(page, size, category, minPrice, maxPrice, conditions, searchText, excludeSellerId);
    }
    
    /**
     * סופר מכרזים פעילים שלא הסתיימו
     */
    public long countActiveAuctions() {
        return auctionQueryService.countActiveAuctions();
    }
    
    /**
     * מוצא פרטי מכרז בודד (רק פעילים)
     */
    public AuctionDetail findAuctionDetail(Long id) {
        return auctionQueryService.findAuctionDetail(id);
    }
    
    /**
     * מוצא פרטי מכרז בודד ללא קשר לסטטוס
     */
    public AuctionDetail findAuctionDetailAnyStatus(Long id) {
        return auctionQueryService.findAuctionDetailAnyStatus(id);
    }
    
    /**
     * מחזיר רשימת כל הקטגוריות הקבועות
     */
    public List<String> getAllCategories() {
        return auctionQueryService.getAllCategories();
    }
    
    /**
     * מחזיר רשימת קודי הקטגוריות הקבועות
     */
    public List<String> getAllCategoryCodes() {
        return auctionQueryService.getAllCategoryCodes();
    }
    
    /**
     * מחזיר מפה של קטגוריות (קוד -> שם בעברית)
     */
    public java.util.Map<String, String> getCategoriesMap() {
        return auctionQueryService.getCategoryMapping();
    }
    
    /**
     * מוצא מכרזים של משתמש ספציפי (בהתבסס על sellerId)
     */
    public List<com.myapp.server.users.dto.UserAuctionItem> getUserAuctions(Long userId) {
        return auctionQueryService.getUserAuctions(userId);
    }

    /**
     * יוצר מכרז חדש
     */
    @Transactional
    public CreateAuctionResponse createAuction(CreateAuctionRequest request, Long currentUserId) {
        return auctionCommandService.createAuction(request, currentUserId);
    }
}
