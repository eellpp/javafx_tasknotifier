package com.tasknotifier.application;

import com.tasknotifier.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskFilterTest {

    private final TaskFilter filter = new TaskFilter();

    @Test
    void filtersBySearchAndStatus() {
        Task a = new Task(1L, "Alpha", "work item", LocalDateTime.now(), Priority.MEDIUM, TaskStatus.TODO,
                List.of("work"), List.of(), "notes/a.md", RecurrenceRule.none(), ReminderSettings.defaults(), LocalDateTime.now());
        Task b = new Task(2L, "Beta", "personal", LocalDateTime.now(), Priority.HIGH, TaskStatus.DONE,
                List.of("home"), List.of(), "notes/b.md", RecurrenceRule.none(), ReminderSettings.defaults(), LocalDateTime.now());

        List<Task> result = filter.filter(List.of(a, b), "work", TaskStatus.TODO, null, TaskFilter.DueRange.ALL, null);
        assertEquals(1, result.size());
        assertEquals("Alpha", result.get(0).getTitle());
    }
}
