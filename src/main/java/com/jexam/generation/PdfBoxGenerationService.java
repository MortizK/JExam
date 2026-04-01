package com.jexam.generation;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Scope;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PdfBoxGenerationService implements PdfGenerationService {
    private static final float START_X = 50;
    private static final float START_Y = 750;
    private static final float LINE_HEIGHT = 16;

    @Override
    public void generate(Exam exam, GenerationMode mode, Path outputPath) {
        if (exam == null) {
            throw new IllegalArgumentException("Exam must not be null.");
        }

        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);

                    float y = START_Y;
                    y = writeLine(content, y, "JExam Export: " + mode);
                    y = writeLine(content, y, "Exam: " + exam.getName());
                    y -= LINE_HEIGHT;

                    for (Chapter chapter : exam.getChapters()) {
                        y = ensureSpace(document, content, y);
                        y = writeLine(content, y, "Chapter: " + chapter.getName());

                        for (Task task : chapter.getTasks()) {
                            if (!shouldIncludeTask(task, mode)) {
                                continue;
                            }

                            y = ensureSpace(document, content, y);
                            y = writeLine(content, y, "  Task: " + task.getName() + " (" + task.getPoints() + " pts, "
                                + task.getDifficulty().toXmlValue() + ")");

                            for (int i = 0; i < task.getVariants().size(); i++) {
                                Variant variant = task.getVariants().get(i);
                                y = ensureSpace(document, content, y);
                                y = writeLine(content, y, "    Variant " + (i + 1) + " Q: " + oneLine(variant.getQuestion()));

                                if (mode == GenerationMode.SOLUTION) {
                                    y = ensureSpace(document, content, y);
                                    y = writeLine(content, y, "    Variant " + (i + 1) + " A: " + oneLine(variant.getAnswer()));
                                }
                            }
                        }
                        y -= LINE_HEIGHT;
                    }
                }

                document.save(outputPath.toFile());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF.", e);
        }
    }

    private boolean shouldIncludeTask(Task task, GenerationMode mode) {
        if (mode == GenerationMode.MOCK_EXAM) {
            return task.getScope() == Scope.MOCK_EXAM;
        }
        return task.getScope() == Scope.EXAM;
    }

    private float writeLine(PDPageContentStream content, float y, String text) throws IOException {
        content.beginText();
        content.newLineAtOffset(START_X, y);
        content.showText(oneLine(text));
        content.endText();
        return y - LINE_HEIGHT;
    }

    private float ensureSpace(PDDocument document, PDPageContentStream content, float y) {
        // Keep v1 implementation single-page to stay minimal for this phase.
        if (y < 70) {
            return START_Y;
        }
        return y;
    }

    private String oneLine(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace('\n', ' ').replace('\r', ' ').trim();
        return normalized.length() > 150 ? normalized.substring(0, 150) + "..." : normalized;
    }
}
