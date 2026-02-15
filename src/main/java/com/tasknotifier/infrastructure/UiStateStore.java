package com.tasknotifier.infrastructure;

import java.nio.file.Path;
import java.util.prefs.Preferences;

public class UiStateStore {

    private static final String KEY_THEME = "ui.theme";
    private static final String KEY_NOTIFICATIONS_ENABLED = "ui.notifications.enabled";
    private static final String KEY_NOTES_FOLDER = "ui.notes.folder";
    private static final String KEY_WINDOW_MAXIMIZED = "ui.window.maximized";
    private static final String KEY_WINDOW_X = "ui.window.x";
    private static final String KEY_WINDOW_Y = "ui.window.y";
    private static final String KEY_WINDOW_W = "ui.window.w";
    private static final String KEY_WINDOW_H = "ui.window.h";

    private final Preferences prefs = Preferences.userNodeForPackage(UiStateStore.class);

    public UiState load(Path defaultNotesFolder) {
        String theme = prefs.get(KEY_THEME, "Primer Light (Default)");
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        Path notesFolder = Path.of(prefs.get(KEY_NOTES_FOLDER, defaultNotesFolder.toString()));
        boolean maximized = prefs.getBoolean(KEY_WINDOW_MAXIMIZED, false);
        double x = prefs.getDouble(KEY_WINDOW_X, -1);
        double y = prefs.getDouble(KEY_WINDOW_Y, -1);
        double w = prefs.getDouble(KEY_WINDOW_W, -1);
        double h = prefs.getDouble(KEY_WINDOW_H, -1);
        return new UiState(theme, notificationsEnabled, notesFolder, maximized, x, y, w, h);
    }

    public void save(UiState state) {
        prefs.put(KEY_THEME, state.theme());
        prefs.putBoolean(KEY_NOTIFICATIONS_ENABLED, state.notificationsEnabled());
        prefs.put(KEY_NOTES_FOLDER, state.notesFolder().toString());
        prefs.putBoolean(KEY_WINDOW_MAXIMIZED, state.windowMaximized());
        prefs.putDouble(KEY_WINDOW_X, state.windowX());
        prefs.putDouble(KEY_WINDOW_Y, state.windowY());
        prefs.putDouble(KEY_WINDOW_W, state.windowWidth());
        prefs.putDouble(KEY_WINDOW_H, state.windowHeight());
    }

    public record UiState(
            String theme,
            boolean notificationsEnabled,
            Path notesFolder,
            boolean windowMaximized,
            double windowX,
            double windowY,
            double windowWidth,
            double windowHeight
    ) {}
}
