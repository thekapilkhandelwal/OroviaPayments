package com.orovia.payment.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.orovia.payment.domain.model.Booking;
import com.orovia.payment.domain.model.BookingStatus;
import com.orovia.payment.domain.model.PaymentMethod;
import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.dto.PaymentOrderRequest;
import com.orovia.payment.event.DomainEventPublisher;
import com.orovia.payment.integration.PaymentGatewayClient;
import com.orovia.payment.integration.PaymentGatewayRouter;
import com.orovia.payment.repository.BookingRepository;
import com.orovia.payment.repository.PaymentOrderRepository;
import com.orovia.payment.repository.PaymentTransactionRepository;
import com.orovia.payment.shared.cache.CacheService;
import com.orovia.payment.shared.config.ScalingProperties;
import com.orovia.payment.shared.id.IdGenerator;
import com.orovia.payment.shared.ratelimit.RateLimiterService;
import com.orovia.payment.shared.shard.ShardRoutingService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    @Mock
    private PaymentOrderRepository paymentOrderRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentGatewayRouter paymentGatewayRouter;
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private PaymentGatewayClient paymentGatewayClient;
    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private RateLimiterService rateLimiterService;
    @Mock
    private CacheService cacheService;
    @Mock
    private ScalingProperties scalingProperties;
    @Mock
    private ShardRoutingService shardRoutingService;

    @InjectMocks
    private PaymentService paymentService;

    private Booking booking;

    @Before
    public void setup() {
        booking = Booking.builder().id(1L).amount(BigDecimal.TEN).currency("USD")
                .status(BookingStatus.PENDING_PAYMENT).createdAt(OffsetDateTime.now()).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentGatewayRouter.resolve(PaymentMethod.CARD)).thenReturn(paymentGatewayClient);
        when(paymentGatewayClient.createOrder(any(PaymentOrder.class)))
                .thenReturn(Map.of("externalOrderId", "ext-1", "paymentUrl", "https://pay"));
        when(idempotencyService.acquire(any(), any())).thenReturn(true);
        when(rateLimiterService.tryConsume(any())).thenReturn(true);
        when(idGenerator.nextId()).thenReturn(10L);
        ScalingProperties.Cache cache = new ScalingProperties.Cache();
        cache.setStatusTtlSeconds(60);
        when(scalingProperties.getCache()).thenReturn(cache);
        when(paymentOrderRepository.save(any(PaymentOrder.class))).thenAnswer(invocation -> {
            PaymentOrder po = invocation.getArgument(0);
            po.setId(10L);
            return po;
        });
    }

    @Test
    public void createPaymentOrder_successful() {
        PaymentOrderRequest request = new PaymentOrderRequest();
        request.setBookingId(1L);
        request.setAmount(BigDecimal.TEN);
        request.setCurrency("USD");
        request.setPaymentMethod(PaymentMethod.CARD.name());
        request.setCustomerId(5L);
        request.setHotelId(9L);

        var response = paymentService.createPaymentOrder(request);
        assertNotNull(response);
        assertEquals(Long.valueOf(10L), response.getId());
        assertEquals("ext-1", response.getExternalOrderId());
    }
}
