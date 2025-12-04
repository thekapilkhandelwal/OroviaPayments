package com.orovia.payment.service;

import com.orovia.payment.domain.event.SettlementCreatedEvent;
import com.orovia.payment.domain.model.HotelAccount;
import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.domain.model.Settlement;
import com.orovia.payment.domain.model.SettlementStatus;
import com.orovia.payment.event.DomainEventPublisher;
import com.orovia.payment.repository.HotelAccountRepository;
import com.orovia.payment.repository.SettlementRepository;
import com.orovia.payment.shared.id.IdGenerator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service applying commission rules and producing settlement batches.
 */
@Service
@RequiredArgsConstructor
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);

    private final SettlementRepository settlementRepository;
    private final HotelAccountRepository hotelAccountRepository;
    private final LedgerService ledgerService;
    private final DomainEventPublisher eventPublisher;
    private final IdGenerator idGenerator;

    /**
     * Calculates commission and updates hotel balance for a successful payment.
     *
     * @param order successful payment order
     */
    @Transactional
    public void applySettlementSplit(PaymentOrder order) {
        BigDecimal commission = order.getAmount().multiply(BigDecimal.valueOf(0.1));
        BigDecimal hotelPayout = order.getAmount().subtract(commission);
        ledgerService.record(LedgerReferenceType.PAYMENT, order.getId(), "customer:" + order.getBookingId(),
                "orovia:commission", commission, order.getCurrency(), "Platform commission");
        ledgerService.record(LedgerReferenceType.PAYMENT, order.getId(), "orovia:platform",
                "hotel:" + order.getBookingId(), hotelPayout, order.getCurrency(), "Hotel payout accrual");

        HotelAccount account = hotelAccountRepository.findByHotelId(order.getBookingId())
                .orElseGet(() -> HotelAccount.builder().hotelId(order.getBookingId()).payableBalance(BigDecimal.ZERO).build());
        account.setPayableBalance(account.getPayableBalance().add(hotelPayout));
        hotelAccountRepository.save(account);
    }

    /**
     * Creates settlement batches for hotels with payable balance.
     *
     * @return list of created settlements
     */
    @Transactional
    public List<Settlement> createPendingSettlements() {
        return hotelAccountRepository.findAll().stream()
                .filter(acc -> acc.getPayableBalance().compareTo(BigDecimal.ZERO) > 0)
                .map(acc -> {
                    Settlement settlement = Settlement.builder()
                            .id(idGenerator.nextId())
                            .hotelId(acc.getHotelId())
                            .amount(acc.getPayableBalance())
                            .status(SettlementStatus.PENDING)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    acc.setPayableBalance(BigDecimal.ZERO);
                    hotelAccountRepository.save(acc);
                    Settlement saved = settlementRepository.save(settlement);
                    eventPublisher.publish("settlements.created", new SettlementCreatedEvent(saved.getId(),
                            saved.getHotelId(), saved.getAmount(), "USD"));
                    return saved;
                })
                .toList();
    }

    /**
     * Marks a settlement as completed after payout.
     *
     * @param settlement settlement record
     * @return updated settlement
     */
    @Transactional
    public Settlement completeSettlement(Settlement settlement) {
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setExternalPayoutReference("payout-" + settlement.getId());
        settlement.setUpdatedAt(OffsetDateTime.now());
        ledgerService.record(LedgerReferenceType.SETTLEMENT, settlement.getId(), "orovia:platform",
                "hotel:" + settlement.getHotelId(), settlement.getAmount(), "USD", "Settlement payout");
        return settlementRepository.save(settlement);
    }

    /**
     * Retrieve settlements for a hotel.
     *
     * @param hotelId hotel id
     * @return list of settlements
     */
    @Transactional(readOnly = true)
    public List<Settlement> findByHotel(Long hotelId) {
        return settlementRepository.findByHotelId(hotelId);
    }

    /**
     * Retrieve settlements within date and status filters.
     *
     * @param status status filter
     * @param start start date
     * @param end end date
     * @return list
     */
    @Transactional(readOnly = true)
    public List<Settlement> findByStatusAndDate(SettlementStatus status, OffsetDateTime start, OffsetDateTime end) {
        return settlementRepository.findByStatusAndCreatedAtBetween(status, start, end);
    }
}
