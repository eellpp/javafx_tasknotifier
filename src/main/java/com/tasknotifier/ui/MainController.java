package com.tasknotifier.ui;

import atlantafx.base.theme.*;
import com.tasknotifier.application.TaskFilter;
import com.tasknotifier.application.TaskService;
import com.tasknotifier.domain.*;
import com.tasknotifier.infrastructure.UiStateStore;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyCode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Rectangle2D;

public class MainController {

    @FXML private HBox windowBar;
    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> dueColumn;
    @FXML private TableColumn<Task, String> recurrenceColumn;
    @FXML private TableColumn<Task, String> tagsColumn;
    @FXML private TableColumn<Task, String> markdownColumn;
    @FXML private TextField searchField;
    @FXML private Button newTaskButton;
    @FXML private Button saveTaskButton;
    @FXML private Button focusSearchButton;
    @FXML private ComboBox<String> themeBox;
    @FXML private ComboBox<TaskStatus> statusFilter;
    @FXML private ComboBox<Priority> priorityFilter;
    @FXML private ComboBox<TaskFilter.DueRange> dueRangeFilter;
    @FXML private TextField tagFilter;

    @FXML private Label overdueCountLabel;
    @FXML private Label dueTodayCountLabel;
    @FXML private Label upcomingCountLabel;

    @FXML private TextField titleField;
    @FXML private TextArea summaryArea;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField dueTimeField;
    @FXML private ComboBox<Priority> priorityBox;
    @FXML private ComboBox<TaskStatus> statusBox;
    @FXML private TextField tagsField;
    @FXML private TextField markdownPathField;
    @FXML private ComboBox<RecurrenceType> recurrenceBox;
    @FXML private DatePicker recurrenceEndDatePicker;
    @FXML private CheckBox reminderEnabledBox;
    @FXML private Spinner<Integer> reminderMinutesSpinner;
    @FXML private Spinner<Integer> overdueRepeatSpinner;
    @FXML private CheckBox reminderSoundBox;
    @FXML private CheckBox notificationsMasterToggle;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button closeButton;

