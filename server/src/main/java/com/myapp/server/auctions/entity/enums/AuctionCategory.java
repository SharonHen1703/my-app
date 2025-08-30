package com.myapp.server.auctions.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * מאגר קטגוריות קבוע למכרזים
 */
public enum AuctionCategory {
    ELECTRONICS("electronics", "אלקטרוניקה"),
    APPLIANCES("appliances", "מכשירי חשמל"), 
    FURNITURE("furniture", "רהיטים"),
    SPORTS("sports", "ספורט"),
    VEHICLES("vehicles", "כלי רכב"),
    MUSIC("music", "מוזיקה"),
    GAMING("gaming", "גיימינג"),
    PHOTOGRAPHY("photography", "צילום"),
    CRAFTS("crafts", "יצירה"),
    KITCHEN("kitchen", "מטבח"),
    CLOTHING("clothing", "ביגוד"),
    BOOKS("books", "ספרים"),
    TOYS("toys", "צעצועים"),
    HOME_GARDEN("home_garden", "בית וגן"),
    BEAUTY("beauty", "יופי"),
    JEWELRY("jewelry", "תכשיטים"),
    COLLECTIBLES("collectibles", "פריטי אספנות"),
    ART("art", "אמנות");

    private final String code;
    private final String displayName;

    AuctionCategory(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * מוצא קטגוריה לפי קוד
     */
    public static AuctionCategory fromCode(String code) {
        for (AuctionCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category code: " + code);
    }

    /**
     * בודק אם קוד קטגוריה קיים
     */
    public static boolean isValidCode(String code) {
        for (AuctionCategory category : values()) {
            if (category.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return code;
    }
}
