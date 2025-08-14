package com.myapp.server.auctions.entity.enums;

public enum AuctionCondition {
    NEW("new"),
    LIKE_NEW("like_new"),
    USED("used"),
    REFURBISHED("refurbished"),
    DAMAGED_OR_PARTS("damaged_or_parts");
    
    private final String value;
    
    AuctionCondition(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
