package com.orovia.payment.integration;

import com.orovia.payment.domain.model.PaymentOrder;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock payment gateway client simulating Orovia Pay style API.
 */
@Component
public class OroviaMockGatewayClient implements PaymentGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(OroviaMockGatewayClient.class);

    @Override
    public Map<String, String> createOrder(PaymentOrder paymentOrder) {
        String externalId = "oro-" + UUID.randomUUID();
        String paymentUrl = "https://mock-gateway.example/pay/" + externalId;
        log.info("Created mock gateway order {} for booking {}", externalId, paymentOrder.getBookingId());
        return Map.of("externalOrderId", externalId, "paymentUrl", paymentUrl);
    }

    @Override
    public String fetchPaymentStatus(String externalOrderId) {
        log.info("Fetching mock status for {}", externalOrderId);
        return "SUCCESS";
    }

    @Override
    public String refundPayment(String externalPaymentId, BigDecimal amount) {
        String refundId = "refund-" + UUID.randomUUID();
        log.info("Mock refund {} for payment {} amount {}", refundId, externalPaymentId, amount);
        return refundId;
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature) {
        return signature != null && signature.startsWith("mock-");
    }
}
