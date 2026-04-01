package com.jexam.app;

import com.jexam.generation.GenerationMode;
import com.jexam.io.ExamXmlException;
import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import com.jexam.validation.ValidationResult;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JExamApp extends Application {
    private final ExamApplicationService appService = new ExamApplicationService();

    private Exam currentExam;

    private final ObservableList<String> chapterItems = FXCollections.observableArrayList();
    private final ObservableList<String> taskItems = FXCollections.observableArrayList();
    private final ObservableList<String> variantItems = FXCollections.observableArrayList();

    private ListView<String> chapterList;
    private ListView<String> taskList;
    private ListView<String> variantList;

    private TextField taskNameField;
    private TextField taskPointsField;
    private ComboBox<Difficulty> taskDifficultyBox;
    private ComboBox<Scope> taskScopeBox;

    private TextArea variantQuestionArea;
    private TextArea variantAnswerArea;

    @Override
    public void start(Stage stage) {
        currentExam = appService.getCurrentExam();

        Label title = new Label("JExam");
        Label subtitle = new Label("Phase 4 MVP: hierarchy editing + validation + PDF export");

        Button newButton = new Button("New");
        Button openButton = new Button("Open");
        Button saveButton = new Button("Save");
        Button validateButton = new Button("Validate");
        Button examPdfButton = new Button("Generate Exam PDF");
        Button solutionPdfButton = new Button("Generate Solution PDF");
        Button mockPdfButton = new Button("Generate Mock PDF");

        newButton.setOnAction(event -> {
            appService.newExam();
            currentExam = appService.getCurrentExam();
            refreshAllLists();
            showInfo("New exam created", "Created a new exam in memory.");
        });
        openButton.setOnAction(event -> openExam(stage));
        saveButton.setOnAction(event -> saveExam(stage));
        validateButton.setOnAction(event -> validateCurrentExam());
        examPdfButton.setOnAction(event -> generatePdf(stage, GenerationMode.EXAM));
        solutionPdfButton.setOnAction(event -> generatePdf(stage, GenerationMode.SOLUTION));
        mockPdfButton.setOnAction(event -> generatePdf(stage, GenerationMode.MOCK_EXAM));

        HBox actionBar = new HBox(
            8,
            newButton,
            openButton,
            saveButton,
            validateButton,
            examPdfButton,
            solutionPdfButton,
            mockPdfButton
        );

        chapterList = new ListView<>(chapterItems);
        taskList = new ListView<>(taskItems);
        variantList = new ListView<>(variantItems);
        chapterList.setPrefHeight(240);
        taskList.setPrefHeight(240);
        variantList.setPrefHeight(240);

        Button addChapterButton = new Button("+ Chapter");
        Button removeChapterButton = new Button("- Chapter");
        Button addTaskButton = new Button("+ Task");
        Button removeTaskButton = new Button("- Task");
        Button addVariantButton = new Button("+ Variant");
        Button removeVariantButton = new Button("- Variant");

        addChapterButton.setOnAction(event -> addChapter());
        removeChapterButton.setOnAction(event -> removeChapter());
        addTaskButton.setOnAction(event -> addTask());
        removeTaskButton.setOnAction(event -> removeTask());
        addVariantButton.setOnAction(event -> addVariant());
        removeVariantButton.setOnAction(event -> removeVariant());

        chapterList.getSelectionModel().selectedIndexProperty().addListener((obs, oldV, newV) -> {
            refreshTasks();
            refreshVariants();
            populateTaskEditor();
            populateVariantEditor();
        });
        taskList.getSelectionModel().selectedIndexProperty().addListener((obs, oldV, newV) -> {
            refreshVariants();
            populateTaskEditor();
            populateVariantEditor();
        });
        variantList.getSelectionModel().selectedIndexProperty().addListener((obs, oldV, newV) -> populateVariantEditor());

        VBox chapterPane = new VBox(6, new Label("Chapters"), chapterList, new HBox(6, addChapterButton, removeChapterButton));
        VBox taskPane = new VBox(6, new Label("Tasks"), taskList, new HBox(6, addTaskButton, removeTaskButton));
        VBox variantPane = new VBox(6, new Label("Variants"), variantList, new HBox(6, addVariantButton, removeVariantButton));
        HBox listsPane = new HBox(12, chapterPane, taskPane, variantPane);

        taskNameField = new TextField();
        taskPointsField = new TextField();
        taskDifficultyBox = new ComboBox<>(FXCollections.observableArrayList(Difficulty.values()));
        taskScopeBox = new ComboBox<>(FXCollections.observableArrayList(Scope.values()));
        Button saveTaskButton = new Button("Save Task Details");
        saveTaskButton.setOnAction(event -> saveTaskDetails());

        GridPane taskEditor = new GridPane();
        taskEditor.setHgap(8);
        taskEditor.setVgap(6);
        taskEditor.add(new Label("Task Name"), 0, 0);
        taskEditor.add(taskNameField, 1, 0);
        taskEditor.add(new Label("Points"), 0, 1);
        taskEditor.add(taskPointsField, 1, 1);
        taskEditor.add(new Label("Difficulty"), 2, 0);
        taskEditor.add(taskDifficultyBox, 3, 0);
        taskEditor.add(new Label("Scope"), 2, 1);
        taskEditor.add(taskScopeBox, 3, 1);
        taskEditor.add(saveTaskButton, 4, 0);

        variantQuestionArea = new TextArea();
        variantQuestionArea.setPrefRowCount(3);
        variantAnswerArea = new TextArea();
        variantAnswerArea.setPrefRowCount(3);
        Button saveVariantButton = new Button("Save Variant Text");
        saveVariantButton.setOnAction(event -> saveVariantDetails());

        GridPane variantEditor = new GridPane();
        variantEditor.setHgap(8);
        variantEditor.setVgap(6);
        variantEditor.add(new Label("Question"), 0, 0);
        variantEditor.add(variantQuestionArea, 1, 0);
        variantEditor.add(new Label("Answer"), 0, 1);
        variantEditor.add(variantAnswerArea, 1, 1);
        variantEditor.add(saveVariantButton, 2, 0);

        VBox editorPane = new VBox(8, new Label("Editors"), taskEditor, variantEditor);

        VBox topPane = new VBox(8, title, subtitle, actionBar);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(topPane);
        root.setCenter(new VBox(10, listsPane, editorPane));

        refreshAllLists();

        Scene scene = new Scene(root, 1100, 760);
        stage.setTitle("JExam");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void refreshAllLists() {
        currentExam = appService.getCurrentExam();
        refreshChapters();
        refreshTasks();
        refreshVariants();
        populateTaskEditor();
        populateVariantEditor();
    }

    private void refreshChapters() {
        chapterItems.setAll(currentExam.getChapters().stream().map(Chapter::getName).toList());
        if (!chapterItems.isEmpty() && chapterList.getSelectionModel().getSelectedIndex() < 0) {
            chapterList.getSelectionModel().select(0);
        }
    }

    private void refreshTasks() {
        Chapter chapter = selectedChapter();
        if (chapter == null) {
            taskItems.clear();
            return;
        }
        taskItems.setAll(chapter.getTasks().stream().map(this::taskLabel).toList());
        if (!taskItems.isEmpty() && taskList.getSelectionModel().getSelectedIndex() < 0) {
            taskList.getSelectionModel().select(0);
        }
    }

    private void refreshVariants() {
        Task task = selectedTask();
        if (task == null) {
            variantItems.clear();
            return;
        }

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < task.getVariants().size(); i++) {
            labels.add("Variant " + (i + 1));
        }
        variantItems.setAll(labels);
        if (!variantItems.isEmpty() && variantList.getSelectionModel().getSelectedIndex() < 0) {
            variantList.getSelectionModel().select(0);
        }
    }

    private void populateTaskEditor() {
        Task task = selectedTask();
        if (task == null) {
            taskNameField.setText("");
            taskPointsField.setText("");
            taskDifficultyBox.setValue(null);
            taskScopeBox.setValue(null);
            return;
        }

        taskNameField.setText(task.getName());
        taskPointsField.setText(Double.toString(task.getPoints()));
        taskDifficultyBox.setValue(task.getDifficulty());
        taskScopeBox.setValue(task.getScope());
    }

    private void populateVariantEditor() {
        Variant variant = selectedVariant();
        if (variant == null) {
            variantQuestionArea.setText("");
            variantAnswerArea.setText("");
            return;
        }

        variantQuestionArea.setText(variant.getQuestion());
        variantAnswerArea.setText(variant.getAnswer());
    }

    private String taskLabel(Task task) {
        return task.getName() + " (" + task.getPoints() + " pts, " + task.getDifficulty().toXmlValue() + ", "
            + task.getScope().toXmlValue() + ")";
    }

    private void addChapter() {
        Optional<String> name = askForText("Add Chapter", "Chapter name", "New Chapter");
        name.ifPresent(value -> {
            appService.addChapter(value);
            refreshAllLists();
            chapterList.getSelectionModel().select(currentExam.getChapters().size() - 1);
        });
    }

    private void removeChapter() {
        int index = chapterList.getSelectionModel().getSelectedIndex();
        if (index < 0) {
            showError("No chapter selected", "Select a chapter to remove.");
            return;
        }

        if (currentExam.getChapters().size() <= 1) {
            showError("Cannot remove chapter", "At least one chapter must remain.");
            return;
        }

        if (!confirm("Remove chapter", "Remove selected chapter?")) {
            return;
        }

        appService.removeChapter(index);
        refreshAllLists();
    }

    private void addTask() {
        Chapter chapter = selectedChapter();
        if (chapter == null) {
            showError("No chapter selected", "Select a chapter before adding a task.");
            return;
        }

        Optional<String> name = askForText("Add Task", "Task name", "New Task");
        name.ifPresent(value -> {
            int chapterIndex = chapterList.getSelectionModel().getSelectedIndex();
            appService.addTask(chapterIndex, value);
            refreshAllLists();
            taskList.getSelectionModel().select(chapter.getTasks().size() - 1);
        });
    }

    private void removeTask() {
        Chapter chapter = selectedChapter();
        int index = taskList.getSelectionModel().getSelectedIndex();
        if (chapter == null || index < 0) {
            showError("No task selected", "Select a task to remove.");
            return;
        }

        if (chapter.getTasks().size() <= 1) {
            showError("Cannot remove task", "At least one task must remain per chapter.");
            return;
        }

        if (!confirm("Remove task", "Remove selected task?")) {
            return;
        }

        int chapterIndex = chapterList.getSelectionModel().getSelectedIndex();
        appService.removeTask(chapterIndex, index);
        refreshAllLists();
    }

    private void addVariant() {
        Task task = selectedTask();
        if (task == null) {
            showError("No task selected", "Select a task before adding a variant.");
            return;
        }

        int chapterIndex = chapterList.getSelectionModel().getSelectedIndex();
        int taskIndex = taskList.getSelectionModel().getSelectedIndex();
        appService.addVariant(chapterIndex, taskIndex);
        refreshAllLists();
        variantList.getSelectionModel().select(task.getVariants().size() - 1);
    }

    private void removeVariant() {
        Task task = selectedTask();
        int index = variantList.getSelectionModel().getSelectedIndex();
        if (task == null || index < 0) {
            showError("No variant selected", "Select a variant to remove.");
            return;
        }

        if (task.getVariants().size() <= 1) {
            showError("Cannot remove variant", "Each task must have at least one variant.");
            return;
        }

        if (!confirm("Remove variant", "Remove selected variant?")) {
            return;
        }

        int chapterIndex = chapterList.getSelectionModel().getSelectedIndex();
        int taskIndex = taskList.getSelectionModel().getSelectedIndex();
        appService.removeVariant(chapterIndex, taskIndex, index);
        refreshAllLists();
    }

    private void saveTaskDetails() {
        Task task = selectedTask();
        if (task == null) {
            showError("No task selected", "Select a task first.");
            return;
        }

        try {
            double points = Double.parseDouble(taskPointsField.getText().trim());
            if (points <= 0) {
                throw new NumberFormatException();
            }
            if (taskDifficultyBox.getValue() == null || taskScopeBox.getValue() == null) {
                showError("Invalid metadata", "Difficulty and scope must be selected.");
                return;
            }
            int chapterIndex = chapterList.getSelectionModel().getSelectedIndex();
            int taskIndex = taskList.getSelectionModel().getSelectedIndex();
            appService.updateTaskDetails(
                chapterIndex,
                taskIndex,
                taskNameField.getText().trim(),
                points,
                taskDifficultyBox.getValue(),
                taskScopeBox.getValue()
            );
            refreshTasks();
        } catch (NumberFormatException e) {
            showError("Invalid points", "Points must be a number greater than 0.");
        }
    }

    private void saveVariantDetails() {
        Variant variant = selectedVariant();
        if (variant == null) {
            showError("No variant selected", "Select a variant first.");
            return;
        }

        int chapterIndex = chapterList.getSelectionModel().getSelectedIndex();
        int taskIndex = taskList.getSelectionModel().getSelectedIndex();
        int variantIndex = variantList.getSelectionModel().getSelectedIndex();
        appService.updateVariantDetails(
            chapterIndex,
            taskIndex,
            variantIndex,
            variantQuestionArea.getText(),
            variantAnswerArea.getText()
        );
    }

    private void openExam(Stage stage) {
        FileChooser fileChooser = xmlFileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            appService.openExam(file.toPath());
            currentExam = appService.getCurrentExam();
            refreshAllLists();
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
            appService.saveExam(path);
            showInfo("Save successful", "Saved exam to: " + path);
        } catch (ExamXmlException e) {
            showError("Save failed", e.getMessage());
        }
    }

    private void validateCurrentExam() {
        ValidationResult result = appService.validateCurrentExam();
        if (result.isValid()) {
            showInfo("Validation successful", "No validation errors found.");
            return;
        }
        showError("Validation failed", result.getErrors().toString());
    }

    private void generatePdf(Stage stage, GenerationMode mode) {
        ValidationResult result = appService.validateCurrentExam();
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
            appService.generatePdf(mode, file.toPath());
            showInfo("PDF generated", "Generated " + mode + " at: " + file.toPath());
        } catch (RuntimeException e) {
            showError("PDF generation failed", e.getMessage());
        }
    }

    private Optional<String> askForText(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Value:");
        Optional<String> value = dialog.showAndWait();
        return value.map(String::trim).filter(v -> !v.isEmpty());
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }

    private Chapter selectedChapter() {
        int chapterIndex = chapterList.getSelectionModel().getSelectedIndex();
        if (chapterIndex < 0 || chapterIndex >= currentExam.getChapters().size()) {
            return null;
        }
        return currentExam.getChapters().get(chapterIndex);
    }

    private Task selectedTask() {
        Chapter chapter = selectedChapter();
        int taskIndex = taskList.getSelectionModel().getSelectedIndex();
        if (chapter == null || taskIndex < 0 || taskIndex >= chapter.getTasks().size()) {
            return null;
        }
        return chapter.getTasks().get(taskIndex);
    }

    private Variant selectedVariant() {
        Task task = selectedTask();
        int variantIndex = variantList.getSelectionModel().getSelectedIndex();
        if (task == null || variantIndex < 0 || variantIndex >= task.getVariants().size()) {
            return null;
        }
        return task.getVariants().get(variantIndex);
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
