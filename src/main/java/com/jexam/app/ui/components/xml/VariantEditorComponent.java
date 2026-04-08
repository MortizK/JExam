package com.jexam.app.ui.components.xml;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Inline variant editor for question and answer text.
 */
public final class VariantEditorComponent extends VBox {
    private final Label questionLabel = new Label("Question");
    private final Label answerLabel = new Label("Answer");
    private final TextArea questionArea = new TextArea();
    private final TextArea answerArea = new TextArea();
    private Consumer<Void> changeHandler = ignored -> { };
    private boolean updating;

    /**
     * Creates the inline variant editor with question and answer text areas.
     */
    public VariantEditorComponent() {
        getStyleClass().add("variant-editor");
        setSpacing(8);
        setPadding(new Insets(8, 0, 8, 0));
        questionLabel.getStyleClass().add("section-label");
        answerLabel.getStyleClass().add("section-label");
        questionArea.getStyleClass().add("editor-area");
        answerArea.getStyleClass().add("editor-area");
        questionArea.setPrefRowCount(3);
        answerArea.setPrefRowCount(3);
        questionArea.setWrapText(true);
        answerArea.setWrapText(true);
        questionArea.setAccessibleText("Variant question field");
        answerArea.setAccessibleText("Variant answer field");
        questionArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
        answerArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating) {
                changeHandler.accept(null);
            }
        });
        getChildren().addAll(questionLabel, questionArea, answerLabel, answerArea);
    }

    /**
     * Updates the question text area without triggering change callbacks.
     *
     * @param value question text
     */
    public void setQuestionText(final String value) {
        runWithoutUpdates(() -> questionArea.setText(value));
    }

    /**
     * Updates the answer text area without triggering change callbacks.
     *
     * @param value answer text
     */
    public void setAnswerText(final String value) {
        runWithoutUpdates(() -> answerArea.setText(value));
    }

    /**
     * Returns the current question text.
     *
     * @return question content
     */
    public String getQuestionText() {
        return questionArea.getText();
    }

    /**
     * Returns the current answer text.
     *
     * @return answer content
     */
    public String getAnswerText() {
        return answerArea.getText();
    }

    /**
     * Clears both question and answer text areas.
     */
    public void clear() {
        runWithoutUpdates(() -> {
            questionArea.clear();
            answerArea.clear();
        });
    }

    /**
     * Registers a callback for variant-text changes.
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
     * Requests keyboard focus for the question text area.
     */
    public void requestEditorFocus() {
        questionArea.requestFocus();
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