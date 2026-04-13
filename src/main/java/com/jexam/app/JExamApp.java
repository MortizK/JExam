package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
import com.jexam.app.ui.styling.ThemeManager;
import com.jexam.app.ui.styling.UiTheme;
import com.jexam.app.ui.components.AppHeaderNavigation;
import com.jexam.io.ExamXmlException;
import com.jexam.validation.ValidationResult;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaFX entry point for JExam.
 */
public class JExamApp extends Application {
    private static final Pattern ISSUE_PATH_PATTERN = Pattern.compile(
        "chapters\\[(\\d+)](?:\\.tasks\\[(\\d+)])?(?:\\.variants\\[(\\d+)])?.*"
    );

    private final ExamApplicationService appService = new ExamApplicationService();
    private final JExamUiSupport ui = new JExamUiSupport();
    private final JExamSelectionModel selectionModel = new JExamSelectionModel();
    private final UiStateManager uiStateManager = new UiStateManager();
    private final UserPreferencesStore preferencesStore = new UserPreferencesStore();
    private final ThemeManager themeManager = new ThemeManager();

    private XmlTabContainer xmlTabContainer;
    private PdfTabContainer pdfTabContainer;
    private TabPane tabPane;
    private UiTheme activeTheme;
    private Path currentXmlPath;
    private final Path autoSavePath = Paths.get(System.getProperty("java.io.tmpdir"), "jexam-autosave.xml");
    private Path currentXmlPath;
    private final Path autoSavePath = Paths.get(System.getProperty("java.io.tmpdir"), "jexam-autosave.xml");

