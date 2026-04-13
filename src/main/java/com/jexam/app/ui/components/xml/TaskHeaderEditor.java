package com.jexam.app.ui.components.xml;

import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

/**
 * Inline task metadata editor.
 */
public final class TaskHeaderEditor extends GridPane {
    private final TextField nameField = new TextField();
    private final TextField pointsField = new TextField();
    private final ComboBox<Difficulty> difficultyBox = new ComboBox<>(FXCollections.observableArrayList(Difficulty.values()));
    private final ComboBox<Scope> scopeBox = new ComboBox<>(FXCollections.observableArrayList(Scope.values()));
    private Consumer<Void> changeHandler = ignored -> { };
    private boolean updating;

    /**
     * Creates the inline task metadata editor with name, points, difficulty, and scope fields.
     */
    public TaskHeaderEditor() {
        getStyleClass().add("task-header-editor");
        setHgap(8);
        setVgap(8);
        setPadding(new Insets(8, 0, 8, 0));

        Label taskLabel = new Label("Task");
        taskLabel.getStyleClass().add("section-label");
        add(taskLabel, 0, 0);
        add(nameField, 1, 0);
        Label pointsLabel = new Label("Points");
        pointsLabel.getStyleClass().add("section-label");
        add(pointsLabel, 0, 1);
        add(pointsField, 1, 1);
        Label difficultyLabel = new Label("Difficulty");
        difficultyLabel.getStyleClass().add("section-label");
        add(difficultyLabel, 2, 0);
        add(difficultyBox, 3, 0);
        Label scopeLabel = new Label("Scope");
        scopeLabel.getStyleClass().add("section-label");
        add(scopeLabel, 2, 1);
        add(scopeBox, 3, 1);

        nameField.getStyleClass().add("editor-input");
        pointsField.getStyleClass().add("editor-input");
        difficultyBox.getStyleClass().add("editor-select");
        scopeBox.getStyleClass().add("editor-select");

        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(pointsField, Priority.ALWAYS);

        nameField.setAccessibleText("Task name field");
        pointsField.setAccessibleText("Task points field");
        difficultyBox.setAccessibleText("Task difficulty selector");
        scopeBox.setAccessibleText("Task scope selector");

        pointsField.setTextFormatter(new TextFormatter<>(change -> {
            String next = change.getControlNewText();
            if (next == null || next.isEmpty() || next.matches("\\d*(?:\\.\\d?)?")) {
                return change;
            }
            return null;
        }));

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
        pointsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
        difficultyBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
        scopeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
    }

    /**
     * Updates the task name field without triggering change callbacks.
     *
     * @param value task name text
     */
    public void setTaskName(final String value) {
        runWithoutUpdates(() -> nameField.setText(value));
    }

    /**
     * Updates the points field without triggering change callbacks.
     *
     * @param value points value
     */
    public void setPoints(final double value) {
        runWithoutUpdates(() -> pointsField.setText(Double.toString(value)));
    }

    /**
     * Updates the difficulty selector without triggering change callbacks.
     *
     * @param value difficulty enumeration
     */
    public void setDifficulty(final Difficulty value) {
        runWithoutUpdates(() -> difficultyBox.setValue(value));
    }

    /**
     * Updates the scope selector without triggering change callbacks.
     *
     * @param value scope enumeration
     */
    public void setScope(final Scope value) {
        runWithoutUpdates(() -> scopeBox.setValue(value));
    }

    /**
     * Returns the current task name text.
     *
     * @return task name
     */
    public String getTaskName() {
        return nameField.getText();
    }

    /**
     * Returns the current points field text.
     *
     * @return points text (may be invalid/empty)
     */
    public String getPointsText() {
        return pointsField.getText();
    }

    /**
     * Returns the currently selected difficulty.
     *
     * @return difficulty value
     */
    public Difficulty getDifficulty() {
        return difficultyBox.getValue();
    }

    /**
     * Returns the currently selected scope.
     *
     * @return scope value
     */
    public Scope getScope() {
        return scopeBox.getValue();
    }

    /**
     * Clears all task metadata fields.
     */
    public void clear() {
        runWithoutUpdates(() -> {
            nameField.clear();
            pointsField.clear();
            difficultyBox.setValue(null);
            scopeBox.setValue(null);
        });
    }

    /**
     * Registers a callback for task-metadata changes.
     *
     * @param handler callback for change events; {@code null} clears callback
     */
    public void setOnChange(final Runnable handler) {
        changeHandler = ignored -> {
            if (handler != null) {
                handler.run();
            }
        };
    }

    /**
     * Requests keyboard focus for the first input field.
     */
    public void requestEditorFocus() {
        nameField.requestFocus();
    }

    /**
     * Executes an action with change-event notifications suppressed.
     *
     * @param action runnable to execute
     */
    private void runWithoutUpdates(final Runnable action) {
        updating = true;
        try {
            action.run();
        } finally {
            updating = false;
        }
    }
}