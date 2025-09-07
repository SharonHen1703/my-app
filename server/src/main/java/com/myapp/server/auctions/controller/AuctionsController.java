package com.myapp.server.auctions.controller;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.dto.UserAuctionItem;
import com.myapp.server.auctions.service.AuctionService;
import com.myapp.server.common.auth.JwtTokenExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
public class AuctionsController {

    private final AuctionService auctionService;
    private final JwtTokenExtractor jwtTokenExtractor;

    public AuctionsController(AuctionService auctionService, JwtTokenExtractor jwtTokenExtractor) { 
        this.auctionService = auctionService;
        this.jwtTokenExtractor = jwtTokenExtractor;
    }

    @GetMapping("/api/auctions")
    public Map<String, Object> listActive(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false, name = "condition") List<String> conditions,
        @RequestParam(required = false) String search,
        HttpServletRequest request
    ) {
        int limit = Math.max(1, Math.min(size, 100));
        int pageNumber = Math.max(0, page);

    // Validate min<=max when both provided
    if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
        throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
    }

    // Normalize empty condition list
    List<String> normalizedConditions = (conditions == null || conditions.isEmpty())
        ? null
        : conditions.stream().filter(s -> s != null && !s.isBlank()).collect(Collectors.toList());

    // Get current user ID to exclude their auctions
    Long excludeSellerId = jwtTokenExtractor.getCurrentUserId(request);

    Page<AuctionListItem> auctionsPage =
        auctionService.findActiveAuctions(pageNumber, limit, category, minPrice, maxPrice, normalizedConditions, search, excludeSellerId);

        Map<String, Object> body = new HashMap<>();
        body.put("page", auctionsPage.getNumber());
        body.put("size", auctionsPage.getSize());
        body.put("total", auctionsPage.getTotalElements());
        body.put("totalPages", auctionsPage.getTotalPages());
        body.put("items", auctionsPage.getContent());
        
        return body;
    }

    @GetMapping("/api/auctions/categories/map")
    public java.util.Map<String, String> getCategoriesMap() {
        return auctionService.getCategoriesMap();
    }

    @GetMapping("/api/auctions/{id}")
    public ResponseEntity<AuctionDetail> getOne(@PathVariable Long id) {
        try {
            AuctionDetail auctionDetail = auctionService.findAuctionDetailAnyStatus(id);
            return ResponseEntity.ok(auctionDetail);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/auctions")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateAuctionResponse createAuction(@Valid @RequestBody CreateAuctionRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = jwtTokenExtractor.getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return auctionService.createAuction(request, currentUserId);
    }

    @GetMapping("/api/auctions/my")
    public ResponseEntity<List<UserAuctionItem>> getCurrentUserAuctions(HttpServletRequest request) {
        Long currentUserId = jwtTokenExtractor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        List<UserAuctionItem> auctions = auctionService.getUserAuctions(currentUserId);
        return ResponseEntity.ok(auctions);
    }
}
