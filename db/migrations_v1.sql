CREATE TABLE IF NOT EXISTS schema_version(version INTEGER PRIMARY KEY);
INSERT INTO schema_version(version)
SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM schema_version);

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    summary TEXT,
    due_date_time TEXT,
    priority TEXT NOT NULL,
    status TEXT NOT NULL,
    tags TEXT,
    references_text TEXT,
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

CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);
