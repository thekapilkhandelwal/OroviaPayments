package com.orovia.payment.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Centralized API exception mapping.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors.
     *
     * @param ex validation exception
     * @return structured error
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiError> handleValidation(Exception ex, HttpServletRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        ApiError error = ApiError.builder()
                .errorCode("VALIDATION_ERROR")
                .message(ex.getMessage())
                .traceId(request.getHeader("X-Correlation-Id"))
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Handles business rules.
     *
     * @param ex business exception
     * @return response
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .errorCode(ex.getCode())
                .message(ex.getMessage())
                .traceId(request.getHeader("X-Correlation-Id"))
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    /**
     * Handles missing resources.
     *
     * @param ex not found
     * @return response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .errorCode("NOT_FOUND")
                .message(ex.getMessage())
                .traceId(request.getHeader("X-Correlation-Id"))
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Catch-all handler for uncaught errors.
     *
     * @param ex throwable
     * @return response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        ApiError error = ApiError.builder()
                .errorCode("INTERNAL_ERROR")
                .message("Unexpected error occurred")
                .traceId(request.getHeader("X-Correlation-Id"))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
