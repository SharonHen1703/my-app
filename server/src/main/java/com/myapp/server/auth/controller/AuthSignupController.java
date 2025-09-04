package com.myapp.server.auth.controller;

import com.myapp.server.auth.dto.SignupRequest;
import com.myapp.server.auth.entity.User;
import com.myapp.server.auth.mapper.AuthMapper;
import com.myapp.server.auth.service.AuthService;
import com.myapp.server.auth.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthSignupController {
    private final AuthService authService;
    private final JwtService jwtService;
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

    public AuthSignupController(AuthService authService, JwtService jwtService, AuthMapper authMapper) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req, HttpServletResponse resp) {
        try {
            // Validate and normalize inputs
            String emailNormalized = authService.validateAndNormalizeSignupData(req);
            
            // Duplicate email check
            if (authService.existsByEmailNormalized(emailNormalized)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "האימייל כבר קיים במערכת"));
            }

            // Create user
            User u = authService.signup(req);
            String token = jwtService.generateToken(u.getId().toString(), Map.of("email", u.getEmail()));
            setAuthCookie(resp, token);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.toUserResponse(u));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
        } catch (DataIntegrityViolationException ex) {
            // Unique index race safety
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "האימייל כבר קיים במערכת"));
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
}
