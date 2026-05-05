package com.jexam.app;

import com.jexam.app.ui.styling.UiTheme;

import java.io.File;
import java.nio.file.Path;
import java.util.prefs.Preferences;

/**
 * Stores lightweight user preferences across application sessions.
 */
final class UserPreferencesStore {
    private static final String KEY_LANGUAGE = "ui.language";
    private static final String KEY_LAST_TAB_INDEX = "ui.lastTabIndex";
    private static final String KEY_LAST_XML_DIR = "path.lastXmlDir";
    private static final String KEY_LAST_XML_FILE = "path.lastXmlFile";
    private static final String KEY_LAST_PDF_DIR = "path.lastPdfDir";
    private static final String KEY_UI_THEME = "ui.theme";

    private final Preferences preferences = Preferences.userNodeForPackage(UserPreferencesStore.class);

    UiLanguage loadLanguage() {
        String raw = preferences.get(KEY_LANGUAGE, UiLanguage.ENGLISH.name());
        try {
            return UiLanguage.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return UiLanguage.ENGLISH;
        }
    }

    void saveLanguage(final UiLanguage language) {
        UiLanguage safeLanguage = language == null ? UiLanguage.ENGLISH : language;
        preferences.put(KEY_LANGUAGE, safeLanguage.name());
    }

    UiTheme loadTheme() {
        String raw = preferences.get(KEY_UI_THEME, UiTheme.LIGHT.name());
        try {
            return UiTheme.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return UiTheme.LIGHT;
        }
    }

    void saveTheme(final UiTheme theme) {
        UiTheme safeTheme = theme == null ? UiTheme.LIGHT : theme;
        preferences.put(KEY_UI_THEME, safeTheme.name());
    }

    int loadLastTabIndex() {
        String raw = preferences.get(KEY_LAST_TAB_INDEX, "0");
        try {
            int index = Integer.parseInt(raw);
            return index < 0 ? 0 : index;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    void saveLastTabIndex(final int tabIndex) {
        preferences.put(KEY_LAST_TAB_INDEX, Integer.toString(Math.max(tabIndex, 0)));
    }

    Path loadLastXmlDirectory() {
        return loadDirectory(KEY_LAST_XML_DIR);
    }

    void saveLastXmlDirectory(final Path directory) {
        saveDirectory(KEY_LAST_XML_DIR, directory);
    }

    Path loadLastXmlFile() {
        String value = preferences.get(KEY_LAST_XML_FILE, null);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            Path path = Path.of(value);
            File file = path.toFile();
            return file.isFile() ? path : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    void saveLastXmlFile(final Path filePath) {
        if (filePath == null) {
            return;
        }
        try {
            File file = filePath.toFile();
            if (file.isFile()) {
                preferences.put(KEY_LAST_XML_FILE, filePath.toString());
            }
        } catch (RuntimeException ignored) {
            // Keep silent fallback behavior when persistence fails.
        }
    }

    Path loadLastPdfDirectory() {
        return loadDirectory(KEY_LAST_PDF_DIR);
    }

    void saveLastPdfDirectory(final Path directory) {
        saveDirectory(KEY_LAST_PDF_DIR, directory);
    }

    private Path loadDirectory(final String key) {
        String value = preferences.get(key, null);
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            Path path = Path.of(value);
            File file = path.toFile();
            return file.isDirectory() ? path : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private void saveDirectory(final String key, final Path directory) {
        if (directory == null) {
            return;
        }
        try {
            File file = directory.toFile();
            if (file.isDirectory()) {
                preferences.put(key, directory.toString());
            }
        } catch (RuntimeException ignored) {
            // Keep silent fallback behavior when persistence fails.
        }
    }
}