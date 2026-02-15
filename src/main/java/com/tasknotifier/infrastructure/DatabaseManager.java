package com.tasknotifier.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final String jdbcUrl;
    private final Path dbPath;

    public DatabaseManager(Path dbPath) {
        this.dbPath = dbPath.toAbsolutePath();
        this.jdbcUrl = "jdbc:sqlite:" + this.dbPath;
    }

    public Connection connection() throws SQLException {
        ensureParentDirectoryExists();
        return DriverManager.getConnection(jdbcUrl);
    }

    public void migrate() {
        try (Connection conn = connection(); Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS schema_version(version INTEGER PRIMARY KEY);
                """);
            st.executeUpdate("""
                INSERT INTO schema_version(version)
                SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM schema_version);
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    summary TEXT,
                    due_date_time TEXT,
                    priority TEXT NOT NULL,
                    status TEXT NOT NULL,
                    tags TEXT,
                    markdown_path TEXT NOT NULL,
                    recurrence_type TEXT NOT NULL,
                    recurrence_end_date TEXT,
                    recurrence_skipped_dates TEXT,
                    reminder_enabled INTEGER NOT NULL,
                    reminder_minutes_before_due INTEGER NOT NULL,
                    reminder_overdue_repeat INTEGER NOT NULL,
                    reminder_sound_enabled INTEGER NOT NULL,
                    updated_at TEXT NOT NULL
                );
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                );
                """);
        } catch (SQLException e) {
            throw new IllegalStateException("Database unavailable or corrupt. Please verify the database file.", e);
        }
    }

    private void ensureParentDirectoryExists() {
        Path parent = dbPath.getParent();
        if (parent == null) return;
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create database directory: " + parent, e);
        }
    }
}
