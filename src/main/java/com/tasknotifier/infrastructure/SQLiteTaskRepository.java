package com.tasknotifier.infrastructure;

import com.tasknotifier.application.TaskRepository;
import com.tasknotifier.domain.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class SQLiteTaskRepository implements TaskRepository {

    private final DatabaseManager db;

    public SQLiteTaskRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            return insert(task);
        }
        return update(task);
    }

    private Task insert(Task task) {
        String sql = """
                INSERT INTO tasks(title, summary, due_date_time, priority, status, tags, markdown_path,
                recurrence_type, recurrence_end_date, recurrence_skipped_dates, reminder_enabled, reminder_minutes_before_due,
                reminder_overdue_repeat, reminder_sound_enabled, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = db.connection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, task);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) task.setId(rs.getLong(1));
            }
            return task;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Task update(Task task) {
        String sql = """
                UPDATE tasks SET title=?, summary=?, due_date_time=?, priority=?, status=?, tags=?, markdown_path=?,
                recurrence_type=?, recurrence_end_date=?, recurrence_skipped_dates=?, reminder_enabled=?, reminder_minutes_before_due=?,
                reminder_overdue_repeat=?, reminder_sound_enabled=?, updated_at=? WHERE id=?
                """;
        try (Connection conn = db.connection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, task);
            ps.setLong(16, task.getId());
            ps.executeUpdate();
            return task;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void bind(PreparedStatement ps, Task t) throws SQLException {
        ps.setString(1, t.getTitle());
        ps.setString(2, t.getSummary());
        ps.setString(3, t.getDueDateTime() == null ? null : t.getDueDateTime().toString());
        ps.setString(4, t.getPriority().name());
        ps.setString(5, t.getStatus().name());
        ps.setString(6, String.join("|", t.getTags()));
        ps.setString(7, t.getMarkdownPath());
        ps.setString(8, t.getRecurrenceRule().type().name());
        ps.setString(9, t.getRecurrenceRule().endDate() == null ? null : t.getRecurrenceRule().endDate().toString());
        ps.setString(10, t.getRecurrenceRule().skippedDates().stream().map(LocalDate::toString).collect(Collectors.joining("|")));
        ps.setInt(11, t.getReminderSettings().enabled() ? 1 : 0);
        ps.setInt(12, t.getReminderSettings().minutesBeforeDue());
        ps.setInt(13, t.getReminderSettings().overdueRepeatMinutes());
        ps.setInt(14, t.getReminderSettings().soundEnabled() ? 1 : 0);
        ps.setString(15, t.getUpdatedAt().toString());
    }

    @Override
    public List<Task> findAll() {
        try (Connection conn = db.connection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM tasks"); ResultSet rs = ps.executeQuery()) {
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) tasks.add(map(rs));
            return tasks;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<Task> findById(long id) {
        try (Connection conn = db.connection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM tasks WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void deleteById(long id) {
        try (Connection conn = db.connection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Task map(ResultSet rs) throws SQLException {
        RecurrenceRule rule = new RecurrenceRule(
                RecurrenceType.valueOf(rs.getString("recurrence_type")),
                rs.getString("recurrence_end_date") == null ? null : LocalDate.parse(rs.getString("recurrence_end_date")),
                split(rs.getString("recurrence_skipped_dates")).stream().map(LocalDate::parse).collect(Collectors.toSet())
        );
        ReminderSettings reminder = new ReminderSettings(
                rs.getInt("reminder_enabled") == 1,
                rs.getInt("reminder_minutes_before_due"),
                rs.getInt("reminder_overdue_repeat"),
                rs.getInt("reminder_sound_enabled") == 1
        );
        return new Task(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("summary"),
                rs.getString("due_date_time") == null ? null : LocalDateTime.parse(rs.getString("due_date_time")),
                Priority.valueOf(rs.getString("priority")),
                TaskStatus.valueOf(rs.getString("status")),
                split(rs.getString("tags")),
                rs.getString("markdown_path"),
                rule,
                reminder,
                LocalDateTime.parse(rs.getString("updated_at"))
        );
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return Arrays.stream(value.split("\\\\|"))
                .filter(v -> !v.isBlank())
                .toList();
    }
}
