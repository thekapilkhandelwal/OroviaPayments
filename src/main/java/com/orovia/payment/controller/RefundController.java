package com.orovia.payment.controller;

import com.orovia.payment.dto.RefundRequest;
import com.orovia.payment.dto.RefundResponse;
import com.orovia.payment.service.RefundService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for refunds.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    /**
     * Create a refund for payment order.
     *
     * @param paymentOrderId order id
     * @param request body
     * @return response
     */
    @PostMapping("/{paymentOrderId}/refunds")
    public ResponseEntity<RefundResponse> createRefund(@PathVariable Long paymentOrderId,
                                                       @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(refundService.createRefund(paymentOrderId, request));
    }

    /**
     * List refunds for payment order.
     *
     * @param paymentOrderId order id
     * @return list
     */
    @GetMapping("/{paymentOrderId}/refunds")
    public ResponseEntity<List<RefundResponse>> listRefunds(@PathVariable Long paymentOrderId) {
        return ResponseEntity.ok(refundService.getRefunds(paymentOrderId));
    }
}
