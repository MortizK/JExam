package dhbw.koehler.jexam.controller;

import dhbw.koehler.jexam.App;
import dhbw.koehler.jexam.model.Chapter;
import dhbw.koehler.jexam.model.Exam;
import dhbw.koehler.jexam.model.Task;
import dhbw.koehler.jexam.model.Variant;
import dhbw.koehler.jexam.model.enums.Difficulty;
import dhbw.koehler.jexam.model.enums.Scope;
import dhbw.koehler.jexam.service.DataService;
import dhbw.koehler.jexam.service.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

        EventService.triggerUpdate();
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
        for(Variant variant : task.getVariants()) {
            tableContent.getChildren().add(variantRow(variant));
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

    private VBox variantRow(Variant variant) {
        VBox row = new VBox();
        row.setSpacing(10);

        // Question
        Label questionHeader = new Label("Question:");
        row.getChildren().add(questionHeader);

        Label question = new Label(variant.getQuestion());
        row.getChildren().add(question);

        // Answer
        Label answerHeader = new Label("Answer:");
        row.getChildren().add(answerHeader);

        Label answer = new Label(variant.getAnswer());
        row.getChildren().add(answer);

        return row;
    }

    private void loadChapterContent(Chapter chapter) {
        // Update Form Content
        txtName.setText(chapter.getName());

        // Update Button Text
        addNewChild.setText("New Task");

        // Update chapterDetails
        chapterDetails.getChildren().add(chapterRow(chapter));

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
        for (Task task : chapter.getTasks()) {
            tableContent.getChildren().add(taskRow(task));
        }

    }

    private HBox taskRow(Task task) {
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

        return  row;
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
        for (Chapter chapter : exam.getChapters()) {
            tableContent.getChildren().add(chapterRow(chapter));
        }
    }

    private HBox chapterRow(Chapter chapter) {
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

        Label numMedium =  new Label(chapter.getNumberOfDifficultyTasks(Difficulty.MEDIUM).toString());
        numMedium.setMinWidth(50);
        row.getChildren().add(numMedium);

        Label numHard = new Label(chapter.getNumberOfDifficultyTasks(Difficulty.HARD).toString());
        numHard.setMinWidth(50);
        row.getChildren().add(numHard);

        return row;
    }

    //=== From Handler ===\\

    public void save() {
        DataService dataService = App.getDataService(); // Get the DataService

        dataService.selectedItem.setName(txtName.getText());

        // BUG: Closes all Chapters of the TreeView
        EventService.triggerUpdate();
    }

    public void cancel() {
        EventService.triggerUpdate();
    }
}
