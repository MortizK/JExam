package dhbw.koehler.jexam.controller;

import dhbw.koehler.jexam.App;
import dhbw.koehler.jexam.model.Chapter;
import dhbw.koehler.jexam.service.DataService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class tabPDFController {
    @FXML
    public VBox pdfSidebar;

    @FXML
    public VBox pdfMainContent;

    @FXML
    public HBox generation;

    public List<Chapter> chapterList;

    public int cutOff;

    @FXML
    public void initialize() {
        loadSidebar();
        loadOperations();
    }

    private void loadOperations() {
        Button generateExam = new Button("Generate Exam");
        Button generateMock = new Button("Generate Mock Exam");

        generation.getChildren().addAll(generateExam, generateMock);
    }

    private void loadSidebar() {
        DataService dataService = App.getDataService(); // Get the DataService
        chapterList = dataService.exam.getChapters();
        cutOff = chapterList.size();

        pdfSidebar.getChildren().clear();
        Label include = new Label("Change Order of the Chapters:");
        pdfSidebar.getChildren().add(include);

        int index = 0;
        for (Chapter chapter : chapterList) {
            createChapter(chapter);
            index++;
            if (index == cutOff) {
                Label exclude = new Label("Excluded from PDF generation:");
                pdfSidebar.getChildren().add(exclude);
            }
        }
    }

    private void createChapter(Chapter chapter) {
        HBox row = new HBox();

        Label chapterName = new Label(chapter.getName());
        MenuButton pointsDropDown = createPointsMenuButton(new double[] {1.0, 2.0, 3.0});

        Button btnUp = new Button("UP");
        Button btnDown = new Button("DOWN");

        row.getChildren().addAll(chapterName, pointsDropDown,  btnUp, btnDown);

        pdfSidebar.getChildren().add(row);
    }

    private MenuButton createPointsMenuButton(double[] options) {
        MenuButton menuButton = new MenuButton(String.valueOf(options[0]));

        for (double value : options) {
            MenuItem item = new MenuItem(String.valueOf(value));
            item.setOnAction(e -> menuButton.setText(String.valueOf(value)));
            menuButton.getItems().add(item);
        }

        return menuButton;
    }
}
