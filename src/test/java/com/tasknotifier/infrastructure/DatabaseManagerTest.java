package com.tasknotifier.infrastructure;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {

    @Test
    void migrateCreatesDbDirectoryAndSchema() throws Exception {
        Path tempRoot = Files.createTempDirectory("tasknotifier-db-init");
        Path dbPath = tempRoot.resolve("data").resolve("tasknotifier.db");

        DatabaseManager manager = new DatabaseManager(dbPath);
        manager.migrate();

        assertTrue(Files.exists(dbPath), "DB file should be created");

        try (Connection conn = manager.connection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='tasks'")) {
            assertTrue(rs.next(), "tasks table should exist after migration");
        }
    }
}
