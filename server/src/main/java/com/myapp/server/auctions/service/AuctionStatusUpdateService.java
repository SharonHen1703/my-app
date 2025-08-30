package com.myapp.server.auctions.service;

import com.myapp.server.auctions.entity.Auction;
import com.myapp.server.auctions.entity.enums.AuctionStatus;
import com.myapp.server.auctions.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuctionStatusUpdateService {

    @Autowired
    private AuctionRepository auctionRepository;

    /**
     * עדכון סטטוס מכרזים - רץ כל דקה
     */
    @Scheduled(fixedRate = 60000) // כל דקה
    @Transactional
    public void updateExpiredAuctions() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // מצא מכרזים פעילים שהסתיימו
        List<Auction> expiredAuctions = auctionRepository.findByStatusAndEndDateBefore(AuctionStatus.ACTIVE, now);
        
        for (Auction auction : expiredAuctions) {
            if (auction.getBidsCount() > 0) {
                // יש הצעות - הסתיים בהצלחה
                auction.setStatus(AuctionStatus.SOLD);
            } else {
                // אין הצעות - הסתיים ללא זכייה
                auction.setStatus(AuctionStatus.UNSOLD);
            }
            auction.setUpdatedAt(now);
            auctionRepository.save(auction);
        }
        
        if (!expiredAuctions.isEmpty()) {
            System.out.println("Updated " + expiredAuctions.size() + " expired auctions");
        }
    }

    /**
     * עדכון ידני של סטטוס מכרז ספציפי
     */
    @Transactional
    public void updateAuctionStatus(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId).orElse(null);
        if (auction == null || auction.getStatus() != AuctionStatus.ACTIVE) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (auction.getEndDate().isBefore(now)) {
            if (auction.getBidsCount() > 0) {
                auction.setStatus(AuctionStatus.SOLD);
            } else {
                auction.setStatus(AuctionStatus.UNSOLD);
            }
            auction.setUpdatedAt(now);
            auctionRepository.save(auction);
        }
    }
}
