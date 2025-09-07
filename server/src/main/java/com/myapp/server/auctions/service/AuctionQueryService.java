package com.myapp.server.auctions.service;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionCategory;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionDetailMapper;
import com.myapp.server.auctions.mapper.AuctionListMapper;
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
    private final AuctionListMapper auctionListMapper;
    private final AuctionValidationPolicy validationPolicy;

    public Page<AuctionListItem> findActiveAuctions(int page, int size, String category, BigDecimal minPrice, BigDecimal maxPrice, List<String> conditions, String searchText, Long excludeSellerId) {
        return auctionRepository.findActiveAuctionsDomain(PageRequest.of(page, size), category, minPrice, maxPrice, validationPolicy.validateAndParseConditions(conditions), validationPolicy.validateSearchText(searchText), excludeSellerId)
            .map(auctionListMapper::toAuctionListItem);
    }

    public AuctionDetail findAuctionDetailAnyStatus(Long id) {
        return runQueryMapToObject(() -> auctionRepository.findAuctionDetailByIdAnyStatus(id), () -> "Auction not found");
    }

    public Map<String, String> getCategoryMapping() {
        return Arrays.stream(AuctionCategory.values()).collect(Collectors.toMap(AuctionCategory::getCode, AuctionCategory::getDisplayName));
    }

    public List<com.myapp.server.auctions.dto.UserAuctionItem> getUserAuctions(Long userId) {
        List<AuctionRepository.UserAuctionProjection> projections = 
            auctionRepository.findBySellerIdOrderByCreatedAtDesc(userId);
        
        return projections.stream()
            .map(p -> new com.myapp.server.auctions.dto.UserAuctionItem(
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

    private AuctionDetail runQueryMapToObject(Supplier<AuctionRepository.AuctionProjection> querySupplier, Supplier<String> errorMessageSupplier) {
        AuctionRepository.AuctionProjection projection = querySupplier.get();
        if (projection == null) {
            throw new BusinessRuleViolationException(HttpStatus.NOT_FOUND, errorMessageSupplier.get());
        }
        return auctionDetailMapper.toAuctionDetail(projection);
    }
}
