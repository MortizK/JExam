package com.jexam.app;

import com.jexam.generation.GenerationMode;
import com.jexam.generation.PdfBoxGenerationService;
import com.jexam.generation.PdfGenerationService;
import com.jexam.io.ExamPersistenceService;
import com.jexam.io.ExamXmlException;
import com.jexam.io.ExamXmlLoader;
import com.jexam.io.ExamXmlWriter;
import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class JExamApp extends Application {
    private final ExamValidator validator = new ExamValidator();
    private final PdfGenerationService pdfGenerationService = new PdfBoxGenerationService();
    private final ExamPersistenceService persistenceService = new ExamPersistenceService(
        new ExamXmlLoader(),
        new ExamXmlWriter(),
        validator
    );

    private Exam currentExam;

    @Override
    public void start(Stage stage) {
        currentExam = createDefaultExam();

        Label title = new Label("JExam - Minimal UI Shell");
        Label subtitle = new Label("Backend-first MVP: New/Open/Save/Validate + PDF Export");

        Button newButton = new Button("New");
        Button openButton = new Button("Open");
        Button saveButton = new Button("Save");
        Button validateButton = new Button("Validate");
        Button examPdfButton = new Button("Generate Exam PDF");
        Button solutionPdfButton = new Button("Generate Solution PDF");
        Button mockPdfButton = new Button("Generate Mock PDF");

        newButton.setOnAction(event -> {
            currentExam = createDefaultExam();
            showInfo("New exam created", "Created a new empty exam in memory.");
        });

        openButton.setOnAction(event -> openExam(stage));
        saveButton.setOnAction(event -> saveExam(stage));
        validateButton.setOnAction(event -> validateCurrentExam());
        examPdfButton.setOnAction(event -> generatePdf(stage, GenerationMode.EXAM));
        solutionPdfButton.setOnAction(event -> generatePdf(stage, GenerationMode.SOLUTION));
        mockPdfButton.setOnAction(event -> generatePdf(stage, GenerationMode.MOCK_EXAM));

        VBox root = new VBox(
            8,
            title,
            subtitle,
            newButton,
            openButton,
            saveButton,
            validateButton,
            examPdfButton,
            solutionPdfButton,
            mockPdfButton
        );
        root.setPadding(new Insets(16));

        Scene scene = new Scene(root, 460, 360);
        stage.setTitle("JExam");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Exam createDefaultExam() {
        return new Exam("New Exam", List.of(new Chapter("New Chapter", List.of())));
    }

    private void openExam(Stage stage) {
        FileChooser fileChooser = xmlFileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            currentExam = persistenceService.loadValidated(file.toPath());
            showInfo("Open successful", "Loaded exam: " + currentExam.getName());
        } catch (ExamXmlException e) {
            showError("Open failed", e.getMessage());
        }
    }

    private void saveExam(Stage stage) {
        FileChooser fileChooser = xmlFileChooser();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            Path path = file.toPath();
            persistenceService.saveValidated(currentExam, path);
            showInfo("Save successful", "Saved exam to: " + path);
        } catch (ExamXmlException e) {
            showError("Save failed", e.getMessage());
        }
    }

    private void validateCurrentExam() {
        ValidationResult result = validator.validate(currentExam);
        if (result.isValid()) {
            showInfo("Validation successful", "No validation errors found.");
            return;
        }

        showError("Validation failed", result.getErrors().toString());
    }

    private void generatePdf(Stage stage, GenerationMode mode) {
        ValidationResult result = validator.validate(currentExam);
        if (!result.isValid()) {
            showError("Cannot generate PDF", "Exam is invalid: " + result.getErrors());
            return;
        }

        FileChooser fileChooser = pdfFileChooser(mode);
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            pdfGenerationService.generate(currentExam, mode, file.toPath());
            showInfo("PDF generated", "Generated " + mode + " at: " + file.toPath());
        } catch (RuntimeException e) {
            showError("PDF generation failed", e.getMessage());
        }
    }

    private FileChooser xmlFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("JExam XML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        return fileChooser;
    }

    private FileChooser pdfFileChooser(GenerationMode mode) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export PDF");
        fileChooser.setInitialFileName("jexam-" + mode.name().toLowerCase() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fileChooser;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
