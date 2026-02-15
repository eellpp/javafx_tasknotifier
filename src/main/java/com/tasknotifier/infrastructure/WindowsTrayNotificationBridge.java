package com.tasknotifier.infrastructure;

import com.tasknotifier.application.NotificationService;

import java.awt.*;

public class WindowsTrayNotificationBridge implements NotificationService {

    private final NotificationService fallback;
    private TrayIcon trayIcon;

    public WindowsTrayNotificationBridge(NotificationService fallback) {
        this.fallback = fallback;
        init();
    }

    private void init() {
        try {
            if (!System.getProperty("os.name").toLowerCase().contains("win") || !SystemTray.isSupported()) return;
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(new byte[0]);
            trayIcon = new TrayIcon(image, "Task Notifier");
            tray.add(trayIcon);
        } catch (Exception ignored) {
            trayIcon = null;
        }
    }

    @Override
    public void notify(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } else {
            fallback.notify(title, message);
        }
    }
}
