package com.myapp.server.auctions;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class AuctionsDao {
    private final JdbcTemplate jdbc;

    public AuctionsDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<AuctionListItem> findActive(int limit, int offset) {
        String sql = """
            SELECT
              id,
              title,
              description,
              min_price                AS minPrice,
              bid_increment            AS bidIncrement,
              current_bid_amount       AS currentBidAmount,
              bids_count               AS bidsCount,
              CASE
                WHEN bids_count = 0 THEN min_price
                ELSE current_bid_amount + bid_increment
              END                      AS minBidToPlace,
              end_date                 AS endDate,
              image_urls->>0           AS firstImageUrl
            FROM public.auctions
            WHERE status = 'active'
            ORDER BY end_date
            LIMIT ? OFFSET ?
            """;
        return jdbc.query(sql, (rs, rowNum) -> new AuctionListItem(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getBigDecimal("minPrice"),
                rs.getBigDecimal("bidIncrement"),
                rs.getBigDecimal("currentBidAmount"),
                (Integer) rs.getObject("bidsCount"),
                rs.getBigDecimal("minBidToPlace"),
                rs.getObject("endDate", OffsetDateTime.class),
                rs.getString("firstImageUrl")
        ), limit, offset);
    }

    public int countActive() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM public.auctions WHERE status='active'", Integer.class);
        return n == null ? 0 : n;
    }
}
