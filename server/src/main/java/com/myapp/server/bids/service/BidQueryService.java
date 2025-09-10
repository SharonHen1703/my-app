package com.myapp.server.bids.service;

import com.myapp.server.bids.repository.BidsDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only service for bid queries and history.
 * Handles all non-mutating bid operations.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidQueryService {
    
    private final BidsDao dao;
    private final BidsMapper bidsMapper;
    
    /**
     * Gets the public bid history for an auction.
     */
    public List<BidsService.BidHistoryItem> getHistory(long auctionId) {
        var rows = dao.getBidHistory(auctionId);
        return bidsMapper.toBidHistoryItems(rows);
    }
    
    /**
     * Gets summary of user's bids across all auctions.
     */
    public List<com.myapp.server.bids.dto.UserBidSummaryItem> getUserBidsSummary(Long userId) {
        List<BidsDao.UserBidSummaryRow> rows = dao.getUserBidsSummary(userId);
        return bidsMapper.toUserBidSummaryItems(rows);
    }
}
