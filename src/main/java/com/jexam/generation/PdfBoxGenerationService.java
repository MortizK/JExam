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

/**
 * PDF generation service backed by Apache PDFBox.
 */
public class PdfBoxGenerationService implements PdfGenerationService {
    private static final float START_X = 50;
    private static final float START_Y = 750;
    private static final float LINE_HEIGHT = 16;
    private static final float MIN_Y = 70;
    private static final int MAX_LINE_LENGTH = 150;

    /**
     * Generates a PDF for the given exam and output mode.
     *
     * @param exam exam model to render
     * @param mode generation mode controlling included content
     * @param outputPath destination file path
     */
    @Override
    public void generate(
        final Exam exam,
        final GenerationMode mode,
        final Path outputPath
    ) {
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

                    RenderContext context = new RenderContext(content, mode, START_Y);
                    writeHeader(context, exam);
                    writeChapters(context, exam);
                }

                document.save(outputPath.toFile());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF.", e);
        }
    }

    private void writeHeader(final RenderContext context, final Exam exam)
        throws IOException {
        context.y = writeLine(
            context.content,
            context.y,
            "JExam Export: " + context.mode
        );
        context.y = writeLine(context.content, context.y, "Exam: " + exam.getName());
        context.y -= LINE_HEIGHT;
    }

    private void writeChapters(final RenderContext context, final Exam exam)
        throws IOException {
        for (Chapter chapter : exam.getChapters()) {
            context.y = ensureSpace(context.y);
            context.y = writeLine(context.content, context.y, "Chapter: " + chapter.getName());
            writeTasks(context, chapter);
            context.y -= LINE_HEIGHT;
        }
    }

    private void writeTasks(final RenderContext context, final Chapter chapter)
        throws IOException {
        for (Task task : chapter.getTasks()) {
            if (!shouldIncludeTask(task, context.mode)) {
                continue;
            }

            context.y = ensureSpace(context.y);
            context.y = writeLine(
                context.content,
                context.y,
                "  Task: "
                    + task.getName()
                    + " ("
                    + task.getPoints()
                    + " pts, "
                    + task.getDifficulty().toXmlValue()
                    + ")"
            );

            writeVariants(context, task);
        }
    }

    private void writeVariants(final RenderContext context, final Task task)
        throws IOException {
        for (int i = 0; i < task.getVariants().size(); i++) {
            Variant variant = task.getVariants().get(i);
            context.y = ensureSpace(context.y);
            context.y = writeLine(
                context.content,
                context.y,
                "    Variant " + (i + 1) + " Q: " + oneLine(variant.getQuestion())
            );

            if (context.mode == GenerationMode.SOLUTION) {
                context.y = ensureSpace(context.y);
                context.y = writeLine(
                    context.content,
                    context.y,
                    "    Variant " + (i + 1) + " A: " + oneLine(variant.getAnswer())
                );
            }
        }
    }

    private boolean shouldIncludeTask(final Task task, final GenerationMode mode) {
        if (mode == GenerationMode.MOCK_EXAM) {
            return task.getScope() == Scope.MOCK_EXAM;
        }
        return task.getScope() == Scope.EXAM;
    }

    private float writeLine(
        final PDPageContentStream content,
        final float y,
        final String text
    ) throws IOException {
        content.beginText();
        content.newLineAtOffset(START_X, y);
        content.showText(oneLine(text));
        content.endText();
        return y - LINE_HEIGHT;
    }

    private float ensureSpace(final float y) {
        // Keep v1 implementation single-page to stay minimal for this phase.
        if (y < MIN_Y) {
            return START_Y;
        }
        return y;
    }

    private String oneLine(final String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.replace('\n', ' ').replace('\r', ' ').trim();
        return normalized.length() > MAX_LINE_LENGTH
            ? normalized.substring(0, MAX_LINE_LENGTH) + "..."
            : normalized;
    }

    private static final class RenderContext {
        /**
         * Active PDF content stream.
         */
        private final PDPageContentStream content;

        /**
         * Current generation mode.
         */
        private final GenerationMode mode;

        /**
         * Current y cursor position.
         */
        private float y;

        private RenderContext(
            final PDPageContentStream stream,
            final GenerationMode currentMode,
            final float startY
        ) {
            this.content = stream;
            this.mode = currentMode;
            this.y = startY;
        }
    }
}
