package com.myapp.server.bids.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UserBidSummaryItem(
    Long auctionId,
    String auctionTitle,
    BigDecimal currentPrice,
    BigDecimal yourMax,
    OffsetDateTime endDate,
    boolean leading,
    String status
) {
}
