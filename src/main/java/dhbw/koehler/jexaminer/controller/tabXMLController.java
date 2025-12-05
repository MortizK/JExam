package dhbw.koehler.jexaminer.controller;

import dhbw.koehler.jexaminer.model.enums.Type;
import dhbw.koehler.jexaminer.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class tabXMLController {

    @FXML
    private HBox breadCrumbs;

    @FXML
    private TreeView<String> xmlTreeView;

    private DataService dataService;

    public tabXMLController() {
        this.dataService = new DataService("My Exam");
    }

    @FXML
    public void initialize() {
        populateTree();

        xmlTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                handleTreeSelection(newSelection);
            }
        });
    }

    //=== Update Logic ===\\

    private void handleTreeSelection(TreeItem<String> selectedItem) {
        List<Integer> path = getTreeItemPath(selectedItem);

        updateData(path);
    }

    private void updateData(List<Integer> path) {
        dataService.updateSelectedFromPath(path);

        updateBreadcrumbs(dataService.breadCrumbs, dataService.path);
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

                // Update Selection of TreeView
                selectTreeItemByPath(newPath);
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
