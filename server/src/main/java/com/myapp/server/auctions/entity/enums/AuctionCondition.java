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

    /**
     * Parse storage value (e.g., "like_new") into enum. Throws IllegalArgumentException if unknown.
     */
    public static AuctionCondition fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Condition value cannot be null");
        }
        for (AuctionCondition c : AuctionCondition.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown condition value: " + value);
    }
}
