# Project Requirements v2: JavaFX Task Notifier

## 1. Project Overview

Build a desktop Task Manager and Alert Notification application using `Java 17` and `JavaFX`, focused on:

- Fast task capture
- Clear task tracking and status changes
- Reliable reminders/alerts
- Modern, clean, easily themeable UI
- Maintainable architecture for long-term evolution

This document is an implementation contract for coding agents. Decisions in this version are locked unless explicitly changed by product input.

## 2. Core Goals

- Deliver a production-like desktop app that runs fully offline.
- Use modern JavaFX UI patterns and styling (prefer `Atlantafx` theme system).
- Keep code modular and maintainable so screens and themes can be changed quickly.
- Persist data in a local file-based embedded database.
- Run on both macOS and Windows in v1.

## 3. Non-Goals (v1)

- Multi-user collaboration
- Cloud sync
- Mobile/web client
- Complex workflow automation
- App-level PIN/password lock (deferred, but architecture must support adding it later)

## 4. Target Users

- Individual users managing personal or work tasks on a desktop machine.
- Users who need visible reminders and daily task planning.

## 5. Functional Requirements

### 5.1 Task Management

- Create task with:
  - Title (required)
  - Summary/quick description (optional, short text for table/list display)
  - Due date/time (optional)
  - Priority (`LOW`, `MEDIUM`, `HIGH`)
  - Status (`TODO`, `IN_PROGRESS`, `DONE`, `ARCHIVED`)
  - Tags (optional, multiple)
  - References (optional, multiple text entries), including:
    - Email reference text (for example, an email subject line)
    - Useful links/URLs
  - Markdown task detail file path (required at save time)
- Edit existing task fields.
- Delete task using hard-delete in v1.
- Mark tasks as done/undone quickly from list view.
- Architecture requirement: keep service/repository abstraction extendable for future soft-delete support.

### 5.2 Recurring Tasks (Required in v1)

- Support recurring tasks with:
  - Daily recurrence
  - Weekly recurrence
  - Monthly recurrence
- End condition options:
  - Never ends
  - Ends on date
- Allow skip single occurrence.
- Show next occurrence in task detail/list where relevant.

### 5.3 Task Views & Navigation

- Dashboard/home view with:
  - Overdue tasks count
  - Due today count
  - Upcoming count
- Dedicated pending sections/views:
  - `Pending Today`
  - `Pending This Week`
  - `All Pending`
- Task list view with filters:
  - By status
  - By priority
  - By due range (today/week/custom)
  - By tag
- Search tasks by title/description/tag.
- Sort by due date, priority, last updated.
- Main task table/list must provide quick columns for:
  - Task name/title
  - Due date
  - Recurrence summary
  - Tags (for example: `project`, `work`, `personal`)
  - Reference preview (email subject and/or first URL)
  - Link/path to markdown file
- Provide an explicit `Open File` action from task detail and list row context menu.
- v1 UI does not need to render markdown content inside the app.
- Include urgency indicators in list/cards:
  - Urgent tasks must be visually distinct (for example red text, red badge, or red row accent)
  - Overdue tasks must have stronger urgency styling than merely high-priority tasks
  - Urgency styling must remain readable in both light and dark themes

### 5.4 Notifications & Alerts

- Reminder scheduler checks due/overdue tasks in background.
- Mandatory baseline on macOS and Windows:
  - In-app alert/toast notifications
- Windows target:
  - Attempt system tray notifications
  - If unsupported/unavailable, gracefully fall back to in-app notifications
- Configurable reminder settings:
  - Master notifications toggle (`ON`/`OFF`) editable from UI settings
  - Task-level reminder toggle (`ON`/`OFF`) editable when creating/updating a task
  - Minutes before due time
  - Repeat interval for overdue tasks
  - Enable/disable sound

### 5.5 Settings

- Theme selection (light/dark + accent preset).
- Notification preferences.
- Default new-task values (priority/status/reminder).
- Recurrence defaults for new recurring tasks.
- Database file location (optional advanced setting).

### 5.6 Data Persistence

- Use `SQLite` as default embedded DB (file-based).
- DB schema versioning via migration mechanism.
- Data survives app restarts.
- Handle DB file corruption/open failures with user-friendly recovery message.
- Store quick task metadata in SQLite (title, dates, status, priority, tags, recurrence, references, markdown file path).
- Save full task detail/content in a markdown (`.md`) file per task.
- Markdown file content is free-form user text.
- Persist and manage file links safely:
  - Use absolute or workspace-relative path strategy consistently.
  - If file is missing/moved, show a non-blocking warning and allow relink.

## 6. UI/UX Requirements

### 6.1 UI Stack (Locked)

- `JavaFX` with `FXML + MVVM`.
- Use `Atlantafx` as the default modern theme framework.
- May use `ControlsFX` for modern controls if needed.

### 6.2 UX Quality

