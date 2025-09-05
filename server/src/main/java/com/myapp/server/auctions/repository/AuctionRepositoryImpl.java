package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.mapper.AuctionListMapper;
import com.myapp.server.auctions.repository.impl.ActiveAuctionsPaging;
import com.myapp.server.auctions.repository.impl.ActiveAuctionsSearchQueries;
import com.myapp.server.auctions.repository.impl.AuctionDetailQueries;
import com.myapp.server.auctions.repository.impl.UserAuctionsQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

/**
 * Pure delegator for auction repository operations.
 */
@Repository
@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionActiveQueries, 
                                            AuctionDetailQueriesFragment, 
                                            AuctionUserQueries {

    private final ActiveAuctionsSearchQueries searchQueries;
    private final ActiveAuctionsPaging pagingHelper;
    private final AuctionDetailQueries auctionDetailQueries;
    private final UserAuctionsQueries userAuctionsQueries;
    private final AuctionListMapper auctionListMapper;

    @Override
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable) {
        return executeSearchWithMapping(() -> searchQueries.findActiveAuctionsWithMinBid(AuctionStatus.ACTIVE, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsWithMinBid(AuctionStatus.ACTIVE), pageable);
    }

    @Override
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable, String category) {
        return category == null || category.trim().isEmpty() ? findActiveAuctions(pageable) : executeSearchWithMapping(() -> searchQueries.findActiveAuctionsByCategory(AuctionStatus.ACTIVE, category, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsByCategory(AuctionStatus.ACTIVE, category), pageable);
    }

    @Override
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable, String category, BigDecimal minPrice, BigDecimal maxPrice, List<AuctionCondition> conditions, String searchText) {
        return executeSearchWithMapping(() -> searchText != null && !searchText.trim().isEmpty() ? searchQueries.findActiveAuctionsFilteredWithSearch(category, minPrice, maxPrice, conditions, searchText, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)) : searchQueries.findActiveAuctionsFilteredNoSearch(category, minPrice, maxPrice, conditions, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchText != null && !searchText.trim().isEmpty() ? searchQueries.countActiveAuctionsFilteredWithSearch(category, minPrice, maxPrice, conditions, searchText) : searchQueries.countActiveAuctionsFilteredNoSearch(category, minPrice, maxPrice, conditions), pageable);
    }

    @Override
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable, String category, BigDecimal minPrice, BigDecimal maxPrice, List<AuctionCondition> conditions, String searchText, Long excludeSellerId) {
        return executeSearchWithMapping(() -> searchText != null && !searchText.trim().isEmpty() ? searchQueries.findActiveAuctionsFilteredWithSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId, searchText, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)) : searchQueries.findActiveAuctionsFilteredNoSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchText != null && !searchText.trim().isEmpty() ? searchQueries.countActiveAuctionsFilteredWithSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId, searchText) : searchQueries.countActiveAuctionsFilteredNoSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId), pageable);
    }

    @Override
    public long countActiveAuctions() {
        return searchQueries.countActiveAuctions();
    }

    @Override
    public List<AuctionListItem> findBySellerId(Long sellerId) {
        return userAuctionsQueries.findBySellerId(sellerId);
    }

    @Override
    public List<AuctionListItem> findAuctionsWithBidsByUserId(Long userId) {
        return userAuctionsQueries.findAuctionsWithBidsByUserId(userId);
    }

    @Override
    public com.myapp.server.auctions.dto.AuctionDetail getAuctionDetailById(Long id) {
        return auctionDetailQueries.getAuctionDetailById(id);
    }

    @Override
    public boolean isAuctionValidForBidding(Long auctionId) {
        return auctionDetailQueries.isAuctionValidForBidding(auctionId);
    }

    @Override
    public long countActiveAuctions(AuctionStatus status) {
        return searchQueries.countActiveAuctions();
    }
    
    @Override
    public Page<AuctionRepository.AuctionProjection> findActiveAuctionsWithMinBid(AuctionStatus status, Pageable pageable) {
        return executeSearchWithProjection(() -> searchQueries.findActiveAuctionsWithMinBid(status, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsWithMinBid(status), pageable);
    }
    
    @Override
    public Page<AuctionRepository.AuctionProjection> findActiveAuctionsByCategory(AuctionStatus status, String categoryPattern, Pageable pageable) {
        return executeSearchWithProjection(() -> searchQueries.findActiveAuctionsByCategory(status, categoryPattern, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsByCategory(status, categoryPattern), pageable);
    }
    
    @Override
    public Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredNoSearch(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice, List<AuctionCondition> conditions, Pageable pageable) {
        return executeSearchWithProjection(() -> searchQueries.findActiveAuctionsFilteredNoSearch(categoryPattern, minPrice, maxPrice, conditions, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsFilteredNoSearch(categoryPattern, minPrice, maxPrice, conditions), pageable);
    }
    
    @Override
    public Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredWithSearch(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice, List<AuctionCondition> conditions, String searchPattern, Pageable pageable) {
        return executeSearchWithProjection(() -> searchQueries.findActiveAuctionsFilteredWithSearch(categoryPattern, minPrice, maxPrice, conditions, searchPattern, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsFilteredWithSearch(categoryPattern, minPrice, maxPrice, conditions, searchPattern), pageable);
    }
    
    @Override
    public Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredNoSearchExcludeSeller(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice, List<AuctionCondition> conditions, Long excludeSellerId, Pageable pageable) {
        return executeSearchWithProjection(() -> searchQueries.findActiveAuctionsFilteredNoSearchExcludeSeller(categoryPattern, minPrice, maxPrice, conditions, excludeSellerId, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsFilteredNoSearchExcludeSeller(categoryPattern, minPrice, maxPrice, conditions, excludeSellerId), pageable);
    }
    
    @Override
    public Page<AuctionRepository.AuctionProjection> findActiveAuctionsFilteredWithSearchExcludeSeller(String categoryPattern, BigDecimal minPrice, BigDecimal maxPrice, List<AuctionCondition> conditions, Long excludeSellerId, String searchPattern, Pageable pageable) {
        return executeSearchWithProjection(() -> searchQueries.findActiveAuctionsFilteredWithSearchExcludeSeller(categoryPattern, minPrice, maxPrice, conditions, excludeSellerId, searchPattern, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchQueries.countActiveAuctionsFilteredWithSearchExcludeSeller(categoryPattern, minPrice, maxPrice, conditions, excludeSellerId, searchPattern), pageable);
    }
    
    @Override
    public AuctionRepository.AuctionProjection findAuctionDetailById(Long id, AuctionStatus status) {
        return auctionDetailQueries.findAuctionDetailById(id, status);
    }
    
    @Override
    public AuctionRepository.AuctionProjection findAuctionDetailByIdAnyStatus(Long id) {
        return auctionDetailQueries.findAuctionDetailByIdAnyStatus(id);
    }
    
    @Override
    public List<AuctionRepository.UserAuctionProjection> findBySellerIdOrderByCreatedAtDesc(Long sellerId) {
        return userAuctionsQueries.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    private Page<AuctionListItem> executeSearchWithMapping(Supplier<List<Auction>> searchFunction, Supplier<Long> countFunction, Pageable pageable) {
        return pagingHelper.createProjectionPage(searchFunction.get(), pageable, countFunction.get()).map(auctionListMapper::toAuctionListItem);
    }
    
    private Page<AuctionRepository.AuctionProjection> executeSearchWithProjection(Supplier<List<Auction>> searchFunction, Supplier<Long> countFunction, Pageable pageable) {
        return pagingHelper.createProjectionPage(searchFunction.get(), pageable, countFunction.get());
    }
}
