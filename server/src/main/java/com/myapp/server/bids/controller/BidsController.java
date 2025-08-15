package com.myapp.server.bids.controller;

import com.myapp.server.bids.service.BidsService;
import com.myapp.server.bids.dto.PlaceBidRequest;
import com.myapp.server.bids.dto.PlaceBidResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
@RequiredArgsConstructor
public class BidsController {

    private final BidsService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaceBidResponse placeBid(@PathVariable long auctionId,
                                     @Valid @RequestBody PlaceBidRequest req) {
        return service.placeBid(auctionId, req);
    }
}
