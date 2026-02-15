package com.tasknotifier.ui;

import com.tasknotifier.application.*;
import com.tasknotifier.domain.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class MainViewModel {

    private final TaskService taskService;
    private final RecurrenceService recurrenceService;
    private final ReminderScheduler reminderScheduler;
    private final TaskFilter filter = new TaskFilter();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();

    public MainViewModel(TaskService taskService, RecurrenceService recurrenceService, ReminderScheduler reminderScheduler) {
        this.taskService = taskService;
        this.recurrenceService = recurrenceService;
        this.reminderScheduler = reminderScheduler;
        refresh();
    }

    public ObservableList<Task> tasks() {
        return tasks;
    }

    public void refresh() {
        tasks.setAll(taskService.listAll());
    }

    public List<Task> filtered(String search, TaskStatus status, Priority priority, TaskFilter.DueRange dueRange, String tag) {
        return filter.filter(taskService.listAll(), search, status, priority, dueRange, tag);
    }

    public TaskService.DashboardSummary dashboardSummary() {
        return taskService.dashboardSummary();
    }

    public void saveTask(Long id, String title, String summary, LocalDate dueDate, String dueTime,
                         Priority priority, TaskStatus status, String tags, String references,
                         String markdownPath, RecurrenceType recurrenceType, LocalDate recurrenceEndDate,
                         boolean reminderEnabled, int minutesBeforeDue, int overdueMinutes, boolean reminderSound) {

        LocalDateTime due = dueDate == null ? null : LocalDateTime.of(dueDate, parseTime(dueTime));
        Task task = new Task(id, title, summary, due, priority, status,
                tokenize(tags), tokenize(references), markdownPath,
                new RecurrenceRule(recurrenceType, recurrenceEndDate, new java.util.HashSet<>()),
                new ReminderSettings(reminderEnabled, minutesBeforeDue, overdueMinutes, reminderSound),
                LocalDateTime.now());
        taskService.save(task);
        refresh();
    }

    public void delete(Task task) {
        if (task == null) return;
        taskService.delete(task.getId());
        refresh();
    }

    public void toggleDone(Task task) {
        if (task == null) return;
        taskService.markDoneToggle(task);
        refresh();
    }

    public String recurrenceSummary(Task task) {
        return recurrenceService.summarize(task.getRecurrenceRule());
    }

    public void skipNextOccurrence(Task task) {
        if (task == null || task.getDueDateTime() == null || !task.getRecurrenceRule().isRecurring()) return;
        recurrenceService.skipOccurrence(task.getRecurrenceRule(), task.getDueDateTime().toLocalDate());
        taskService.save(task);
        refresh();
    }

    public void openMarkdown(Task task) {
        if (task == null) return;
        try {
            File file = new File(task.getMarkdownPath());
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException ignored) {
        }
    }

    public void setNotificationsEnabled(boolean enabled) {
        reminderScheduler.setEnabled(enabled);
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) return LocalTime.of(9, 0);
        return LocalTime.parse(value);
    }

    private List<String> tokenize(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }
}
