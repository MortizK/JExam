package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
import com.jexam.app.ui.components.pdf.ChapterConfigurationComponent;
import com.jexam.app.ui.components.pdf.GenerationControlsComponent;
import com.jexam.app.ui.components.pdf.PreviewRegionComponent;
import com.jexam.app.ui.components.pdf.ValidationSummaryComponent;
import com.jexam.generation.GenerationMode;
import com.jexam.model.Chapter;
import com.jexam.validation.ValidationResult;
import javafx.geometry.Insets;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Orientation;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * PDF tab shell with generation controls, chapter configuration, validation, and preview.
 */
public final class PdfTabContainer extends BorderPane {
    private final ExamApplicationService appService;
    private final JExamUiSupport ui;
    private final UiStateManager uiStateManager;
    private final Stage stage;

    private final GenerationControlsComponent generationControls = new GenerationControlsComponent();
    private final ValidationSummaryComponent validationSummary = new ValidationSummaryComponent();
    private final ChapterConfigurationComponent chapterConfiguration = new ChapterConfigurationComponent();
    private final PreviewRegionComponent previewRegion = new PreviewRegionComponent();
    private final VBox leftColumn = new VBox(10, generationControls, chapterConfiguration);
    private final SplitPane contentSplit = new SplitPane(leftColumn, previewRegion);

    private Consumer<String> issueSelectedHandler = path -> { };

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

        setPadding(new Insets(8));

        leftColumn.setPrefWidth(380);
        contentSplit.setDividerPositions(0.34);
        setCenter(contentSplit);

        configureHandlers();
        refreshFromService();

        uiStateManager.previewStaleProperty().addListener((observable, oldValue, newValue) -> {
            generationControls.setStaleIndicatorVisible(newValue);
            previewRegion.setStale(newValue);
        });
    }

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
        generationControls.setStaleIndicatorVisible(uiStateManager.isPreviewStale());
        generationControls.setFallbackPreference(appService.getGoalPointFallbackPreference());
        generationControls.setRandomSeed(appService.getGenerationRandomSeed());
        if (previewRegion.getPreviewPath() == null) {
            previewRegion.setIdle();
        }
    }

    public void focusDefaultControl() {
        if (validationSummary.hasIssues()) {
            validationSummary.requestIssueTreeFocus();
            return;
        }
        generationControls.requestControlFocus();
    }

    public void generatePreview() {
        previewRegion.setLoading();
        try {
            GenerationMode mode = generationControls.getSelectedMode();
            Path path = appService.generatePreviewPdf(mode);
            previewRegion.setReady(path);
            uiStateManager.clearPreviewStale();
            refreshFromService();
            showGenerationWarnings("Preview generated with warnings");
        } catch (RuntimeException e) {
            previewRegion.setError(e.getMessage());
            validationSummary.setValidationResult(appService.validateCurrentExam());
        }
    }

    public void setOnIssueSelected(final Consumer<String> handler) {
        issueSelectedHandler = handler == null ? path -> { } : handler;
    }

    private void configureHandlers() {
        generationControls.setOnPreviewRequested(this::generatePreview);
        generationControls.setOnExportRequested(() -> {
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
            try {
                List<Path> generatedFiles = appService.generatePdfPair(mode, output.toPath());
                List<String> warnings = appService.getLastGenerationWarnings();
                String fileList = generatedFiles.stream()
                    .map(Path::toString)
                    .collect(java.util.stream.Collectors.joining("\n- ", "- ", ""));
                if (warnings.isEmpty()) {
                    ui.showInfo("PDF files generated", "Generated " + mode + " files:\n" + fileList);
                } else {
                    ui.showInfo(
                        "PDF files generated with warnings",
                        "Generated " + mode + " files:\n" + fileList + "\n\nWarnings:\n- " + String.join("\n- ", warnings)
                    );
                }
            } catch (RuntimeException e) {
                ui.showError("PDF generation failed", e.getMessage());
            }
        });
        generationControls.setOnModeChanged(mode -> uiStateManager.markPreviewStale());
        generationControls.setOnFallbackPreferenceChanged(preference -> {
            appService.setGoalPointFallbackPreference(preference);
            uiStateManager.markPreviewStale();
        });
        generationControls.setOnSeedChanged(this::handleSeedChanged);
        validationSummary.setOnIssueSelected(path -> issueSelectedHandler.accept(path));

        chapterConfiguration.setOnMoveUp(index -> {
            appService.moveGenerationChapterUp(index);
            uiStateManager.markPreviewStale();
            refreshFromService();
        });
        chapterConfiguration.setOnMoveDown(index -> {
            appService.moveGenerationChapterDown(index);
            uiStateManager.markPreviewStale();
            refreshFromService();
        });
        chapterConfiguration.setOnExclude(index -> {
            appService.excludeGenerationChapter(index);
            uiStateManager.markPreviewStale();
            refreshFromService();
        });
        chapterConfiguration.setOnInclude(chapterIndex -> {
            appService.includeGenerationChapter(chapterIndex);
            uiStateManager.markPreviewStale();
            refreshFromService();
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
        });

        previewRegion.setOnRefresh(this::generatePreview);
        previewRegion.setOnOpenExternal(() -> {
            Path path = previewRegion.getPreviewPath();
            if (path == null) {
                return;
            }
            if (!ui.openFile(path)) {
                ui.showError("Open failed", "Could not open preview file externally.");
            }
        });
    }

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

    private void showGenerationWarnings(final String title) {
        List<String> warnings = appService.getLastGenerationWarnings();
        if (!warnings.isEmpty()) {
            ui.showInfo(title, "Warnings:\n- " + String.join("\n- ", warnings));
        }
    }
}