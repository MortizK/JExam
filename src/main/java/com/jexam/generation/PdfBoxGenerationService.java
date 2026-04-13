package com.jexam.generation;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF generation service backed by Apache PDFBox.
 */
public class PdfBoxGenerationService implements PdfGenerationService {
    private static final PDType1Font BODY_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float LEFT_MARGIN = 50;
    private static final float RIGHT_MARGIN = 50;
    private static final float TOP_MARGIN = 56;
    private static final float BOTTOM_MARGIN = 56;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;
    private static final float CONTENT_HEIGHT = PAGE_HEIGHT - TOP_MARGIN - BOTTOM_MARGIN;
    private static final float BODY_FONT_SIZE = 12;
    private static final float TITLE_FONT_SIZE = 24;
    private static final float SUBTITLE_FONT_SIZE = 14;
    private static final float SMALL_FONT_SIZE = 10;
    private static final float LINE_HEIGHT = 16;
    private static final float SMALL_LINE_HEIGHT = 12;
    private static final float SECTION_GAP = 10;
    private static final float CHAPTER_GAP = 18;
    private static final float QUESTION_GAP = 6;
    private static final float ANSWER_LABEL_GAP = 4;
    private static final float ANSWER_PADDING = 10;
    private static final float MIN_ANSWER_BOX_HEIGHT = 60;
    private static final float ANSWER_LINE_HEIGHT = 14;

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
                List<ChapterBookmark> chapterBookmarks;
                try (RenderContext context = new RenderContext(document, mode, exam)) {
                    writeCoverPage(context, exam);
                    if (exam.chapterCount() > 0) {
                        context.startNewPage();
                    }
                    chapterBookmarks = writeChapters(context, exam);
                }

                applyMetadata(document, exam, mode);
                applyOutline(document, chapterBookmarks);

