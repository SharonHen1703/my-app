package com.myapp.server.auctions.mapper;

import com.myapp.server.auctions.entity.enums.AuctionStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AuctionStatusConverter implements AttributeConverter<AuctionStatus, String> {

    @Override
    public String convertToDatabaseColumn(AuctionStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AuctionStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return AuctionStatus.valueOf(dbData.toUpperCase());
    }
}
