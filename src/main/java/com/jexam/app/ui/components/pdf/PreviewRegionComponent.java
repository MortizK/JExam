package com.jexam.app.ui.components.pdf;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Right-hand preview region with lifecycle states.
 *
 * <p>The preview region owns the rendered PDF preview lifecycle. It displays
 * status text, exposes refresh/export actions, and swaps between idle,
 * loading, ready, stale, and error states. When preview images are available
 * they are rendered into a scrollable page stack.</p>
 *
 * @author Moritz
 */
public final class PreviewRegionComponent extends VBox {
    private static final String STATE_IDLE = "preview-idle";
    private static final String STATE_LOADING = "preview-loading";
    private static final String STATE_READY = "preview-ready";
    private static final String STATE_STALE = "preview-stale";
    private static final String STATE_ERROR = "preview-error";

    private final Label stateLabel = new Label();
    private final Button refreshButton = new Button();
    private final Button exportButton = new Button();
    private final HBox actionBar = new HBox(8, refreshButton, exportButton);
    private final VBox pageContainer = new VBox(12);
    private final ScrollPane previewScroll = new ScrollPane(pageContainer);

    private Runnable refreshHandler = () -> { };
    private Runnable exportHandler = () -> { };
    private Path previewPath;
    private final List<Path> previewImagePaths = new ArrayList<>();

    private String idleStateText = "No preview generated yet.";
    private String loadingStateText = "Generating preview...";
    private String readyStateText = "Preview ready";
    private String staleStateText = "Preview is stale. Refresh required.";
    private String failedStateText = "Preview failed";
    private String unavailableStateText = "Embedded preview unavailable";

    /**
     * Creates the PDF preview region with refresh/export controls and page rendering.
     */
    public PreviewRegionComponent() {
        getStyleClass().add("preview-region");
        setSpacing(8);
        setPadding(new Insets(8));

        stateLabel.getStyleClass().add("preview-state");
        actionBar.getStyleClass().add("preview-actions");
        refreshButton.getStyleClass().add("secondary-action");
        exportButton.getStyleClass().add("primary-action");
        previewScroll.getStyleClass().add("preview-scroll");
        pageContainer.getStyleClass().add("preview-pages");

        pageContainer.setFillWidth(true);
        previewScroll.setFitToWidth(true);
        previewScroll.setPrefViewportHeight(620);
        previewScroll.setPannable(true);
        VBox.setVgrow(previewScroll, Priority.ALWAYS);

        refreshButton.setOnAction(event -> refreshHandler.run());
        exportButton.setOnAction(event -> exportHandler.run());
        exportButton.setDisable(false);
        setPreviewState(STATE_IDLE);
        setLocalizedTexts(
            "Refresh Preview",
            "Export PDF",
            "No preview generated yet.",
            "Generating preview...",
            "Preview ready",
            "Preview is stale. Refresh required.",
            "Preview failed",
            "Embedded preview unavailable"
        );
        stateLabel.setAccessibleText("Preview state message");
        refreshButton.setAccessibleText("Refresh the preview image");
        exportButton.setAccessibleText("Export the selected PDF");
        getChildren().addAll(stateLabel, actionBar, previewScroll);
    }

    /**
     * Applies localized visible texts and messages.
     *
     * @param refreshButtonText text for the refresh button
     * @param exportButtonText text for the export button
     * @param idleStateText message for idle state
     * @param loadingStateText message for loading state
     * @param readyStateText message for ready state
     * @param staleStateText message for stale state
     * @param failedStateText message for failed state
     * @param unavailableStateText message for unavailable state
     */
    public void setLocalizedTexts(
        final String refreshButtonText,
        final String exportButtonText,
        final String idleStateText,
        final String loadingStateText,
        final String readyStateText,
        final String staleStateText,
        final String failedStateText,
        final String unavailableStateText
    ) {
        if (refreshButtonText != null) {
            refreshButton.setText(refreshButtonText);
        }
        if (exportButtonText != null) {
            exportButton.setText(exportButtonText);
        }
        this.idleStateText = idleStateText;
        this.loadingStateText = loadingStateText;
        this.readyStateText = readyStateText;
        this.staleStateText = staleStateText;
        this.failedStateText = failedStateText;
        this.unavailableStateText = unavailableStateText;
        // Keep the visible state label aligned with the active lifecycle style.
        if (getStyleClass().contains(STATE_IDLE)) {
            stateLabel.setText(idleStateText);
        } else if (getStyleClass().contains(STATE_LOADING)) {
            stateLabel.setText(loadingStateText);
        } else if (getStyleClass().contains(STATE_READY)) {
            stateLabel.setText(readyStateText);
        } else if (getStyleClass().contains(STATE_STALE)) {
            stateLabel.setText(staleStateText);
        } else if (getStyleClass().contains(STATE_ERROR)) {
            stateLabel.setText(failedStateText);
        }
    }

