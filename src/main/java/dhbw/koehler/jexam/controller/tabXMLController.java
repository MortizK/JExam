package dhbw.koehler.jexam.controller;

import dhbw.koehler.jexam.App;
import dhbw.koehler.jexam.service.DataService;
import dhbw.koehler.jexam.service.EventService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class tabXMLController {

    @FXML
    public VBox xmlMainContent;

    @FXML
    private HBox breadCrumbs;

    @FXML
    private TreeView<String> xmlTreeView;

    private mainContentController mainContentController;

    public tabXMLController() {}

    @FXML
    public void initialize() {
        EventService.setPdfUpdate(this::reload);

        populateTree();

        updateData(new ArrayList<>());

        loadMainContent();

        xmlTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                handleTreeSelection(newSelection);
            }
        });
    }

    //=== Update Logic ===\\

    private void reload() {
        DataService dataService = App.getDataService(); // Get the DataService
        populateTree();
        updateData(dataService.path);
    }

    private void handleTreeSelection(TreeItem<String> selectedItem) {
        List<Integer> path = getTreeItemPath(selectedItem);

        updateData(path);
    }

    private void updateData(List<Integer> path) {
        DataService dataService = App.getDataService(); // Get the DataService
        dataService.updateSelectedFromPath(path);

        updateBreadcrumbs(dataService.breadCrumbs, dataService.path);

        // Update Selection of TreeView
        selectTreeItemByPath(path);

        if (mainContentController != null) {
            mainContentController.updateContent();
        }
    }

    //=== Main Content ===\\

    private void loadMainContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/mainContent.fxml"));
            VBox xmlContent = loader.load();

            mainContentController = loader.getController();

            xmlMainContent.getChildren().add(xmlContent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //=== Breadcrumbs Management ===\\

    private void updateBreadcrumbs(List<String> breadcrumbNames, List<Integer> path) {
        breadCrumbs.getChildren().clear();

        for (int i = 0; i < breadcrumbNames.size(); i++) {
            String name = breadcrumbNames.get(i);
            Button btn = new Button(name);
            btn.setId("breadcrumb-" + i);

            final int index = i;
            btn.setOnAction(e -> {
                List<Integer> newPath = path.subList(0, index);
                updateData(newPath);
            });

            breadCrumbs.getChildren().add(btn);

            if (i < breadcrumbNames.size() - 1) {
                Label separator = new Label(" / ");
                breadCrumbs.getChildren().add(separator);
            }
        }
    }

    // === Tree Management ===\\

    private TreeItem<String> getTreeItemByPath(TreeItem<String> root, List<Integer> path) {
        TreeItem<String> current = root;
        for (int index : path) {
            // Path invalid
            if (current == null || current.getChildren().size() <= index) {
                return null;
            }
            current = current.getChildren().get(index);
        }
        return current;
    }

    private void selectTreeItemByPath(List<Integer> path) {
        TreeItem<String> root = xmlTreeView.getRoot();
        TreeItem<String> itemToSelect = getTreeItemByPath(root, path);

        if (itemToSelect != null) {
            xmlTreeView.getSelectionModel().select(itemToSelect);
            xmlTreeView.scrollTo(xmlTreeView.getRow(itemToSelect)); // optional: Scrollen
        }
    }

    private List<Integer> getTreeItemPath(TreeItem<String> selectedItem) {
        List<Integer> path = new ArrayList<>();
        TreeItem<?> current = selectedItem;
        while (current.getParent() != null) { // Root hat null Parent
            TreeItem<?> parent = current.getParent();
            int index = parent.getChildren().indexOf(current);
            path.add(0, index); // vorne einfügen, damit Root zuerst kommt
            current = parent;
        }
        return path;
    }

    private void populateTree() {
        DataService dataService = App.getDataService(); // Get the DataService
        if (dataService == null || dataService.exam == null) return;

        // Root des TreeView ist der Exam-Name
        TreeItem<String> rootItem = new TreeItem<>(dataService.exam.getName());
        rootItem.setExpanded(true);

        // Kapitel durchlaufen
        dataService.exam.getChapters().forEach(chapter -> {
            TreeItem<String> chapterItem = new TreeItem<>(chapter.getName());

            // Tasks durchlaufen
            chapter.getTasks().forEach(task -> {
                TreeItem<String> taskItem = new TreeItem<>(task.getName());
                chapterItem.getChildren().add(taskItem);
            });

            rootItem.getChildren().add(chapterItem);
        });

        xmlTreeView.setRoot(rootItem);
    }
}
