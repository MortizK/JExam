package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
import com.jexam.app.ui.components.pdf.ChapterConfigurationComponent;
import com.jexam.app.ui.components.pdf.GenerationControlsComponent;
import com.jexam.app.ui.components.pdf.PreviewRegionComponent;
import com.jexam.app.ui.components.pdf.ValidationSummaryComponent;
import com.jexam.generation.GenerationMode;
import com.jexam.model.Chapter;
import com.jexam.model.DifficultyDistributionSummary;
import com.jexam.validation.ValidationResult;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Orientation;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * PDF tab shell with generation controls, chapter configuration, validation,
 * and preview.
 *
 * <p>This container is the state hub for the PDF workflow. It keeps the
 * generation controls, chapter configuration, validation summary, and preview
 * panel in sync with the current application service state. It also owns the
 * asynchronous preview/export tasks and the temporary busy state that disables
 * the tab while generation runs.</p>
 *
 * <p>Major responsibilities:</p>
 * - mirror the current generation settings into the UI,
 * - coordinate preview rendering and PDF export,
 * - keep the preview stale/ready state in sync with edits,
 * - surface validation issues and generation warnings.
 *
 * @author Moritz
 */
public final class PdfTabContainer extends BorderPane {
    /**
     * Result container for preview generation and image rendering.
     */
    private record PreviewRenderResult(Path previewPath, List<Path> imagePaths) {
    }

    private final ExamApplicationService appService;
    private final JExamUiSupport ui;
    private final UiStateManager uiStateManager;
    private final Stage stage;
    private final UserPreferencesStore preferencesStore = new UserPreferencesStore();

    private final GenerationControlsComponent generationControls = new GenerationControlsComponent();
    private final ValidationSummaryComponent validationSummary = new ValidationSummaryComponent();
    private final ChapterConfigurationComponent chapterConfiguration = new ChapterConfigurationComponent();
    private final PreviewRegionComponent previewRegion = new PreviewRegionComponent();
    private final Label generationDifficultySummaryLabel = new Label();
    private final Label generationStatusLabel = new Label();
    private final VBox leftColumn = new VBox(10, generationControls, chapterConfiguration, generationStatusLabel);
    private final SplitPane contentSplit = new SplitPane(leftColumn, previewRegion);

    private Consumer<String> issueSelectedHandler = path -> { };

    /**
     * Creates the PDF tab container and wires core services with UI state.
     *
     * @param appService application service facade
     * @param ui UI support and localization helper
     * @param uiStateManager shared UI state manager
     * @param stage owner stage for native file chooser dialogs
     */
    public PdfTabContainer(
        final ExamApplicationService appService,
        final JExamUiSupport ui,
        final UiStateManager uiStateManager,
        final Stage stage
    ) {
        this.appService = Objects.requireNonNull(appService, "appService");
        this.ui = Objects.requireNonNull(ui, "ui");
        this.uiStateManager = Objects.requireNonNull(uiStateManager, "uiStateManager");
        this.stage = Objects.requireNonNull(stage, "stage");

        getStyleClass().add("pdf-tab");

        setPadding(new Insets(8));
        generationDifficultySummaryLabel.getStyleClass().add("pdf-generation-summary");
        generationDifficultySummaryLabel.setWrapText(true);
        generationStatusLabel.getStyleClass().add("pdf-generation-status");
        generationStatusLabel.setWrapText(true);
        generationStatusLabel.setText("");

        leftColumn.setPrefWidth(380);
        contentSplit.setDividerPositions(0.34);
        setCenter(contentSplit);

        // Hook up event handlers before the first refresh so the UI can react
        // immediately to user interactions.
        configureHandlers();
        applyLocalizedTexts();
        ui.addLanguageChangeListener(this::applyLocalizedTexts);
        refreshFromService();

        uiStateManager.previewStaleProperty().addListener((observable, oldValue, newValue) -> {
            previewRegion.setStale(newValue);
        });
    }

    /**
     * Adapts the split-pane layout for smaller or wider windows.
     *
     * @param width current scene width in pixels
     */
    public void updateLayout(final double width) {
        // Collapse the preview on narrow windows to prioritize controls.
        if (width < 1024) {
            leftColumn.setPrefWidth(Double.MAX_VALUE);
            previewRegion.setVisible(false);
            previewRegion.setManaged(false);
            contentSplit.setDividerPositions(1.0);
        } else {
            contentSplit.setOrientation(Orientation.HORIZONTAL);
            contentSplit.setDividerPositions(width >= 1400 ? 0.40 : 0.50);
            leftColumn.setPrefWidth(380);
            previewRegion.setVisible(true);
            previewRegion.setManaged(true);
        }
    }

