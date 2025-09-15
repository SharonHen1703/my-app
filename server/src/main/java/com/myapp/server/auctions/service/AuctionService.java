package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.dto.UserAuctionItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
     * מוצא מכרזים פעילים עם paging וסינון מתקדם - כל הפרמטרים אופציונליים
     * 
     * @param page מספר עמוד (מתחיל מ-0)
     * @param size כמות פריטים בעמוד
     * @param category קטגוריה לסינון (null = כל הקטגוריות)
     * @param minPrice מחיר מינימלי (null = ללא מגבלה)
     * @param maxPrice מחיר מקסימלי (null = ללא מגבלה)
     * @param conditions רשימת מצבי מוצר לסינון (null/empty = כל המצבים)
     * @param searchText טקסט חופשי לחיפוש בכותרת ותיאור (null = ללא חיפוש)
     * @param excludeSellerId ID של מוכר לא רצוי (null = ללא)
     * @return דף מכרזים מסונן ומוין
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
     * מוצא פרטי מכרז בודד ללא קשר לסטטוס
     */
    public AuctionDetail findAuctionDetailAnyStatus(Long id) {
        return auctionQueryService.findAuctionDetailAnyStatus(id);
    }
    
    /**
     * מוצא מכרזים של משתמש ספציפי (בהתבסס על sellerId)
     */
    public List<UserAuctionItem> getUserAuctions(Long userId) {
        return auctionQueryService.getUserAuctions(userId);
    }

    /**
     * יוצר מכרז חדש
     */
    @Transactional
    public CreateAuctionResponse createAuction(CreateAuctionRequest request, Long currentUserId, List<MultipartFile> images) {
        return auctionCommandService.createAuction(request, currentUserId, images);
    }
}
