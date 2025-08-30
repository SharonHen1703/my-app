package com.myapp.server.auctions.dto;

public record CreateAuctionResponse(
    Long id,
    String title,
    String message
) {}
