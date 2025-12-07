package jexam.controller;

import jexam.App;
import jexam.model.Chapter;
import jexam.model.Exam;
import jexam.model.Task;
import jexam.model.Variant;
import jexam.model.enums.Difficulty;
import jexam.model.enums.Scope;
import jexam.model.enums.Type;
import jexam.service.DataService;
import jexam.service.EventService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Objects;

public class mainContentController {

    @FXML
    public VBox form;

    @FXML
    public VBox chapterDetails;

    @FXML
    public HBox tableHeader;

    @FXML
    public VBox tableContent;

    @FXML
    public Button addNewChild;

    @FXML
    public TextField parentName;

    @FXML
    public VBox formTasks;

    @FXML
    public Label changeLabel;

    @FXML
    public void initialize() {
        loadContent();

        // Dynamically change the width of the TextField
        parentName.textProperty().addListener((obs, oldText, newText) -> {
            Text text = new Text(newText);
            text.setFont(parentName.getFont());
            double width = text.getLayoutBounds().getWidth() + 20; // +Padding
            double minWidth = 100;
            parentName.setMinWidth(Math.max(minWidth, width));
            parentName.setMaxWidth(Math.max(minWidth, width));
        });

        addNewChild.setOnAction(e -> {
           createNewChild();
        });
    }

    private void createNewChild() {
        DataService dataService = App.getDataService(); // Get the DataService

        Variant variant = new Variant("New Question", "New Answer");

        switch (dataService.type) {
            case EXAM:
                Chapter chapter = new Chapter("New Chapter");
                dataService.selectedItem.addChapter(chapter);
                break;
            case CHAPTER:
                Task task = new Task("New Task", variant, 1.0, Difficulty.EASY, Scope.EXAM);
                dataService.selectedItem.addTask(task);
                break;
            case TASK:
                dataService.selectedItem.addVariant(variant);
                break;
        }

        EventService.triggerPdfUpdate();
    }

    public void updateContent() {
        loadContent();
    }

    private void loadContent() {
        DataService dataService = App.getDataService(); // Get the DataService

        chapterDetails.getChildren().clear();
        tableContent.getChildren().clear();
        tableHeader.getChildren().clear();
        formTasks.getChildren().clear();

        switch (dataService.type) {
            case EXAM:
                loadExamContent((Exam) dataService.selectedItem, 200, 320);
                break;
            case CHAPTER:
                loadChapterContent((Chapter) dataService.selectedItem, 200, 220);
                break;
            case TASK:
                loadTaskContent((Task) dataService.selectedItem);
                break;
        }
    }

    private void loadTaskContent(Task task) {
        // Update Form Content
        parentName.setText(task.getName());
        changeLabel.setText("Change Chapter Name");

        // Create Radio Menus for Scope and Difficulty
        formTasks.getChildren().addAll(
                createScopeRadioButtons(task),
                createDifficultyRadioButtons(task),
                createEditPoints(task)
        );

        // Update Button Text
        addNewChild.setText("New Variant");

        // Row Content
        List<Variant> variants = task.getVariants();
        for(Variant variant : variants) {
            tableContent.getChildren().add(createVariantComponent(variant, variants.size()));
        }
    }

    private VBox createScopeRadioButtons(Task task) {

        String scopeBaseId = "scopeRadio";

        // RadioButtons
        ToggleButton rbExam = createRadioChip("Exam", "chip-exam-radio");
        ToggleButton rbMock = createRadioChip("Mock", "chip-mock-radio");

        // ToggleGroup
        ToggleGroup group = new ToggleGroup();
        rbExam.setToggleGroup(group);
        rbMock.setToggleGroup(group);

        // Falls Task bereits einen Scope hat → vorauswählen
        if (task != null) {
            switch (task.getScope()) {
                case EXAM -> rbExam.setSelected(true);
                case MOCK -> rbMock.setSelected(true);
            }
        }

        // Event: Auswahl geändert → Task aktualisieren
        group.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null || task == null) return;

