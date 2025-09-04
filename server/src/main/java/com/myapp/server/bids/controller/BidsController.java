package com.myapp.server.bids.controller;

import com.myapp.server.bids.service.BidsService;
import com.myapp.server.auth.service.JwtService;
import java.util.List;
import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.dto.PlaceBidResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
@RequiredArgsConstructor
public class BidsController {

    private final BidsService service;
    private final JwtService jwtService;

    @Value("${app.auth.cookie.name}")
    private String cookieName;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceBidResponse placeBid(@PathVariable long auctionId,
                                     @Valid @RequestBody PlaceBidRequest req,
                                     HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        return service.placeBid(auctionId, req, currentUserId);
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

    @GetMapping("/history")
    public List<BidsService.BidHistoryItem> getHistory(@PathVariable long auctionId) {
        return service.getHistory(auctionId);
    }

}
