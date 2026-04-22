package com.jexam.app;

import com.jexam.app.ui.styling.UiTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserPreferencesStoreTest {
    private static final String KEY_LANGUAGE = "ui.language";
    private static final String KEY_LAST_XML_DIR = "path.lastXmlDir";
    private static final String KEY_LAST_XML_FILE = "path.lastXmlFile";
    private static final String KEY_LAST_PDF_DIR = "path.lastPdfDir";
    private static final String KEY_UI_THEME = "ui.theme";

    private final Preferences preferences = Preferences.userNodeForPackage(UserPreferencesStore.class);

    @BeforeEach
    void setUp() {
        clearPreferenceKeys();
    }

    @AfterEach
    void tearDown() {
        clearPreferenceKeys();
    }

    @Test
    void languageShouldFallbackToEnglishForInvalidOrNullValues() {
        UserPreferencesStore store = new UserPreferencesStore();

        preferences.put(KEY_LANGUAGE, "INVALID_LANG");
        assertEquals(UiLanguage.ENGLISH, store.loadLanguage());

        store.saveLanguage(null);
        assertEquals(UiLanguage.ENGLISH, store.loadLanguage());

        store.saveLanguage(UiLanguage.GERMAN);
        assertEquals(UiLanguage.GERMAN, store.loadLanguage());
    }

    @Test
    void themeShouldFallbackToLightForInvalidOrNullValues() {
        UserPreferencesStore store = new UserPreferencesStore();

        preferences.put(KEY_UI_THEME, "NOT_A_THEME");
        assertEquals(UiTheme.LIGHT, store.loadTheme());

        store.saveTheme(null);
        assertEquals(UiTheme.LIGHT, store.loadTheme());

        store.saveTheme(UiTheme.DARK);
        assertEquals(UiTheme.DARK, store.loadTheme());
    }

    @Test
    void xmlDirectoryShouldPersistOnlyValidDirectories(@TempDir Path tempDir) throws Exception {
        UserPreferencesStore store = new UserPreferencesStore();

        store.saveLastXmlDirectory(tempDir);
        assertEquals(tempDir, store.loadLastXmlDirectory());

        Path file = Files.createTempFile(tempDir, "pref-", ".xml");
        preferences.put(KEY_LAST_XML_DIR, file.toString());
        assertNull(store.loadLastXmlDirectory());

        preferences.put(KEY_LAST_XML_DIR, "Z:/definitely/nonexistent/directory/for/jexam-tests");
        assertNull(store.loadLastXmlDirectory());

        preferences.remove(KEY_LAST_XML_DIR);
        store.saveLastXmlDirectory(null);
        assertNull(store.loadLastXmlDirectory());
    }

    @Test
    void xmlFileShouldPersistOnlyValidFiles(@TempDir Path tempDir) throws Exception {
        UserPreferencesStore store = new UserPreferencesStore();
        Path xmlFile = Files.createTempFile(tempDir, "exam-", ".xml");

        store.saveLastXmlFile(xmlFile);
        assertEquals(xmlFile, store.loadLastXmlFile());

        preferences.put(KEY_LAST_XML_FILE, tempDir.toString());
        assertNull(store.loadLastXmlFile());

        preferences.put(KEY_LAST_XML_FILE, "Z:/definitely/nonexistent/file/for/jexam-tests.xml");
        assertNull(store.loadLastXmlFile());

        preferences.remove(KEY_LAST_XML_FILE);
        store.saveLastXmlFile(null);
        assertNull(store.loadLastXmlFile());
    }

    @Test
    void pdfDirectoryShouldPersistOnlyValidDirectories(@TempDir Path tempDir) throws Exception {
        UserPreferencesStore store = new UserPreferencesStore();

        store.saveLastPdfDirectory(tempDir);
        assertEquals(tempDir, store.loadLastPdfDirectory());

        Path file = Files.createTempFile(tempDir, "pref-pdf-", ".tmp");
        preferences.put(KEY_LAST_PDF_DIR, file.toString());
        assertNull(store.loadLastPdfDirectory());
    }

    private void clearPreferenceKeys() {
        preferences.remove(KEY_LANGUAGE);
        preferences.remove(KEY_LAST_XML_DIR);
        preferences.remove(KEY_LAST_XML_FILE);
        preferences.remove(KEY_LAST_PDF_DIR);
        preferences.remove(KEY_UI_THEME);
    }
}