    /**
     * Rebinds all UI controls to the latest state from the application service.
     */
    public void refreshFromService() {
        List<Chapter> chapters = appService.getCurrentExam().getChapters();
        List<String> chapterNames = chapters.stream().map(Chapter::getName).toList();
        // Keep the chapter list, ordering, and goal points synchronized with
        // the underlying application service state.
        chapterConfiguration.setChapterData(
            chapterNames,
            appService.generationChapterOrder(),
            appService.generationChapterGoalPoints(),
            chapters
        );
        ValidationResult validationResult = appService.validateCurrentExam();
        validationSummary.setValidationResult(validationResult);
        generationControls.setFallbackPreference(appService.getGoalPointFallbackPreference());
        generationControls.setRandomSeed(appService.getGenerationRandomSeed());
        updateGenerationDifficultySummary();
        if (previewRegion.getPreviewPath() == null) {
            previewRegion.setIdle();
        }
    }

    /**
     * Apply localized labels and tooltips to all controls in the PDF tab.
     */
    private void applyLocalizedTexts() {
        generationControls.setTooltips(
            ui.text("tooltip.pdf.mode"),
            ui.text("tooltip.pdf.export")
        );
        generationControls.setLocalizedTexts(
            ui.text("label.generation.mode"),
            ui.text("checkbox.include.cover"),
            ui.text("button.export.pdf")
        );
        chapterConfiguration.setLocalizedTexts(
            ui.text("chapterConfiguration.includedLabel"),
            ui.text("chapterConfiguration.excludedLabel"),
            ui.text("chapterConfiguration.up"),
            ui.text("chapterConfiguration.down"),
            ui.text("chapterConfiguration.exclude"),
            ui.text("chapterConfiguration.include"),
            ui.text("chapterConfiguration.reset"),
            ui.text("chapterConfiguration.includedList"),
            ui.text("chapterConfiguration.excludedList"),
            ui.text("chapterConfiguration.moveUp"),
            ui.text("chapterConfiguration.moveDown"),
            ui.text("chapterConfiguration.excludeChapter"),
            ui.text("chapterConfiguration.includeChapter")
        );
        previewRegion.setLocalizedTexts(
            ui.text("preview.refresh"),
            ui.text("preview.export"),
            ui.text("preview.idle"),
            ui.text("preview.loading"),
            ui.text("preview.ready"),
            ui.text("preview.stale"),
            ui.text("preview.failed"),
            ui.text("preview.unavailable")
        );
        previewRegion.setTooltips(
            ui.text("tooltip.pdf.preview.refresh"),
            ui.text("tooltip.pdf.preview.export"),
            ui.text("tooltip.pdf.preview.state")
        );
    }

    /**
     * Moves focus to the first meaningful control in the PDF tab.
     */
    public void focusDefaultControl() {
        // Validation issues take precedence because they are the most useful
        // thing for the user to fix before export.
        if (validationSummary.hasIssues()) {
            validationSummary.requestIssueTreeFocus();
            return;
        }
        generationControls.requestControlFocus();
    }

