package com.orovia.payment.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.orovia.payment.domain.model.LedgerEntry;
import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LedgerServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private LedgerService ledgerService;

    @Before
    public void setup() {
        when(ledgerEntryRepository.save(any(LedgerEntry.class))).thenAnswer(invocation -> {
            LedgerEntry entry = invocation.getArgument(0);
            entry.setId(1L);
            return entry;
        });
    }

    @Test
    public void recordCreatesEntry() {
        LedgerEntry entry = ledgerService.record(LedgerReferenceType.PAYMENT, 1L, "d", "c", BigDecimal.ONE, "USD", "test");
        assertEquals(Long.valueOf(1L), entry.getId());
    }
}
