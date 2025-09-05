package com.myapp.server.bids.repository;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Package-private helper for auction locking and existence checks.
 */
class AuctionLockingJdbcOps {

    private final JdbcTemplate jdbc;
    private final BidsRowMappers mappers;

    AuctionLockingJdbcOps(JdbcTemplate jdbc, BidsRowMappers mappers) {
        this.jdbc = jdbc;
        this.mappers = mappers;
    }

    /**
     * Snapshot without locking – used to decide 409 vs 422 before we acquire row lock.
     */
    BidsDao.AuctionRow getAuctionSnapshot(long auctionId) {
        return jdbc.query("""
                SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                       current_bid_amount, bids_count, highest_user_id, highest_max_bid, buy_now_price
                FROM public.auctions
                WHERE id = ?
            """, (rs, rn) -> mappers.mapAuctionRow(rs), auctionId).stream().findFirst().orElse(null);
    }

    /**
     * Lock the auction row for update (FOR UPDATE).
     */
    BidsDao.AuctionRow lockAuctionForUpdate(long auctionId) {
        return jdbc.queryForObject("""
            SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                   current_bid_amount, bids_count, highest_user_id, highest_max_bid, buy_now_price
            FROM public.auctions
            WHERE id = ?
            FOR UPDATE
        """, (rs, rn) -> mappers.mapAuctionRow(rs), auctionId);
    }

    /** Lightweight fetch of public auction fields after update */
    BidsDao.AuctionRow getAuction(long auctionId) {
        return jdbc.query("""
            SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                   current_bid_amount, bids_count, highest_user_id, highest_max_bid, buy_now_price
            FROM public.auctions
            WHERE id = ?
        """, (rs, rn) -> mappers.mapAuctionRow(rs), auctionId).stream().findFirst().orElse(null);
    }

    /**
     * Check if a user exists by id.
     */
    boolean userExists(long userId) {
        Boolean exists = jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM public.users WHERE id = ?)",
            Boolean.class,
            userId
        );
        return exists != null && exists;
    }
}
