package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
import com.jexam.app.ui.components.pdf.ChapterConfigurationComponent;
import com.jexam.app.ui.components.pdf.GenerationControlsComponent;
import com.jexam.app.ui.components.pdf.PreviewRegionComponent;
import com.jexam.app.ui.components.pdf.ValidationSummaryComponent;
import com.jexam.generation.GenerationMode;
import com.jexam.model.Chapter;
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
 * PDF tab shell with generation controls, chapter configuration, validation, and preview.
 *
 * @author Moritz
 */
public final class PdfTabContainer extends BorderPane {
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
        generationStatusLabel.getStyleClass().add("pdf-generation-status");
        generationStatusLabel.setWrapText(true);
        generationStatusLabel.setText("");

        leftColumn.setPrefWidth(380);
        contentSplit.setDividerPositions(0.34);
        setCenter(contentSplit);

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
        if (previewRegion.getPreviewPath() == null) {
            previewRegion.setIdle();
        }
    }

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
            showGenerationWarnings("Preview generated with warnings");
            setGenerationBusy(false);
        });
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            previewRegion.setError(failure == null ? "Unknown error" : failure.getMessage());
            validationSummary.setValidationResult(appService.validateCurrentExam());
            setGenerationBusy(false);
        });

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
    private void configureHandlers() {
        generationControls.setOnPreviewRequested(this::generatePreview);
        generationControls.setOnExportRequested(this::exportPdf);
        generationControls.setOnModeChanged(mode -> uiStateManager.markPreviewStale());
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
            if (result.warnings().isEmpty()) {
                setGenerationStatus("Generated " + mode + " files: " + fileList);
            } else {
                setGenerationStatus("Generated " + mode + " files with warnings: " + String.join(" | ", result.warnings()));
            }
            setGenerationBusy(false);
        });
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            setGenerationStatus("PDF generation failed: " + (failure == null ? "Unknown error" : failure.getMessage()));
            setGenerationBusy(false);
        });

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
    private void showGenerationWarnings(final String title) {
        List<String> warnings = appService.getLastGenerationWarnings();
        if (!warnings.isEmpty()) {
            setGenerationStatus(title + ": " + String.join(" | ", warnings));
        }
    }

    private void setGenerationStatus(final String message) {
        generationStatusLabel.setText(message == null ? "" : message);
    }

    private void setGenerationBusy(final boolean busy) {
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

    private record ExportResult(List<Path> generatedFiles, List<String> warnings) {
    }
}
