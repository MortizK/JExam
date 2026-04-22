package com.jexam.app;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfPreviewRendererTest {
    @Test
    void shouldRenderPreviewImagesForPdfPages() throws Exception {
        Path pdfPath = Files.createTempFile("jexam-preview-renderer-test-", ".pdf");
        List<Path> imagePaths = null;

        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage());
            document.save(pdfPath.toFile());
        }

        try {
            imagePaths = PdfPreviewRenderer.renderPreviewImages(pdfPath);
            assertEquals(1, imagePaths.size());
            assertTrue(Files.exists(imagePaths.get(0)));
        } finally {
            Files.deleteIfExists(pdfPath);
            if (imagePaths != null) {
                for (Path imagePath : imagePaths) {
                    Files.deleteIfExists(imagePath);
                }
            }
        }
    }
}