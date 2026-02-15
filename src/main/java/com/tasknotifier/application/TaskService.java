package com.tasknotifier.application;

import com.tasknotifier.domain.Task;
import com.tasknotifier.domain.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskService {

    private final TaskRepository repository;
    private final RecurrenceService recurrenceService;

    public TaskService(TaskRepository repository, RecurrenceService recurrenceService) {
        this.repository = repository;
        this.recurrenceService = recurrenceService;
    }

    public Task save(Task task) {
        task.validate();
        ensureMarkdownFile(task);
        task.setUpdatedAt(LocalDateTime.now());
        return repository.save(task);
    }

    public List<Task> listAll() {
        return repository.findAll();
    }

    public void delete(long id) {
        repository.deleteById(id);
    }

    public void markDoneToggle(Task task) {
        task.setStatus(task.getStatus() == TaskStatus.DONE ? TaskStatus.TODO : TaskStatus.DONE);
        if (task.getStatus() == TaskStatus.DONE && task.getRecurrenceRule().isRecurring() && task.getDueDateTime() != null) {
            task = new Task(task.getId(), task.getTitle(), task.getSummary(),
                    recurrenceService.nextOccurrence(task.getDueDateTime(), task.getRecurrenceRule()),
                    task.getPriority(), TaskStatus.TODO, task.getTags(), task.getReferences(), task.getMarkdownPath(),
                    task.getRecurrenceRule(), task.getReminderSettings(), LocalDateTime.now());
        }
        save(task);
    }

    public DashboardSummary dashboardSummary() {
        LocalDate now = LocalDate.now();
        List<Task> pending = listAll().stream().filter(t -> t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.ARCHIVED).toList();
        long overdue = pending.stream().filter(t -> t.getDueDateTime() != null && t.getDueDateTime().toLocalDate().isBefore(now)).count();
        long today = pending.stream().filter(t -> t.getDueDateTime() != null && t.getDueDateTime().toLocalDate().isEqual(now)).count();
        long upcoming = pending.stream().filter(t -> t.getDueDateTime() != null && t.getDueDateTime().toLocalDate().isAfter(now)).count();
        return new DashboardSummary(overdue, today, upcoming);
    }

    private void ensureMarkdownFile(Task task) {
        try {
            Path path = Path.of(task.getMarkdownPath());
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            if (Files.notExists(path)) Files.writeString(path, "# " + task.getTitle() + System.lineSeparator() + task.getSummary());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create markdown file", e);
        }
    }

    public record DashboardSummary(long overdue, long dueToday, long upcoming) {}
}
