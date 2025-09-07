package com.myapp.server.bids.service;

import com.myapp.server.bids.repository.BidsDao;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps between DAO records and DTOs.
 * Handles all Entity ↔ DTO conversions for bid operations.
 */
@Component
public class BidsMapper {
    
    /**
     * Converts DAO bid history rows to DTOs.
     */
    public List<BidsService.BidHistoryItem> toBidHistoryItems(List<BidsDao.HistoryRow> rows) {
        return rows.stream()
                .map(row -> new BidsService.BidHistoryItem(
                        row.snapshotId(),
                        row.bidId(),
                        row.bidderId(),
                        row.displayedBid(),
                        row.snapshotTime(),
                        row.kind(),
                        row.bidType()
                ))
                .toList();
    }
    
    /**
     * Converts DAO user bid summary rows to DTOs.
     */
    public List<com.myapp.server.bids.dto.UserBidSummaryItem> toUserBidSummaryItems(
            List<BidsDao.UserBidSummaryRow> rows) {
        return rows.stream()
                .map(row -> new com.myapp.server.bids.dto.UserBidSummaryItem(
                        row.auctionId(),
                        row.auctionTitle(),
                        row.currentPrice(),
                        row.yourMax(),
                        row.endDate(),
                        row.leading(),
                        row.status()
                ))
                .toList();
    }
}
