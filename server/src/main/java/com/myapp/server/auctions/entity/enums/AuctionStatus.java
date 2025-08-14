package com.myapp.server.auctions.entity.enums;

public enum AuctionStatus {
    ACTIVE("active"),
    SOLD("sold"),
    UNSOLD("unsold");
    
    private final String value;
    
    AuctionStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
