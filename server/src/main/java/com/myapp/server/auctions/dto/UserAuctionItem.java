package com.myapp.server.auctions.dto;

import java.math.BigDecimal;

public record UserAuctionItem(
    Long id,
    String title,
    BigDecimal currentPrice,
    String auctionStatus,
    Integer bidsCount,
    String endDate
) {
}
