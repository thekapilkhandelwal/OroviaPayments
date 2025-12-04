package com.orovia.payment.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.orovia.payment.domain.model.PaymentMethod;
import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.domain.model.PaymentOrderStatus;
import com.orovia.payment.dto.RefundRequest;
import com.orovia.payment.integration.PaymentGatewayClient;
import com.orovia.payment.integration.PaymentGatewayRouter;
import com.orovia.payment.repository.PaymentOrderRepository;
import com.orovia.payment.repository.RefundRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;
    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @Mock
    private PaymentGatewayRouter paymentGatewayRouter;
    @Mock
    private PaymentGatewayClient paymentGatewayClient;
    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private RefundService refundService;

    private PaymentOrder paymentOrder;

    @Before
    public void setup() {
        paymentOrder = PaymentOrder.builder()
                .id(2L)
                .bookingId(5L)
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.TEN)
                .currency("USD")
                .externalPgPaymentId("pg_1")
                .status(PaymentOrderStatus.SUCCESS)
                .createdAt(OffsetDateTime.now())
                .build();
        when(paymentOrderRepository.findById(2L)).thenReturn(Optional.of(paymentOrder));
        when(paymentGatewayRouter.resolve(PaymentMethod.CARD)).thenReturn(paymentGatewayClient);
        when(paymentGatewayClient.refundPayment(any(String.class), any(BigDecimal.class))).thenReturn("refund-1");
        when(refundRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void createRefund_success() {
        RefundRequest request = new RefundRequest();
        request.setAmount(BigDecimal.ONE);
        request.setReason("test");

        var response = refundService.createRefund(2L, request);
        assertNotNull(response);
        assertEquals("refund-1", response.getExternalRefundId());
    }
}
