package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
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
    
    // === Domain methods (return entities without DTO mapping) ===
    
    @Override
    public Page<Auction> findActiveAuctionsDomain(Pageable pageable, String category, BigDecimal minPrice, BigDecimal maxPrice, List<AuctionCondition> conditions, String searchText, Long excludeSellerId) {
        return executeSearchWithoutMapping(() -> searchText != null && !searchText.trim().isEmpty() ? searchQueries.findActiveAuctionsFilteredWithSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId, searchText, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)) : searchQueries.findActiveAuctionsFilteredNoSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId, pagingHelper.getOffset(pageable), pagingHelper.getPageSize(pageable)), () -> searchText != null && !searchText.trim().isEmpty() ? searchQueries.countActiveAuctionsFilteredWithSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId, searchText) : searchQueries.countActiveAuctionsFilteredNoSearchExcludeSeller(category, minPrice, maxPrice, conditions, excludeSellerId), pageable);
    }
    
    // === Domain methods for user queries ===
    
    @Override
    public List<Auction> findBySellerIdDomain(Long sellerId) {
        return userAuctionsQueries.findBySellerIdDomain(sellerId);
    }

    @Override
    public List<Auction> findAuctionsWithBidsByUserIdDomain(Long userId) {
        return userAuctionsQueries.findAuctionsWithBidsByUserIdDomain(userId);
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
    public AuctionRepository.AuctionProjection findAuctionDetailByIdAnyStatus(Long id) {
        return auctionDetailQueries.findAuctionDetailByIdAnyStatus(id);
    }
    
    @Override
    public List<AuctionRepository.UserAuctionProjection> findBySellerIdOrderByCreatedAtDesc(Long sellerId) {
        return userAuctionsQueries.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }
    
    private Page<AuctionRepository.AuctionProjection> executeSearchWithProjection(Supplier<List<Auction>> searchFunction, Supplier<Long> countFunction, Pageable pageable) {
        return pagingHelper.createProjectionPage(searchFunction.get(), pageable, countFunction.get());
    }
    
    /**
     * Execute search returning domain entities without DTO mapping.
     * Used by Domain methods to avoid coupling with Mapper layer.
     */
    private Page<Auction> executeSearchWithoutMapping(Supplier<List<Auction>> searchFunction, Supplier<Long> countFunction, Pageable pageable) {
        return pagingHelper.createEntityPage(searchFunction.get(), pageable, countFunction.get());
    }
}
