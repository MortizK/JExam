package com.jexam.app.ui.components.pdf;

import com.jexam.validation.ValidationError;
import com.jexam.validation.ValidationResult;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Displays validation issues grouped by top-level path segment.
 */
public final class ValidationSummaryComponent extends VBox {
    private final Label title = new Label("Validation");
    private final Label summary = new Label();
    private final TreeView<String> issueTree = new TreeView<>();
    private final Map<TreeItem<String>, String> pathByLeaf = new LinkedHashMap<>();

    private Consumer<String> issueSelectedHandler = path -> { };

    public ValidationSummaryComponent() {
        setSpacing(8);
        setPadding(new Insets(8));
        issueTree.setShowRoot(false);
        issueTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            String path = pathByLeaf.get(newValue);
            if (path != null && !path.isBlank()) {
                issueSelectedHandler.accept(path);
            }
        });
        getChildren().addAll(title, summary, issueTree);
        setValidationResult(new ValidationResult());
    }

    public void setValidationResult(final ValidationResult result) {
        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);
        pathByLeaf.clear();

        if (result == null || result.isValid()) {
            summary.setText("All validations passed. Ready to generate.");
            issueTree.setRoot(root);
            return;
        }

        summary.setText("Found " + result.getErrors().size() + " issue(s)");
        Map<String, TreeItem<String>> groups = new LinkedHashMap<>();
        for (ValidationError error : result.getErrors()) {
            String path = error.getPath();
            String groupKey = path;
            int splitIndex = path.indexOf('.', path.indexOf(']') + 1);
            if (splitIndex > 0) {
                groupKey = path.substring(0, splitIndex);
            }

            TreeItem<String> group = groups.computeIfAbsent(groupKey, key -> {
                TreeItem<String> item = new TreeItem<>(key);
                item.setExpanded(true);
                root.getChildren().add(item);
                return item;
            });
            TreeItem<String> leaf = new TreeItem<>(error.getMessage());
            pathByLeaf.put(leaf, path);
            group.getChildren().add(leaf);
        }
        issueTree.setRoot(root);
    }

    public void setOnIssueSelected(final Consumer<String> handler) {
        issueSelectedHandler = handler == null ? path -> { } : handler;
    }
}