package com.myapp.server.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping({"/health","/api/health"})
    public Map<String, Object> health() {
        return Map.of("status","ok");
    }
}
