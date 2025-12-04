package com.orovia.payment.service.consumer;

import com.orovia.payment.domain.event.PaymentAuthorizedEvent;
import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.event.DomainEventSubscriber;
import com.orovia.payment.repository.PaymentOrderRepository;
import com.orovia.payment.service.LedgerService;
import com.orovia.payment.service.SettlementService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAuthorizedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentAuthorizedEventConsumer.class);

    private final DomainEventSubscriber subscriber;
    private final LedgerService ledgerService;
    private final SettlementService settlementService;
    private final PaymentOrderRepository paymentOrderRepository;

    @PostConstruct
    public void subscribe() {
        subscriber.subscribe("payments.authorized", event -> {
            if (event instanceof PaymentAuthorizedEvent authorizedEvent) {
                handle(authorizedEvent);
            }
        });
    }

    private void handle(PaymentAuthorizedEvent event) {
        log.info("Consuming PaymentAuthorizedEvent for order {}", event.getPaymentOrderId());
        PaymentOrder order = paymentOrderRepository.findById(event.getPaymentOrderId()).orElse(null);
        if (order == null) {
            log.warn("Payment order {} not found for ledger update", event.getPaymentOrderId());
            return;
        }

        ledgerService.record(LedgerReferenceType.PAYMENT, order.getId(), "customer:" + order.getBookingId(),
                "orovia:platform", order.getAmount(), order.getCurrency(), "Payment captured");
        ledgerService.record(LedgerReferenceType.PAYMENT, order.getId(), "orovia:platform",
                "hotel:" + order.getBookingId(), order.getAmount(), order.getCurrency(), "Hotel payable");
        settlementService.applySettlementSplit(order);
    }
}

