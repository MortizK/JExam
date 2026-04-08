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

    public void setIdle() {
        stateLabel.setText("No preview generated yet.");
        previewPath = null;
        pageContainer.getChildren().clear();
        deletePreviewImages();
    }

    public void setLoading() {
        stateLabel.setText("Generating preview...");
    }

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

    public void setStale(final boolean stale) {
        if (stale) {
            stateLabel.setText("Preview is stale. Refresh required.");
        }
    }

    public void setError(final String message) {
        stateLabel.setText("Preview failed");
        pageContainer.getChildren().clear();
    }

    public Path getPreviewPath() {
        return previewPath;
    }

    public void setOnRefresh(final Runnable handler) {
        refreshHandler = handler == null ? () -> { } : handler;
    }

    public void setOnExportRequested(final Runnable handler) {
        exportHandler = handler == null ? () -> { } : handler;
    }

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