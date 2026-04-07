package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
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

    private XmlTabContainer xmlTabContainer;
    private PdfTabContainer pdfTabContainer;
    private TabPane tabPane;

    @Override
    public void start(final Stage stage) {
        ui.setLanguage(UiLanguage.ENGLISH);
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
        root.setPadding(new Insets(12));
        root.setTop(topPane);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1100, 760);
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
            ui.text("button.validate"),
            ui.text("button.preview")
        );
        headerNavigation.setLanguageLabel(ui.text("label.language"));
        headerNavigation.setSelectedLanguage(UiLanguage.ENGLISH);
        headerNavigation.setOnLanguageChanged(value -> {
            ui.setLanguage(value);
            headerNavigation.setButtonText(
                ui.text("button.new"),
                ui.text("button.open"),
                ui.text("button.save"),
                ui.text("button.validate"),
                ui.text("button.preview")
            );
            headerNavigation.setLanguageLabel(ui.text("label.language"));
            stage.setTitle(ui.text("app.title"));
        });
        headerNavigation.setOnNew(this::createNewExam);
        headerNavigation.setOnOpen(() -> openExam(stage));
        headerNavigation.setOnSave(() -> saveExam(stage));
        headerNavigation.setOnValidate(this::validateCurrentExam);
        headerNavigation.setOnPreview(this::previewPdf);
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
    }

    private void createNewExam() {
        appService.newExam();
        selectionModel.setExam(appService.getCurrentExam());
        xmlTabContainer.refreshFromService();
        pdfTabContainer.refreshFromService();
        uiStateManager.markSaved();
        uiStateManager.markPreviewStale();
        ui.showInfo("New exam created", "Created a new exam in memory.");
    }

    private void openExam(final Stage stage) {
        FileChooser fileChooser = ui.xmlFileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            appService.openExam(file.toPath());
            selectionModel.setExam(appService.getCurrentExam());
            xmlTabContainer.refreshFromService();
            pdfTabContainer.refreshFromService();
            uiStateManager.markSaved();
            uiStateManager.markPreviewStale();
            ui.showInfo("Open successful", "Loaded exam: " + selectionModel.currentExamName());
        } catch (ExamXmlException e) {
            ui.showError("Open failed", e.getMessage());
        }
    }

    private void saveExam(final Stage stage) {
        FileChooser fileChooser = ui.xmlFileChooser();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            Path path = file.toPath();
            appService.saveExam(path);
            uiStateManager.markSaved();
            ui.showInfo("Save successful", "Saved exam to: " + path);
        } catch (ExamXmlException e) {
            ui.showError("Save failed", e.getMessage());
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
