package com.myapp.server.auth;

import com.myapp.server.auth.dto.SignupRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public Optional<User> findByEmailNormalized(String emailNormalized) {
        return userRepository.findByEmailNormalized(emailNormalized);
    }

    public boolean existsByEmailNormalized(String emailNormalized) {
        return userRepository.findByEmailNormalized(emailNormalized).isPresent();
    }

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public User signup(SignupRequest req) {
        final String emailNormalized = normalizeEmail(req.getEmail());
        userRepository.findByEmailNormalized(emailNormalized).ifPresent(u -> {
            throw new RuntimeException("Email already in use");
        });
        User u = new User();
        // Store normalized email to keep symmetry with checks & index semantics
        u.setEmail(emailNormalized);
        u.setFullName(req.getFullName());
        // best effort split
        String[] parts = req.getFullName().trim().split(" ", 2);
        u.setFirstName(parts.length > 0 ? parts[0] : req.getFullName());
        u.setLastName(parts.length > 1 ? parts[1] : "");
        // store digits-only phone if provided
        String phone = req.getPhone();
        if (phone != null) {
            String digits = phone.replaceAll("\\D", "");
            u.setPhone(digits.isEmpty() ? null : digits);
        }
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        return userRepository.save(u);
    }

    public Optional<User> validateCredentials(String email, String rawPassword) {
        final String emailNormalized = normalizeEmail(email);
        return userRepository.findByEmailNormalized(emailNormalized)
                .filter(u -> u.getPasswordHash() != null && passwordEncoder.matches(rawPassword, u.getPasswordHash()));
    }
}
