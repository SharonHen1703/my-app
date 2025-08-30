package com.myapp.server.users.controller;

import com.myapp.server.bids.dto.UserBidSummaryItem;
import com.myapp.server.bids.service.BidsService;
import com.myapp.server.users.dto.UserAuctionItem;
import com.myapp.server.auctions.service.AuctionService;
import com.myapp.server.auth.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final BidsService bidsService;
    private final AuctionService auctionService;
    private final JwtService jwtService;

    @Value("${app.auth.cookie.name}")
    private String cookieName;

    @GetMapping("/{userId}/bids/summary")
    public ResponseEntity<List<UserBidSummaryItem>> getUserBidsSummary(@PathVariable Long userId) {
        List<UserBidSummaryItem> bids = bidsService.getUserBidsSummary(userId);
        return ResponseEntity.ok(bids);
    }

    @GetMapping("/{userId}/auctions")
    public ResponseEntity<List<UserAuctionItem>> getUserAuctions(@PathVariable Long userId) {
        List<UserAuctionItem> auctions = auctionService.getUserAuctions(userId);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/me/auctions")
    public ResponseEntity<List<UserAuctionItem>> getCurrentUserAuctions(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<UserAuctionItem> auctions = auctionService.getUserAuctions(currentUserId);
        return ResponseEntity.ok(auctions);
    }

    @GetMapping("/me/bids/summary")
    public ResponseEntity<List<UserBidSummaryItem>> getCurrentUserBidsSummary(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<UserBidSummaryItem> bids = bidsService.getUserBidsSummary(currentUserId);
        return ResponseEntity.ok(bids);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractTokenFromCookie(request);
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        try {
            String sub = jwtService.getSubject(token);
            return Long.valueOf(sub);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String extractTokenFromCookie(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
