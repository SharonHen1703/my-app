package com.myapp.server.bids.dto;

import java.math.BigDecimal;

public record NextBidInfo(
        BigDecimal userPrevMax,
        BigDecimal requiredMin
) {}
