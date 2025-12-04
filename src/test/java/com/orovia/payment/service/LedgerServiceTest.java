package com.orovia.payment.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.orovia.payment.domain.model.LedgerEntry;
import com.orovia.payment.domain.model.LedgerReferenceType;
import com.orovia.payment.event.DomainEventPublisher;
import com.orovia.payment.repository.LedgerEntryRepository;
import com.orovia.payment.shared.id.IdGenerator;
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
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private LedgerService ledgerService;

    @Before
    public void setup() {
        when(idGenerator.nextId()).thenReturn(1L);
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
