package com.tasknotifier.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void rejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> new Task(null, "  ", "", LocalDateTime.now(), Priority.MEDIUM,
                TaskStatus.TODO, List.of(), List.of(), "notes/a.md", RecurrenceRule.none(), ReminderSettings.defaults(), LocalDateTime.now()));
    }

    @Test
    void createsValidTask() {
        Task task = new Task(null, "Task", "summary", LocalDateTime.now(), Priority.HIGH,
                TaskStatus.TODO, List.of("work"), List.of("mail"), "notes/a.md", RecurrenceRule.none(), ReminderSettings.defaults(), LocalDateTime.now());
        assertEquals("Task", task.getTitle());
    }
}
