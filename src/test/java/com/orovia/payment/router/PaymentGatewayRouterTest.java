package com.orovia.payment.router;

import static org.junit.Assert.assertNotNull;

import com.orovia.payment.domain.model.PaymentMethod;
import com.orovia.payment.integration.OroviaMockGatewayClient;
import com.orovia.payment.integration.PaymentGatewayRouter;
import org.junit.Test;

public class PaymentGatewayRouterTest {

    @Test
    public void routerReturnsClientForAllMethods() {
        PaymentGatewayRouter router = new PaymentGatewayRouter(new OroviaMockGatewayClient());
        for (PaymentMethod method : PaymentMethod.values()) {
            assertNotNull(router.resolve(method));
        }
    }
}
