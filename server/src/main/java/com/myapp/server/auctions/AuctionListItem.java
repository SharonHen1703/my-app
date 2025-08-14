package com.myapp.server.auctions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuctionListItem(
        Long id,
        String title,
        String description,
        BigDecimal minPrice,
        BigDecimal bidIncrement,
        BigDecimal currentBidAmount,
        Integer bidsCount,
        BigDecimal minBidToPlace,
        OffsetDateTime endDate,
        String firstImageUrl
) {}
