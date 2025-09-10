package com.myapp.server.bids.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class BidsDao {

    // Package-private helpers - not Spring beans to avoid circular dependencies
    private final BidsJdbcReadHistory historyOps;
    private final BidsJdbcUserSummaries userSummariesOps;
    private final BidsJdbcWriteOps writeOps;
    private final AuctionLockingJdbcOps lockingOps;
    
    public BidsDao(JdbcTemplate jdbc, NamedParameterJdbcTemplate namedJdbc) {
        
        // Initialize helpers
        BidsRowMappers mappers = new BidsRowMappers();
        this.historyOps = new BidsJdbcReadHistory(jdbc);
        this.userSummariesOps = new BidsJdbcUserSummaries(jdbc);
        this.writeOps = new BidsJdbcWriteOps(jdbc, namedJdbc);
        this.lockingOps = new AuctionLockingJdbcOps(jdbc, mappers);
    }

    // === LOCKING OPERATIONS ===
    
    public AuctionRow lockAuctionForUpdate(long auctionId) { return lockingOps.lockAuctionForUpdate(auctionId); }
    public AuctionRow getAuction(long auctionId) { return lockingOps.getAuction(auctionId); }

    // === WRITE OPERATIONS ===
    
    public Long insertBidReturningId(long auctionId, long bidderId, BigDecimal maxBid) { return writeOps.insertBidReturningId(auctionId, bidderId, maxBid); }
    public BigDecimal getUserPrevMax(long auctionId, long bidderId) { return writeOps.getUserPrevMax(auctionId, bidderId); }
    public int updateUserMax(long auctionId, long bidderId, BigDecimal maxBid) { return writeOps.updateUserMax(auctionId, bidderId, maxBid); }
    public Long getExistingBidId(long auctionId, long bidderId) { return writeOps.getExistingBidId(auctionId, bidderId); }

    public void updateAuctionAfterBid(long auctionId, long highestUserId, BigDecimal highestMaxBid, BigDecimal current) { writeOps.updateAuctionAfterBid(auctionId, highestUserId, highestMaxBid, current); }

    // === HISTORY OPERATIONS ===
    
    public void insertBidSnapshot(long auctionId, long bidId, long actorUserId, BigDecimal displayedBid, String kind, String bidType, OffsetDateTime when) { historyOps.insertBidSnapshot(auctionId, bidId, actorUserId, displayedBid, kind, bidType, when); }
    public List<HistoryRow> getBidHistory(long auctionId) { return historyOps.getBidHistory(auctionId); }
    public int countBidHistorySnapshots(long auctionId) { return historyOps.countBidHistorySnapshots(auctionId); }

    // === USER SUMMARIES ===
    
    public List<TopBidRow> getTopBids(long auctionId, int limit) { return userSummariesOps.getTopBids(auctionId, limit); }
    public List<UserBidSummaryRow> getUserBidsSummary(Long userId) { return userSummariesOps.getUserBidsSummary(userId); }

    // === TYPE ALIASES FOR BACKWARD COMPATIBILITY ===
    // Re-export record types so existing services don't break
    public record HistoryRow(
            long snapshotId,
            long bidId,
            long bidderId,
            BigDecimal displayedBid,
            OffsetDateTime snapshotTime,
            String kind,
            String bidType
    ) {}

    public record TopBidRow(
            long userId,
            BigDecimal maxBid,
            OffsetDateTime createdAt,
            long id
    ) {}

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
            BigDecimal highestMaxBid
    ) {}

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
