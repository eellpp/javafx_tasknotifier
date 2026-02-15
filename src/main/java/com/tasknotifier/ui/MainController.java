package com.tasknotifier.ui;

import atlantafx.base.theme.*;
import com.tasknotifier.application.TaskFilter;
import com.tasknotifier.application.TaskService;
import com.tasknotifier.domain.*;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainController {

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> dueColumn;
    @FXML private TableColumn<Task, String> recurrenceColumn;
    @FXML private TableColumn<Task, String> tagsColumn;
    @FXML private TableColumn<Task, String> referenceColumn;
    @FXML private TableColumn<Task, String> markdownColumn;
    @FXML private TextField searchField;
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
    @FXML private TextField refsField;
    @FXML private TextField markdownPathField;
    @FXML private ComboBox<RecurrenceType> recurrenceBox;
    @FXML private DatePicker recurrenceEndDatePicker;
    @FXML private CheckBox reminderEnabledBox;
    @FXML private Spinner<Integer> reminderMinutesSpinner;
    @FXML private Spinner<Integer> overdueRepeatSpinner;
    @FXML private CheckBox reminderSoundBox;
    @FXML private CheckBox notificationsMasterToggle;

    private MainViewModel viewModel;
    private Long editingTaskId;
    private final Map<String, Theme> themes = buildThemes();
    private Path selectedMarkdownFolder = Path.of("notes");

    public void setViewModel(MainViewModel viewModel) {
        this.viewModel = viewModel;
        initializeView();
    }

    private void initializeView() {
        themeBox.setItems(FXCollections.observableArrayList(themes.keySet()));
        themeBox.setValue("Primer Light (Default)");
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
        referenceColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReferences().isEmpty() ? "" : c.getValue().getReferences().get(0)));
        markdownColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMarkdownPath()));

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
            markdownPathField.setText(buildMarkdownPathFromTitle(titleField.getText(), selectedMarkdownFolder).toString());
        }
        viewModel.saveTask(editingTaskId, titleField.getText(), summaryArea.getText(), dueDatePicker.getValue(), dueTimeField.getText(),
                priorityBox.getValue(), statusBox.getValue(), tagsField.getText(), refsField.getText(), markdownPathField.getText(),
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
        markdownPathField.setText(buildMarkdownPathFromTitle(titleField.getText(), selectedMarkdownFolder).toString());
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
        refsField.clear();
        markdownPathField.setText(buildMarkdownPathFromTitle("", selectedMarkdownFolder).toString());
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
        refsField.setText(String.join(",", task.getReferences()));
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

    private Path buildMarkdownPathFromTitle(String title, Path folder) {
        String cleaned = title == null ? "" : title.trim().toLowerCase();
        cleaned = cleaned.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (cleaned.isBlank()) cleaned = "task-" + System.currentTimeMillis();
        return folder.resolve(cleaned + ".md");
    }
}
