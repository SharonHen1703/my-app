package com.myapp.server.auctions;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuctionsController {

    private final AuctionsDao dao;

    public AuctionsController(AuctionsDao dao) { this.dao = dao; }

    @GetMapping("/api/auctions")
    public Map<String, Object> listActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        int limit = Math.max(1, Math.min(size, 100));
        int offset = Math.max(0, page) * limit;

        List<AuctionListItem> items = dao.findActive(limit, offset);
        int total = dao.countActive();
        int totalPages = (int) Math.ceil((double) total / limit);

        Map<String, Object> body = new HashMap<>();
        body.put("page", page);
        body.put("size", limit);
        body.put("total", total);
        body.put("totalPages", totalPages);
        body.put("items", items);
        return body;
    }
}
