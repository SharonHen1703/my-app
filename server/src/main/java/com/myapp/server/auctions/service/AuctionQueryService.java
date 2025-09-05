package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionCategory;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionDetailMapper;
import com.myapp.server.auctions.repository.AuctionRepository;
import com.myapp.server.auctions.service.policy.AuctionValidationPolicy;
import com.myapp.server.common.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Thin orchestrator for auction read operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryService {
    
    private final AuctionRepository auctionRepository;
    private final AuctionDetailMapper auctionDetailMapper;
    private final AuctionValidationPolicy validationPolicy;
    
    public Page<AuctionListItem> findActiveAuctions(int page, int size) {
        return runQueryMapToPage(() -> auctionRepository.findActiveAuctions(PageRequest.of(page, size)));
    }

    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category) {
        return runQueryMapToPage(() -> auctionRepository.findActiveAuctions(PageRequest.of(page, size), category));
    }

    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        return findActiveAuctions(page, size, category, minPrice, maxPrice, null, null, null);
    }

    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category, BigDecimal minPrice, BigDecimal maxPrice, List<String> conditions, String searchText) {
        return findActiveAuctions(page, size, category, minPrice, maxPrice, conditions, searchText, null);
    }

    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category, BigDecimal minPrice, BigDecimal maxPrice, List<String> conditions, String searchText, Long excludeSellerId) {
        return runQueryMapToPage(() -> auctionRepository.findActiveAuctions(PageRequest.of(page, size), category, minPrice, maxPrice, validationPolicy.validateAndParseConditions(conditions), validationPolicy.validateSearchText(searchText), excludeSellerId));
    }

    public long countActiveAuctions() {
        return auctionRepository.countActiveAuctions(AuctionStatus.ACTIVE);
    }

    public AuctionDetail findAuctionDetail(Long id) {
        return runQueryMapToObject(() -> auctionRepository.findAuctionDetailById(id, AuctionStatus.ACTIVE), () -> "Auction not found or not active");
    }

    public AuctionDetail findAuctionDetailAnyStatus(Long id) {
        return runQueryMapToObject(() -> auctionRepository.findAuctionDetailByIdAnyStatus(id), () -> "Auction not found");
    }

    public List<String> getAllCategories() {
        return Arrays.stream(AuctionCategory.values()).map(AuctionCategory::getDisplayName).sorted().toList();
    }

    public List<String> getAllCategoryCodes() {
        return Arrays.stream(AuctionCategory.values()).map(AuctionCategory::getCode).sorted().toList();
    }

    public Map<String, String> getCategoryMapping() {
        return Arrays.stream(AuctionCategory.values()).collect(Collectors.toMap(AuctionCategory::getCode, AuctionCategory::getDisplayName));
    }

    public List<com.myapp.server.users.dto.UserAuctionItem> getUserAuctions(Long userId) {
        List<AuctionRepository.UserAuctionProjection> projections = 
            auctionRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        
        return projections.stream()
            .map(p -> new com.myapp.server.users.dto.UserAuctionItem(
                p.getId(),
                p.getTitle(),
                p.getCurrentPrice(),
                mapStatusToHebrew(p.getAuctionStatus()),
                p.getBidsCount(),
                p.getEndDate().toString()
            ))
            .toList();
    }

    private String mapStatusToHebrew(com.myapp.server.auctions.entity.enums.AuctionStatus status) {
        return switch (status) {
            case ACTIVE -> "פעיל";
            case SOLD -> "הסתיים בהצלחה";
            case UNSOLD -> "הסתיים ללא זכייה";
            default -> status.toString();
        };
    }

    private Page<AuctionListItem> runQueryMapToPage(Supplier<Page<AuctionListItem>> querySupplier) {
        return querySupplier.get();
    }

    private AuctionDetail runQueryMapToObject(Supplier<AuctionRepository.AuctionProjection> querySupplier, Supplier<String> errorMessageSupplier) {
        AuctionRepository.AuctionProjection projection = querySupplier.get();
        if (projection == null) {
            throw new BusinessRuleViolationException(HttpStatus.NOT_FOUND, errorMessageSupplier.get());
        }
        return auctionDetailMapper.toAuctionDetail(projection);
    }
}
