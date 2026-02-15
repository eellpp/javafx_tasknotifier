package com.tasknotifier.application;

import com.tasknotifier.domain.Task;
import com.tasknotifier.domain.TaskStatus;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private final TaskService taskService;
    private final NotificationService notificationService;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Set<String> sentReminderKeys = new HashSet<>();
    private boolean enabled = true;

    public ReminderScheduler(TaskService taskService, NotificationService notificationService) {
        this.taskService = taskService;
        this.notificationService = notificationService;
    }

    public void start() {
        executorService.scheduleAtFixedRate(this::check, 2, 60, TimeUnit.SECONDS);
    }

    public void stop() {
        executorService.shutdownNow();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    void check() {
        if (!enabled) return;
        LocalDateTime now = LocalDateTime.now();
        for (Task task : taskService.listAll()) {
            if (!task.getReminderSettings().enabled() || task.getStatus() == TaskStatus.DONE || task.getDueDateTime() == null) continue;
            LocalDateTime remindAt = task.getDueDateTime().minusMinutes(task.getReminderSettings().minutesBeforeDue());
            boolean overdue = task.getDueDateTime().isBefore(now);
            if (now.isAfter(remindAt)) {
                String key = task.getId() + ":" + (overdue ? "overdue" : remindAt.toLocalDate().toString());
                if (sentReminderKeys.add(key)) {
                    notificationService.notify(overdue ? "Task overdue" : "Task reminder", task.getTitle());
                }
            }
        }
    }
}
