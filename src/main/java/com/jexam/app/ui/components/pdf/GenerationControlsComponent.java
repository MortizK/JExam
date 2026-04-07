package com.jexam.app.ui.components.pdf;

import com.jexam.app.ExamApplicationService;
import com.jexam.generation.GenerationMode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Top-left generation mode and actions section.
 */
public final class GenerationControlsComponent extends VBox {
    private final ComboBox<GenerationMode> modeBox = new ComboBox<>(FXCollections.observableArrayList(
        GenerationMode.EXAM,
        GenerationMode.MOCK_EXAM
    ));
    private final TextField seedField = new TextField();
    private final Button previewButton = new Button("Generate Preview");
    private final Button exportButton = new Button("Export PDF");
    private final Label staleLabel = new Label("Preview is stale. Refresh manually.");

    private Runnable previewHandler = () -> { };
    private Runnable exportHandler = () -> { };
    private Consumer<GenerationMode> modeHandler = mode -> { };
    private Consumer<String> seedHandler = value -> { };
    private boolean updatingControls;

    public GenerationControlsComponent() {
        setSpacing(8);
        setPadding(new Insets(8));

        modeBox.setValue(GenerationMode.EXAM);
        modeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (updatingControls) {
                return;
            }
            if (newValue != null) {
                modeHandler.accept(newValue);
            }
        });
        seedField.setPromptText("Optional random seed (e.g. 42)");
        seedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (updatingControls) {
                return;
            }
            seedHandler.accept(newValue);
        });
        previewButton.setOnAction(event -> previewHandler.run());
        exportButton.setOnAction(event -> exportHandler.run());
        staleLabel.setVisible(false);
        staleLabel.setManaged(false);

        modeBox.setAccessibleText("Generation mode selector");
        seedField.setAccessibleText("Optional deterministic random seed");
        previewButton.setAccessibleText("Generate PDF preview");
        exportButton.setAccessibleText("Export the selected PDF variant");
        staleLabel.setAccessibleText("Preview stale indicator");

        getChildren().addAll(
            new Label("Generation Mode"),
            modeBox,
            staleLabel,
            exportButton
        );
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

    public void setOnFallbackPreferenceChanged(
        final Consumer<ExamApplicationService.GoalPointFallbackPreference> handler
    ) {
        // Fallback selector was removed from UI by request.
    }

    public void setOnSeedChanged(final Consumer<String> handler) {
        seedHandler = handler == null ? value -> { } : handler;
    }

    public void setFallbackPreference(final ExamApplicationService.GoalPointFallbackPreference preference) {
        // Fallback selector was removed from UI by request.
    }

    public void setRandomSeed(final Long seed) {
        updatingControls = true;
        seedField.setText(seed == null ? "" : Long.toString(seed));
        updatingControls = false;
    }

    public void setOnPreviewRequested(final Runnable handler) {
        previewHandler = handler == null ? () -> { } : handler;
    }

    public void setOnExportRequested(final Runnable handler) {
        exportHandler = handler == null ? () -> { } : handler;
    }

    public void requestControlFocus() {
        modeBox.requestFocus();
    }
}