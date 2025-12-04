package com.orovia.payment.exception;

/**
 * Thrown when required resource is not available.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
