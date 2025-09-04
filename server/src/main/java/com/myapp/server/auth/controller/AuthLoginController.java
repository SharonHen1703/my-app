package com.myapp.server.auth.controller;

import com.myapp.server.auth.dto.LoginRequest;
import com.myapp.server.auth.entity.User;
import com.myapp.server.auth.mapper.AuthMapper;
import com.myapp.server.auth.service.AuthService;
import com.myapp.server.auth.service.JwtService;
import com.myapp.server.auth.service.LoginRateLimiter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthLoginController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final LoginRateLimiter rateLimiter;
    private final AuthMapper authMapper;

    @Value("${app.auth.cookie.name}")
    private String cookieName;

    @Value("${app.auth.cookie.domain}")
    private String cookieDomain;

    @Value("${app.auth.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.auth.cookie.same-site:Strict}")
    private String sameSite;

    @Value("${app.auth.cookie.path:/}")
    private String cookiePath;

    public AuthLoginController(AuthService authService, JwtService jwtService, 
                             LoginRateLimiter rateLimiter, AuthMapper authMapper) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.rateLimiter = rateLimiter;
        this.authMapper = authMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, 
                                 HttpServletResponse resp, HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        if (rateLimiter.isBlocked(req.getEmail(), ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("message", "נחסמת זמנית. נסה שוב מאוחר יותר"));
        }
        
        Optional<User> user = authService.validateCredentials(req.getEmail(), req.getPassword());
        if (user.isEmpty()) {
            rateLimiter.recordFailure(req.getEmail(), ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
        }
        
        User u = user.get();
        rateLimiter.reset(req.getEmail(), ip);
        String token = jwtService.generateToken(u.getId().toString(), Map.of("email", u.getEmail()));
        setAuthCookie(resp, token);
        
        return ResponseEntity.ok(authMapper.toUserResponse(u));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse resp) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        if (cookieDomain != null && !cookieDomain.isBlank()) cookie.setDomain(cookieDomain);
        
        // SameSite is not directly supported via API; add header
        resp.addHeader("Set-Cookie", cookieName + "=; Max-Age=0; Path=" + cookiePath + 
            "; HttpOnly; SameSite=" + sameSite + (cookieSecure ? "; Secure" : "") + 
            (cookieDomain != null && !cookieDomain.isBlank() ? "; Domain=" + cookieDomain : ""));
        resp.addCookie(cookie);
        
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        String token = extractTokenFromCookie(req);
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Not authenticated"));
        }
        
        try {
            String sub = jwtService.getSubject(token);
            Long userId = Long.valueOf(sub);
            
            // Get full user details from database
            Optional<User> userOpt = authService.findUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
            }
            
            User user = userOpt.get();
            return ResponseEntity.ok(authMapper.toUserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid token"));
        }
    }

    private void setAuthCookie(HttpServletResponse resp, String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(60 * 60 * 24 * 7); // 7 days
        if (cookieDomain != null && !cookieDomain.isBlank()) cookie.setDomain(cookieDomain);
        
        // SameSite sticking via header to ensure enforcement
        resp.addHeader("Set-Cookie", cookieName + "=" + token + "; Max-Age=604800; Path=" + cookiePath + 
            "; HttpOnly; SameSite=" + sameSite + (cookieSecure ? "; Secure" : "") + 
            (cookieDomain != null && !cookieDomain.isBlank() ? "; Domain=" + cookieDomain : ""));
        resp.addCookie(cookie);
    }

    private String extractTokenFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (cookieName.equals(c.getName())) return c.getValue();
        }
        return null;
    }
}
