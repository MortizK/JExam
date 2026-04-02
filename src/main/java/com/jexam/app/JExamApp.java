package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
import com.jexam.app.ui.components.AppHeaderNavigation;
import com.jexam.generation.GenerationMode;
import com.jexam.io.ExamXmlException;
import com.jexam.validation.ValidationResult;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

/**
 * JavaFX entry point for JExam.
 */
public class JExamApp extends Application {
    private final ExamApplicationService appService = new ExamApplicationService();
    private final JExamUiSupport ui = new JExamUiSupport();
    private final JExamSelectionModel selectionModel = new JExamSelectionModel();
    private final UiStateManager uiStateManager = new UiStateManager();

    private XmlTabContainer xmlTabContainer;

    @Override
    public void start(final Stage stage) {
        ui.setLanguage(UiLanguage.ENGLISH);
        selectionModel.setExam(appService.getCurrentExam());

        AppHeaderNavigation headerNavigation = new AppHeaderNavigation();
        configureHeader(stage, headerNavigation);

        xmlTabContainer = new XmlTabContainer(appService, ui, selectionModel, uiStateManager);
        xmlTabContainer.setOnDirtyStateChanged(headerNavigation::setDirty);

        VBox topPane = new VBox(8, headerNavigation);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(topPane);
        root.setCenter(xmlTabContainer);

        Scene scene = new Scene(root, 1100, 760);
        stage.setTitle(ui.text("app.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(final String[] args) {
        launch(args);
    }

    private void configureHeader(final Stage stage, final AppHeaderNavigation headerNavigation) {
        headerNavigation.setTitle(ui.text("app.title"));
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
            headerNavigation.setTitle(ui.text("app.title"));
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
        headerNavigation.setOnNew(() -> {
            appService.newExam();
            selectionModel.setExam(appService.getCurrentExam());
            xmlTabContainer.refreshFromService();
            uiStateManager.markSaved();
            headerNavigation.setDirty(false);
            ui.showInfo("New exam created", "Created a new exam in memory.");
        });
        headerNavigation.setOnOpen(() -> openExam(stage));
        headerNavigation.setOnSave(() -> saveExam(stage));
        headerNavigation.setOnValidate(this::validateCurrentExam);
        headerNavigation.setOnPreview(this::previewPdf);
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
            uiStateManager.markSaved();
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
        try {
            Path previewPath = appService.generatePreviewPdf(GenerationMode.EXAM);
            if (!ui.openFile(previewPath)) {
                ui.showInfo("Preview ready", "Preview generated at: " + previewPath);
                return;
            }
            ui.showInfo("Preview ready", "Opened preview: " + previewPath);
        } catch (RuntimeException e) {
            ui.showError("Preview failed", e.getMessage());
        }
    }
}
