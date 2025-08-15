package com.myapp.server.bids.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PlaceBidResponse(
        Long auctionId,
        Long highestUserId,
        BigDecimal highestMaxBid,
        BigDecimal currentPrice,
        int bidsCount,
        BigDecimal minNextBid,     // המחיר המינימלי להצעה הבאה
        boolean youAreLeading,     // האם המציע עכשיו מוביל
        OffsetDateTime endsAt
) {}
