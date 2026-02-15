package com.tasknotifier.application;

import com.tasknotifier.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReminderSchedulerTest {

    @Test
    void emitsReminderForDueTask() {
        List<String> messages = new ArrayList<>();
        TaskRepository repo = new InMemoryRepo();
        TaskService service = new TaskService(repo, new RecurrenceService());
        service.save(new Task(null, "Due soon", "", LocalDateTime.now().plusMinutes(5), Priority.MEDIUM, TaskStatus.TODO,
                List.of(), List.of(), "notes/t.md", RecurrenceRule.none(), new ReminderSettings(true, 10, 60, false), LocalDateTime.now()));
        ReminderScheduler scheduler = new ReminderScheduler(service, (title, message) -> messages.add(title + message));
        scheduler.check();
        assertEquals(1, messages.size());
    }

    private static class InMemoryRepo implements TaskRepository {
        List<Task> tasks = new ArrayList<>();
        long id = 1;
        public Task save(Task task) { if (task.getId() == null) task.setId(id++); tasks.removeIf(t -> t.getId().equals(task.getId())); tasks.add(task); return task; }
        public List<Task> findAll() { return tasks; }
        public java.util.Optional<Task> findById(long id) { return tasks.stream().filter(t -> t.getId() == id).findFirst(); }
        public void deleteById(long id) { tasks.removeIf(t -> t.getId() == id); }
    }
}
