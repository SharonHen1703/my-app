package com.myapp.server.bids.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Package-private helper for user summaries and aggregations.
 */
class BidsJdbcUserSummaries {

    private final JdbcTemplate jdbc;

    BidsJdbcUserSummaries(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    List<BidsDao.UserBidSummaryRow> getUserBidsSummary(Long userId) {
        String sql = """
            SELECT DISTINCT
                a.id as auction_id,
                a.title as auction_title,
                a.current_bid_amount as current_price,
                MAX(b.max_bid) as your_max,
                a.end_date,
                CASE WHEN a.highest_user_id = ? THEN true ELSE false END as leading,
                CASE WHEN a.end_date > NOW() THEN 'active' ELSE 'ended' END as status
            FROM public.bids b
            JOIN public.auctions a ON b.auction_id = a.id
            WHERE b.bidder_id = ?
            GROUP BY a.id, a.title, a.current_bid_amount, a.end_date, a.highest_user_id
            ORDER BY a.end_date DESC
        """;
        
        return jdbc.query(sql, (rs, rowNum) -> new BidsDao.UserBidSummaryRow(
            rs.getLong("auction_id"),
            rs.getString("auction_title"),
            rs.getBigDecimal("current_price"),
            rs.getBigDecimal("your_max"),
            rs.getObject("end_date", OffsetDateTime.class),
            rs.getBoolean("leading"),
            rs.getString("status")
        ), userId, userId);
    }

    boolean isLeader(long auctionId, long userId) {
        Long highest = jdbc.queryForObject(
                "SELECT highest_user_id FROM public.auctions WHERE id = ?",
                Long.class,
                auctionId
        );
        return highest != null && highest.equals(userId);
    }

    List<BidsDao.TopBidRow> getTopBids(long auctionId, int limit) {
        return jdbc.query("""
            SELECT bidder_id AS user_id, max_bid, created_at, id
            FROM public.bids
            WHERE auction_id = ?
            ORDER BY max_bid DESC, created_at ASC, id ASC
            LIMIT ?
        """, (rs, rn) -> new BidsDao.TopBidRow(
                rs.getLong("user_id"),
                rs.getBigDecimal("max_bid"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getLong("id")
        ), auctionId, limit);
    }
}
