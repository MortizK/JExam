package com.jexam.app;

import com.jexam.generation.GenerationMode;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Encapsulates UI dialogs and file chooser setup used by the JavaFX app.
 */
class JExamUiSupport {
    private final Map<String, String> englishText = new HashMap<>();
    private final Map<String, String> germanText = new HashMap<>();

    private UiLanguage language = UiLanguage.ENGLISH;

    JExamUiSupport() {
        initText();
    }

    void setLanguage(UiLanguage value) {
        this.language = value == null ? UiLanguage.ENGLISH : value;
    }

    String text(String key) {
        if (language == UiLanguage.GERMAN && germanText.containsKey(key)) {
            return germanText.get(key);
        }
        return englishText.getOrDefault(key, key);
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
        return fileChooser;
    }

    FileChooser pdfFileChooser(GenerationMode mode) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(text("file.pdf.title"));
        fileChooser.setInitialFileName("jexam-" + mode.name().toLowerCase() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fileChooser;
    }

    void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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

    private void initText() {
        englishText.put("app.subtitle", "Phase 4 MVP: hierarchy editing + validation + PDF export");
        englishText.put("button.new", "New");
        englishText.put("button.open", "Open");
        englishText.put("button.save", "Save");
        englishText.put("button.validate", "Validate");
        englishText.put("button.preview", "Preview Exam PDF");
        englishText.put("button.generate.exam", "Generate Exam PDF");
        englishText.put("button.generate.solution", "Generate Solution PDF");
        englishText.put("button.generate.mock", "Generate Mock PDF");
        englishText.put("label.language", "Language");
        englishText.put("dialog.value", "Value:");
        englishText.put("file.xml.title", "JExam XML File");
        englishText.put("file.pdf.title", "Export PDF");

        germanText.put("app.subtitle", "Phase 4 MVP: Hierarchie bearbeiten + Validierung + PDF-Export");
        germanText.put("button.new", "Neu");
        germanText.put("button.open", "Offnen");
        germanText.put("button.save", "Speichern");
        germanText.put("button.validate", "Prufen");
        germanText.put("button.preview", "Klausur-PDF Vorschau");
        germanText.put("button.generate.exam", "Klausur-PDF erzeugen");
        germanText.put("button.generate.solution", "Losungs-PDF erzeugen");
        germanText.put("button.generate.mock", "Probe-PDF erzeugen");
        germanText.put("label.language", "Sprache");
        germanText.put("dialog.value", "Wert:");
        germanText.put("file.xml.title", "JExam XML-Datei");
        germanText.put("file.pdf.title", "PDF exportieren");
    }
}