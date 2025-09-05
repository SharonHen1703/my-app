package com.myapp.server.bids.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Data transfer objects for bid operations
 */
public class BidsRecords {

    public record HistoryRow(
            long snapshotId,
            long bidId,
            long bidderId,
            BigDecimal displayedBid,
            OffsetDateTime snapshotTime,
            String kind
    ) {}

    public record TopBidRow(
            long userId,
            BigDecimal maxBid,
            OffsetDateTime createdAt,
            long id
    ) {}

    // רשומת עזר לסטטוס מלא של מכרז
    public record AuctionRow(
            long id,
            long sellerId,
            String status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            BigDecimal minPrice,
            BigDecimal bidIncrement,
            BigDecimal currentBid,
            int bidsCount,
            Long highestUserId,
            BigDecimal highestMaxBid,
            BigDecimal buyNowPrice
    ) {}

    public record UserBidSummaryRow(
            Long auctionId,
            String auctionTitle,
            BigDecimal currentPrice,
            BigDecimal yourMax,
            OffsetDateTime endDate,
            boolean leading,
            String status
    ) {}
}
