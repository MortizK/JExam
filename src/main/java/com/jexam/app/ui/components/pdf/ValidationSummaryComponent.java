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
 *
 * @author Moritz
 */
public final class ValidationSummaryComponent extends VBox {
    private static final String CLASS_STATE_SUCCESS = "validation-success";
    private static final String CLASS_STATE_ERROR = "validation-error";

    private final Label title = new Label("Validation");
    private final Label summary = new Label();
    private final TreeView<String> issueTree = new TreeView<>();
    private final Map<TreeItem<String>, String> pathByLeaf = new LinkedHashMap<>();

    private Consumer<String> issueSelectedHandler = path -> { };
    private boolean hasIssues;

    /**
     * Creates the validation summary panel with grouped issue tree.
     */
    public ValidationSummaryComponent() {
        getStyleClass().add("validation-summary");
        setSpacing(8);
        setPadding(new Insets(8));

        title.getStyleClass().add("section-title");
        summary.getStyleClass().add("summary-text");
        issueTree.getStyleClass().add("validation-tree");
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

    /**
     * Renders validation output and groups issues by top-level hierarchy path.
     *
     * @param result validation result to display
     */
    public void setValidationResult(final ValidationResult result) {
        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);
        pathByLeaf.clear();
        hasIssues = false;
        getStyleClass().removeAll(CLASS_STATE_SUCCESS, CLASS_STATE_ERROR);

        if (result == null || result.isValid()) {
            getStyleClass().add(CLASS_STATE_SUCCESS);
            summary.setText("All validations passed. Ready to generate.");
            issueTree.setRoot(root);
            return;
        }

        hasIssues = true;
        getStyleClass().add(CLASS_STATE_ERROR);
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

    /**
     * Registers callback for selecting a concrete validation issue path.
     *
     * @param handler callback receiving selected issue path; {@code null} resets to no-op
     */
    public void setOnIssueSelected(final Consumer<String> handler) {
        issueSelectedHandler = handler == null ? path -> { } : handler;
    }

    /**
     * Requests keyboard focus for the issue tree control.
     */
    public void requestIssueTreeFocus() {
        issueTree.requestFocus();
    }

    /**
     * Returns whether current validation output contains issues.
     *
     * @return {@code true} when at least one issue exists
     */
    public boolean hasIssues() {
        return hasIssues;
    }
}
