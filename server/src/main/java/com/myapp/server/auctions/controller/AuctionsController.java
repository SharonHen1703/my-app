package com.myapp.server.auctions.controller;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.service.AuctionService;
import com.myapp.server.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
public class AuctionsController {

    private final AuctionService auctionService;
    private final JwtService jwtService;

    @Value("${app.auth.cookie.name}")
    private String cookieName;

    public AuctionsController(AuctionService auctionService, JwtService jwtService) { 
        this.auctionService = auctionService;
        this.jwtService = jwtService;
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
    Long excludeSellerId = getCurrentUserId(request);

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

    @GetMapping("/api/auctions/categories")
    public List<String> getCategories() {
        return auctionService.getAllCategories();
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
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return auctionService.createAuction(request, currentUserId);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractTokenFromCookie(request);
        if (token == null || token.isBlank()) {
            return null; // For public endpoints, return null instead of throwing
        }
        try {
            String sub = jwtService.getSubject(token);
            return Long.valueOf(sub);
        } catch (Exception e) {
            return null; // Invalid token, return null for public endpoints
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
