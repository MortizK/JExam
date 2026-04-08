package com.jexam.app.ui.components.pdf;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Right-hand preview region with lifecycle states.
 */
public final class PreviewRegionComponent extends VBox {
    private static final float PREVIEW_DPI = 140f;

    private final Label stateLabel = new Label("No preview generated yet.");
    private final Button refreshButton = new Button("Refresh Preview");
    private final Button exportButton = new Button("Export PDF");
    private final HBox actionBar = new HBox(8, refreshButton, exportButton);
    private final VBox pageContainer = new VBox(12);
    private final ScrollPane previewScroll = new ScrollPane(pageContainer);

    private Runnable refreshHandler = () -> { };
    private Runnable exportHandler = () -> { };
    private Path previewPath;
    private final List<Path> previewImagePaths = new ArrayList<>();

    /**
     * Creates the PDF preview region with refresh/export controls and page rendering.
     */
    public PreviewRegionComponent() {
        setSpacing(8);
        setPadding(new Insets(8));

        pageContainer.setFillWidth(true);
        previewScroll.setFitToWidth(true);
        previewScroll.setPrefViewportHeight(620);
        previewScroll.setPannable(true);
        VBox.setVgrow(previewScroll, Priority.ALWAYS);

        refreshButton.setOnAction(event -> refreshHandler.run());
        exportButton.setOnAction(event -> exportHandler.run());
        exportButton.setDisable(false);
        stateLabel.setAccessibleText("Preview state message");
        refreshButton.setAccessibleText("Refresh the preview image");
        exportButton.setAccessibleText("Export the selected PDF");
        getChildren().addAll(stateLabel, actionBar, previewScroll);
    }

    /**
     * Transitions to idle state and clears rendered preview.
     */
    public void setIdle() {
        stateLabel.setText("No preview generated yet.");
        previewPath = null;
        pageContainer.getChildren().clear();
        deletePreviewImages();
    }

    /**
     * Transitions to loading state during PDF generation.
     */
    public void setLoading() {
        stateLabel.setText("Generating preview...");
    }

    /**
     * Loads and renders a PDF preview from the specified path.
     *
     * @param path PDF file path, or {@code null} to clear
     */
    public void setReady(final Path path) {
        previewPath = path;
        if (path == null || !Files.exists(path)) {
            stateLabel.setText("Preview failed");
            pageContainer.getChildren().clear();
            return;
        }

        try {
            renderAllPages(path);
            stateLabel.setText("Preview ready");
        } catch (IOException e) {
            pageContainer.getChildren().clear();
            stateLabel.setText("Embedded preview unavailable");
        }
    }

    /**
     * Updates the stale indicator when preview is no longer synchronized with configuration.
     *
     * @param stale whether preview is out-of-date
     */
    public void setStale(final boolean stale) {
        if (stale) {
            stateLabel.setText("Preview is stale. Refresh required.");
        }
    }

    /**
     * Transitions to error state with a failure message.
     *
     * @param message error description
     */
    public void setError(final String message) {
        stateLabel.setText("Preview failed");
        pageContainer.getChildren().clear();
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
     * Renders all pages of a PDF file as temporary PNG images and displays them in the preview region.
     * Images are stored as temporary files and tracked for cleanup.
     * @param pdfPath path to the PDF file to render
     * @throws IOException if PDF loading or image rendering fails
     */
    private void renderAllPages(final Path pdfPath) throws IOException {
        deletePreviewImages();
        pageContainer.getChildren().clear();

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            if (document.getNumberOfPages() == 0) {
                throw new IOException("PDF has no pages.");
            }

            PDFRenderer renderer = new PDFRenderer(document);
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                var image = renderer.renderImageWithDPI(pageIndex, PREVIEW_DPI, ImageType.RGB);
                Path imagePath = Files.createTempFile("jexam-preview-page-", ".png");
                ImageIO.write(image, "png", imagePath.toFile());
                previewImagePaths.add(imagePath);

                ImageView pageImage = new ImageView(new Image(imagePath.toUri().toString()));
                pageImage.setPreserveRatio(true);
                pageImage.setSmooth(true);
                pageImage.setFitWidth(650);
                pageImage.setAccessibleText("Embedded PDF preview page " + (pageIndex + 1));
                pageContainer.getChildren().add(pageImage);
            }
        }
    }

    /**
     * Cleans up temporary preview image files from the file system.
     * Performs best-effort deletion and continues on I/O errors.
     */
    private void deletePreviewImages() {
        for (Path imagePath : previewImagePaths) {
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException ignored) {
                // Best-effort cleanup for temporary preview images.
            }
        }
        previewImagePaths.clear();
    }
}