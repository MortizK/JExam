package com.jexam.app;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders PDF preview pages to temporary images.
 */
final class PdfPreviewRenderer {
    private static final float PREVIEW_DPI = 140f;

    private PdfPreviewRenderer() {
    }

    static List<Path> renderPreviewImages(final Path pdfPath) throws IOException {
        List<Path> previewImagePaths = new ArrayList<>();

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
            }
        } catch (IOException e) {
            deletePreviewImages(previewImagePaths);
            throw e;
        } catch (RuntimeException e) {
            deletePreviewImages(previewImagePaths);
            throw e;
        }

        return previewImagePaths;
    }

    private static void deletePreviewImages(final List<Path> previewImagePaths) {
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