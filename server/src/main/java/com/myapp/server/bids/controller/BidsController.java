package com.myapp.server.bids.controller;

import com.myapp.server.bids.service.BidsService;
import com.myapp.server.bids.dto.UserBidSummaryItem;
import com.myapp.server.common.auth.JwtTokenExtractor;
import java.util.List;
import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.dto.PlaceBidResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class BidsController {

    private final BidsService service;
    private final JwtTokenExtractor jwtTokenExtractor;

    @PostMapping("/api/auctions/{auctionId}/bids")
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceBidResponse placeBid(@PathVariable long auctionId,
                                     @Valid @RequestBody PlaceBidRequest req,
                                     HttpServletRequest request) {
        Long currentUserId = jwtTokenExtractor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return service.placeBid(auctionId, req, currentUserId);
    }

    @GetMapping("/api/auctions/{auctionId}/bids/history")
    public List<BidsService.BidHistoryItem> getHistory(@PathVariable long auctionId) {
        return service.getHistory(auctionId);
    }

    @GetMapping("/api/bids/my/summary")
    public ResponseEntity<List<UserBidSummaryItem>> getCurrentUserBidsSummary(HttpServletRequest request) {
        Long currentUserId = jwtTokenExtractor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        List<UserBidSummaryItem> bids = service.getUserBidsSummary(currentUserId);
        return ResponseEntity.ok(bids);
    }

}
