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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

        VBox leftColumn = new VBox(10, generationControls, validationSummary, chapterConfiguration);
        leftColumn.setPrefWidth(380);
        HBox root = new HBox(12, leftColumn, previewRegion);
        HBox.setHgrow(previewRegion, Priority.ALWAYS);
        setCenter(root);

        configureHandlers();
        refreshFromService();

        uiStateManager.previewStaleProperty().addListener((observable, oldValue, newValue) -> {
            generationControls.setStaleIndicatorVisible(newValue);
            previewRegion.setStale(newValue);
        });
    }

    public void refreshFromService() {
        List<String> chapterNames = appService.getCurrentExam().getChapters().stream().map(Chapter::getName).toList();
        chapterConfiguration.setChapterData(chapterNames, appService.generationChapterOrder());
        ValidationResult validationResult = appService.validateCurrentExam();
        validationSummary.setValidationResult(validationResult);
        generationControls.setStaleIndicatorVisible(uiStateManager.isPreviewStale());
        if (previewRegion.getPreviewPath() == null) {
            previewRegion.setIdle();
        }
    }

    public void generatePreview() {
        previewRegion.setLoading();
        try {
            GenerationMode mode = generationControls.getSelectedMode();
            Path path = appService.generatePreviewPdf(mode);
            previewRegion.setReady(path);
            uiStateManager.clearPreviewStale();
            refreshFromService();
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
            FileChooser chooser = ui.pdfFileChooser(mode);
            File output = chooser.showSaveDialog(stage);
            if (output == null) {
                return;
            }
            try {
                appService.generatePdf(mode, output.toPath());
                ui.showInfo("PDF generated", "Generated " + mode + " at: " + output.toPath());
            } catch (RuntimeException e) {
                ui.showError("PDF generation failed", e.getMessage());
            }
        });
        generationControls.setOnModeChanged(mode -> uiStateManager.markPreviewStale());
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
}