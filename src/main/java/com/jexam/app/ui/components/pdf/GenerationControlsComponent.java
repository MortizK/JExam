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
    private final Label modeLabel = new Label("Generation Mode");
    private final Label seedLabel = new Label("Random Seed");
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

    /**
     * Creates generation controls for mode selection and export actions.
     */
    public GenerationControlsComponent() {
        getStyleClass().add("generation-controls");
        setSpacing(8);
        setPadding(new Insets(8));

        modeLabel.getStyleClass().add("section-label");
        seedLabel.getStyleClass().add("section-label");
        modeBox.getStyleClass().add("generation-mode-box");
        seedField.getStyleClass().add("seed-field");
        previewButton.getStyleClass().add("primary-action");
        exportButton.getStyleClass().add("secondary-action");
        staleLabel.getStyleClass().add("stale-indicator");

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
            modeLabel,
            modeBox,
            seedLabel,
            seedField,
            staleLabel,
            previewButton,
            exportButton
        );
    }

    /**
     * Returns the currently selected generation mode.
     *
     * @return selected generation mode
     */
    public GenerationMode getSelectedMode() {
        return modeBox.getValue();
    }

    /**
     * Toggles visibility of the preview-stale indicator.
     *
     * @param value whether stale indicator should be shown
     */
    public void setStaleIndicatorVisible(final boolean value) {
        staleLabel.setVisible(value);
        staleLabel.setManaged(value);
    }

    /**
     * Registers callback for generation-mode changes.
     *
     * @param handler callback receiving selected mode; {@code null} resets to no-op
     */
    public void setOnModeChanged(final Consumer<GenerationMode> handler) {
        modeHandler = handler == null ? mode -> { } : handler;
    }

    /**
     * Retained API hook for fallback preference changes.
     *
     * @param handler ignored because fallback selector is not currently rendered
     */
    public void setOnFallbackPreferenceChanged(
        final Consumer<ExamApplicationService.GoalPointFallbackPreference> handler
    ) {
        // Fallback selector was removed from UI by request.
    }

    /**
     * Registers callback for random-seed input changes.
     *
     * @param handler callback receiving raw seed text; {@code null} resets to no-op
     */
    public void setOnSeedChanged(final Consumer<String> handler) {
        seedHandler = handler == null ? value -> { } : handler;
    }

    /**
     * Retained API hook for fallback preference state.
     *
     * @param preference ignored because fallback selector is not currently rendered
     */
    public void setFallbackPreference(final ExamApplicationService.GoalPointFallbackPreference preference) {
        // Fallback selector was removed from UI by request.
    }

    /**
     * Updates seed field from application state without triggering input callbacks.
     *
     * @param seed seed value, or {@code null} to clear
     */
    public void setRandomSeed(final Long seed) {
        updatingControls = true;
        seedField.setText(seed == null ? "" : Long.toString(seed));
        updatingControls = false;
    }

    /**
     * Registers callback for preview generation requests.
     *
     * @param handler preview action callback; {@code null} resets to no-op
     */
    public void setOnPreviewRequested(final Runnable handler) {
        previewHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Registers callback for export requests.
     *
     * @param handler export action callback; {@code null} resets to no-op
     */
    public void setOnExportRequested(final Runnable handler) {
        exportHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Requests keyboard focus for the primary control in this component.
     */
    public void requestControlFocus() {
        modeBox.requestFocus();
    }
}