package com.tasknotifier;

import atlantafx.base.theme.PrimerLight;
import com.tasknotifier.application.*;
import com.tasknotifier.infrastructure.*;
import com.tasknotifier.ui.MainController;
import com.tasknotifier.ui.MainViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.nio.file.Files;
import java.nio.file.Path;

public class TaskNotifierApplication extends Application {

    private ReminderScheduler scheduler;

    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        Path taskDetailDir = AppPaths.taskDetailDirectory();
        Files.createDirectories(taskDetailDir);
        UiStateStore uiStateStore = new UiStateStore();
        UiStateStore.UiState savedUiState = uiStateStore.load(taskDetailDir);

        DatabaseManager db = new DatabaseManager(AppPaths.databaseFile());
        db.migrate();
        TaskRepository repository = new SQLiteTaskRepository(db);
        RecurrenceService recurrenceService = new RecurrenceService();
        TaskService taskService = new TaskService(repository, recurrenceService);
        NotificationService inApp = new InAppNotificationService();
        NotificationService notificationService = new WindowsTrayNotificationBridge(inApp);
        scheduler = new ReminderScheduler(taskService, notificationService);
        scheduler.start();

        MainViewModel viewModel = new MainViewModel(taskService, recurrenceService, scheduler);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main-view.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.setStage(stage);
        controller.setDefaultMarkdownFolder(taskDetailDir);
        controller.setViewModel(viewModel);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Task Notifier v1");
        stage.setScene(new Scene(root, 1280, 760));
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        controller.applyUiState(savedUiState);
        stage.setOnCloseRequest(event -> uiStateStore.save(controller.captureUiState()));
        stage.show();
    }

    @Override
    public void stop() {
        if (scheduler != null) scheduler.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
