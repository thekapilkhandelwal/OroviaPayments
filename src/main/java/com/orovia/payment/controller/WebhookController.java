package com.orovia.payment.controller;

import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.dto.WebhookNotification;
import com.orovia.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook endpoints used by payment gateways.
 */
@RestController
@RequestMapping("/api/v1/payments/webhook")
public class WebhookController {

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Handle webhook callback.
     *
     * @param provider provider name
     * @param notification payload
     * @param raw raw body
     * @return updated order
     */
    @PostMapping("/{provider}")
    public ResponseEntity<PaymentOrder> webhook(@PathVariable String provider,
                                                @RequestBody WebhookNotification notification) {
        PaymentOrder order = paymentService.handleWebhook(provider, notification, notification.toString());
        return ResponseEntity.ok(order);
    }
}
