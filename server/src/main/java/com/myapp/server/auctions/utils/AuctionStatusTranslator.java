package com.myapp.server.auctions.utils;

import com.myapp.server.auctions.entity.enums.AuctionStatus;

public class AuctionStatusTranslator {

    public static String translateToHebrew(AuctionStatus status) {
        if (status == null) {
            return "לא ידוע";
        }
        
        switch (status) {
            case ACTIVE:
                return "פעיל";
            case SOLD:
                return "הסתיים בהצלחה";
            case UNSOLD:
                return "הסתיים ללא זכייה";
            default:
                return "לא ידוע";
        }
    }

    public static String translateToEnglish(AuctionStatus status) {
        if (status == null) {
            return "unknown";
        }
        
        return status.getValue();
    }
}
