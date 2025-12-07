package dhbw.koehler.jexam.controller;

import dhbw.koehler.jexam.App;
import dhbw.koehler.jexam.model.Chapter;
import dhbw.koehler.jexam.service.DataService;
import dhbw.koehler.jexam.service.EventService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class tabPDFController {
    @FXML
    public VBox pdfSidebar;

    @FXML
    public VBox pdfMainContent;

    @FXML
    public HBox generation;

    public List<Chapter> chapterList = new ArrayList<>();

    public int cutOff;

    @FXML
    public void initialize() {
        EventService.setXmlUpdate(this::reload);
        reload();
    }

    private void reload() {
        DataService dataService = App.getDataService(); // Get the DataService
        chapterList = dataService.exam.getChapters();
        cutOff = chapterList.size();

        loadSidebar();
        loadOperations();
    }

    private void loadOperations() {
        generation.getChildren().clear();

        Button generateExam = new Button("Generate Exam");
        generateExam.getStyleClass().addAll("btn", "btn-success");

        Button generateMock = new Button("Generate Mock Exam");
        generateMock.getStyleClass().addAll("btn", "btn-primary");

        HBox.setMargin(generateExam, new Insets(0, 10, 0, 0));

        generation.getChildren().addAll(generateExam, generateMock);
    }

    private void loadSidebar() {
        pdfSidebar.getChildren().clear();

        Label include = new Label("Change Order of the Chapters:");
        include.getStyleClass().addAll("h5", "mb-3", "fw-bold");

        pdfSidebar.getChildren().add(include);

        if (cutOff == 0) {
            addCutOff();
        }

        int index = 0;
        for (Chapter chapter : chapterList) {
            createChapter(chapter);
            index++;
            if (index == cutOff) {
                addCutOff();
            }
        }

        pdfSidebar.setPadding(new Insets(10));
        pdfSidebar.setSpacing(5);
    }

    private void addCutOff() {
        Label cutOffLabel = new Label("Exclude from PDF generation:");
        cutOffLabel.getStyleClass().addAll("h5", "mb-2", "fw-bold");
        pdfSidebar.getChildren().add(cutOffLabel);
    }

    private void createChapter(Chapter chapter) {
        HBox row = new HBox();
        row.setSpacing(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label chapterName = new Label(chapter.getName());

        MenuButton pointsDropDown = createPointsMenuButton(chapter.getPossiblePoints()); // FEATURE MISSING: these points should be saved on this Tab
        pointsDropDown.getStyleClass().addAll("btn", "btn-secondary", "btn-sm");

        Button btnUp = new Button("UP");
        btnUp.getStyleClass().addAll("btn", "btn-outline-primary", "btn-sm");
        btnUp.setOnAction(e -> {
            moveChapter(chapter, -1);
        });

        Button btnDown = new Button("DOWN");
        btnDown.getStyleClass().addAll("btn", "btn-outline-primary", "btn-sm");
        btnDown.setOnAction(e -> {
            moveChapter(chapter, 1);
        });

        row.getChildren().addAll(chapterName, pointsDropDown,  btnUp, btnDown);
        row.setPadding(new Insets(5));
        pdfSidebar.getChildren().add(row);
    }

    private void moveChapter(Chapter chapter, int direction) {
        int index = chapterList.indexOf(chapter);
        int newIndex = index + direction;

        // System.out.println("Direction:" + direction + " \tnewIndex:" + newIndex + " \tcutOff:" + cutOff);

        // Move the cutOff, if needed
        if (direction == 1 && newIndex == cutOff) {
            cutOff--;
            loadSidebar();
            return;
        } else if (direction == -1 && newIndex == cutOff - 1) {
            cutOff++;
            loadSidebar();
            return;
        }

        // Check for the ending
        if (newIndex < 0 || newIndex >= chapterList.size()) {
            return;
        }

        // swap Chapter
        chapterList.remove(index);
        chapterList.add(newIndex, chapter);

        loadSidebar();
    }

    private MenuButton createPointsMenuButton(List<Double> points) {
        MenuButton menuButton = new MenuButton(String.valueOf(points.getFirst()));
        menuButton.getStyleClass().addAll("btn", "btn-info", "btn-sm");

        for (double value : points) {
            MenuItem item = new MenuItem(String.valueOf(value));
            item.setOnAction(e -> menuButton.setText(String.valueOf(value)));
            menuButton.getItems().add(item);
        }

        return menuButton;
    }
}
