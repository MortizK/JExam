package com.jexam.app.ui.components.pdf;

import com.jexam.generation.GenerationMode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Top-left generation mode and actions section.
 */
public final class GenerationControlsComponent extends VBox {
    private final ComboBox<GenerationMode> modeBox = new ComboBox<>(FXCollections.observableArrayList(GenerationMode.values()));
    private final Button previewButton = new Button("Generate Preview");
    private final Button exportButton = new Button("Export PDF");
    private final Label staleLabel = new Label("Preview is stale. Refresh manually.");

    private Runnable previewHandler = () -> { };
    private Runnable exportHandler = () -> { };
    private Consumer<GenerationMode> modeHandler = mode -> { };

    public GenerationControlsComponent() {
        setSpacing(8);
        setPadding(new Insets(8));

        modeBox.setValue(GenerationMode.EXAM);
        modeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                modeHandler.accept(newValue);
            }
        });
        previewButton.setOnAction(event -> previewHandler.run());
        exportButton.setOnAction(event -> exportHandler.run());
        staleLabel.setVisible(false);
        staleLabel.setManaged(false);

        modeBox.setAccessibleText("Generation mode selector");
        previewButton.setAccessibleText("Generate PDF preview");
        exportButton.setAccessibleText("Export the selected PDF variant");
        staleLabel.setAccessibleText("Preview stale indicator");

        getChildren().addAll(new Label("Generation Mode"), modeBox, staleLabel, previewButton, exportButton);
    }

    public GenerationMode getSelectedMode() {
        return modeBox.getValue();
    }

    public void setStaleIndicatorVisible(final boolean value) {
        staleLabel.setVisible(value);
        staleLabel.setManaged(value);
    }

    public void setOnModeChanged(final Consumer<GenerationMode> handler) {
        modeHandler = handler == null ? mode -> { } : handler;
    }

    public void setOnPreviewRequested(final Runnable handler) {
        previewHandler = handler == null ? () -> { } : handler;
    }

    public void setOnExportRequested(final Runnable handler) {
        exportHandler = handler == null ? () -> { } : handler;
    }
}