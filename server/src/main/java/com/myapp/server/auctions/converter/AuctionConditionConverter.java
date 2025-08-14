package com.myapp.server.auctions.converter;

import com.myapp.server.auctions.entity.enums.AuctionCondition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuctionConditionConverter implements AttributeConverter<AuctionCondition, String> {
    
    @Override
    public String convertToDatabaseColumn(AuctionCondition condition) {
        if (condition == null) {
            return null;
        }
        return condition.getValue();
    }
    
    @Override
    public AuctionCondition convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        
        for (AuctionCondition condition : AuctionCondition.values()) {
            if (condition.getValue().equals(value)) {
                return condition;
            }
        }
        
        throw new IllegalArgumentException("Unknown condition value: " + value);
    }
}
