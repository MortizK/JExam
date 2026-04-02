package com.jexam.app.ui.components.xml;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

/**
 * Inline exam name editor.
 */
public final class ExamHeaderEditor extends HBox {
    private final TextField nameField = new TextField();
    private Consumer<Void> changeHandler = ignored -> { };
    private boolean updating;

    public ExamHeaderEditor() {
        setSpacing(8);
        setPadding(new Insets(8, 0, 8, 0));
        getChildren().addAll(new Label("Exam"), nameField);
        HBox.setHgrow(nameField, Priority.ALWAYS);
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
    }

    public void setExamName(final String name) {
        updating = true;
        try {
            nameField.setText(name);
        } finally {
            updating = false;
        }
    }

    public String getExamName() {
        return nameField.getText();
    }

    public void clear() {
        setExamName("");
    }

    public void setOnChange(final Runnable handler) {
        changeHandler = ignored -> {
            if (handler != null) {
                handler.run();
            }
        };
    }
}