            ToggleButton rb = (ToggleButton) selected;
            String text = rb.getText();

            switch (text) {
                case "Exam" -> task.setScope(Scope.EXAM);
                case "Mock" -> task.setScope(Scope.MOCK);
            }
        });

        // Layout
        HBox scopeBox = new HBox(10);
        scopeBox.getChildren().addAll(rbExam, rbMock);

        VBox scopeHeading = new VBox(10);
        Label heading = new Label("Select Scope");
        heading.getStyleClass().add("fw-bold");
        scopeHeading.getChildren().addAll(heading, scopeBox);

        return scopeHeading;
    }

    private VBox createDifficultyRadioButtons(Task task) {

        String baseId = "difficultyRadio";

        // RadioButtons
        ToggleButton rbEasy = createRadioChip("Easy", "chip-easy-radio");
        ToggleButton rbMedium = createRadioChip("Medium", "chip-medium-radio");
        ToggleButton rbHard = createRadioChip("Hard", "chip-hard-radio");

        // ToggleGroup
        ToggleGroup group = new ToggleGroup();
        rbEasy.setToggleGroup(group);
        rbMedium.setToggleGroup(group);
        rbHard.setToggleGroup(group);

        // Vorauswahl anhand des Tasks
        if (task != null) {
            switch (task.getDifficulty()) {
                case EASY -> rbEasy.setSelected(true);
                case MEDIUM -> rbMedium.setSelected(true);
                case HARD -> rbHard.setSelected(true);
            }
        }

        // Listener → ändere Difficulty im Task
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || task == null) return;

            ToggleButton rb = (ToggleButton) newVal;
            String text = rb.getText();

            switch (text) {
                case "Easy" -> task.setDifficulty(Difficulty.EASY);
                case "Medium" -> task.setDifficulty(Difficulty.MEDIUM);
                case "Hard" -> task.setDifficulty(Difficulty.HARD);
            }
        });

        // Layout
        HBox difficultyBox = new HBox(10);
        difficultyBox.getChildren().addAll(rbEasy, rbMedium, rbHard);

        VBox difficultyHeading = new VBox(10);
        Label heading = new Label("Select Difficulty");
        heading.getStyleClass().add("fw-bold");
        difficultyHeading.getChildren().addAll(heading, difficultyBox);

        return difficultyHeading;
    }

    private VBox createEditPoints(Task task) {

        Double currentPoints = task != null ? task.getPoints() : 0.0;

        // Spinner mit minimal 0, maximal 100, Schrittweite 0.5
        Spinner<Double> pointSpinner = new Spinner<>(0.0, 100.0, currentPoints, 0.5);
        pointSpinner.setEditable(true);
        pointSpinner.setId("pointsSpinner");

        // Task aktualisieren, wenn sich der Wert ändert
        pointSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (task != null && newValue != null) {
                task.setPoints(newValue);
            }
        });

        VBox pointsHeading = new VBox(10);
        Label heading = new Label("Enter Points");
        heading.getStyleClass().add("fw-bold");
        pointsHeading.getChildren().addAll(heading, pointSpinner);

        return pointsHeading;
    }

    private double getHeight(TextArea textArea, String newText) {
        Text text = new Text(newText);
        text.setFont(textArea.getFont());
        text.setWrappingWidth(textArea.getWidth() - 10);
        return text.getLayoutBounds().getHeight() + 20;
    }

    private TextArea createTextArea(String text) {
        TextArea textArea = new TextArea(text);
        textArea.setWrapText(true);
        textArea.setMinHeight(Region.USE_PREF_SIZE);
        textArea.setMaxHeight(Region.USE_PREF_SIZE);
        textArea.setPrefHeight(getHeight(textArea, text));

        return textArea;
    }

    private VBox createVariantComponent(Variant variant, int numRows) {
        VBox row = new VBox();
        row.setSpacing(10);

        // Question
        HBox question = new HBox();
        question.setAlignment(Pos.CENTER_LEFT);
        Label questionHeader = new Label("Question:");
        questionHeader.getStyleClass().add("fw-bold");
        question.getChildren().add(questionHeader);
        TextArea questionArea = createTextArea(variant.getQuestion());

        questionArea.textProperty().addListener((obs, oldText, newText) -> {
            variant.setQuestion(newText);
            questionArea.setPrefHeight(getHeight(questionArea, newText));
        });

        // Answer
        Label answerHeader = new Label("Answer:");
        answerHeader.getStyleClass().add("fw-bold");
        TextArea answerArea = createTextArea(variant.getAnswer());

        answerArea.textProperty().addListener((obs, oldText, newText) -> {
            variant.setAnswer(newText);
            answerArea.setPrefHeight(getHeight(answerArea, newText));
        });

        // Delete Button
        if (numRows > 1) {
            Button deleteBtn = getDeleteVariantButton(variant, row);
            question.getChildren().addAll(growing(), deleteBtn);
        }

        row.getChildren().addAll(
                question, questionArea,
                answerHeader, answerArea
        );

        return row;
    }

    private Button getDeleteVariantButton(Variant variant, VBox row) {
        Button deleteBtn = createDeleteButton("Delete Variant");

        deleteBtn.setOnAction(event -> {
            // Confirmation Dialog
            showAlert("Delete Variant",
                    "You are about to delete variant \"" + variant.getName() + "\"?",
                    () -> {
                        ((Task) App.getDataService().selectedItem).deleteVariant(variant);
                        tableContent.getChildren().remove(row);
                        EventService.triggerPdfUpdate();
                    });
        });
        return deleteBtn;
    }

    private void loadChapterContent(Chapter chapter, Integer col1Width, Integer col2Width) {
        // Update Form Content
        parentName.setText(chapter.getName());
        changeLabel.setText("Change Chapter Name");

        // Update Button Text
        addNewChild.setText("New Task");

        // Update chapterDetails
        chapterDetails.getChildren().add(createChapterComponent(chapter));

        // Loading the Header
        HBox header = new HBox();
        header.setSpacing(10);

        Label name = new Label("Name");
        name.getStyleClass().add("fw-bold");
        name.setMinWidth(col1Width);
        header.getChildren().add(name);

        Label amounts = new Label("Amounts");
        amounts.getStyleClass().add("fw-bold");
        amounts.setMinWidth(col2Width);
        header.getChildren().add(amounts);

        Label scope = new Label("Scope");
        scope.getStyleClass().add("fw-bold");
        header.getChildren().add(scope);

        Label difficulty = new Label("Difficulty");
        difficulty.getStyleClass().add("fw-bold");
        header.getChildren().add(difficulty);

        tableHeader.getChildren().add(header);

        // Loading the rows
        int index = 0;
        for (Task task : chapter.getTasks()) {
            tableContent.getChildren().add(createTaskComponent(task, index, col1Width, col2Width));
            index++;
        }

    }

    private HBox createTaskComponent(Task task, int index, Integer width1, Integer width2) {
        HBox row = new HBox();
        row.setSpacing(10);

        // Name
        Label taskName = new Label(task.getName());
        taskName.setMinWidth(width1);
        row.getChildren().add(taskName);

        // Amounts (2 Labels in 1 column)
        HBox amounts = new HBox(10);
        amounts.getChildren().addAll(
                createChip(task.getNumberOfVariants().toString(), "Variants - ", ""),
                createChip(task.getPoints().toString(), "Points - ", "")
        );
        amounts.setMinWidth(width2);
        row.getChildren().add(amounts);

        // Difficulty & Scope
        HBox difficulty = new HBox(10);
        String scopeStyle = "chip-" + task.getScope().toString().toLowerCase();
        String difficultyStyle = "chip-" + task.getDifficulty().toString().toLowerCase();
        difficulty.getChildren().addAll(
                createChip(task.getFormatedScope(), "", scopeStyle),
                createChip(task.getFormatedDifficulty(), "", difficultyStyle)
        );
        row.getChildren().add(difficulty);

        // Edit Button
        Button editBtn = createEditButton("Edit Task");

        editBtn.setOnAction(event -> {
            DataService dataService = App.getDataService(); // Get the DataService
            dataService.selectedItem = task;
            dataService.path.add(index);
            EventService.triggerPdfUpdate();
        });

        // Delete Button
        Button deleteBtn = getDeleteTaskButton(task, row);

        row.getChildren().addAll(growing(), editBtn, deleteBtn);

        return  row;
    }

    private Button getDeleteTaskButton(Task task, HBox row) {
        Button deleteBtn = createDeleteButton("Delete Task");

        deleteBtn.setOnAction(event -> {
            // Confirmation Dialog
            showAlert("Delete Task",
                    "You are about to delete task \"" + task.getName() + "\" with all its Variants?",
                    () -> {
                        ((Chapter) App.getDataService().selectedItem).deleteTask(task);
                        tableContent.getChildren().remove(row);
                        EventService.triggerPdfUpdate();
                    });
        });
        return deleteBtn;
    }

    private void loadExamContent(Exam exam, Integer col1Width, Integer col2Width) {
        // Update Form Content
        parentName.setText(exam.getName());
        changeLabel.setText("Change Exam Name");

        // Update Button Text
        addNewChild.setText("New Chapter");

        // Loading the Header
        HBox header = new HBox();
        header.setSpacing(10);

        Label name = new Label("Name");
        name.getStyleClass().add("fw-bold");
        name.setMinWidth(col1Width);
        header.getChildren().add(name);

        Label amounts = new Label("Amounts");
        amounts.getStyleClass().add("fw-bold");
        amounts.setMinWidth(col2Width);
        header.getChildren().add(amounts);

        Label difficulty = new Label("Difficulty");
        difficulty.getStyleClass().add("fw-bold");
        header.getChildren().add(difficulty);

        tableHeader.getChildren().add(header);

        // Loading the rows
        int index = 0;
        for (Chapter chapter : exam.getChapters()) {
            tableContent.getChildren().add(createChapterComponent(chapter, index, col1Width, col2Width));
            index++;
        }
    }

    private HBox createChapterComponent(Chapter chapter) {
        return createChapterComponent(chapter, 0, 200, 320);
    }

    private HBox createChapterComponent(Chapter chapter, int index, Integer width1, Integer width2) {
        HBox row = new HBox();
        row.setSpacing(10);

        // Name
        Label chapterName = new Label(chapter.getName());
        chapterName.setMinWidth(width1);
        row.getChildren().add(chapterName);

        // Amounts (3 Labels in 1 column)
        HBox amounts = new HBox(10);
        amounts.getChildren().addAll(
                createChip(chapter.getNumberOfTasks().toString(), "Tasks - ", ""),
                createChip(chapter.getNumberOfVariants().toString(), "Variants - ", ""),
                createChip(chapter.getNumberOfPoints().toString(), "Points - ", "")
        );
        amounts.setMinWidth(width2);
        row.getChildren().add(amounts);

        // Difficulty (3 Labels in 1 column)
        HBox difficulty = new HBox(10);
        difficulty.getChildren().addAll(
                createChip(chapter.getNumberOfDifficultyTasks(Difficulty.EASY).toString(), "Easy - ", "chip-easy"),
                createChip(chapter.getNumberOfDifficultyTasks(Difficulty.MEDIUM).toString(), "Medium - ", "chip-medium"),
                createChip(chapter.getNumberOfDifficultyTasks(Difficulty.HARD).toString(), "Hard - ", "chip-hard")
        );
        row.getChildren().add(difficulty);

        DataService dataService = App.getDataService(); // Get the DataService
        if (dataService.type == Type.EXAM) {
            // Edit Button
            Button editBtn = createEditButton("Edit Task");

            editBtn.setOnAction(event -> {
                dataService.selectedItem = chapter;
                dataService.path.add(index);
                EventService.triggerPdfUpdate();
            });

            // Delete Button
            Button deleteBtn = getDeleteChapterButton(chapter, row);

            row.getChildren().addAll(growing(), editBtn, deleteBtn);
        }

        return row;
    }

    private Button getDeleteChapterButton(Chapter chapter, HBox row) {
        Button deleteBtn = createDeleteButton("Delete Chapter");

        deleteBtn.setOnAction(event -> {
            // Confirmation Dialog
            showAlert("Delete Chapter",
                    "You are about to delete chapter \"" + chapter.getName() + "\" with all its Task and their Variants?",
                    () -> {
                ((Exam) App.getDataService().selectedItem).deleteChapter(chapter);
                tableContent.getChildren().remove(row);
                EventService.triggerPdfUpdate();
            });
        });
        return deleteBtn;
    }

    private HBox growing() {
        HBox grow = new HBox();
        HBox.setHgrow(grow, Priority.ALWAYS);
        return grow;
    }

    //=== Chip ===\\

    private Label createChip(String text, String before, String styleClass) {
        Label chip = new Label(text);
        chip.getStyleClass().addAll("chip", styleClass);

        // Punkt davor
        Label beforeLabel = new Label(before);
        beforeLabel.setTextFill(Color.GRAY);
        chip.setGraphic(beforeLabel);
        chip.setContentDisplay(ContentDisplay.LEFT);

        chip.setMinWidth(50);
        chip.setAlignment(Pos.CENTER);
        return chip;
    }

    private ToggleButton createRadioChip(String text, String styleClass) {
        ToggleButton rb = new ToggleButton(text);

        rb.setId(text + "-radio");

        rb.getStyleClass().addAll("chip", "chip-radio", styleClass);

        rb.setPadding(new Insets(2, 10, 2, 10));

        return rb;
    }


    //=== Alert ===\\

    private void showAlert(String title, String header, Runnable onAccept) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setGraphic(null);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText("This action cannot be undone.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm()
        );

        ButtonType yesButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, cancelButton);

        Button yesBtn = (Button) dialogPane.lookupButton(yesButton);
        yesBtn.getStyleClass().addAll("btn", "btn-danger");

        Button noBtn = (Button) dialogPane.lookupButton(cancelButton);
        noBtn.getStyleClass().addAll("btn", "btn-primary");

        // Show dialog and wait for response
        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                onAccept.run();
            }
        });
    }

    //=== Buttons ===\\

    private Button createEditButton(String toolTip) {
        Button editBtn = new Button("✎"); // Icon als Text oder setze ein Image
        editBtn.setCursor(Cursor.HAND);

        editBtn.setStyle("-fx-background-color: transparent;");

        editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                "-fx-background-color: rgba(0,255,0,0.1);"));

        editBtn.setOnMouseExited(e -> editBtn.setStyle(
                "-fx-background-color: transparent;"));

        editBtn.setTooltip(new Tooltip(toolTip));

        return editBtn;
    }

    private Button createDeleteButton(String toolTip) {
        Button deleteBtn = new Button("🗑"); // Icon als Text oder setze ein Image
        deleteBtn.setCursor(Cursor.HAND);

        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");

        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                "-fx-background-color: rgba(255,0,0,0.1); -fx-text-fill: red;"));

        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: red;"));

        deleteBtn.setTooltip(new Tooltip(toolTip));

        return deleteBtn;
    }

    //=== From Handler ===\\

    public void save() {
        DataService dataService = App.getDataService(); // Get the DataService

        dataService.selectedItem.setName(parentName.getText());

        EventService.triggerPdfUpdate();
    }

    public void cancel() {
        EventService.triggerPdfUpdate();
    }
}