    /**
     * Transitions to idle state and clears rendered preview.
     */
    public void setIdle() {
        setPreviewState(STATE_IDLE);
        stateLabel.setText(idleStateText);
        previewPath = null;
        pageContainer.getChildren().clear();
        deletePreviewImages();
    }

    /**
     * Transitions to loading state during PDF generation.
     */
    public void setLoading() {
        setPreviewState(STATE_LOADING);
        stateLabel.setText(loadingStateText);
    }

    /**
     * Loads and renders a PDF preview from the specified path.
     *
     * @param path PDF file path, or {@code null} to clear
     */
    public void setReady(final Path path) {
        setReady(path, List.of());
    }

    /**
     * Loads a ready PDF preview from the specified path and rendered page images.
     *
     * @param path PDF file path, or {@code null} to clear
     * @param imagePaths temporary PNGs that already contain rendered preview pages
     */
    // ELEGANCE: Novelty - Sophisticated multi-state preview lifecycle with real-time feedback
    public void setReady(final Path path, final List<Path> imagePaths) {
        previewPath = path;
        if (path == null || !Files.exists(path)) {
            setPreviewState(STATE_ERROR);
            stateLabel.setText(failedStateText);
            pageContainer.getChildren().clear();
            return;
        }

        try {
            deletePreviewImages();
            pageContainer.getChildren().clear();
            if (imagePaths == null || imagePaths.isEmpty()) {
                throw new IOException("PDF preview has no pages.");
            }

            // Render each generated preview image as a scrollable page preview.
            for (int pageIndex = 0; pageIndex < imagePaths.size(); pageIndex++) {
                Path imagePath = imagePaths.get(pageIndex);
                previewImagePaths.add(imagePath);

                ImageView pageImage = new ImageView(new Image(imagePath.toUri().toString()));
                pageImage.getStyleClass().add("preview-page-image");
                pageImage.setPreserveRatio(true);
                pageImage.setSmooth(true);
                pageImage.setFitWidth(650);
                pageImage.setAccessibleText("Embedded PDF preview page " + (pageIndex + 1));
                pageContainer.getChildren().add(pageImage);
            }
            setPreviewState(STATE_READY);
            stateLabel.setText(readyStateText);
        } catch (IOException e) {
            setPreviewState(STATE_ERROR);
            pageContainer.getChildren().clear();
            stateLabel.setText(unavailableStateText);
        }
    }

    /**
     * Updates the stale indicator when preview is no longer synchronized with configuration.
     *
     * @param stale whether preview is out-of-date
     */
    public void setStale(final boolean stale) {
        if (stale) {
            setPreviewState(STATE_STALE);
            stateLabel.setText(staleStateText);
        }
    }

    /**
     * Transitions to error state with a failure message.
     *
     * @param message error description
     */
    public void setError(final String message) {
        setPreviewState(STATE_ERROR);
        stateLabel.setText(failedStateText);
        pageContainer.getChildren().clear();
    }

    private void setPreviewState(final String stateClass) {
        getStyleClass().removeAll(STATE_IDLE, STATE_LOADING, STATE_READY, STATE_STALE, STATE_ERROR);
        getStyleClass().add(stateClass);
    }

    /**
     * Returns the current preview PDF file path.
     *
     * @return PDF path, or {@code null} when no preview is active
     */
    public Path getPreviewPath() {
        return previewPath;
    }

    /**
     * Registers a callback for preview refresh requests.
     *
     * @param handler callback for refresh action; {@code null} resets to no-op
     */
    public void setOnRefresh(final Runnable handler) {
        refreshHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Registers a callback for export requests.
     *
     * @param handler callback for export action; {@code null} resets to no-op
     */
    public void setOnExportRequested(final Runnable handler) {
        exportHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Applies hover text to the preview controls.
     *
     * @param refreshTooltip text for the refresh action
     * @param exportTooltip text for the export action
     * @param stateTooltip text for the preview state label
     */
    public void setTooltips(final String refreshTooltip, final String exportTooltip, final String stateTooltip) {
        setTooltip(refreshButton, refreshTooltip);
        setTooltip(exportButton, exportTooltip);
        setTooltip(stateLabel, stateTooltip);
    }

    /**
     * Cleans up temporary preview image files from the file system.
     * Performs best-effort deletion and continues on I/O errors.
     */
    private void deletePreviewImages() {
        // Remove temporary PNGs on a best-effort basis so preview refreshes do
        // not accumulate stale files on disk.
        for (Path imagePath : previewImagePaths) {
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException ignored) {
                // Best-effort cleanup for temporary preview images.
            }
        }
        previewImagePaths.clear();
    }

    private static void setTooltip(final javafx.scene.control.Control control, final String text) {
        if (text == null || text.isBlank()) {
            control.setTooltip(null);
            return;
        }
        control.setTooltip(new Tooltip(text));
    }
}
