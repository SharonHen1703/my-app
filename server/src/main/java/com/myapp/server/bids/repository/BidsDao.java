package com.myapp.server.bids.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BidsDao {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    /**
     * Snapshot without locking – used to decide 409 vs 422 before we acquire row lock.
     */
    public AuctionRow getAuctionSnapshot(long auctionId) {
        return jdbc.query("""
                SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                       current_bid_amount, bids_count, highest_user_id, highest_max_bid, buy_now_price
                FROM public.auctions
                WHERE id = ?
            """, (rs, rn) -> mapAuctionRow(rs), auctionId).stream().findFirst().orElse(null);
    }

    /**
     * Lock the auction row for update (FOR UPDATE).
     */
    public AuctionRow lockAuctionForUpdate(long auctionId) {
        return jdbc.queryForObject("""
            SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                   current_bid_amount, bids_count, highest_user_id, highest_max_bid, buy_now_price
            FROM public.auctions
            WHERE id = ?
            FOR UPDATE
        """, (rs, rn) -> mapAuctionRow(rs), auctionId);
    }

    /**
     * Check if a user exists by id.
     */
    public boolean userExists(long userId) {
    Boolean exists = jdbc.queryForObject(
        "SELECT EXISTS (SELECT 1 FROM public.users WHERE id = ?)",
        Boolean.class,
        userId
    );
    return exists != null && exists;
    }

    public void insertBid(long auctionId, long bidderId, BigDecimal maxBid) {
        jdbc.update("""
            INSERT INTO public.bids(auction_id, bidder_id, max_bid)
            VALUES (?, ?, ?)
        """, auctionId, bidderId, maxBid);
    }

    public Long insertBidReturningId(long auctionId, long bidderId, BigDecimal maxBid) {
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

    public BigDecimal getUserPrevMax(long auctionId, long bidderId) {
    return jdbc.query("""
        SELECT max_bid AS prev
        FROM public.bids
        WHERE auction_id = ? AND bidder_id = ?
        """, rs -> rs.next() ? rs.getBigDecimal("prev") : null, auctionId, bidderId);
    }

    /* Update helpers for the four core cases + buy-now */
    public void updateBuyNow(long auctionId, long userId, BigDecimal maxBid, BigDecimal buyNow, OffsetDateTime now, int inc) {
    namedJdbc.update("""
            UPDATE public.auctions
            SET status = 'sold',
                current_bid_amount = :buy,
                highest_user_id = :uid,
                highest_max_bid = GREATEST(COALESCE(highest_max_bid, 0), :maxBid),
                bids_count = bids_count + :inc,
                updated_at = now(),
                end_date = :now
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("uid", userId)
                .addValue("maxBid", maxBid)
                .addValue("buy", buyNow)
                .addValue("now", now)
                .addValue("inc", inc));
    }

    /**
     * Finalize a buy-now by recomputing bids_count from snapshots to reflect all displayed events.
     */
    public void updateBuyNowFinal(long auctionId, long userId, BigDecimal maxBid, BigDecimal buyNow, OffsetDateTime now) {
        namedJdbc.update("""
            UPDATE public.auctions
            SET status = 'sold',
                current_bid_amount = :buy,
                highest_user_id = :uid,
                highest_max_bid = GREATEST(COALESCE(highest_max_bid, 0), :maxBid),
                bids_count = (SELECT COUNT(*) FROM public.bid_history_snapshots WHERE auction_id = :id),
                updated_at = now(),
                end_date = :now
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("uid", userId)
                .addValue("maxBid", maxBid)
                .addValue("buy", buyNow)
                .addValue("now", now));
    }

    public void updateFirstBid(long auctionId, long userId, BigDecimal maxBid) {
        namedJdbc.update("""
            UPDATE public.auctions
            SET highest_user_id = :uid,
                highest_max_bid = :maxBid,
                current_bid_amount = min_price,
                bids_count = bids_count + 1,
                updated_at = now()
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("uid", userId)
                .addValue("maxBid", maxBid));
    }

    public void updateIncreaseOwnCeiling(long auctionId, BigDecimal maxBid) {
        namedJdbc.update("""
            UPDATE public.auctions
            SET highest_max_bid = :maxBid,
                updated_at = now()
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("maxBid", maxBid));
    }

    public void updateStayHighest(long auctionId, BigDecimal newCurrent, int inc) {
        namedJdbc.update("""
            UPDATE public.auctions
            SET current_bid_amount = :cur,
                bids_count = bids_count + :inc,
                updated_at = now()
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("cur", newCurrent)
                .addValue("inc", inc));
    }

    public void updateNewHighest(long auctionId, long userId, BigDecimal maxBid, BigDecimal newCurrent, int inc) {
        namedJdbc.update("""
            UPDATE public.auctions
            SET highest_user_id = :uid,
                highest_max_bid = :maxBid,
                current_bid_amount = :cur,
                bids_count = bids_count + :inc,
                updated_at = now()
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("uid", userId)
                .addValue("maxBid", maxBid)
                .addValue("cur", newCurrent)
                .addValue("inc", inc));
    }

    /**
     * Update auction fields after writing bid_history_snapshots. Recomputes bids_count from snapshots.
     */
    public void updateAuctionAfterBid(long auctionId, long highestUserId, BigDecimal highestMaxBid, BigDecimal current) {
        namedJdbc.update("""
            UPDATE public.auctions
            SET current_bid_amount = :cur,
                highest_user_id = :uid,
                highest_max_bid = :maxBid,
                bids_count = bids_count + 1,
                updated_at = now()
            WHERE id = :id
        """, new MapSqlParameterSource()
                .addValue("id", auctionId)
                .addValue("uid", highestUserId)
                .addValue("maxBid", highestMaxBid)
                .addValue("cur", current));
    }

    public int updateUserMax(long auctionId, long bidderId, BigDecimal maxBid) {
        return jdbc.update("""
            UPDATE public.bids
            SET max_bid = ?
            WHERE auction_id = ? AND bidder_id = ?
        """, maxBid, auctionId, bidderId);
    }

    public Long getExistingBidId(long auctionId, long bidderId) {
        return jdbc.query("""
            SELECT id FROM public.bids
            WHERE auction_id = ? AND bidder_id = ?
            ORDER BY id DESC
            LIMIT 1
            """, rs -> rs.next() ? rs.getLong(1) : null, auctionId, bidderId);
    }

    public void insertBidSnapshot(long auctionId, long bidId, long actorUserId, BigDecimal displayedBid, String kind, OffsetDateTime when) {
        jdbc.update("""
            INSERT INTO public.bid_history_snapshots(auction_id, bid_id, actor_user_id, displayed_bid, kind, snapshot_time)
            VALUES (?, ?, ?, ?, ?::bid_history_kind, ?)
        """, auctionId, bidId, actorUserId, displayedBid, kind, when);
    }

    public List<HistoryRow> getBidHistory(long auctionId) {
    return jdbc.query("""
        SELECT s.id as snapshot_id, s.bid_id, s.displayed_bid, s.snapshot_time,
           s.actor_user_id, s.kind
        FROM public.bid_history_snapshots s
        WHERE s.auction_id = ?
        ORDER BY s.snapshot_time ASC, s.id ASC
    """, (rs, rn) -> new HistoryRow(
        rs.getLong("snapshot_id"),
        rs.getLong("bid_id"),
        rs.getLong("actor_user_id"),
        rs.getBigDecimal("displayed_bid"),
        rs.getObject("snapshot_time", OffsetDateTime.class),
        rs.getString("kind")
    ), auctionId);
    }

    public record HistoryRow(
            long snapshotId,
            long bidId,
            long bidderId,
            BigDecimal displayedBid,
            OffsetDateTime snapshotTime,
            String kind
    ) {}

    /** Check if user is the current leader by comparing to auctions.highest_user_id */
    public boolean isLeader(long auctionId, long userId) {
        Long highest = jdbc.queryForObject(
                "SELECT highest_user_id FROM public.auctions WHERE id = ?",
                Long.class,
                auctionId
        );
        return highest != null && highest.equals(userId);
    }

    /**
     * Return top-N bids for an auction ordered by max_bid DESC, then created_at ASC, then id ASC.
     * Used to compute leader/runner-up and second-price public current.
     */
    public List<TopBidRow> getTopBids(long auctionId, int limit) {
        return jdbc.query("""
            SELECT bidder_id AS user_id, max_bid, created_at, id
            FROM public.bids
            WHERE auction_id = ?
            ORDER BY max_bid DESC, created_at ASC, id ASC
            LIMIT ?
        """, (rs, rn) -> new TopBidRow(
                rs.getLong("user_id"),
                rs.getBigDecimal("max_bid"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getLong("id")
        ), auctionId, limit);
    }

    public record TopBidRow(
            long userId,
            BigDecimal maxBid,
            OffsetDateTime createdAt,
            long id
    ) {}

    private AuctionRow mapAuctionRow(ResultSet rs) throws java.sql.SQLException {
        Long highestUser = (Long) rs.getObject("highest_user_id") == null ? null : rs.getLong("highest_user_id");
        var start = getOffsetDateTime(rs, "start_date");
        var end = getOffsetDateTime(rs, "end_date");
        return new AuctionRow(
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

    // רשומת עזר לסטטוס מלא של מכרז
    public record AuctionRow(
            long id,
            long sellerId,
            String status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            BigDecimal minPrice,
            BigDecimal bidIncrement,
            BigDecimal currentBid,
            int bidsCount,
            Long highestUserId,
            BigDecimal highestMaxBid,
            BigDecimal buyNowPrice
    ) {}

    /** Lightweight fetch of public auction fields after update */
    public AuctionRow getAuction(long auctionId) {
        return jdbc.query("""
            SELECT id, seller_id, status, start_date, end_date, min_price, bid_increment,
                   current_bid_amount, bids_count, highest_user_id, highest_max_bid, buy_now_price
            FROM public.auctions
            WHERE id = ?
        """, (rs, rn) -> mapAuctionRow(rs), auctionId).stream().findFirst().orElse(null);
    }

    /**
     * Get all auctions that a user has bid on with their status
     */
    public List<UserBidSummaryRow> getUserBidsSummary(Long userId) {
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
        
        return jdbc.query(sql, (rs, rowNum) -> new UserBidSummaryRow(
            rs.getLong("auction_id"),
            rs.getString("auction_title"),
            rs.getBigDecimal("current_price"),
            rs.getBigDecimal("your_max"),
            rs.getObject("end_date", OffsetDateTime.class),
            rs.getBoolean("leading"),
            rs.getString("status")
        ), userId, userId);
    }

    public record UserBidSummaryRow(
            Long auctionId,
            String auctionTitle,
            BigDecimal currentPrice,
            BigDecimal yourMax,
            OffsetDateTime endDate,
            boolean leading,
            String status
    ) {}

}
