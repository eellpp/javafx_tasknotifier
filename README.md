# JavaFX Task Notifier (v1)

A desktop offline task manager with reminders, recurrence, and linked markdown notes.

## Tech Stack
- Java 17
- Maven
- JavaFX (FXML + controller + ViewModel)
- Atlantafx theme
- SQLite (JDBC)
- JUnit 5

## Run
```bash
mvn clean javafx:run
```

## Build (fat JAR)
```bash
mvn clean package
java -jar target/javafx-task-notifier-1.0.0.jar
```

## Create Executable (Windows + macOS)

Use `jpackage` to generate native executables/installers.

Important:
- Build Windows installer on Windows.
- Build macOS installer on macOS.
- Install JDK 17+ (includes `jpackage`).

### 1) Build the app JAR first
```bash
mvn clean package
```

### 2) Generate Windows executable (run on Windows)
```powershell
jpackage ^
  --name TaskNotifier ^
  --app-version 1.0.0 ^
  --input target ^
  --main-jar javafx-task-notifier-1.0.0.jar ^
  --main-class com.tasknotifier.TaskNotifierApplication ^
  --type exe ^
  --dest dist
```

Optional:
- Use `--type msi` for MSI output.
- Add icon: `--icon path\\to\\app.ico`

### 3) Generate macOS executable (run on macOS)
```bash
jpackage \
  --name TaskNotifier \
  --app-version 1.0.0 \
  --input target \
  --main-jar javafx-task-notifier-1.0.0.jar \
  --main-class com.tasknotifier.TaskNotifierApplication \
  --type dmg \
  --dest dist
```

Optional:
- Add icon: `--icon path/to/app.icns`

Output artifacts are created in `dist/`.

## Test
```bash
mvn test
```

## Features Included in v1
- Task create/edit/delete with title, summary, due datetime, priority, status, tags, references, markdown path.
- Recurrence (`NONE`, `DAILY`, `WEEKLY`, `MONTHLY`) with optional end date.
- Skip next recurrence occurrence from table row context menu.
- Pending-centric dashboard counts (overdue / due today / upcoming).
- Filtering by status, priority, due-range, tag and search by title/summary/tag.
- Quick table columns for title, due, recurrence, tags, reference preview, markdown path.
- `Open File` action from row context menu.
- Urgent/high and overdue visual highlighting.
- Background reminder scheduler and in-app notifications.
- Windows tray notification attempt with in-app fallback.
- Master reminder toggle in UI.
- SQLite persistence with startup migration.
- Markdown note file auto-created when saving a task if missing.

## Keyboard Shortcuts
- `Ctrl+N`: clear form for a new task (button exposed).
- `Ctrl+S`: save task (button exposed).
- `Ctrl+F`: focus search (button exposed).
- `Ctrl+M`: mark selected task done/undone in table.

## Project Structure
- `com.tasknotifier.ui`: FXML/controller/viewmodel.
- `com.tasknotifier.application`: use-case/services/repository contracts.
- `com.tasknotifier.domain`: entities and value objects.
- `com.tasknotifier.infrastructure`: SQLite, migration, notifications.
- `db/`: SQL migration + seed scripts.

## Database + Migration
- Runtime DB path default: `data/tasknotifier.db`.
- App migration runs on startup via `DatabaseManager`.
- SQL snapshot: `db/migrations_v1.sql`.

## Seed Data
Use `db/seed_v1.sql` if you want starter records.

## Markdown storage/path handling rules
- Task metadata is stored in SQLite.
- Each task stores a markdown file path string in `tasks.markdown_path`.
- On save, app creates the file if missing and writes starter content.
- Absolute or relative paths are accepted; relative paths resolve from current working directory.
- If a markdown file was moved/deleted, the app keeps task metadata and does not crash; open action no-ops if missing.

## Notes on tests and exemptions
- Domain/application/infrastructure logic has concrete unit/integration tests.
- `MainController` is intentionally thin FXML wiring + delegation and is exempted from deep tests in v1.