    /**
     * Generates an in-app preview PDF and updates stale-state and validation feedback.
     */
    public void generatePreview() {
        previewRegion.setLoading();
        setGenerationBusy(true);
        final GenerationMode mode = generationControls.getSelectedMode();
        appService.setCoverEnabled(generationControls.isCoverIncluded());
        Task<PreviewRenderResult> task = new Task<>() {
            @Override
            protected PreviewRenderResult call() throws IOException {
                Path previewPath = appService.generatePreviewPdf(mode);
                List<Path> imagePaths = PdfPreviewRenderer.renderPreviewImages(previewPath);
                return new PreviewRenderResult(previewPath, imagePaths);
            }
        };

        task.setOnSucceeded(event -> {
            PreviewRenderResult result = task.getValue();
            previewRegion.setReady(result.previewPath(), result.imagePaths());
            uiStateManager.clearPreviewStale();
            refreshFromService();
            setGenerationStatus(buildGenerationStatus("Preview generated", appService.getLastGenerationDifficultySummary(), appService.getLastGenerationWarnings()));
            setGenerationBusy(false);
        });
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            previewRegion.setError(failure == null ? "Unknown error" : failure.getMessage());
            validationSummary.setValidationResult(appService.validateCurrentExam());
            setGenerationBusy(false);
        });

        // Generate the preview on a background thread so the UI stays
        // responsive while PDF rendering and image conversion happen.
        Thread worker = new Thread(task, "jexam-preview-generation");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Registers the callback that handles selection of validation issue paths.
     *
     * @param handler consumer receiving a selected issue path; {@code null} resets to no-op
     */
    public void setOnIssueSelected(final Consumer<String> handler) {
        issueSelectedHandler = handler == null ? path -> { } : handler;
    }

    /**
     * Wires all child-component event handlers to service operations and UI state updates.
     */
    /**
     * Wire child-component callbacks to application service operations.
     */
    private void configureHandlers() {
        generationControls.setOnPreviewRequested(this::generatePreview);
        generationControls.setOnExportRequested(this::exportPdf);
        generationControls.setOnModeChanged(mode -> {
            uiStateManager.markPreviewStale();
            refreshFromService();
        });
        generationControls.setOnFallbackPreferenceChanged(preference -> {
            appService.setGoalPointFallbackPreference(preference);
            uiStateManager.markPreviewStale();
        });
        generationControls.setOnSeedChanged(this::handleSeedChanged);
        validationSummary.setOnIssueSelected(path -> issueSelectedHandler.accept(path));

        chapterConfiguration.setOnMoveUp(index -> {
            int chapterIndex = chapterConfiguration.selectedIncludedChapterIndex();
            appService.moveGenerationChapterUp(index);
            uiStateManager.markPreviewStale();
            refreshFromService();
            chapterConfiguration.selectIncludedChapterByChapterIndex(chapterIndex);
        });
        chapterConfiguration.setOnMoveDown(index -> {
            int chapterIndex = chapterConfiguration.selectedIncludedChapterIndex();
            appService.moveGenerationChapterDown(index);
            uiStateManager.markPreviewStale();
            refreshFromService();
            chapterConfiguration.selectIncludedChapterByChapterIndex(chapterIndex);
        });
        chapterConfiguration.setOnExclude(index -> {
            int chapterIndex = chapterConfiguration.selectedIncludedChapterIndex();
            appService.excludeGenerationChapter(index);
            uiStateManager.markPreviewStale();
            refreshFromService();
            chapterConfiguration.selectExcludedChapterByChapterIndex(chapterIndex);
        });
        chapterConfiguration.setOnInclude(chapterIndex -> {
            appService.includeGenerationChapter(chapterIndex);
            uiStateManager.markPreviewStale();
            refreshFromService();
            chapterConfiguration.selectIncludedChapterByChapterIndex(chapterIndex);
        });
        chapterConfiguration.setOnGoalChanged((chapterIndex, points) -> {
            try {
                appService.setGenerationChapterGoalPoints(chapterIndex, points);
                uiStateManager.markPreviewStale();
                refreshFromService();
            } catch (IllegalArgumentException e) {
                ui.showError("Invalid chapter goal", e.getMessage());
            }
        });
        chapterConfiguration.setOnReset(() -> {
            appService.resetGenerationChapterSelection();
            uiStateManager.markPreviewStale();
            refreshFromService();
        });
        chapterConfiguration.setOnReorder((fromIndex, toIndex) -> {
            int chapterIndex = chapterConfiguration.selectedIncludedChapterIndex();
            // Reorder by repeatedly stepping the selected chapter so the model
            // preserves the relative order of all other chapters.
            if (fromIndex < toIndex) {
                for (int index = fromIndex; index < toIndex; index++) {
                    appService.moveGenerationChapterDown(index);
                }
            } else {
                for (int index = fromIndex; index > toIndex; index--) {
                    appService.moveGenerationChapterUp(index);
                }
            }
            uiStateManager.markPreviewStale();
            refreshFromService();
            chapterConfiguration.selectIncludedChapterByChapterIndex(chapterIndex);
        });

        previewRegion.setOnRefresh(this::generatePreview);
        previewRegion.setOnExportRequested(this::exportPdf);
    }

    /**
     * Exports the selected generation mode as PDF files using either preview reuse or fresh generation.
     */
    private void exportPdf() {
        GenerationMode mode = generationControls.getSelectedMode();
        // Only the exportable modes can produce a PDF pair or preview-based
        // export. Other modes are rejected immediately with a user-facing error.
        if (mode != GenerationMode.EXAM && mode != GenerationMode.MOCK_EXAM) {
            ui.showError("Unsupported mode", "Please select EXAM or MOCK_EXAM.");
            return;
        }

        FileChooser chooser = ui.pdfFileChooser(mode);
        File output = chooser.showSaveDialog(stage);
        if (output == null) {
            return;
        }

        Path parent = output.toPath().getParent();
        preferencesStore.saveLastPdfDirectory(parent);
        ui.setLastPdfDirectory(parent);

        setGenerationBusy(true);
        final Path outputPath = output.toPath();
        appService.setCoverEnabled(generationControls.isCoverIncluded());
        final boolean canReusePreview = previewRegion.getPreviewPath() != null && !uiStateManager.isPreviewStale();

        Task<ExportResult> task = new Task<>() {
            @Override
            protected ExportResult call() {
                // Reuse the last preview when possible so the exported PDFs match
                // the already rendered preview exactly.
                List<Path> generatedFiles = canReusePreview
                    ? appService.generatePdfPairFromLastPreview(mode, outputPath)
                    : appService.generatePdfPair(mode, outputPath);
                return new ExportResult(generatedFiles, List.copyOf(appService.getLastGenerationWarnings()));
            }
        };

        task.setOnSucceeded(event -> {
            ExportResult result = task.getValue();
            String fileList = result.generatedFiles().stream()
                .map(path -> path.getFileName().toString())
                .collect(java.util.stream.Collectors.joining(", "));
            setGenerationStatus(buildGenerationStatus(
                "Generated " + mode + " files: " + fileList,
                appService.getLastGenerationDifficultySummary(),
                result.warnings()
            ));
            setGenerationBusy(false);
        });
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            setGenerationStatus("PDF generation failed: " + (failure == null ? "Unknown error" : failure.getMessage()));
            setGenerationBusy(false);
        });

        // Export also runs off the FX thread because it can produce multiple
        // PDFs and take noticeably longer than a small UI update.
        Thread worker = new Thread(task, "jexam-export-generation");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Validates and applies the optional random seed used for deterministic generation.
     *
     * @param rawValue user-entered seed text; blank clears the seed
     */
    private void handleSeedChanged(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            appService.setGenerationRandomSeed(null);
            uiStateManager.markPreviewStale();
            return;
        }

        try {
            appService.setGenerationRandomSeed(Long.parseLong(rawValue.trim()));
            uiStateManager.markPreviewStale();
        } catch (NumberFormatException ignored) {
            // Keep prior valid value until the user enters a valid numeric seed.
        }
    }

    /**
     * Displays non-blocking generation warnings produced by the last service call.
     *
     * @param title dialog title used for the warning message
     */
    /**
     * Refresh the generation difficulty summary label from the current mode.
     */
    private void updateGenerationDifficultySummary() {
        // Always refresh the label from the currently selected generation mode
        // so the UI reflects the same task selection rules as export.
        DifficultyDistributionSummary summary = appService.getGenerationDifficultySummary(generationControls.getSelectedMode());
        generationDifficultySummaryLabel.setText(
            ui.text("pdf.generation.difficulty.current") + " " + summary.toHumanReadableText()
        );
    }

    /**
     * Combine the generation prefix, difficulty summary, and warnings into one
     * status line for the PDF tab.
     */
    private String buildGenerationStatus(
        final String prefix,
        final DifficultyDistributionSummary summary,
        final List<String> warnings
    ) {
        // Build one compact status line so the tab can surface both the result
        // summary and any non-fatal generation warnings.
        StringBuilder builder = new StringBuilder(prefix);
        if (summary != null) {
            builder.append(" | ")
                .append(ui.text("pdf.generation.difficulty.result"))
                .append(' ')
                .append(summary.toHumanReadableText());
        }
        if (warnings != null && !warnings.isEmpty()) {
            builder.append(" | ").append(String.join(" | ", warnings));
        }
        return builder.toString();
    }

    /**
     * Update the visible generation status label.
     */
    private void setGenerationStatus(final String message) {
        generationStatusLabel.setText(message == null ? "" : message);
    }

    /**
     * Toggle the busy state for the entire PDF tab.
     */
    private void setGenerationBusy(final boolean busy) {
        // Disable the entire tab while a generation task runs so edits do not
        // race against preview/export work.
        if (stage.getScene() == null) {
            return;
        }
        stage.getScene().setCursor(busy ? Cursor.WAIT : Cursor.DEFAULT);
        generationControls.setDisable(busy);
        chapterConfiguration.setDisable(busy);
        validationSummary.setDisable(busy);
        previewRegion.setDisable(busy);
        leftColumn.setDisable(busy);
        contentSplit.setDisable(busy);
    }

    /**
     * Result payload for PDF export tasks.
     */
    private record ExportResult(List<Path> generatedFiles, List<String> warnings) {
    }
}
