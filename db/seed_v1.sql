INSERT INTO tasks(title, summary, due_date_time, priority, status, tags, references_text, markdown_path,
                  recurrence_type, recurrence_end_date, recurrence_skipped_dates, reminder_enabled,
                  reminder_minutes_before_due, reminder_overdue_repeat, reminder_sound_enabled, updated_at)
VALUES
('Plan weekly review', 'Prepare agenda for team sync', '2030-01-14T10:00', 'HIGH', 'TODO', 'work,planning',
 'Weekly agenda doc|https://example.com/meeting-notes', 'notes/weekly-review.md', 'WEEKLY', null, '', 1, 60, 120, 0, '2030-01-10T09:00');
