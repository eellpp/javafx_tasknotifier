package com.tasknotifier.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Task {

    private Long id;
    private String title;
    private String summary;
    private LocalDateTime dueDateTime;
    private Priority priority;
    private TaskStatus status;
    private List<String> tags;
    private List<String> references;
    private String markdownPath;
    private RecurrenceRule recurrenceRule;
    private ReminderSettings reminderSettings;
    private LocalDateTime updatedAt;

    public Task(Long id,
                String title,
                String summary,
                LocalDateTime dueDateTime,
                Priority priority,
                TaskStatus status,
                List<String> tags,
                List<String> references,
                String markdownPath,
                RecurrenceRule recurrenceRule,
                ReminderSettings reminderSettings,
                LocalDateTime updatedAt) {
        this.id = id;
        this.title = Objects.requireNonNull(title, "title is required").trim();
        this.summary = summary == null ? "" : summary.trim();
        this.dueDateTime = dueDateTime;
        this.priority = priority == null ? Priority.MEDIUM : priority;
        this.status = status == null ? TaskStatus.TODO : status;
        this.tags = tags == null ? new ArrayList<>() : tags;
        this.references = references == null ? new ArrayList<>() : references;
        this.markdownPath = Objects.requireNonNull(markdownPath, "markdownPath is required").trim();
        this.recurrenceRule = recurrenceRule == null ? RecurrenceRule.none() : recurrenceRule;
        this.reminderSettings = reminderSettings == null ? ReminderSettings.defaults() : reminderSettings;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
        validate();
    }

    public void validate() {
        if (title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (markdownPath.isBlank()) throw new IllegalArgumentException("Markdown file path is required");
        if (reminderSettings.minutesBeforeDue() < 0 || reminderSettings.overdueRepeatMinutes() < 1) {
            throw new IllegalArgumentException("Reminder settings are invalid");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public LocalDateTime getDueDateTime() { return dueDateTime; }
    public Priority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public List<String> getTags() { return tags; }
    public List<String> getReferences() { return references; }
    public String getMarkdownPath() { return markdownPath; }
    public RecurrenceRule getRecurrenceRule() { return recurrenceRule; }
    public ReminderSettings getReminderSettings() { return reminderSettings; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