    private MainViewModel viewModel;
    private Long editingTaskId;
    private final Map<String, Theme> themes = buildThemes();
    private Path selectedMarkdownFolder = Path.of("task-detail");
    private Stage stage;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean windowMaximized;
    private double restoreX;
    private double restoreY;
    private double restoreWidth;
    private double restoreHeight;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDefaultMarkdownFolder(Path folder) {
        if (folder == null) return;
        selectedMarkdownFolder = folder;
    }

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        initializeView();
    }

    private void initializeView() {
        themeBox.setItems(FXCollections.observableArrayList(themes.keySet()));
        themeBox.setValue("Primer Light (Default)");
        newTaskButton.getStyleClass().add(Styles.ACCENT);
        saveTaskButton.getStyleClass().add(Styles.ACCENT);
        focusSearchButton.getStyleClass().add(Styles.ACCENT);
        minimizeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        maximizeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT);
        closeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.DANGER);
        statusFilter.setItems(FXCollections.observableArrayList(TaskStatus.values()));
        priorityFilter.setItems(FXCollections.observableArrayList(Priority.values()));
        dueRangeFilter.setItems(FXCollections.observableArrayList(TaskFilter.DueRange.values()));
        dueRangeFilter.setValue(TaskFilter.DueRange.ALL);

        priorityBox.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityBox.setValue(Priority.MEDIUM);
        statusBox.setItems(FXCollections.observableArrayList(TaskStatus.values()));
        statusBox.setValue(TaskStatus.TODO);
        recurrenceBox.setItems(FXCollections.observableArrayList(RecurrenceType.values()));
        recurrenceBox.setValue(RecurrenceType.NONE);

        reminderMinutesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 30));
        overdueRepeatSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 60));
        reminderEnabledBox.setSelected(true);

        titleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        dueColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDueDateTime() == null ? "" : c.getValue().getDueDateTime().toString()));
        recurrenceColumn.setCellValueFactory(c -> new SimpleStringProperty(viewModel.recurrenceSummary(c.getValue())));
        tagsColumn.setCellValueFactory(c -> new SimpleStringProperty(String.join(",", c.getValue().getTags())));
        markdownColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMarkdownPath()));
        markdownColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setTooltip(null);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    return;
                }

                setText(Path.of(item).getFileName().toString());
                Tooltip preview = new Tooltip(readMarkdownPreview(item));
                preview.setWrapText(true);
                preview.setMaxWidth(540);
                setTooltip(preview);
            }
        });

        taskTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("overdue-row", "urgent-row");
                if (empty || item == null || item.getDueDateTime() == null) return;
                if (item.getDueDateTime().toLocalDate().isBefore(LocalDate.now()) && item.getStatus() != TaskStatus.DONE) {
                    getStyleClass().add("overdue-row");
                } else if (item.getPriority() == Priority.HIGH) {
                    getStyleClass().add("urgent-row");
                }
            }
        });

        taskTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) populateForm(taskTable.getSelectionModel().getSelectedItem());
        });

        taskTable.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.M && e.isShortcutDown()) viewModel.toggleDone(taskTable.getSelectionModel().getSelectedItem());
        });

        ContextMenu menu = new ContextMenu();
        MenuItem openFile = new MenuItem("Open File");
        openFile.setOnAction(e -> viewModel.openMarkdown(taskTable.getSelectionModel().getSelectedItem()));
        MenuItem markDone = new MenuItem("Mark Done/Undone");
        markDone.setOnAction(e -> { viewModel.toggleDone(taskTable.getSelectionModel().getSelectedItem()); applyFilters(); updateDashboard(); });
        MenuItem skipOccurrence = new MenuItem("Skip Next Occurrence");
        skipOccurrence.setOnAction(e -> { viewModel.skipNextOccurrence(taskTable.getSelectionModel().getSelectedItem()); applyFilters(); });
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction(e -> { viewModel.delete(taskTable.getSelectionModel().getSelectedItem()); applyFilters(); updateDashboard(); });
        menu.getItems().addAll(openFile, markDone, skipOccurrence, delete);
        taskTable.setContextMenu(menu);

        applyTheme();
        applyFilters();
        updateDashboard();
    }

    @FXML
    public void applyTheme() {
        Theme theme = themes.getOrDefault(themeBox.getValue(), new PrimerLight());
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
    }

    @FXML
    public void applyFilters() {
        taskTable.setItems(FXCollections.observableArrayList(viewModel.filtered(searchField.getText(), statusFilter.getValue(), priorityFilter.getValue(), dueRangeFilter.getValue(), tagFilter.getText())));
    }

    @FXML
    public void saveTask() {
        if (markdownPathField.getText() == null || markdownPathField.getText().isBlank()) {
            markdownPathField.setText(buildDefaultMarkdownPath(selectedMarkdownFolder).toString());
        }
        viewModel.saveTask(editingTaskId, titleField.getText(), summaryArea.getText(), dueDatePicker.getValue(), dueTimeField.getText(),
                priorityBox.getValue(), statusBox.getValue(), tagsField.getText(), markdownPathField.getText(),
                recurrenceBox.getValue(), recurrenceEndDatePicker.getValue(), reminderEnabledBox.isSelected(),
                reminderMinutesSpinner.getValue(), overdueRepeatSpinner.getValue(), reminderSoundBox.isSelected());
        clearForm();
        applyFilters();
        updateDashboard();
    }

    @FXML
    public void newTask() {
        clearForm();
    }

    @FXML
    public void setNotificationMaster() {
        viewModel.setNotificationsEnabled(notificationsMasterToggle.isSelected());
    }

    @FXML
    public void focusSearch() {
        searchField.requestFocus();
    }

    @FXML
    public void chooseMarkdownFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Markdown Folder");
        chooser.setInitialDirectory(selectedMarkdownFolder.toFile().exists() ? selectedMarkdownFolder.toFile() : Path.of(".").toAbsolutePath().normalize().toFile());
        Stage stage = (Stage) taskTable.getScene().getWindow();
        var dir = chooser.showDialog(stage);
        if (dir == null) return;
        selectedMarkdownFolder = dir.toPath();
        markdownPathField.setText(buildDefaultMarkdownPath(selectedMarkdownFolder).toString());
    }

    @FXML
    public void onWindowBarPressed(javafx.scene.input.MouseEvent event) {
        if (stage == null) return;
        dragOffsetX = event.getSceneX();
        dragOffsetY = event.getSceneY();
    }

    @FXML
    public void onWindowBarDragged(javafx.scene.input.MouseEvent event) {
        if (stage == null || windowMaximized) return;
        stage.setX(event.getScreenX() - dragOffsetX);
        stage.setY(event.getScreenY() - dragOffsetY);
    }

    @FXML
    public void minimizeWindow() {
        if (stage != null) stage.setIconified(true);
    }

    @FXML
    public void maximizeWindow() {
        if (stage == null) return;
        if (!windowMaximized) {
            restoreX = stage.getX();
            restoreY = stage.getY();
            restoreWidth = stage.getWidth();
            restoreHeight = stage.getHeight();
            Rectangle2D bounds = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                    .stream()
                    .findFirst()
                    .orElse(Screen.getPrimary())
                    .getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            windowMaximized = true;
            return;
        }

        stage.setX(restoreX);
        stage.setY(restoreY);
        stage.setWidth(restoreWidth);
        stage.setHeight(restoreHeight);
        windowMaximized = false;
    }

    @FXML
    public void closeWindow() {
        if (stage != null) stage.close();
    }

    public void applyUiState(UiStateStore.UiState state) {
        if (state == null) return;
        if (state.theme() != null && themes.containsKey(state.theme())) {
            themeBox.setValue(state.theme());
        }
        applyTheme();

        selectedMarkdownFolder = state.notesFolder() == null ? selectedMarkdownFolder : state.notesFolder();
        notificationsMasterToggle.setSelected(state.notificationsEnabled());
        viewModel.setNotificationsEnabled(state.notificationsEnabled());

        if (stage != null) {
            if (state.windowWidth() > 0 && state.windowHeight() > 0) {
                stage.setWidth(Math.max(state.windowWidth(), stage.getMinWidth()));
                stage.setHeight(Math.max(state.windowHeight(), stage.getMinHeight()));
            }
            if (state.windowX() >= 0 && state.windowY() >= 0) {
                stage.setX(state.windowX());
                stage.setY(state.windowY());
                if (!isWindowVisibleOnAnyScreen(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())) {
                    centerStageOnPrimaryScreen();
                }
            } else {
                centerStageOnPrimaryScreen();
            }
            if (state.windowMaximized()) {
                maximizeWindow();
            }
        }
        clearForm();
    }

    public UiStateStore.UiState captureUiState() {
        double x = stage == null ? -1 : stage.getX();
        double y = stage == null ? -1 : stage.getY();
        double w = stage == null ? -1 : stage.getWidth();
        double h = stage == null ? -1 : stage.getHeight();
        if (windowMaximized) {
            x = restoreX;
            y = restoreY;
            w = restoreWidth;
            h = restoreHeight;
        }
        return new UiStateStore.UiState(
                themeBox.getValue(),
                notificationsMasterToggle.isSelected(),
                selectedMarkdownFolder,
                windowMaximized,
                x, y, w, h
        );
    }

    private void clearForm() {
        editingTaskId = null;
        titleField.clear();
        summaryArea.clear();
        dueDatePicker.setValue(null);
        dueTimeField.setText("09:00");
        priorityBox.setValue(Priority.MEDIUM);
        statusBox.setValue(TaskStatus.TODO);
        tagsField.clear();
        markdownPathField.setText(buildDefaultMarkdownPath(selectedMarkdownFolder).toString());
        recurrenceBox.setValue(RecurrenceType.NONE);
        recurrenceEndDatePicker.setValue(null);
    }

    private void populateForm(Task task) {
        if (task == null) return;
        editingTaskId = task.getId();
        titleField.setText(task.getTitle());
        summaryArea.setText(task.getSummary());
        dueDatePicker.setValue(task.getDueDateTime() == null ? null : task.getDueDateTime().toLocalDate());
        dueTimeField.setText(task.getDueDateTime() == null ? "09:00" : task.getDueDateTime().toLocalTime().toString());
        priorityBox.setValue(task.getPriority());
        statusBox.setValue(task.getStatus());
        tagsField.setText(String.join(",", task.getTags()));
        markdownPathField.setText(task.getMarkdownPath());
        recurrenceBox.setValue(task.getRecurrenceRule().type());
        recurrenceEndDatePicker.setValue(task.getRecurrenceRule().endDate());
    }

    private void updateDashboard() {
        TaskService.DashboardSummary s = viewModel.dashboardSummary();
        overdueCountLabel.setText(String.valueOf(s.overdue()));
        dueTodayCountLabel.setText(String.valueOf(s.dueToday()));
        upcomingCountLabel.setText(String.valueOf(s.upcoming()));
    }

    private Map<String, Theme> buildThemes() {
        Map<String, Theme> available = new LinkedHashMap<>();
        available.put("Primer Light (Default)", new PrimerLight());
        available.put("Primer Dark", new PrimerDark());
        available.put("Cupertino Light", new CupertinoLight());
        available.put("Cupertino Dark", new CupertinoDark());
        available.put("Nord Light", new NordLight());
        available.put("Nord Dark", new NordDark());
        available.put("Dracula", new Dracula());
        return available;
    }

    private Path buildDefaultMarkdownPath(Path folder) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-hhmmss"));
        return folder.resolve(timestamp + ".md");
    }

    private boolean isWindowVisibleOnAnyScreen(double x, double y, double w, double h) {
        return Screen.getScreens().stream()
                .map(Screen::getVisualBounds)
                .anyMatch(bounds -> bounds.intersects(x, y, w, h));
    }

    private void centerStageOnPrimaryScreen() {
        if (stage == null) return;
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double x = bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2;
        double y = bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2;
        stage.setX(Math.max(bounds.getMinX(), x));
        stage.setY(Math.max(bounds.getMinY(), y));
    }

    private String readMarkdownPreview(String pathValue) {
        try {
            Path path = Path.of(pathValue);
            if (Files.notExists(path)) return "File not found:\n" + path;
            String content = Files.readString(path);
            if (content.length() > 2000) {
                return content.substring(0, 2000) + "\n\n...";
            }
            return content.isBlank() ? "(Empty file)" : content;
        } catch (IOException | RuntimeException e) {
            return "Unable to read file preview.";
        }
    }
}
