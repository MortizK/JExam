package dhbw.koehler.jexam.controller;

import dhbw.koehler.jexam.App;
import dhbw.koehler.jexam.model.Chapter;
import dhbw.koehler.jexam.model.Exam;
import dhbw.koehler.jexam.model.Task;
import dhbw.koehler.jexam.model.Variant;
import dhbw.koehler.jexam.model.enums.Difficulty;
import dhbw.koehler.jexam.model.enums.Scope;
import dhbw.koehler.jexam.model.enums.Type;
import dhbw.koehler.jexam.service.DataService;
import dhbw.koehler.jexam.service.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Box;
import javafx.scene.text.Text;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.util.List;

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
    public TextField txtName;

    @FXML
    public VBox formTasks;

    @FXML
    public void initialize() {
        loadContent();

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
                loadExamContent((Exam) dataService.selectedItem);
                break;
            case CHAPTER:
                loadChapterContent((Chapter) dataService.selectedItem);
                break;
            case TASK:
                loadTaskContent((Task) dataService.selectedItem);
                break;
        }
    }

    private void loadTaskContent(Task task) {
        // Update Form Content
        txtName.setText(task.getName());

        // Create Radio Menus for Scope and Difficulty
        formTasks.getChildren().add(createScopeRadioButtons(task));
        formTasks.getChildren().add(createDifficultyRadioButtons(task));
        formTasks.getChildren().add(createEditPoints(task));

        // Update Button Text
        addNewChild.setText("New Variant");

        // Row Content
        List<Variant> variants = task.getVariants();
        for(Variant variant : variants) {
            tableContent.getChildren().add(variantRow(variant, variants.size()));
        }
    }
    private HBox createScopeRadioButtons(Task task) {

        String scopeBaseId = "scopeRadio";

        // RadioButtons
        RadioButton rbExam = new RadioButton("Exam");
        rbExam.setId(scopeBaseId + "Exam");

        RadioButton rbMock = new RadioButton("Mock");
        rbMock.setId(scopeBaseId + "Mock");

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

            RadioButton rb = (RadioButton) selected;
            String text = rb.getText();

            switch (text) {
                case "Exam" -> task.setScope(Scope.EXAM);
                case "Mock" -> task.setScope(Scope.MOCK);
            }
        });

        // Layout
        HBox scopeBox = new HBox(10);
        scopeBox.getChildren().addAll(rbExam, rbMock);

        return scopeBox;
    }

    private HBox createDifficultyRadioButtons(Task task) {

        String baseId = "difficultyRadio";

        // RadioButtons
        RadioButton rbEasy = new RadioButton("Easy");
        rbEasy.setId(baseId + "Easy");

        RadioButton rbMedium = new RadioButton("Medium");
        rbMedium.setId(baseId + "Medium");

        RadioButton rbHard = new RadioButton("Hard");
        rbHard.setId(baseId + "Hard");

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

            RadioButton rb = (RadioButton) newVal;
            String text = rb.getText();

            switch (text) {
                case "Easy" -> task.setDifficulty(Difficulty.EASY);
                case "Medium" -> task.setDifficulty(Difficulty.MEDIUM);
                case "Hard" -> task.setDifficulty(Difficulty.HARD);
            }
        });

        // Layout
        HBox box = new HBox(10);
        box.getChildren().addAll(rbEasy, rbMedium, rbHard);

        return box;
    }

    private HBox createEditPoints(Task task) {

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

        Label label = new Label("Points:");
        label.setId("pointsLabel");

        HBox container = new HBox(10);
        container.getChildren().addAll(label, pointSpinner);

        return container;
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

    private VBox variantRow(Variant variant, int size) {
        VBox row = new VBox();
        row.setSpacing(10);

        // Question
        Label questionHeader = new Label("Question:");
        TextArea questionArea = createTextArea(variant.getQuestion());

        questionArea.textProperty().addListener((obs, oldText, newText) -> {
            variant.setQuestion(newText);
            questionArea.setPrefHeight(getHeight(questionArea, newText));
        });

        row.getChildren().addAll(questionHeader, questionArea);

        // Answer
        Label answerHeader = new Label("Answer:");
        TextArea answerArea = createTextArea(variant.getAnswer());

        answerArea.textProperty().addListener((obs, oldText, newText) -> {
            variant.setAnswer(newText);
            answerArea.setPrefHeight(getHeight(answerArea, newText));
        });

        row.getChildren().addAll(answerHeader, answerArea);

        // Delete Button
        if (size > 1) {
            Button deleteBtn = getDeleteVariantButton(variant, row);
            row.getChildren().add(deleteBtn);
        }

        return row;
    }

    private Button getDeleteVariantButton(Variant variant, VBox row) {
        Button deleteBtn = getDeleteButton("Delete Variant");

        deleteBtn.setOnAction(event -> {
            // Confirmation Dialog
            showAlert("Delete Variant",
                    "Are you sure you want to delete variant \"" + variant.getName() + "\"?",
                    () -> {
                        ((Task) App.getDataService().selectedItem).deleteVariant(variant);
                        tableContent.getChildren().remove(row);
                        EventService.triggerPdfUpdate();
                    });
        });
        return deleteBtn;
    }

    private void loadChapterContent(Chapter chapter) {
        // Update Form Content
        txtName.setText(chapter.getName());

        // Update Button Text
        addNewChild.setText("New Task");

        // Update chapterDetails
        chapterDetails.getChildren().add(chapterRow(chapter, 0));

        // Loading the Header
        HBox header = new HBox();
        header.setSpacing(10);

        Label name = new Label("Name");
        name.setMinWidth(200);
        header.getChildren().add(name);

        Label amounts = new Label("Amounts");
        amounts.setMinWidth(100);
        header.getChildren().add(amounts);

        Label difficulty = new Label("Difficulty");
        difficulty.setMinWidth(50);
        header.getChildren().add(difficulty);

        Label scope = new Label("Scope");
        scope.setMinWidth(50);
        header.getChildren().add(scope);

        tableHeader.getChildren().add(header);

        // Loading the rows
        int index = 0;
        for (Task task : chapter.getTasks()) {
            tableContent.getChildren().add(taskRow(task, index));
            index++;
        }

    }

    private HBox taskRow(Task task, int index) {
        HBox row = new HBox();
        row.setSpacing(10);

        // Name
        Label taskName = new Label(task.getName());
        taskName.setMinWidth(200);
        row.getChildren().add(taskName);

        // Amounts (2 Labels in 1 column)
        Label numVariants = new Label(task.getNumberOfVariants().toString());
        numVariants.setMinWidth(50);
        row.getChildren().add(numVariants);

        Label numPoints = new Label(task.getPoints().toString());
        numPoints.setMinWidth(50);
        row.getChildren().add(numPoints);

        // Difficulty
        Label difficulty = new Label(task.getDifficulty().toString());
        difficulty.setMinWidth(50);
        row.getChildren().add(difficulty);

        // Scope
        Label scope = new Label(task.getScope().toString());
        scope.setMinWidth(50);
        row.getChildren().add(scope);

        // Edit Button
        Button editBtn = getEditButton("Edit Task");

        editBtn.setOnAction(event -> {
            DataService dataService = App.getDataService(); // Get the DataService
            dataService.selectedItem = task;
            dataService.path.add(index);
            EventService.triggerPdfUpdate();
        });

        row.getChildren().add(editBtn);

        // Delete Button
        Button deleteBtn = getDeleteTaskButton(task, row);
        row.getChildren().add(deleteBtn);

        return  row;
    }

    private Button getDeleteTaskButton(Task task, HBox row) {
        Button deleteBtn = getDeleteButton("Delete Task");

        deleteBtn.setOnAction(event -> {
            // Confirmation Dialog
            showAlert("Delete Task",
                    "Are you sure you want to delete task \"" + task.getName() + "\" with all its Variants?",
                    () -> {
                        ((Chapter) App.getDataService().selectedItem).deleteTask(task);
                        tableContent.getChildren().remove(row);
                        EventService.triggerPdfUpdate();
                    });
        });
        return deleteBtn;
    }

    private void loadExamContent(Exam exam) {
        // Update Form Content
        txtName.setText(exam.getName());

        // Update Button Text
        addNewChild.setText("New Chapter");

        // Loading the Header
        HBox header = new HBox();
        header.setSpacing(10);

        Label name = new Label("Name");
        name.setMinWidth(200);
        header.getChildren().add(name);

        Label amounts = new Label("Amounts");
        amounts.setMinWidth(150);
        header.getChildren().add(amounts);

        Label questions = new Label("Questions");
        questions.setMinWidth(150);
        header.getChildren().add(questions);

        tableHeader.getChildren().add(header);

        // Loading the rows
        int index = 0;
        for (Chapter chapter : exam.getChapters()) {
            tableContent.getChildren().add(chapterRow(chapter, index));
            index++;
        }
    }

    private HBox chapterRow(Chapter chapter, int index) {
        HBox row = new HBox();
        row.setSpacing(10);

        // Name
        Label chapterName = new Label(chapter.getName());
        chapterName.setMinWidth(200);
        row.getChildren().add(chapterName);

        // Amounts (3 Labels in 1 column)
        Label numTasks = new Label(chapter.getNumberOfTasks().toString());
        numTasks.setMinWidth(50);
        row.getChildren().add(numTasks);

        Label numVariants = new Label(chapter.getNumberOfVariants().toString());
        numVariants.setMinWidth(50);
        row.getChildren().add(numVariants);

        Label numPoints = new Label(chapter.getNumberOfPoints().toString());
        numPoints.setMinWidth(50);
        row.getChildren().add(numPoints);

        // Difficulty (3 Labels in 1 column)
        Label numEasy = new Label(chapter.getNumberOfDifficultyTasks(Difficulty.EASY).toString());
        numEasy.setMinWidth(50);
        row.getChildren().add(numEasy);

        Label numMedium = new Label(chapter.getNumberOfDifficultyTasks(Difficulty.MEDIUM).toString());
        numMedium.setMinWidth(50);
        row.getChildren().add(numMedium);

        Label numHard = new Label(chapter.getNumberOfDifficultyTasks(Difficulty.HARD).toString());
        numHard.setMinWidth(50);
        row.getChildren().add(numHard);

        DataService dataService = App.getDataService(); // Get the DataService
        if (dataService.type == Type.EXAM) {
            // Edit Button
            Button editBtn = getEditButton("Edit Task");

            editBtn.setOnAction(event -> {
                dataService.selectedItem = chapter;
                dataService.path.add(index);
                EventService.triggerPdfUpdate();
            });

            row.getChildren().add(editBtn);

            // Delete Button
            Button deleteBtn = getDeleteChapterButton(chapter, row);
            row.getChildren().add(deleteBtn);
        }

        return row;
    }

    private Button getDeleteChapterButton(Chapter chapter, HBox row) {
        Button deleteBtn = getDeleteButton("Delete Chapter");

        deleteBtn.setOnAction(event -> {
            // Confirmation Dialog
            showAlert("Delete Chapter",
                    "Are you sure you want to delete chapter \"" + chapter.getName() + "\" with all its Task and their Variants?",
                    () -> {
                ((Exam) App.getDataService().selectedItem).deleteChapter(chapter);
                tableContent.getChildren().remove(row);
                EventService.triggerPdfUpdate();
            });
        });
        return deleteBtn;
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
                BootstrapFX.bootstrapFXStylesheet());

        ButtonType yesButton = new ButtonType("Yes/ Delete", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("No/ Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
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

    private Button getEditButton(String toolTip) {
        Button editBtn = new Button("✎"); // Icon als Text oder setze ein Image

        editBtn.setStyle("-fx-background-color: transparent;");

        editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                "-fx-background-color: rgba(0,255,0,0.1);"));

        editBtn.setOnMouseExited(e -> editBtn.setStyle(
                "-fx-background-color: transparent;"));

        editBtn.setTooltip(new Tooltip(toolTip));

        return editBtn;
    }

    private Button getDeleteButton(String toolTip) {
        Button deleteBtn = new Button("🗑"); // Icon als Text oder setze ein Image

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

        dataService.selectedItem.setName(txtName.getText());

        EventService.triggerPdfUpdate();
    }

    public void cancel() {
        EventService.triggerPdfUpdate();
    }
}
