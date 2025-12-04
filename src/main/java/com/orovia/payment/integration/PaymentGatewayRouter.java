package com.orovia.payment.integration;

import com.orovia.payment.domain.model.PaymentMethod;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Selects payment gateways based on payment method or region.
 */
@Component
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentGatewayClient> routingTable = new EnumMap<>(PaymentMethod.class);

    public PaymentGatewayRouter(OroviaMockGatewayClient mockGatewayClient) {
        for (PaymentMethod method : PaymentMethod.values()) {
            routingTable.put(method, mockGatewayClient);
        }
    }

    /**
     * Resolve a gateway for a given payment method.
     *
     * @param method payment method
     * @return client
     */
    public PaymentGatewayClient resolve(PaymentMethod method) {
        return routingTable.get(method);
    }
}
