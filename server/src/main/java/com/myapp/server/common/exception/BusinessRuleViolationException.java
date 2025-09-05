package com.myapp.server.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain exception for business rule violations.
 * Replaces ResponseStatusException to maintain layer separation.
 */
public class BusinessRuleViolationException extends RuntimeException {
    
    private final HttpStatus status;
    
    public BusinessRuleViolationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
}
