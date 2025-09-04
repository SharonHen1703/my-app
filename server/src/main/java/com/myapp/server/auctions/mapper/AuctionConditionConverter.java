package com.myapp.server.auctions.mapper;

import com.myapp.server.auctions.entity.enums.AuctionCondition;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuctionConditionConverter implements AttributeConverter<AuctionCondition, String> {

    @Override
    public String convertToDatabaseColumn(AuctionCondition attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public AuctionCondition convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return AuctionCondition.fromValue(dbData);
    }
}
