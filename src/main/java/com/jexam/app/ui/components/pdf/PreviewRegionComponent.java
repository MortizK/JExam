package com.jexam.app.ui.components.pdf;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

/**
 * Right-hand preview region with lifecycle states.
 */
public final class PreviewRegionComponent extends VBox {
    private static final float PREVIEW_DPI = 140f;

    private final Label stateLabel = new Label("No preview generated yet.");
    private final Label pathLabel = new Label();
    private final Button refreshButton = new Button("Refresh Preview");
    private final Button openExternalButton = new Button("Open External");
    private final ImageView previewImage = new ImageView();
    private final ScrollPane previewScroll = new ScrollPane(previewImage);

    private Runnable refreshHandler = () -> { };
    private Runnable openExternalHandler = () -> { };
    private Path previewPath;
    private Path previewImagePath;

    public PreviewRegionComponent() {
        setSpacing(8);
        setPadding(new Insets(8));

        previewImage.setPreserveRatio(true);
        previewImage.setSmooth(true);
        previewImage.setFitWidth(650);
        previewImage.setAccessibleText("Embedded PDF preview image");
        previewScroll.setFitToWidth(true);
        previewScroll.setPrefViewportHeight(620);
        previewScroll.setPannable(true);

        refreshButton.setOnAction(event -> refreshHandler.run());
        openExternalButton.setOnAction(event -> openExternalHandler.run());
        openExternalButton.setDisable(true);
        stateLabel.setAccessibleText("Preview state message");
        pathLabel.setAccessibleText("Preview file information");
        refreshButton.setAccessibleText("Refresh the preview image");
        openExternalButton.setAccessibleText("Open the preview PDF in an external application");
        getChildren().addAll(new Label("Preview"), stateLabel, pathLabel, previewScroll, refreshButton, openExternalButton);
    }

    public void setIdle() {
        stateLabel.setText("No preview generated yet.");
        pathLabel.setText("");
        previewPath = null;
        previewImage.setImage(null);
        deletePreviewImage();
        openExternalButton.setDisable(true);
    }

    public void setLoading() {
        stateLabel.setText("Generating preview...");
        pathLabel.setText("");
    }

    public void setReady(final Path path) {
        previewPath = path;
        if (path == null || !Files.exists(path)) {
            stateLabel.setText("Preview failed");
            pathLabel.setText("Preview file is not available.");
            previewImage.setImage(null);
            openExternalButton.setDisable(true);
            return;
        }

        try {
            renderFirstPage(path);
            stateLabel.setText("Preview ready");
            pathLabel.setText("Showing first page in-app.");
            openExternalButton.setDisable(false);
        } catch (IOException e) {
            previewImage.setImage(null);
            stateLabel.setText("Embedded preview unavailable");
            pathLabel.setText("Use Open External. " + e.getMessage());
            openExternalButton.setDisable(false);
        }
    }

    public void setStale(final boolean stale) {
        if (stale) {
            stateLabel.setText("Preview is stale. Refresh required.");
        }
    }

    public void setError(final String message) {
        stateLabel.setText("Preview failed");
        pathLabel.setText(message == null ? "" : message);
        previewImage.setImage(null);
    }

    public Path getPreviewPath() {
        return previewPath;
    }

    public void setOnRefresh(final Runnable handler) {
        refreshHandler = handler == null ? () -> { } : handler;
    }

    public void setOnOpenExternal(final Runnable handler) {
        openExternalHandler = handler == null ? () -> { } : handler;
    }

    private void renderFirstPage(final Path pdfPath) throws IOException {
        deletePreviewImage();
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            if (document.getNumberOfPages() == 0) {
                throw new IOException("PDF has no pages.");
            }

            PDFRenderer renderer = new PDFRenderer(document);
            var image = renderer.renderImageWithDPI(0, PREVIEW_DPI, ImageType.RGB);
            previewImagePath = Files.createTempFile("jexam-preview-", ".png");
            ImageIO.write(image, "png", previewImagePath.toFile());
            previewImage.setImage(new Image(previewImagePath.toUri().toString()));
        }
    }

    private void deletePreviewImage() {
        if (previewImagePath == null) {
            return;
        }
        try {
            Files.deleteIfExists(previewImagePath);
        } catch (IOException ignored) {
            // Best-effort cleanup for temporary preview images.
        }
        previewImagePath = null;
    }
}