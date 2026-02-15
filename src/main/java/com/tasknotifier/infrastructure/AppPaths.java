package com.tasknotifier.infrastructure;

import java.nio.file.Path;

public final class AppPaths {

    private static final String APP_NAME = "TaskNotifier";

    private AppPaths() {
    }

    public static Path appHome() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String userHome = System.getProperty("user.home", ".");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            Path base = (appData == null || appData.isBlank()) ? Path.of(userHome, "AppData", "Roaming") : Path.of(appData);
            return base.resolve(APP_NAME);
        }

        if (os.contains("mac")) {
            return Path.of(userHome, "Library", "Application Support", APP_NAME);
        }

        return Path.of(userHome, "." + APP_NAME.toLowerCase());
    }

    public static Path databaseFile() {
        return appHome().resolve("tasknotifier.db");
    }

    public static Path taskDetailDirectory() {
        return appHome().resolve("task-detail");
    }
}
