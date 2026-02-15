package com.tasknotifier.infrastructure;

import com.tasknotifier.domain.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLiteTaskRepositoryTest {

    @Test
    void supportsCrud() throws Exception {
        Path dbPath = Files.createTempFile("tasknotifier-test", ".db");
        DatabaseManager db = new DatabaseManager(dbPath);
        db.migrate();
        SQLiteTaskRepository repo = new SQLiteTaskRepository(db);

        Task saved = repo.save(new Task(null, "Persist", "", LocalDateTime.now(), Priority.LOW, TaskStatus.TODO,
                List.of("tag"), "task-detail/persist.md", RecurrenceRule.none(), ReminderSettings.defaults(), LocalDateTime.now()));
        assertNotNull(saved.getId());
        assertEquals(1, repo.findAll().size());

        repo.deleteById(saved.getId());
        assertTrue(repo.findAll().isEmpty());
    }
}
