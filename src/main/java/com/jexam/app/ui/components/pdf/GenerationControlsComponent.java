package com.jexam.app.ui.components.pdf;

import com.jexam.app.ExamApplicationService;
import com.jexam.generation.GenerationMode;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Top-left generation mode and actions section.
 *
 * <p>The component exposes the core PDF generation settings that the user
 * needs before exporting: generation mode, whether to include the cover page,
 * and the export action itself. The component intentionally keeps some legacy
 * API hooks as no-ops because the surrounding application still calls them.</p>
 *
 * @author Moritz
 */
public final class GenerationControlsComponent extends VBox {
    private final Label modeLabel = new Label();
    private final ComboBox<GenerationMode> modeBox = new ComboBox<>(FXCollections.observableArrayList(
        GenerationMode.EXAM,
        GenerationMode.MOCK_EXAM
    ));
    private final CheckBox coverCheckbox = new CheckBox("Include cover");
    private final Button exportButton = new Button("Export PDF");

    private Runnable exportHandler = () -> { };
    private Consumer<GenerationMode> modeHandler = mode -> { };
    private boolean updatingControls;

    /**
     * Creates generation controls for mode selection and export actions.
     */
    public GenerationControlsComponent() {
        getStyleClass().add("generation-controls");
        setSpacing(8);
        setPadding(new Insets(8));

        modeLabel.getStyleClass().add("section-label");
        modeBox.getStyleClass().add("generation-mode-box");
        exportButton.getStyleClass().add("primary-action");

        modeBox.setValue(GenerationMode.EXAM);
        coverCheckbox.setSelected(true);
        // Default texts are applied by parent via setLocalizedTexts
        coverCheckbox.getStyleClass().add("cover-checkbox");
        modeBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (updatingControls) {
                return;
            }
            if (newValue != null) {
                modeHandler.accept(newValue);
            }
        });
        exportButton.setOnAction(event -> exportHandler.run());

        modeBox.setAccessibleText("Generation mode selector");
        exportButton.setAccessibleText("Export the selected PDF variant");

        getChildren().addAll(
            modeLabel,
            modeBox,
            coverCheckbox,
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
     * Returns whether the cover page should be included when exporting.
     *
     * @return {@code true} when the cover page is selected
     */
    public boolean isCoverIncluded() {
        return coverCheckbox.isSelected();
    }

    /**
     * Applies localized visible texts for this component.
     *
     * @param modeLabelText label for the generation mode selector
     * @param coverCheckboxText label for the cover checkbox
     * @param exportButtonText label for the export button
     */
    public void setLocalizedTexts(final String modeLabelText, final String coverCheckboxText, final String exportButtonText) {
        if (modeLabelText != null) {
            modeLabel.setText(modeLabelText);
        }
        if (coverCheckboxText != null) {
            coverCheckbox.setText(coverCheckboxText);
        }
        if (exportButtonText != null) {
            exportButton.setText(exportButtonText);
        }
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
        // Fallback preference is handled elsewhere in the current UI layout.
        // Fallback selector was removed from UI by request.
    }

    /**
     * Registers callback for random-seed input changes.
     *
     * @param handler callback receiving raw seed text; {@code null} resets to no-op
     */
    public void setOnSeedChanged(final Consumer<String> handler) {
        // Seed input is no longer rendered in this component.
        // Seed input is not currently rendered.
    }

    /**
     * Retained API hook for fallback preference state.
     *
     * @param preference ignored because fallback selector is not currently rendered
     */
    public void setFallbackPreference(final ExamApplicationService.GoalPointFallbackPreference preference) {
        // The hidden fallback selector does not need to mirror state here.
        // Fallback selector was removed from UI by request.
    }

    /**
     * Updates seed field from application state without triggering input callbacks.
     *
     * @param seed seed value, or {@code null} to clear
     */
    public void setRandomSeed(final Long seed) {
        // No visible seed field exists in this trimmed-down layout.
        updatingControls = true;
        updatingControls = false;
    }

    /**
     * Registers callback for preview generation requests.
     *
     * @param handler preview action callback; {@code null} resets to no-op
     */
    public void setOnPreviewRequested(final Runnable handler) {
        // Preview generation is triggered from the preview panel instead.
        // Preview action is handled from the preview region controls.
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
     * Applies hover text to the generation controls.
     *
     * @param modeTooltip text for the generation mode selector
     * @param exportTooltip text for the export action
     */
    public void setTooltips(final String modeTooltip, final String exportTooltip) {
        setTooltip(modeBox, modeTooltip);
        setTooltip(exportButton, exportTooltip);
    }

    /**
     * Requests keyboard focus for the primary control in this component.
     */
    public void requestControlFocus() {
        modeBox.requestFocus();
    }

    private static void setTooltip(final javafx.scene.control.Control control, final String text) {
        if (text == null || text.isBlank()) {
            control.setTooltip(null);
            return;
        }
        control.setTooltip(new Tooltip(text));
    }
}
