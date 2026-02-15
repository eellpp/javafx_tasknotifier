package com.tasknotifier.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public record RecurrenceRule(RecurrenceType type, LocalDate endDate, Set<LocalDate> skippedDates) {

    public RecurrenceRule {
        skippedDates = skippedDates == null ? new HashSet<>() : skippedDates;
    }

    public static RecurrenceRule none() {
        return new RecurrenceRule(RecurrenceType.NONE, null, new HashSet<>());
    }

    public boolean isRecurring() {
        return type != RecurrenceType.NONE;
    }
}
