package com.myapp.server.bids.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Package-private helper for bid inserts, updates, and winner calculations.
 */
class BidsJdbcWriteOps {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    BidsJdbcWriteOps(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbc = jdbc;
        this.namedJdbc = namedJdbc;
    }

    Long insertBidReturningId(long auctionId, long bidderId, BigDecimal maxBid) {
        // Avoid relying on a missing UNIQUE constraint. Try update first; if no row, insert and return id.
        int updated = updateUserMax(auctionId, bidderId, maxBid);
        if (updated > 0) {
            return getExistingBidId(auctionId, bidderId);
        }
        return jdbc.queryForObject("""
            INSERT INTO public.bids(auction_id, bidder_id, max_bid)
            VALUES (?, ?, ?)
            RETURNING id
        """, Long.class, auctionId, bidderId, maxBid);
    }

    BigDecimal getUserPrevMax(long auctionId, long bidderId) {
        return jdbc.query("""
            SELECT max_bid AS prev
            FROM public.bids
            WHERE auction_id = ? AND bidder_id = ?
            """, rs -> rs.next() ? rs.getBigDecimal("prev") : null, auctionId, bidderId);
    }

    int updateUserMax(long auctionId, long bidderId, BigDecimal maxBid) {
        return jdbc.update("""
            UPDATE public.bids
            SET max_bid = ?
            WHERE auction_id = ? AND bidder_id = ?
        """, maxBid, auctionId, bidderId);
    }

    Long getExistingBidId(long auctionId, long bidderId) {
        return jdbc.query("""
            SELECT id FROM public.bids
            WHERE auction_id = ? AND bidder_id = ?
            ORDER BY id DESC
            LIMIT 1
            """, rs -> rs.next() ? rs.getLong(1) : null, auctionId, bidderId);
    }

    void updateAuctionAfterBid(long auctionId, long highestUserId, BigDecimal highestMaxBid, BigDecimal current) {
        namedJdbc.update("""
            UPDATE public.auctions
            SET current_bid_amount = :cur,
                highest_user_id = :uid,
                highest_max_bid = :maxBid,
                bids_count = (SELECT COUNT(*) FROM public.bid_history_snapshots WHERE auction_id = :id),
                updated_at = now()
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("uid", highestUserId)
                .addValue("maxBid", highestMaxBid)
                .addValue("cur", current));
    }
}