    @Override
    public void start(final Stage stage) {
        UiLanguage initialLanguage = preferencesStore.loadLanguage();
        activeTheme = preferencesStore.loadTheme();
        ui.setLanguage(initialLanguage);
        ui.setLastXmlDirectory(preferencesStore.loadLastXmlDirectory());
        ui.setLastPdfDirectory(preferencesStore.loadLastPdfDirectory());
        currentXmlPath = null;
        currentXmlPath = null;

        Path lastXmlFile = preferencesStore.loadLastXmlFile();
        if (lastXmlFile != null) {
            try {
                appService.openExam(lastXmlFile);
                currentXmlPath = lastXmlFile;
                currentXmlPath = lastXmlFile;
            } catch (ExamXmlException ignored) {
                // Fallback to current in-memory state when last file cannot be loaded.
            }
        }
        selectionModel.setExam(appService.getCurrentExam());

        AppHeaderNavigation headerNavigation = new AppHeaderNavigation();
        configureHeader(stage, headerNavigation);

        xmlTabContainer = new XmlTabContainer(appService, ui, selectionModel, uiStateManager);
        xmlTabContainer.setOnDirtyStateChanged(headerNavigation::setDirty);
        xmlTabContainer.setOnCreateNewExam(this::createNewExam);
        xmlTabContainer.setOnLoadXml(() -> openExam(stage));
        pdfTabContainer = new PdfTabContainer(appService, ui, uiStateManager, stage);
        pdfTabContainer.setOnIssueSelected(this::navigateToValidationIssue);

        tabPane = new TabPane();
        Tab xmlTab = new Tab("XML", xmlTabContainer);
        xmlTab.setClosable(false);
        Tab pdfTab = new Tab("PDF", pdfTabContainer);
        pdfTab.setClosable(false);
        tabPane.getTabs().addAll(xmlTab, pdfTab);
        pdfTab.setOnSelectionChanged(event -> {
            if (pdfTab.isSelected()) {
                Platform.runLater(() -> {
                    pdfTabContainer.refreshFromService();
                    pdfTabContainer.focusDefaultControl();
                });
            }
        });
        xmlTab.setOnSelectionChanged(event -> {
            if (xmlTab.isSelected()) {
                Platform.runLater(xmlTabContainer::focusNavigationTree);
            }
        });

        VBox topPane = new VBox(8, headerNavigation);
        BorderPane root = new BorderPane();
        root.getStyleClass().add("jexam-root");
        topPane.getStyleClass().add("app-top-pane");
        root.setPadding(new Insets(12));
        root.setTop(topPane);
        root.setCenter(tabPane);
        tabPane.getStyleClass().add("jexam-tabs");

        Scene scene = new Scene(root, 1100, 760);
        themeManager.applyTheme(scene, activeTheme);
        stage.setTitle(ui.text("app.title"));
        stage.setScene(scene);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            double width = newValue == null ? scene.getWidth() : newValue.doubleValue();
            xmlTabContainer.updateLayout(width);
            pdfTabContainer.updateLayout(width);
        });
        xmlTabContainer.updateLayout(scene.getWidth());
        pdfTabContainer.updateLayout(scene.getWidth());
        configureKeyboardShortcuts(scene, stage);
        configureCloseHandling(stage);
        stage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }

    private void configureHeader(final Stage stage, final AppHeaderNavigation headerNavigation) {
        headerNavigation.setButtonText(
            ui.text("button.new"),
            ui.text("button.open"),
            ui.text("button.save"),
            ui.text("button.validate")
        );
        headerNavigation.setLanguageLabel(ui.text("label.language"));
        headerNavigation.setSelectedLanguage(preferencesStore.loadLanguage());
        headerNavigation.setOnLanguageChanged(value -> {
            ui.setLanguage(value);
            preferencesStore.saveLanguage(value);
            headerNavigation.setButtonText(
                ui.text("button.new"),
                ui.text("button.open"),
                ui.text("button.save"),
                ui.text("button.validate")
            );
            headerNavigation.setLanguageLabel(ui.text("label.language"));
            stage.setTitle(ui.text("app.title"));
        });
        headerNavigation.setOnNew(this::createNewExam);
        headerNavigation.setOnOpen(() -> openExam(stage));
        headerNavigation.setOnSave(() -> saveExam(stage));
        headerNavigation.setOnValidate(this::validateCurrentExam);
    }

    private void configureCloseHandling(final Stage stage) {
        stage.setOnCloseRequest(event -> {
            if (!uiStateManager.isDirty()) {
                return;
            }

            boolean shouldClose = ui.confirm(
                ui.text("label.unsaved.close.title"),
                ui.text("label.unsaved.close.message")
            );
            if (!shouldClose) {
                event.consume();
            }
        });
    }

    private void configureKeyboardShortcuts(final Scene scene, final Stage stage) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), this::createNewExam);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN), () -> openExam(stage));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), () -> saveExam(stage));
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN), this::previewPdf);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ESCAPE), this::navigateXmlOneLevelUp);
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
            () -> {
                activeTheme = themeManager.toggleTheme(scene);
                preferencesStore.saveTheme(activeTheme);
            }
        );
    }

    private void createNewExam() {
        if (!ensureSwitchSafe("create a new exam")) {
            return;
        }
        if (!ensureSwitchSafe("create a new exam")) {
            return;
        }
        appService.newExam();
        currentXmlPath = null;
        currentXmlPath = null;
        selectionModel.setExam(appService.getCurrentExam());
        xmlTabContainer.refreshFromService();
        pdfTabContainer.refreshFromService();
        xmlTabContainer.markSaved();
        uiStateManager.markPreviewStale();
        ui.showInfo("New exam created", "Created a new exam in memory.");
    }

    private void openExam(final Stage stage) {
        if (!ensureSwitchSafe("open another exam")) {
            return;
        }

        if (!ensureSwitchSafe("open another exam")) {
            return;
        }

        FileChooser fileChooser = ui.xmlFileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            Path path = file.toPath();
            appService.openExam(path);
            currentXmlPath = path;
            currentXmlPath = path;
            Path parent = path.getParent();
            preferencesStore.saveLastXmlDirectory(parent);
            ui.setLastXmlDirectory(parent);
            preferencesStore.saveLastXmlFile(path);
            selectionModel.setExam(appService.getCurrentExam());
            xmlTabContainer.refreshFromService();
            pdfTabContainer.refreshFromService();
            xmlTabContainer.markSaved();
            uiStateManager.markPreviewStale();
            ui.showInfo("Open successful", "Loaded exam: " + selectionModel.currentExamName());
        } catch (ExamXmlException e) {
            ui.showError("Open failed", e.getMessage());
        }
    }

    private void saveExam(final Stage stage) {
        Path path = currentXmlPath;
        if (path == null) {
            FileChooser fileChooser = ui.xmlFileChooser();
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }
            path = file.toPath();
        Path path = currentXmlPath;
        if (path == null) {
            FileChooser fileChooser = ui.xmlFileChooser();
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }
            path = file.toPath();
        }

        try {
            Path parent = path.getParent();
            appService.saveExam(path);
            currentXmlPath = path;
            currentXmlPath = path;
            preferencesStore.saveLastXmlDirectory(parent);
            ui.setLastXmlDirectory(parent);
            preferencesStore.saveLastXmlFile(path);
            xmlTabContainer.markSaved();
            ui.showInfo("Save successful", "Saved exam to: " + path);
        } catch (ExamXmlException e) {
            ui.showError("Save failed", e.getMessage());
        }
    }

    private boolean ensureSwitchSafe(final String actionDescription) {
        if (!uiStateManager.isDirty()) {
            return true;
        }

        Path targetPath = currentXmlPath == null ? autoSavePath : currentXmlPath;
        try {
            appService.saveExam(targetPath);
            currentXmlPath = targetPath;
            Path parent = targetPath.getParent();
            preferencesStore.saveLastXmlDirectory(parent);
            ui.setLastXmlDirectory(parent);
            preferencesStore.saveLastXmlFile(targetPath);
            xmlTabContainer.markSaved();
            return true;
        } catch (ExamXmlException e) {
            ui.showError(
                "Action blocked",
                "Cannot " + actionDescription + " while current exam is invalid: " + e.getMessage()
            );
            return false;
        }
    }

    private void validateCurrentExam() {
        ValidationResult result = appService.validateCurrentExam();
        if (result.isValid()) {
            ui.showInfo("Validation successful", "No validation errors found.");
            return;
        }
        ui.showError("Validation failed", result.getErrors().toString());
    }

    private void previewPdf() {
        tabPane.getSelectionModel().select(1);
        pdfTabContainer.generatePreview();
    }

    private void navigateXmlOneLevelUp() {
        if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
            xmlTabContainer.navigateOneLevelUp();
        }
    }

    private void navigateToValidationIssue(final String issuePath) {
        tabPane.getSelectionModel().select(0);
        if (issuePath == null || issuePath.isBlank()) {
            return;
        }

        Matcher matcher = ISSUE_PATH_PATTERN.matcher(issuePath);
        if (!matcher.matches()) {
            return;
        }

        int chapterIndex = Integer.parseInt(matcher.group(1));
        int taskIndex = matcher.group(2) == null ? -1 : Integer.parseInt(matcher.group(2));
        int variantIndex = matcher.group(3) == null ? -1 : Integer.parseInt(matcher.group(3));
        xmlTabContainer.navigateToSelection(chapterIndex, taskIndex, variantIndex);
    }
}
