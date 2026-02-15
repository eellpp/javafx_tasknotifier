package com.tasknotifier.application;

import com.tasknotifier.domain.RecurrenceRule;
import com.tasknotifier.domain.RecurrenceType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RecurrenceService {

    public LocalDateTime nextOccurrence(LocalDateTime due, RecurrenceRule rule) {
        if (due == null || rule == null || !rule.isRecurring()) return due;
        LocalDateTime next = due;
        do {
            next = switch (rule.type()) {
                case DAILY -> next.plusDays(1);
                case WEEKLY -> next.plusWeeks(1);
                case MONTHLY -> next.plusMonths(1);
                case NONE -> next;
            };
            if (rule.endDate() != null && next.toLocalDate().isAfter(rule.endDate())) {
                return null;
            }
        } while (rule.skippedDates().contains(next.toLocalDate()));
        return next;
    }

    public String summarize(RecurrenceRule rule) {
        if (rule == null || rule.type() == RecurrenceType.NONE) return "None";
        String suffix = rule.endDate() == null ? "never ends" : "until " + rule.endDate();
        return rule.type().name() + " (" + suffix + ")";
    }

    public RecurrenceRule skipOccurrence(RecurrenceRule rule, LocalDate occurrenceDate) {
        rule.skippedDates().add(occurrenceDate);
        return rule;
    }
}
