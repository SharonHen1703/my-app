package com.myapp.server.auth;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 10 * 60; // 10 minutes

    private static class Entry {
        int count;
        long windowStartEpoch;
    }

    private final Map<String, Entry> attempts = new ConcurrentHashMap<>();

    private String key(String email, String ip) {
        return (email == null ? "" : email.toLowerCase()) + "|" + (ip == null ? "" : ip);
    }

    public boolean isBlocked(String email, String ip) {
        String k = key(email, ip);
        Entry e = attempts.get(k);
        if (e == null) return false;
        long now = Instant.now().getEpochSecond();
        if (now - e.windowStartEpoch > WINDOW_SECONDS) {
            attempts.remove(k);
            return false;
        }
        return e.count >= MAX_ATTEMPTS;
    }

    public void recordFailure(String email, String ip) {
        String k = key(email, ip);
        attempts.compute(k, (kk, e) -> {
            long now = Instant.now().getEpochSecond();
            if (e == null) {
                e = new Entry();
                e.count = 1;
                e.windowStartEpoch = now;
            } else {
                if (now - e.windowStartEpoch > WINDOW_SECONDS) {
                    e.count = 1;
                    e.windowStartEpoch = now;
                } else {
                    e.count += 1;
                }
            }
            return e;
        });
    }

    public void reset(String email, String ip) {
        attempts.remove(key(email, ip));
    }
}
