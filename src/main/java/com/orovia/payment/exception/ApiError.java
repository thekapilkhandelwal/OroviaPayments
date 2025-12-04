package com.orovia.payment.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Structured error response.
 */
@Data
@Builder
@AllArgsConstructor
public class ApiError {
    private String errorCode;
    private String message;
    private String traceId;
}