                document.save(outputPath.toFile());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate PDF.", e);
        }
    }

    private void applyMetadata(final PDDocument document, final Exam exam, final GenerationMode mode) {
        PDDocumentInformation information = new PDDocumentInformation();
        information.setTitle(oneLine(exam.getName()));
        information.setSubject("Exam PDF - " + mode.name());
        information.setCreator("JExam");
        document.setDocumentInformation(information);
    }

    private void applyOutline(final PDDocument document, final List<ChapterBookmark> chapterBookmarks) {
        PDDocumentOutline outline = new PDDocumentOutline();

        if (document.getNumberOfPages() > 0) {
            PDOutlineItem coverItem = new PDOutlineItem();
            coverItem.setTitle("Cover");
            coverItem.setDestination(destinationForPage(document.getPage(0)));
            outline.addLast(coverItem);
        }

        for (ChapterBookmark chapterBookmark : chapterBookmarks) {
            PDOutlineItem chapterItem = new PDOutlineItem();
            chapterItem.setTitle(chapterBookmark.title());
            chapterItem.setDestination(destinationForPage(chapterBookmark.page()));

            for (TaskBookmark taskBookmark : chapterBookmark.taskBookmarks()) {
                PDOutlineItem taskItem = new PDOutlineItem();
                taskItem.setTitle(taskBookmark.title());
                taskItem.setDestination(destinationForPage(taskBookmark.page()));
                chapterItem.addLast(taskItem);
            }

            outline.addLast(chapterItem);
        }

        outline.openNode();
        document.getDocumentCatalog().setDocumentOutline(outline);
    }

    private PDPageXYZDestination destinationForPage(final PDPage page) {
        PDPageXYZDestination destination = new PDPageXYZDestination();
        destination.setPage(page);
        destination.setTop(Math.round(page.getMediaBox().getHeight()));
        return destination;
    }

    private void writeCoverPage(final RenderContext context, final Exam exam)
        throws IOException {
        context.content.setFont(BOLD_FONT, TITLE_FONT_SIZE);
        context.y = writeCenteredLine(context, context.y, "Deckblatt", BOLD_FONT, TITLE_FONT_SIZE);
        context.y -= SECTION_GAP;

        context.content.setFont(BODY_FONT, SUBTITLE_FONT_SIZE);
        context.y = writeCenteredLine(context, context.y, oneLine(exam.getName()), BODY_FONT, SUBTITLE_FONT_SIZE);
        context.y -= CHAPTER_GAP;

        context.y = writeLine(context, context.y, "Datum: ________________________________");
        context.y = writeLine(context, context.y, "Matrikelnummer: _______________________");
        context.y -= SECTION_GAP;

        context.y = writeSectionTitle(context, "Punkteuebersicht");
        context.y = writeLine(context, context.y, "Gesamtpunkte: " + formatPoints(totalPoints(exam, context.mode)));
        context.y -= SECTION_GAP;

        context.y = writeLine(context, context.y, "Aufgabe                              Teilaufgaben   Punkte");
        context.y = writeLine(context, context.y, "---------------------------------------------------------");

        final int chapterCount = exam.chapterCount();
        for (int index = 0; index < exam.chapterCount(); index++) {
            Chapter chapter = exam.chapterAt(index);
            context.y = ensureSpace(context, LINE_HEIGHT * 2.5f);
            String chapterLabel = chapterHeading(index, chapterCount, chapter.getName(), totalPoints(chapter, context.mode));
            context.y = writeLine(
                context,
                context.y,
                padRight(chapterLabel, 36)
                    + padLeft(Integer.toString(includedTasks(chapter, context.mode).size()), 4)
                    + "           "
                    + padLeft(formatPoints(totalPoints(chapter, context.mode)), 6)
            );
        }
    }

    private List<ChapterBookmark> writeChapters(final RenderContext context, final Exam exam)
        throws IOException {
        List<ChapterBookmark> chapterBookmarks = new ArrayList<>();
        for (int chapterIndex = 0; chapterIndex < exam.chapterCount(); chapterIndex++) {
            if (chapterIndex > 0) {
                context.startNewPage();
            }
            Chapter chapter = exam.chapterAt(chapterIndex);
            chapterBookmarks.add(writeChapter(context, chapterIndex, chapter));
        }
        return chapterBookmarks;
    }

    private ChapterBookmark writeChapter(final RenderContext context, final int chapterIndex, final Chapter chapter)
        throws IOException {
        PDPage chapterPage = context.currentPage;
        final int chapterCount = context.exam.chapterCount();
        final String heading = chapterHeading(chapterIndex, chapterCount, chapter.getName(), totalPoints(chapter, context.mode));
        context.y = writeSectionTitle(
            context,
            heading
        );
        context.y = writeLine(
            context,
            context.y,
            "Tasks: " + includedTasks(chapter, context.mode).size() + " | Points: " + formatPoints(totalPoints(chapter, context.mode))
        );
        context.y -= SECTION_GAP;
        List<TaskBookmark> taskBookmarks = writeTasks(context, chapter);
        return new ChapterBookmark(heading, chapterPage, taskBookmarks);
    }

    private List<TaskBookmark> writeTasks(final RenderContext context, final Chapter chapter)
        throws IOException {
        List<Task> tasks = includedTasks(chapter, context.mode);
        boolean enumerateTasks = tasks.size() > 1;
        int displayTaskIndex = 0;
        List<TaskBookmark> taskBookmarks = new ArrayList<>();
        for (Task task : tasks) {

            displayTaskIndex++;

            float taskHeight = estimateTaskBlockHeight(task, context.mode);
            if (taskHeight <= CONTENT_HEIGHT) {
                context.y = ensureSpace(context, taskHeight);
            } else {
                context.y = ensureSpace(context, LINE_HEIGHT * 2.5f);
            }
            PDPage taskStartPage = context.currentPage;
            String taskLabelPrefix = enumerateTasks ? taskOrdinal(displayTaskIndex - 1) + " " : "";
            String taskTitle = taskLabelPrefix
                + oneLine(task.getName())
                + " ("
                + formatPoints(task.getPoints())
                + " Punkte)";
            context.y = writeLine(
                context,
                context.y,
                taskTitle
            );
            taskBookmarks.add(new TaskBookmark(taskTitle, taskStartPage));
            writeVariants(context, task);
            context.y -= SECTION_GAP;
        }
        return taskBookmarks;
    }

    private void writeVariants(final RenderContext context, final Task task)
        throws IOException {
        for (int i = 0; i < task.getVariants().size(); i++) {
            Variant variant = task.getVariants().get(i);
            context.y = ensureSpace(context, estimateVariantBlockHeight(task, variant, context.mode));
            context.y = writeLine(context, context.y, "Variant " + (i + 1));
            List<String> questionLines = wrapText(
                variant.getQuestion(),
                BODY_FONT,
                BODY_FONT_SIZE,
                CONTENT_WIDTH - 20
            );
            context.y = writeWrappedLines(context, questionLines, 14, LEFT_MARGIN + 10, context.y);
            context.y -= QUESTION_GAP;

            float boxHeight = estimateAnswerBoxHeight(variant.getAnswer(), task.getPoints());
            context.y = ensureSpace(context, boxHeight + LINE_HEIGHT);
            context.y = writeLine(context, context.y, "Answer");
            drawAnswerBox(context, variant, boxHeight);
        }
    }

    private boolean shouldIncludeTask(final Task task, final GenerationMode mode) {
        if (mode == GenerationMode.EXAM) {
            return task.getScope() == Scope.EXAM;
        }
        return true;
    }

    private float writeLine(
        final RenderContext context,
        final float y,
        final String text
    ) throws IOException {
        return writeText(context, oneLine(text), LEFT_MARGIN, y, BODY_FONT, BODY_FONT_SIZE);
    }

    private float writeSectionTitle(final RenderContext context, final String text) throws IOException {
        context.content.setFont(BOLD_FONT, SUBTITLE_FONT_SIZE);
        float result = writeText(context, oneLine(text), LEFT_MARGIN, context.y, BOLD_FONT, SUBTITLE_FONT_SIZE);
        context.content.setFont(BODY_FONT, BODY_FONT_SIZE);
        return result;
    }

    private float writeText(
        final RenderContext context,
        final String text,
        final float x,
        final float y,
        final PDFont font,
        final float fontSize
    ) throws IOException {
        context.content.setFont(font, fontSize);
        context.content.beginText();
        context.content.newLineAtOffset(x, y);
        context.content.showText(text == null ? "" : text);
        context.content.endText();
        context.content.setFont(BODY_FONT, BODY_FONT_SIZE);
        return y - LINE_HEIGHT;
    }

    private float writeCenteredLine(
        final RenderContext context,
        final float y,
        final String text,
        final PDFont font,
        final float fontSize
    ) throws IOException {
        float width = textWidth(font, fontSize, text);
        float x = LEFT_MARGIN + Math.max(0, (CONTENT_WIDTH - width) / 2f);
        return writeText(context, text == null ? "" : text, x, y, font, fontSize);
    }

    private float writeWrappedLines(
        final RenderContext context,
        final List<String> lines,
        final float lineHeight,
        final float x,
        final float startY
    ) throws IOException {
        float currentY = startY;
        for (String line : lines) {
            currentY = writeText(context, line, x, currentY, BODY_FONT, BODY_FONT_SIZE);
            currentY += LINE_HEIGHT - lineHeight;
        }
        return currentY;
    }

    private float ensureSpace(final RenderContext context, final float requiredHeight) throws IOException {
        if (context.y - requiredHeight < BOTTOM_MARGIN) {
            context.startNewPage();
        }
        return context.y;
    }

    private void drawAnswerBox(final RenderContext context, final Variant variant, final float boxHeight)
        throws IOException {
        float boxWidth = CONTENT_WIDTH;
        float topY = context.y;
        float bottomY = topY - boxHeight;

        context.content.setLineWidth(0.8f);
        context.content.addRect(LEFT_MARGIN, bottomY, boxWidth, boxHeight);
        context.content.stroke();

        if (context.mode == GenerationMode.SOLUTION) {
            List<String> answerLines = wrapText(variant.getAnswer(), BODY_FONT, BODY_FONT_SIZE, boxWidth - 2 * ANSWER_PADDING);
            float textY = topY - ANSWER_PADDING - BODY_FONT_SIZE;
            for (String line : answerLines) {
                context.content.setFont(BODY_FONT, BODY_FONT_SIZE);
                context.content.beginText();
                context.content.newLineAtOffset(LEFT_MARGIN + ANSWER_PADDING, textY);
                context.content.showText(line);
                context.content.endText();
                textY -= ANSWER_LINE_HEIGHT;
            }
        }

        context.y = bottomY - ANSWER_LABEL_GAP;
    }

    private String oneLine(final String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private float totalPoints(final Exam exam, final GenerationMode mode) {
        float total = 0f;
        for (Chapter chapter : exam.getChapters()) {
            total += totalPoints(chapter, mode);
        }
        return total;
    }

    private float totalPoints(final Chapter chapter, final GenerationMode mode) {
        float total = 0f;
        for (Task task : includedTasks(chapter, mode)) {
            total += (float) task.getPoints();
        }
        return total;
    }

    private List<Task> includedTasks(final Chapter chapter, final GenerationMode mode) {
        List<Task> tasks = new ArrayList<>();
        for (Task task : chapter.getTasks()) {
            if (shouldIncludeTask(task, mode)) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    private String formatPoints(final double points) {
        return String.format(java.util.Locale.ROOT, "%.1f", points);
    }

    private String chapterHeading(
        final int chapterIndex,
        final int chapterCount,
        final String chapterName,
        final float points
    ) {
        if (chapterCount <= 1) {
            return "Aufgabe: " + oneLine(chapterName) + " (" + formatPoints(points) + " Punkte)";
        }
        return "Aufgabe " + (chapterIndex + 1) + ": " + oneLine(chapterName) + " (" + formatPoints(points) + " Punkte)";
    }

    private String taskOrdinal(final int index) {
        int value = index + 1;
        StringBuilder builder = new StringBuilder();
        while (value > 0) {
            value--;
            builder.insert(0, (char) ('a' + (value % 26)));
            value /= 26;
        }
        return builder + ")";
    }

    private String padRight(final String value, final int width) {
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        return value + " ".repeat(width - value.length());
    }

    private String padLeft(final String value, final int width) {
        if (value.length() >= width) {
            return value;
        }
        return " ".repeat(width - value.length()) + value;
    }

    private float estimateTaskBlockHeight(final Task task, final GenerationMode mode) throws IOException {
        float totalHeight = LINE_HEIGHT + SECTION_GAP;
        for (Variant variant : task.getVariants()) {
            totalHeight += estimateVariantBlockHeight(task, variant, mode);
            totalHeight += SECTION_GAP;
        }
        return totalHeight;
    }

    private float estimateVariantBlockHeight(final Task task, final Variant variant, final GenerationMode mode)
        throws IOException {
        float questionHeight = wrapText(variant.getQuestion(), BODY_FONT, BODY_FONT_SIZE, CONTENT_WIDTH - 20).size() * LINE_HEIGHT;
        float answerHeight = estimateAnswerBoxHeight(variant.getAnswer(), task.getPoints());
        float answerTextHeight = mode == GenerationMode.SOLUTION
            ? wrapText(variant.getAnswer(), BODY_FONT, BODY_FONT_SIZE, CONTENT_WIDTH - 2 * ANSWER_PADDING).size() * ANSWER_LINE_HEIGHT
            : 0f;
        return LINE_HEIGHT + questionHeight + QUESTION_GAP + Math.max(answerHeight, answerTextHeight + ANSWER_PADDING * 2) + LINE_HEIGHT;
    }

    static float estimateAnswerBoxHeight(final String answerText, final double points) throws IOException {
        List<String> wrapped = wrapText(answerText, BODY_FONT, BODY_FONT_SIZE, CONTENT_WIDTH - 2 * ANSWER_PADDING);
        float textHeight = wrapped.size() * ANSWER_LINE_HEIGHT;
        float handwrittenPadding = Math.max(28f, (float) points * 10f);
        return Math.max(MIN_ANSWER_BOX_HEIGHT, textHeight + handwrittenPadding + (2 * ANSWER_PADDING));
    }

    private static List<String> wrapText(
        final String text,
        final PDFont font,
        final float fontSize,
        final float maxWidth
    ) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        String normalized = text.replace("\r", "");
        String[] paragraphs = normalized.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                lines.add("");
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            for (String word : paragraph.trim().split("\\s+")) {
                if (word.isBlank()) {
                    continue;
                }

                String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
                if (textWidth(font, fontSize, candidate) <= maxWidth) {
                    currentLine.setLength(0);
                    currentLine.append(candidate);
                    continue;
                }

                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

                if (textWidth(font, fontSize, word) <= maxWidth) {
                    currentLine.append(word);
                } else {
                    List<String> chunks = splitWord(word, font, fontSize, maxWidth);
                    for (int i = 0; i < chunks.size(); i++) {
                        String chunk = chunks.get(i);
                        if (i == chunks.size() - 1) {
                            currentLine.append(chunk);
                        } else {
                            lines.add(chunk);
                        }
                    }
                }
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    private static List<String> splitWord(
        final String word,
        final PDFont font,
        final float fontSize,
        final float maxWidth
    ) throws IOException {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char character : word.toCharArray()) {
            String candidate = current + String.valueOf(character);
            if (current.length() > 0 && textWidth(font, fontSize, candidate) > maxWidth) {
                chunks.add(current.toString());
                current.setLength(0);
            }
            current.append(character);
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }

    private static float textWidth(final PDFont font, final float fontSize, final String text) throws IOException {
        return font.getStringWidth(text == null ? "" : text) / 1000f * fontSize;
    }

    private static final class RenderContext implements AutoCloseable {
        /**
         * Active document.
         */
        private final PDDocument document;

        /**
         * Current generation mode.
         */
        private final GenerationMode mode;

        /**
         * Exam currently rendered.
         */
        private final Exam exam;

        /**
         * Active PDF content stream.
         */
        private PDPageContentStream content;

        /**
         * Current page number.
         */
        private int pageNumber;

        /**
         * Current page reference.
         */
        private PDPage currentPage;

        /**
         * Current y cursor position.
         */
        private float y;

        private RenderContext(final PDDocument document, final GenerationMode currentMode, final Exam currentExam) throws IOException {
            this.document = document;
            this.mode = currentMode;
            this.exam = currentExam;
            this.pageNumber = 0;
            startNewPage();
        }

        private void startNewPage() throws IOException {
            if (content != null) {
                content.close();
            }

            pageNumber++;
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            currentPage = page;
            content = new PDPageContentStream(document, page);
            drawFooter(this);
            content.setFont(BODY_FONT, BODY_FONT_SIZE);
            y = PAGE_HEIGHT - TOP_MARGIN;
        }

        @Override
        public void close() throws IOException {
            if (content != null) {
                content.close();
            }
        }
    }

    private static void drawFooter(final RenderContext context) throws IOException {
        context.content.setFont(BODY_FONT, SMALL_FONT_SIZE);
        context.content.beginText();
        context.content.newLineAtOffset(LEFT_MARGIN, 24);
        context.content.showText("JExam");
        context.content.endText();

        context.content.beginText();
        context.content.newLineAtOffset(PAGE_WIDTH - RIGHT_MARGIN - 60, 24);
        context.content.showText("Page " + context.pageNumber);
        context.content.endText();
    }

    private String chapterSummary(final Chapter chapter) {
        int includedTaskCount = 0;
        float points = 0f;
        int easy = 0;
        int medium = 0;
        int hard = 0;
        for (Task task : chapter.getTasks()) {
            if (!shouldIncludeTask(task, GenerationMode.EXAM) && !shouldIncludeTask(task, GenerationMode.SOLUTION) && !shouldIncludeTask(task, GenerationMode.MOCK_EXAM)) {
                continue;
            }
            includedTaskCount++;
            points += (float) task.getPoints();
            if (task.getDifficulty() == Difficulty.EASY) {
                easy++;
            } else if (task.getDifficulty() == Difficulty.MEDIUM) {
                medium++;
            } else if (task.getDifficulty() == Difficulty.HARD) {
                hard++;
            }
        }

        return "tasks=" + includedTaskCount
            + ", points=" + formatPoints(points)
            + ", difficulty=" + easy + "/" + medium + "/" + hard;
    }

    private record ChapterBookmark(String title, PDPage page, List<TaskBookmark> taskBookmarks) {
    }

    private record TaskBookmark(String title, PDPage page) {
    }
}
