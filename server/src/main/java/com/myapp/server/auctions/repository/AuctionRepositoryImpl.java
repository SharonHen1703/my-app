package com.myapp.server.auctions.repository;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.entity.enums.AuctionCondition;
import com.myapp.server.auctions.repository.impl.ActiveAuctionsQueries;
import com.myapp.server.auctions.repository.impl.AuctionDetailQueries;
import com.myapp.server.auctions.repository.impl.UserAuctionsQueries;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of custom auction repository operations.
 * Delegates to themed query helpers to maintain focused responsibilities.
 * Reduced from 350 lines to lean delegation pattern.
 */
@Repository
@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepositoryCustom {

    private final ActiveAuctionsQueries activeAuctionsQueries;
    private final AuctionDetailQueries auctionDetailQueries;
    private final UserAuctionsQueries userAuctionsQueries;

    @Override
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable) {
        return activeAuctionsQueries.findActiveAuctions(pageable);
    }

    @Override
    public Page<AuctionListItem> findActiveAuctions(Pageable pageable, String category) {
        return activeAuctionsQueries.findActiveAuctions(pageable, category);
    }

    @Override
    public Page<AuctionListItem> findActiveAuctions(
        Pageable pageable,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText
    ) {
        return activeAuctionsQueries.findActiveAuctions(
            pageable, category, minPrice, maxPrice, conditions, searchText
        );
    }

    @Override
    public Page<AuctionListItem> findActiveAuctions(
        Pageable pageable,
        String category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        List<AuctionCondition> conditions,
        String searchText,
        Long excludeSellerId
    ) {
        return activeAuctionsQueries.findActiveAuctions(
            pageable, category, minPrice, maxPrice, conditions, searchText, excludeSellerId
        );
    }

    @Override
    public long countActiveAuctions() {
        return activeAuctionsQueries.countActiveAuctions();
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
}
