package com.myapp.server.auctions.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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
        List<String> imageUrls
) {}
