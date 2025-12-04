package com.orovia.payment.integration;

import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.domain.model.Refund;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Contract for payment gateway interactions.
 */
public interface PaymentGatewayClient {

    /**
     * Create an order with the gateway.
     *
     * @param paymentOrder order to create
     * @return map containing external order id and payment url/token
     */
    Map<String, String> createOrder(PaymentOrder paymentOrder);

    /**
     * Fetch payment status for reconciliation.
     *
     * @param externalOrderId external order id
     * @return status string from gateway
     */
    String fetchPaymentStatus(String externalOrderId);

    /**
     * Issue a refund for a given external payment.
     *
     * @param externalPaymentId payment identifier
     * @param amount amount to refund
     * @return gateway refund id
     */
    String refundPayment(String externalPaymentId, BigDecimal amount);

    /**
     * Verify webhook signature.
     *
     * @param payload raw body
     * @param signature provided signature
     * @return true when valid
     */
    boolean verifyWebhookSignature(String payload, String signature);
}
