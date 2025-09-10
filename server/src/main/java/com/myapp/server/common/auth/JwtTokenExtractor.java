package com.myapp.server.common.auth;

import com.myapp.server.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting JWT tokens from HTTP requests.
 * Provides common functionality for JWT token extraction and user ID resolution.
 */
@Component
public class JwtTokenExtractor {

    private final JwtService jwtService;
    
    @Value("${app.auth.cookie.name}")
    private String cookieName;

    public JwtTokenExtractor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Extracts JWT token from the configured cookie in the HTTP request.
     * 
     * @param request the HTTP request
     * @return the JWT token value, or null if not found
     */
    public String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Extracts the current user ID from the JWT token in the request.
     * 
     * @param request the HTTP request
     * @return the user ID from the token, or null if token is invalid/missing
     */
    public Long getCurrentUserId(HttpServletRequest request) {
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
}
