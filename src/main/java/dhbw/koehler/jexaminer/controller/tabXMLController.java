package dhbw.koehler.jexaminer.controller;

import dhbw.koehler.jexaminer.model.enums.Type;
import dhbw.koehler.jexaminer.service.DataService;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.List;

public class tabXMLController {

    @FXML
    TreeView<String> xmlTreeView;

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

    private void handleTreeSelection(TreeItem<String> selectedItem) {
        List<Integer> path = getTreeItemPath(selectedItem);

        dataService.updateSelectedFromPath(path);
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
