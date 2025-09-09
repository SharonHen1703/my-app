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
     * Lock the auction row for update (FOR UPDATE).
     */
    BidsDao.AuctionRow lockAuctionForUpdate(long auctionId) {
        return jdbc.queryForObject("""
            SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                   current_bid_amount, bids_count, highest_user_id, highest_max_bid
            FROM public.auctions
            WHERE id = ?
            FOR UPDATE
        """, (rs, rn) -> mappers.mapAuctionRow(rs), auctionId);
    }

    /** Lightweight fetch of public auction fields after update */
    BidsDao.AuctionRow getAuction(long auctionId) {
        return jdbc.query("""
            SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                   current_bid_amount, bids_count, highest_user_id, highest_max_bid
            FROM public.auctions
            WHERE id = ?
        """, (rs, rn) -> mappers.mapAuctionRow(rs), auctionId).stream().findFirst().orElse(null);
    }
}
