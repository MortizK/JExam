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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Encapsulates UI dialogs and file chooser setup used by the JavaFX app.
 */
class JExamUiSupport {
    private final Map<String, String> englishText = new HashMap<>();
    private final Map<String, String> germanText = new HashMap<>();
    private final List<Runnable> languageChangeListeners = new ArrayList<>();

    private UiLanguage language = UiLanguage.ENGLISH;
    private Path lastXmlDirectory;
    private Path lastPdfDirectory;

    JExamUiSupport() {
        initText();
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

    private void initText() {
        englishText.put("app.subtitle", "Phase 4 MVP: hierarchy editing + validation + PDF export");
        englishText.put("app.title", "JExam");
        englishText.put("button.load.xml", "Load XML");
        englishText.put("button.create.exam", "Create New Exam");
        englishText.put("button.refresh.preview", "Refresh Preview");
        englishText.put("button.open.external", "Open External");
        englishText.put("label.xml.empty.title", "No XML loaded");
        englishText.put("label.xml.empty.subtitle", "Create a new exam or load an existing XML file to begin.");
        englishText.put("label.unsaved.close.title", "Unsaved changes");
        englishText.put("label.unsaved.close.message", "XML auto-saves to a temporary folder, but closing now may still lose progress. Continue?");
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
        englishText.put("tree.filter.prompt", "Filter...");
        englishText.put("tree.filter.accessible", "Filter exam chapters and tasks");
        englishText.put("button.expand.all", "Expand All");
        englishText.put("button.collapse.all", "Collapse All");

        germanText.put("app.subtitle", "Phase 4 MVP: Hierarchie bearbeiten + Validierung + PDF-Export");
    germanText.put("app.title", "JExam");
    germanText.put("button.load.xml", "XML laden");
    germanText.put("button.create.exam", "Neues Examen erstellen");
    germanText.put("button.refresh.preview", "Vorschau aktualisieren");
    germanText.put("button.open.external", "Extern öffnen");
    germanText.put("label.xml.empty.title", "Kein XML geladen");
    germanText.put("label.xml.empty.subtitle", "Erstellen Sie ein neues Examen oder laden Sie eine XML-Datei, um zu beginnen.");
    germanText.put("label.unsaved.close.title", "Ungespeicherte Änderungen");
    germanText.put("label.unsaved.close.message", "XML wird zwar temporär gespeichert, aber beim Schließen kann dennoch Fortschritt verloren gehen. Fortfahren?");
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
        germanText.put("tree.filter.prompt", "Filtern...");
        germanText.put("tree.filter.accessible", "Klausurkapitel und Aufgaben filtern");
        germanText.put("button.expand.all", "Alle aufklappen");
        germanText.put("button.collapse.all", "Alle einklappen");
    }
}