- Responsive desktop layout (usable at small and large window sizes).
- Consistent spacing, typography scale, and color tokens.
- Provide clear visual hierarchy for urgency and due-date proximity.
- UI must make adding, searching, and viewing tasks very easy in v1:
  - Add task flow should require minimal clicks and have sensible defaults
  - Search should be prominently placed and responsive for quick filtering
  - Viewing task details should be one-click from list/table with clear metadata layout
- Keyboard shortcuts for common actions:
  - New task
  - Save task
  - Search focus
  - Mark done
- Clear empty states and validation messages.

### 6.3 Themeability

- Centralized theme tokens/colors.
- Avoid hardcoded style values in controllers.
- Easy to add/replace themes without changing business logic.

## 7. Architecture & Maintainability

### 7.1 Architectural Style

- Use layered architecture with clear boundaries:
  - `ui` (fxml/controllers/viewmodels)
  - `application` (use cases/services)
  - `domain` (entities, value objects, rules)
  - `infrastructure` (DB, repositories, scheduler, notifications)

### 7.2 Design Patterns

- Use `MVVM` for UI state separation.
- Dependency injection via lightweight approach (manual wiring acceptable in v1).
- Repository pattern for persistence abstraction.
- Service interfaces for notification and scheduler components.
- UI controllers should remain thin and delegate state/logic to ViewModels/services.

### 7.3 Code Quality Rules

- Single responsibility per class.
- Avoid god controllers and direct SQL from UI layer.
- Centralized constants/config.
- Clear package structure and naming conventions.
- Javadoc only for non-obvious public contracts.

## 8. Technology Requirements

- Java: `17`
- Build tool: `Maven` (locked)
- UI: `JavaFX` + `Atlantafx`
- DB: `SQLite` (via JDBC driver)
- Testing:
  - `JUnit 5` for unit tests
  - Optional `TestFX` for UI interaction tests
- Logging: `SLF4J + Logback`

## 9. Performance & Reliability

- App startup target: under 3 seconds on a normal developer machine.
- UI actions (open/edit/search in moderate dataset) should feel immediate.
- Background reminders must not freeze UI thread.
- No data loss on normal close.

## 10. Security & Privacy

- Data stored locally only by default.
- No telemetry in v1.
- Basic validation/sanitization for user input fields.
- App lock not required in v1, but architecture must allow future extension.

## 11. Testing Requirements

- Unit tests for:
  - Task creation/update validation
  - Filter/search logic
  - Reminder scheduling logic
  - Recurrence generation and next-occurrence calculations
  - Repository CRUD behavior
- Integration tests for DB layer with test database file.
- Minimum coverage target: `50%` for domain + application layers in v1.
- Testability requirement: design classes so logic is injectable and deterministic.
- For each production class, add a corresponding test class that demonstrates how that class is tested.
- If a class is a thin UI/FXML wiring class with no direct logic, add at least a smoke test or a clearly documented test exemption.

## 12. Deliverables

- Source code with documented structure.
- `README.md` with run/build/test instructions.
- DB migration scripts.
- Sample seed data script or bootstrap option.
- Basic user guide section in README.
- Fat runnable JAR packaging as first release artifact.
- Documentation for task markdown-file storage layout and file-path handling rules.

## 13. Definition of Done (DoD)

- All core requirements in sections 5.1-5.5 implemented.
- No critical runtime exceptions in normal user flows.
- Tests pass in CI/local.
- Code follows defined architecture boundaries.
- UI uses modern theme framework and can switch theme at runtime or app restart.
- Recurring tasks are fully functional in create/edit/schedule flows.
- App runs on both macOS and Windows.
- Windows tray support attempted with documented fallback behavior if unavailable.
- Task metadata appears in list/table quick columns (title, due, recurrence, tags, references, file link).
- Each task has a linked markdown file containing free-form details, and users can open that file from the UI.
- `Pending Today`, `Pending This Week`, and `All Pending` sections are implemented and usable.
- Urgent/overdue tasks are visually highlighted with clear red-based urgency indicators.
- Notifications can be edited as `ON`/`OFF` from the UI, and add/search/view flows are demonstrably low-friction.

## 14. Suggested Initial Backlog (Implementation Order)

1. Project bootstrap, dependencies, module structure.
2. Domain model and repository interfaces.
3. SQLite schema + migration + repository implementation.
4. Task CRUD UI and validation.
5. Recurrence model + scheduling rules.
6. Filters/search/dashboard summaries.
7. Reminder scheduler + notification service.
8. Settings + theming.
9. Testing hardening + fat JAR packaging.

## 15. Locked Product Decisions

1. Scope: single-user offline v1.
2. Delete policy: hard-delete in v1; architecture extendable for soft-delete later.
3. Recurring tasks: included in v1.
4. UI approach: FXML + MVVM.
5. Platforms: macOS + Windows in v1.
6. Notifications: mandatory in-app on both platforms; attempt Windows system tray support with fallback.
7. Theme framework: Atlantafx approved.
8. Packaging: fat runnable JAR first.
9. Privacy lock: not in v1; keep architecture extendable.
10. Testing bar: lower initial coverage target with strong emphasis on testable design and per-class test examples.
