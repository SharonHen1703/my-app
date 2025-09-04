package com.myapp.server.auth.controller;

import com.myapp.server.auth.dto.LoginRequest;
import com.myapp.server.auth.dto.SignupRequest;
import com.myapp.server.auth.dto.UserResponse;
import com.myapp.server.auth.entity.User;
import com.myapp.server.auth.service.AuthService;
import com.myapp.server.auth.service.JwtService;
import com.myapp.server.auth.service.LoginRateLimiter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final LoginRateLimiter rateLimiter;

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

    @Autowired
    private DataSource dataSource;

    public AuthController(AuthService authService,
                          LoginRateLimiter rateLimiter,
                          @Value("${app.auth.jwt.secret}") String secret,
                          @Value("${app.auth.jwt.ttl-seconds}") long ttlSeconds) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
        this.jwtService = new JwtService(secret, ttlSeconds);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req, HttpServletResponse resp) {
        // Normalize and validate inputs
        final String emailNormalized = authService.normalizeEmail(req.getEmail());
        final String fullName = req.getFullName() == null ? "" : req.getFullName().trim();
        final String password = req.getPassword() == null ? "" : req.getPassword().trim();
        final String phoneRaw = req.getPhone();
        final String phoneDigits = phoneRaw == null ? "" : phoneRaw.replaceAll("\\D", "");

        if (fullName.length() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "שם מלא חייב להיות באורך של לפחות 2 תווים"));
        }
        if (password.length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "הסיסמה חייבת להיות באורך של לפחות 8 תווים"));
        }
        if (phoneRaw != null && !phoneRaw.isBlank() && phoneDigits.length() != 10) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "מספר טלפון חייב להכיל בדיוק 10 ספרות"));
        }

        // Duplicate email check
        if (authService.existsByEmailNormalized(emailNormalized)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "האימייל כבר קיים במערכת"));
        }

        // proceed (store normalized email and digits-only phone)
        try {
            // set normalized into DTO for service
            req.setEmail(emailNormalized);
            if (!phoneDigits.isBlank()) req.setPhone(phoneDigits);
            User u = authService.signup(req);
            String token = jwtService.generateToken(u.getId().toString(), Map.of("email", u.getEmail()));
            setAuthCookie(resp, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(u.getId(), u.getEmail(), u.getFullName()));
        } catch (DataIntegrityViolationException ex) {
            // Unique index race safety
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "האימייל כבר קיים במערכת"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletResponse resp, HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        if (rateLimiter.isBlocked(req.getEmail(), ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", "נחסמת זמנית. נסה שוב מאוחר יותר"));
        }
        Optional<User> user = authService.validateCredentials(req.getEmail(), req.getPassword());
        if (user.isEmpty()) {
            rateLimiter.recordFailure(req.getEmail(), ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
        }
        User u = user.get();
        rateLimiter.reset(req.getEmail(), ip);
        String token = jwtService.generateToken(u.getId().toString(), Map.of("email", u.getEmail()));
        setAuthCookie(resp, token);
        return ResponseEntity.ok(new UserResponse(u.getId(), u.getEmail(), u.getFullName()));
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
        resp.addHeader("Set-Cookie", cookieName + "=; Max-Age=0; Path=" + cookiePath + "; HttpOnly; SameSite=" + sameSite + (cookieSecure ? "; Secure" : "") + (cookieDomain != null && !cookieDomain.isBlank() ? "; Domain=" + cookieDomain : ""));
        resp.addCookie(cookie);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        String token = extractTokenFromCookie(req);
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }
        try {
            String sub = jwtService.getSubject(token);
            Long userId = Long.valueOf(sub);
            // Get full user details from database
            Optional<User> userOpt = authService.findUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
            }
            User user = userOpt.get();
            return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getFullName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid token"));
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
        resp.addHeader("Set-Cookie", cookieName + "=" + token + "; Max-Age=604800; Path=" + cookiePath + "; HttpOnly; SameSite=" + sameSite + (cookieSecure ? "; Secure" : "") + (cookieDomain != null && !cookieDomain.isBlank() ? "; Domain=" + cookieDomain : ""));
        resp.addCookie(cookie);
    }

    private String extractTokenFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (cookieName.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private static String redact(String url) {
        if (url == null) return "null";
        // redact credentials if present
        return url.replaceAll("://([^:@/]+):([^@/]+)@", "://$1:***@");
    }

    private static String safe(String s) {
        if (s == null) return "null";
        return s.replaceAll("[@]", "[at]");
    }
}
