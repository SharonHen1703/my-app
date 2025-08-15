package com.myapp.server.bids.service;

import com.myapp.server.bids.repository.BidsDao;
import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.dto.PlaceBidResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class BidsService {

    private final BidsDao dao;

    @Transactional
    public PlaceBidResponse placeBid(long auctionId, PlaceBidRequest req) {
        var a = dao.lockAuctionForUpdate(auctionId); // FOR UPDATE

        // בדיקות בסיסיות
        if (!"active".equalsIgnoreCase(a.status())) {
            throw new ResponseStatusException(CONFLICT, "auction is not active");
        }
        if (a.endDate().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(CONFLICT, "auction already ended");
        }
        if (req.bidderId().equals(a.sellerId())) {
            throw new ResponseStatusException(FORBIDDEN, "seller cannot bid on own auction");
        }

        // המינימום להגשה עכשיו
        var minToPlace = (a.bidsCount() == 0)
                ? a.minPrice()
                : a.currentBid().add(a.bidIncrement());

        if (req.maxBid().compareTo(minToPlace) < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "maxBid is below minimum allowed");
        }

        // מוסיפים הצעה
        dao.insertBid(auctionId, req.bidderId(), req.maxBid());

        // מחשבים top-2 לפי משתמש
        List<BidsDao.TopBid> top2 = dao.top2PerUser(auctionId);
        int total = dao.totalBids(auctionId);

        var highest = top2.isEmpty() ? null : top2.get(0);
        var second  = (top2.size() < 2) ? null : top2.get(1);

        BigDecimal newCurrent;
        Long highestUserId = null;
        BigDecimal highestMax = null;

        if (highest == null) {
            // לא אמור לקרות – בדיוק הוספנו הצעה
            newCurrent = a.minPrice();
        } else {
            highestUserId = highest.bidderId();
            highestMax    = highest.maxBid();

            if (second == null) {
                // רק מציע אחד → המחיר נשאר מינימום
                newCurrent = a.minPrice();
            } else {
                // Second-Price Proxy:
                // המחיר המוצג הוא המינימום בין המקסימום של המוביל
                // לבין (המקסימום של השני + אינקרמנט)
                var secondPlusInc = second.maxBid().add(a.bidIncrement());
                newCurrent = (highestMax.compareTo(secondPlusInc) < 0)
                        ? highestMax
                        : secondPlusInc;
                // שמירה שלא נרד מתחת למחיר פתיחה
                if (newCurrent.compareTo(a.minPrice()) < 0) {
                    newCurrent = a.minPrice();
                }
            }
        }

        dao.updateAuctionAfterBid(auctionId, highestUserId, highestMax, newCurrent, total);

        var youAreLeading = highestUserId != null && highestUserId.equals(req.bidderId());
        var nextMin = newCurrent.add(a.bidIncrement());

        return new PlaceBidResponse(
                auctionId,
                highestUserId,
                highestMax,
                newCurrent,
                total,
                nextMin,
                youAreLeading,
                a.endDate()
        );
    }
}
