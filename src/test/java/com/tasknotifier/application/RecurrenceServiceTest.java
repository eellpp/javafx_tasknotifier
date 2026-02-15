package com.tasknotifier.application;

import com.tasknotifier.domain.RecurrenceRule;
import com.tasknotifier.domain.RecurrenceType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class RecurrenceServiceTest {

    @Test
    void computesNextOccurrenceSkippingDates() {
        RecurrenceService service = new RecurrenceService();
        LocalDateTime due = LocalDateTime.of(2026, 1, 1, 9, 0);
        HashSet<LocalDate> skipped = new HashSet<>();
        skipped.add(LocalDate.of(2026, 1, 2));
        RecurrenceRule rule = new RecurrenceRule(RecurrenceType.DAILY, null, skipped);

        LocalDateTime next = service.nextOccurrence(due, rule);
        assertEquals(LocalDateTime.of(2026, 1, 3, 9, 0), next);
    }

    @Test
    void returnsNullAfterEndDate() {
        RecurrenceService service = new RecurrenceService();
        LocalDateTime due = LocalDateTime.of(2026, 1, 1, 9, 0);
        RecurrenceRule rule = new RecurrenceRule(RecurrenceType.MONTHLY, LocalDate.of(2026, 1, 31), new HashSet<>());
        assertNull(service.nextOccurrence(due, rule));
    }
}
