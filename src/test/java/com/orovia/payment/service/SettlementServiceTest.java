package com.orovia.payment.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.orovia.payment.domain.model.HotelAccount;
import com.orovia.payment.domain.model.PaymentMethod;
import com.orovia.payment.domain.model.PaymentOrder;
import com.orovia.payment.repository.HotelAccountRepository;
import com.orovia.payment.repository.SettlementRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;
    @Mock
    private HotelAccountRepository hotelAccountRepository;
    @Mock
    private LedgerService ledgerService;

    @InjectMocks
    private SettlementService settlementService;

    @Before
    public void setup() {
        HotelAccount account = HotelAccount.builder().id(1L).hotelId(9L).payableBalance(BigDecimal.ZERO).build();
        when(hotelAccountRepository.findByHotelId(5L)).thenReturn(Optional.of(account));
        when(hotelAccountRepository.save(any(HotelAccount.class))).thenReturn(account);
    }

    @Test
    public void applySettlementSplit_updatesBalance() {
        PaymentOrder order = PaymentOrder.builder()
                .id(1L)
                .bookingId(5L)
                .paymentMethod(PaymentMethod.CARD)
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .build();
        settlementService.applySettlementSplit(order);
        var account = hotelAccountRepository.findByHotelId(5L).orElseThrow();
        assertEquals(0, account.getPayableBalance().compareTo(new BigDecimal("90.00")));
    }
}
