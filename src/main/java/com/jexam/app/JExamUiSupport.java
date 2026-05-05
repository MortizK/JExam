package com.jexam.app;

import com.jexam.generation.GenerationMode;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Encapsulates UI dialogs and file chooser setup used by the JavaFX app.
 */
class JExamUiSupport {
    private final UiTextCatalog textCatalog = UiTextCatalog.loadDefault();
    private final List<Runnable> languageChangeListeners = new ArrayList<>();

    private UiLanguage language = UiLanguage.ENGLISH;
    private Path lastXmlDirectory;
    private Path lastPdfDirectory;

    JExamUiSupport() {
    }

    void setLanguage(UiLanguage value) {
        UiLanguage newLanguage = value == null ? UiLanguage.ENGLISH : value;
        if (newLanguage == this.language) {
            return;
        }
        this.language = newLanguage;
        for (Runnable listener : languageChangeListeners) {
            listener.run();
        }
    }

    void addLanguageChangeListener(final Runnable listener) {
        if (listener != null) {
            languageChangeListeners.add(listener);
        }
    }

    String text(String key) {
        return textCatalog.text(language, key);
    }

    Optional<String> askForText(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(text("dialog.value"));
        Optional<String> value = dialog.showAndWait();
        return value.map(String::trim).filter(v -> !v.isEmpty());
    }

    boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }

    FileChooser xmlFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(text("file.xml.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        applyInitialDirectory(fileChooser, lastXmlDirectory);
        return fileChooser;
    }

    FileChooser pdfFileChooser(GenerationMode mode) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(text("file.pdf.title"));
        fileChooser.setInitialFileName("jexam-" + mode.name().toLowerCase() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        applyInitialDirectory(fileChooser, lastPdfDirectory);
        return fileChooser;
    }

    void setLastXmlDirectory(final Path path) {
        lastXmlDirectory = path;
    }

    void setLastPdfDirectory(final Path path) {
        lastPdfDirectory = path;
    }

    void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ELEGANCE: Empathy - User-friendly error feedback acknowledges failures clearly
    void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    boolean openFile(Path path) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            return false;
        }

        try {
            desktop.open(path.toFile());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void applyInitialDirectory(final FileChooser chooser, final Path directory) {
        if (directory == null) {
            return;
        }
        try {
            File file = directory.toFile();
            if (file.isDirectory()) {
                chooser.setInitialDirectory(file);
            }
        } catch (IllegalArgumentException ignored) {
            // Stiller Fallback ohne initialDirectory.
        }
    }

}