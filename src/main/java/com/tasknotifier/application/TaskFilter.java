package com.tasknotifier.application;

import com.tasknotifier.domain.Priority;
import com.tasknotifier.domain.Task;
import com.tasknotifier.domain.TaskStatus;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class TaskFilter {

    public List<Task> filter(List<Task> tasks, String search, TaskStatus status, Priority priority, DueRange dueRange, String tag) {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> priority == null || t.getPriority() == priority)
                .filter(t -> search == null || search.isBlank() || matchSearch(t, search.toLowerCase()))
                .filter(t -> tag == null || tag.isBlank() || t.getTags().stream().anyMatch(v -> v.equalsIgnoreCase(tag)))
                .filter(t -> filterDueRange(t, dueRange, today))
                .sorted(Comparator.comparing(Task::getDueDateTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getUpdatedAt).reversed())
                .toList();
    }

    private boolean matchSearch(Task task, String search) {
        return task.getTitle().toLowerCase().contains(search)
                || task.getSummary().toLowerCase().contains(search)
                || task.getTags().stream().anyMatch(t -> t.toLowerCase().contains(search));
    }

    private boolean filterDueRange(Task task, DueRange range, LocalDate today) {
        if (range == null || range == DueRange.ALL || task.getDueDateTime() == null) return true;
        LocalDate due = task.getDueDateTime().toLocalDate();
        return switch (range) {
            case TODAY -> due.isEqual(today);
            case THIS_WEEK -> !due.isBefore(today) && !due.isAfter(today.plusDays(7));
            case OVERDUE -> due.isBefore(today);
            case ALL -> true;
        };
    }

    public enum DueRange { TODAY, THIS_WEEK, OVERDUE, ALL }
}
