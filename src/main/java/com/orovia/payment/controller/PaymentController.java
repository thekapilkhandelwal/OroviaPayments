package com.orovia.payment.controller;

import com.orovia.payment.dto.PaymentOrderRequest;
import com.orovia.payment.dto.PaymentOrderResponse;
import com.orovia.payment.dto.WebhookNotification;
import com.orovia.payment.mapper.PaymentMapper;
import com.orovia.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for payment order lifecycle.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Create a new payment order for a booking.
     *
     * @param request body
     * @return created order
     */
    @PostMapping("/orders")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(@Valid @RequestBody PaymentOrderRequest request) {
        PaymentOrderResponse response = paymentService.createPaymentOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment order by id.
     *
     * @param id identifier
     * @return order
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<PaymentOrderResponse> getPaymentOrder(@PathVariable Long id) {
        return ResponseEntity.ok(PaymentMapper.toResponse(paymentService.getPaymentOrder(id), null));
    }
}
