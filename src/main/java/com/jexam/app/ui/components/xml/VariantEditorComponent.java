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
    private final TextArea questionArea = new TextArea();
    private final TextArea answerArea = new TextArea();
    private Consumer<Void> changeHandler = ignored -> { };
    private boolean updating;

    public VariantEditorComponent() {
        setSpacing(8);
        setPadding(new Insets(8, 0, 8, 0));
        questionArea.setPrefRowCount(3);
        answerArea.setPrefRowCount(3);
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
        getChildren().addAll(new Label("Question"), questionArea, new Label("Answer"), answerArea);
    }

    public void setQuestionText(final String value) {
        runWithoutUpdates(() -> questionArea.setText(value));
    }

    public void setAnswerText(final String value) {
        runWithoutUpdates(() -> answerArea.setText(value));
    }

    public String getQuestionText() {
        return questionArea.getText();
    }

    public String getAnswerText() {
        return answerArea.getText();
    }

    public void clear() {
        runWithoutUpdates(() -> {
            questionArea.clear();
            answerArea.clear();
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