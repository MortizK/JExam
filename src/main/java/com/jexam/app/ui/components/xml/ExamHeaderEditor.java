package com.jexam.app.ui.components.xml;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.Consumer;

/**
 * Inline exam name editor.
 *
 * @author Moritz
 */
public final class ExamHeaderEditor extends HBox {
    private final Label titleLabel = new Label("Exam");
    private final TextField nameField = new TextField();
    private Consumer<Void> changeHandler = ignored -> { };
    private boolean updating;

    /**
     * Creates the inline exam name editor.
     */
    public ExamHeaderEditor() {
        getStyleClass().add("exam-header-editor");
        setSpacing(8);
        setPadding(new Insets(8, 0, 8, 0));
        titleLabel.getStyleClass().add("section-label");
        nameField.getStyleClass().add("editor-input");
        getChildren().addAll(titleLabel, nameField);
        HBox.setHgrow(nameField, Priority.ALWAYS);
        nameField.setAccessibleText("Exam name field");
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
    }

    /**
     * Updates the exam name field without triggering change callbacks.
     *
     * @param name exam name text
     */
    public void setExamName(final String name) {
        updating = true;
        try {
            nameField.setText(name);
        } finally {
            updating = false;
        }
    }

    /**
     * Returns the current exam name text.
     *
     * @return exam name
     */
    public String getExamName() {
        return nameField.getText();
    }

    /**
     * Clears the exam name field.
     */
    public void clear() {
        setExamName("");
    }

    /**
     * Registers a callback for exam-name changes.
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
     * Requests keyboard focus for the name field.
     */
    public void requestEditorFocus() {
        nameField.requestFocus();
    }
}
