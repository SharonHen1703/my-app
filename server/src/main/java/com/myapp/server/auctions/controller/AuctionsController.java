package com.myapp.server.auctions.controller;

import com.myapp.server.auctions.dto.AuctionDetail;
import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.dto.CreateAuctionRequest;
import com.myapp.server.auctions.dto.CreateAuctionResponse;
import com.myapp.server.auctions.dto.UserAuctionItem;
import com.myapp.server.auctions.service.AuctionService;
import com.myapp.server.common.auth.JwtTokenExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    public CreateAuctionResponse createAuction(
            @RequestPart("auctionData") String auctionDataJson,
            @RequestPart(value = "image_0", required = false) MultipartFile image0,
            @RequestPart(value = "image_1", required = false) MultipartFile image1,
            @RequestPart(value = "image_2", required = false) MultipartFile image2,
            @RequestPart(value = "image_3", required = false) MultipartFile image3,
            @RequestPart(value = "image_4", required = false) MultipartFile image4,
            @RequestPart(value = "image_5", required = false) MultipartFile image5,
            @RequestPart(value = "image_6", required = false) MultipartFile image6,
            @RequestPart(value = "image_7", required = false) MultipartFile image7,
            @RequestPart(value = "image_8", required = false) MultipartFile image8,
            @RequestPart(value = "image_9", required = false) MultipartFile image9,
            HttpServletRequest httpRequest) throws Exception {
                
        Long currentUserId = jwtTokenExtractor.getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        
        // Parse JSON data
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        CreateAuctionRequest request = mapper.readValue(auctionDataJson, CreateAuctionRequest.class);
        
        // Collect images
        List<MultipartFile> images = Arrays.asList(image0, image1, image2, image3, image4, image5, image6, image7, image8, image9)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        return auctionService.createAuction(request, currentUserId, images);
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
