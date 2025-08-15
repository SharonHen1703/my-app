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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BidsDao {

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;

    public AuctionRow lockAuctionForUpdate(long auctionId) {
        return jdbc.queryForObject("""
            SELECT id, seller_id, status, end_date, min_price, bid_increment,
                   current_bid_amount, bids_count
            FROM public.auctions
            WHERE id = ?
            FOR UPDATE
        """, (rs, rn) -> mapAuctionRow(rs), auctionId);
    }

    public void insertBid(long auctionId, long bidderId, BigDecimal maxBid) {
        jdbc.update("""
            INSERT INTO public.bids(auction_id, bidder_id, max_bid)
            VALUES (?, ?, ?)
        """, auctionId, bidderId, maxBid);
    }

    public List<TopBid> top2PerUser(long auctionId) {
        return namedJdbc.query("""
            SELECT bidder_id,
                   MAX(max_bid) AS max_bid,
                   MIN(created_at) AS first_time
            FROM public.bids
            WHERE auction_id = :aid
            GROUP BY bidder_id
            ORDER BY max_bid DESC, first_time ASC
            LIMIT 2
        """, new MapSqlParameterSource("aid", auctionId), (rs, rn) -> mapTopBid(rs));
    }

    public int totalBids(long auctionId) {
        Integer c = jdbc.queryForObject("""
            SELECT COUNT(*) FROM public.bids WHERE auction_id = ?
        """, Integer.class, auctionId);
        return (c == null ? 0 : c);
    }

    public void updateAuctionAfterBid(long auctionId,
                                      Long highestUserId,
                                      BigDecimal highestMax,
                                      BigDecimal currentPrice,
                                      int bidsCount) {
        jdbc.update("""
            UPDATE public.auctions
            SET highest_user_id = ?, highest_max_bid = ?, current_bid_amount = ?,
                bids_count = ?, updated_at = now()
            WHERE id = ?
        """, highestUserId, highestMax, currentPrice, bidsCount, auctionId);
    }

    private AuctionRow mapAuctionRow(ResultSet rs) throws java.sql.SQLException {
        return new AuctionRow(
                rs.getLong("id"),
                rs.getLong("seller_id"),
                rs.getString("status"),
                rs.getObject("end_date", OffsetDateTime.class),
                rs.getBigDecimal("min_price"),
                rs.getBigDecimal("bid_increment"),
                rs.getBigDecimal("current_bid_amount"),
                rs.getInt("bids_count")
        );
    }

    private TopBid mapTopBid(ResultSet rs) throws java.sql.SQLException {
        return new TopBid(
                rs.getLong("bidder_id"),
                rs.getBigDecimal("max_bid")
        );
    }

    // רשומות עזר פנימיות
    public record AuctionRow(
            long id,
            long sellerId,
            String status,
            OffsetDateTime endDate,
            BigDecimal minPrice,
            BigDecimal bidIncrement,
            BigDecimal currentBid,
            int bidsCount
    ) {}

    public record TopBid(long bidderId, BigDecimal maxBid) {}
}
