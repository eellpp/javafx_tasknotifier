package com.tasknotifier.infrastructure;

import com.tasknotifier.application.NotificationService;
import javafx.application.Platform;
import org.controlsfx.control.Notifications;

public class InAppNotificationService implements NotificationService {
    @Override
    public void notify(String title, String message) {
        Platform.runLater(() -> Notifications.create().title(title).text(message).showInformation());
    }
}
