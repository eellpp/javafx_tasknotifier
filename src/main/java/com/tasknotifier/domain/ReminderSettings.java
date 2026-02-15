package com.tasknotifier.domain;

public record ReminderSettings(boolean enabled, int minutesBeforeDue, int overdueRepeatMinutes, boolean soundEnabled) {

    public static ReminderSettings defaults() {
        return new ReminderSettings(true, 30, 60, false);
    }
}
