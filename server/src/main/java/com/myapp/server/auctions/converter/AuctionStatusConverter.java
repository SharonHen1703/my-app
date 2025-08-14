package com.myapp.server.auctions.converter;

import com.myapp.server.auctions.entity.enums.AuctionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuctionStatusConverter implements AttributeConverter<AuctionStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(AuctionStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }
    
    @Override
    public AuctionStatus convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        
        for (AuctionStatus status : AuctionStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown status value: " + value);
    }
}
