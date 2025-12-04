package com.orovia.payment.controller;

import com.orovia.payment.domain.model.SettlementStatus;
import com.orovia.payment.dto.SettlementResponse;
import com.orovia.payment.mapper.SettlementMapper;
import com.orovia.payment.service.SettlementService;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Settlement APIs for hotel payouts.
 */
@RestController
@RequestMapping("/api/v1/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    /**
     * List settlements for a hotel.
     *
     * @param hotelId hotel id
     * @return settlements
     */
    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<List<SettlementResponse>> settlementsForHotel(@PathVariable Long hotelId) {
        List<SettlementResponse> responses = settlementService.findByHotel(hotelId).stream()
                .map(SettlementMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * List settlements using filter.
     *
     * @param status status
     * @param start start time
     * @param end end time
     * @return list
     */
    @GetMapping
    public ResponseEntity<List<SettlementResponse>> search(@RequestParam SettlementStatus status,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
                                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end) {
        List<SettlementResponse> responses = settlementService.findByStatusAndDate(status, start, end)
                .stream().map(SettlementMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }
}
