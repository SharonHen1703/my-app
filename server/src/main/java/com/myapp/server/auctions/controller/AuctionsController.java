package com.myapp.server.auctions.controller;

import com.myapp.server.auctions.dto.AuctionListItem;
import com.myapp.server.auctions.service.AuctionService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuctionsController {

    private final AuctionService auctionService;

    public AuctionsController(AuctionService auctionService) { 
        this.auctionService = auctionService; 
    }

    @GetMapping("/api/auctions")
    public Map<String, Object> listActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        int limit = Math.max(1, Math.min(size, 100));
        int pageNumber = Math.max(0, page);

        Page<AuctionListItem> auctionsPage = auctionService.findActiveAuctions(pageNumber, limit);

        Map<String, Object> body = new HashMap<>();
        body.put("page", auctionsPage.getNumber());
        body.put("size", auctionsPage.getSize());
        body.put("total", auctionsPage.getTotalElements());
        body.put("totalPages", auctionsPage.getTotalPages());
        body.put("items", auctionsPage.getContent());
        
        return body;
    }
}
