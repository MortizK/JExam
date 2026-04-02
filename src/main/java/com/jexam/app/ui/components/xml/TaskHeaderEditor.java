package com.jexam.app.ui.components.xml;

import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

    public TaskHeaderEditor() {
        setHgap(8);
        setVgap(8);
        setPadding(new Insets(8, 0, 8, 0));

        add(new Label("Task"), 0, 0);
        add(nameField, 1, 0);
        add(new Label("Points"), 0, 1);
        add(pointsField, 1, 1);
        add(new Label("Difficulty"), 2, 0);
        add(difficultyBox, 3, 0);
        add(new Label("Scope"), 2, 1);
        add(scopeBox, 3, 1);

        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox.setHgrow(pointsField, Priority.ALWAYS);

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

    public void setTaskName(final String value) {
        runWithoutUpdates(() -> nameField.setText(value));
    }

    public void setPoints(final double value) {
        runWithoutUpdates(() -> pointsField.setText(Double.toString(value)));
    }

    public void setDifficulty(final Difficulty value) {
        runWithoutUpdates(() -> difficultyBox.setValue(value));
    }

    public void setScope(final Scope value) {
        runWithoutUpdates(() -> scopeBox.setValue(value));
    }

    public String getTaskName() {
        return nameField.getText();
    }

    public String getPointsText() {
        return pointsField.getText();
    }

    public Difficulty getDifficulty() {
        return difficultyBox.getValue();
    }

    public Scope getScope() {
        return scopeBox.getValue();
    }

    public void clear() {
        runWithoutUpdates(() -> {
            nameField.clear();
            pointsField.clear();
            difficultyBox.setValue(null);
            scopeBox.setValue(null);
        });
    }

    public void setOnChange(final Runnable handler) {
        changeHandler = ignored -> {
            if (handler != null) {
                handler.run();
            }
        };
    }

    private void runWithoutUpdates(final Runnable action) {
        updating = true;
        try {
            action.run();
        } finally {
            updating = false;
        }
    }
}