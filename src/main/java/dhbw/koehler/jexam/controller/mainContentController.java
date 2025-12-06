package dhbw.koehler.jexam.controller;

import dhbw.koehler.jexam.App;
import dhbw.koehler.jexam.model.Chapter;
import dhbw.koehler.jexam.model.Exam;
import dhbw.koehler.jexam.model.Task;
import dhbw.koehler.jexam.model.Variant;
import dhbw.koehler.jexam.model.enums.Difficulty;
import dhbw.koehler.jexam.service.DataService;
import dhbw.koehler.jexam.service.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    public void initialize() {
        loadContent();
    }

    public void updateContent() {
        loadContent();
    }

    private void loadContent() {
        DataService dataService = App.getDataService(); // Get the DataService

        chapterDetails.getChildren().clear();
        tableContent.getChildren().clear();
        tableHeader.getChildren().clear();

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

        // Update Button Text
        addNewChild.setText("New Variant");

        // Row Content
        for(Variant variant : task.getVariants()) {
            tableContent.getChildren().add(variantRow(variant));
        }
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

        EventService.triggerUpdate();
    }

    public void cancel() {
        EventService.triggerUpdate();
    }
}
