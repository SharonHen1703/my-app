package com.myapp.server.bids.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Package-private helper for bid history retrieval and snapshot operations.
 */
class BidsJdbcReadHistory {

    private final JdbcTemplate jdbc;

    BidsJdbcReadHistory(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    List<BidsDao.HistoryRow> getBidHistory(long auctionId) {
        return jdbc.query("""
            SELECT s.id as snapshot_id, s.bid_id, s.displayed_bid, s.snapshot_time,
               s.actor_user_id, s.kind, s.bid_type
            FROM public.bid_history_snapshots s
            WHERE s.auction_id = ?
            ORDER BY s.snapshot_time ASC, s.id ASC
        """, (rs, rn) -> new BidsDao.HistoryRow(
            rs.getLong("snapshot_id"),
            rs.getLong("bid_id"),
            rs.getLong("actor_user_id"),
            rs.getBigDecimal("displayed_bid"),
            rs.getObject("snapshot_time", OffsetDateTime.class),
            rs.getString("kind"),
            rs.getString("bid_type")
        ), auctionId);
    }

    void insertBidSnapshot(long auctionId, long bidId, long actorUserId, BigDecimal displayedBid, String kind, String bidType, OffsetDateTime when) {
        jdbc.update("""
            INSERT INTO public.bid_history_snapshots(auction_id, bid_id, actor_user_id, displayed_bid, kind, bid_type, snapshot_time)
            VALUES (?, ?, ?, ?, ?::bid_history_kind, ?::bid_type, ?)
        """, auctionId, bidId, actorUserId, displayedBid, kind, bidType, when);
    }

    int countBidHistorySnapshots(long auctionId) {
        return jdbc.queryForObject("""
            SELECT COUNT(*) FROM public.bid_history_snapshots WHERE auction_id = ?
        """, Integer.class, auctionId);
    }
}
