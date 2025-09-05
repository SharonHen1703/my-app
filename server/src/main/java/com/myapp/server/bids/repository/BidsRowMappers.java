package com.myapp.server.bids.repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.OffsetDateTime;

/**
 * Package-private helper for shared ResultSet→DTO mappers.
 */
class BidsRowMappers {

    BidsDao.AuctionRow mapAuctionRow(ResultSet rs) throws java.sql.SQLException {
        Long highestUser = (Long) rs.getObject("highest_user_id") == null ? null : rs.getLong("highest_user_id");
        var start = getOffsetDateTime(rs, "start_date");
        var end = getOffsetDateTime(rs, "end_date");
        return new BidsDao.AuctionRow(
                rs.getLong("id"),
                rs.getLong("seller_id"),
                rs.getString("status"),
                start,
                end,
                rs.getBigDecimal("min_price"),
                rs.getBigDecimal("bid_increment"),
                rs.getBigDecimal("current_bid_amount"),
                rs.getInt("bids_count"),
                highestUser,
                rs.getBigDecimal("highest_max_bid"),
                rs.getBigDecimal("buy_now_price")
        );
    }

    private OffsetDateTime getOffsetDateTime(ResultSet rs, String col) throws java.sql.SQLException {
        Object obj = rs.getObject(col);
        if (obj == null) return null;
        if (obj instanceof OffsetDateTime odt) return odt;
        if (obj instanceof java.time.LocalDateTime ldt) {
            return ldt.atOffset(java.time.ZoneOffset.UTC);
        }
        if (obj instanceof java.sql.Timestamp ts) {
            return ts.toInstant().atOffset(java.time.ZoneOffset.UTC);
        }
        try {
            return rs.getObject(col, OffsetDateTime.class);
        } catch (Exception e) {
            // Fallback: toString parse as LocalDateTime
            var ldt = java.time.LocalDateTime.parse(obj.toString().replace(' ', 'T'));
            return ldt.atOffset(java.time.ZoneOffset.UTC);
        }
    }
}
