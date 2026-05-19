package com.jexam.generation;

import com.jexam.model.Chapter;
import com.jexam.model.DifficultyDistributionSummary;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
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
import com.jexam.app.UiTextCatalog;
import com.jexam.app.UiLanguage;

/**
 * PDF generation service backed by Apache PDFBox.
 *
 * <p>This implementation renders an {@link com.jexam.model.Exam} into a PDF using
 * a simple layout model: a cover (optional), then one chapter per section with
 * tasks and variants rendered in sequence. Coordinates use the A4 page
 * coordinate space where the origin (0,0) is the bottom-left corner; the
 * renderer maintains a descending Y cursor that moves down the page as lines
 * and blocks are written. When remaining space is insufficient the renderer
 * starts a new page.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 * <li>Layout tasks and answer boxes with heuristics to estimate heights.</li>
 * <li>Wrap and split long text to fit available width.</li>
 * <li>Produce an outline (bookmarks) and basic metadata with difficulty summary.</li>
 * </ul>
 *
 * <p>The class favors readability and predictable output over sophisticated
 * typographic features.</p>
 *
 * @author Moritz
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
    private static final float SECTION_GAP = 10;
    private static final float CHAPTER_GAP = 18;
    private static final float QUESTION_GAP = 6;
    private static final float ANSWER_LABEL_GAP = 4;
    private static final float ANSWER_PADDING = 10;
    private static final float MIN_ANSWER_BOX_HEIGHT = 60;
    private static final float ANSWER_LINE_HEIGHT = 14;

    // Feature flags / options
    private boolean coverEnabled = true;
    // Localization
    private UiTextCatalog uiTextCatalog = UiTextCatalog.loadDefault();
    private UiLanguage uiLanguage = UiLanguage.ENGLISH;

    /**
     * Sets UI language to be used for localized cover texts.
     *
     * @param language selected UI language; {@code null} falls back to English
     */
    public void setUiLanguage(final UiLanguage language) {
        this.uiLanguage = language == null ? UiLanguage.ENGLISH : language;
    }

    /**
     * Enable or disable generation of the cover page. Default: true.
     *
     * @param enabled whether the cover page should be rendered
     */
    public void setCoverEnabled(final boolean enabled) {
        this.coverEnabled = enabled;
    }

    /**
     * {@inheritDoc}
     *
     * Generates a PDF for the given exam and output mode.
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
                try (RenderContext context = new RenderContext(document, mode, exam, uiTextCatalog.text(uiLanguage, "footer.page"))) {
                    if (coverEnabled) {
                        writeCoverPage(context, exam);
                        if (exam.chapterCount() > 0) {
                            context.startNewPage();
                        }
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

    /**
     * Populate basic PDF metadata for the generated document.
     *
     * The difficulty summary is encoded in the keyword field so external PDF
     * tools can index or filter the document by difficulty distribution.
     */
    private void applyMetadata(final PDDocument document, final Exam exam, final GenerationMode mode) {
        // Populate basic PDF document metadata. The keywords field contains a
        // compact difficulty summary so external tools can index generated
        // documents by difficulty distribution.
        PDDocumentInformation information = new PDDocumentInformation();
        DifficultyDistributionSummary summary = summarizeDifficultyDistribution(exam);
        information.setTitle(oneLine(exam.getName()));
        information.setSubject("Exam PDF - " + mode.name());
        information.setKeywords(summary.toMetadataText());
        information.setCreator("JExam");
        document.setDocumentInformation(information);
    }

    private DifficultyDistributionSummary summarizeDifficultyDistribution(final Exam exam) {
        /**
         * Create a difficulty summary for the whole exam by collecting all
         * tasks across chapters. This mirrors the summary used for metadata and
         * warnings and treats the exam as a flat list of tasks.
         */
        List<Task> tasks = new ArrayList<>();
        for (int chapterIndex = 0; chapterIndex < exam.chapterCount(); chapterIndex++) {
            tasks.addAll(exam.chapterAt(chapterIndex).getTasks());
        }
        return DifficultyDistributionSummary.fromTasks(tasks);
    }

    /**
     * Attach a document outline (bookmarks) to the PDF.
     *
     * The outline mirrors the rendered structure: optional cover, chapters,
     * and tasks. This improves navigation in PDF viewers.
     */
    private void applyOutline(final PDDocument document, final List<ChapterBookmark> chapterBookmarks) {
        // Build a simple document outline (bookmarks) referencing the cover
        // and chapter/task locations recorded while rendering. This improves
        // navigation in PDF viewers.
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

    /**
     * Create a page destination positioned at the top of the provided page.
     * Used by the document outline (bookmarks) so items jump to the top of a
     * page in PDF viewers.
     */
    /**
     * Create a top-of-page destination for bookmark navigation.
     */
    private PDPageXYZDestination destinationForPage(final PDPage page) {
        PDPageXYZDestination destination = new PDPageXYZDestination();
        destination.setPage(page);
        destination.setTop(Math.round(page.getMediaBox().getHeight()));
        return destination;
    }

    // Cover Page layout
    /**
     * Render the optional cover page. This method writes localized labels,
     * exam title, per-chapter placeholders and totals. It does not advance to
     * the next page; the caller is responsible for creating a new page when
     * appropriate.
     *
     * @param context current render context with document and cursor
     * @param exam exam model to render cover content for
     * @throws IOException on PDFBox I/O errors
     */
    /**
     * Render the cover page.
     *
     * The cover contains localized labels, the exam title, and point summary
     * placeholders. It uses the current y cursor and keeps the layout within
     * the configured page margins.
     */
    private void writeCoverPage(final RenderContext context, final Exam exam)
        throws IOException {
        
        // Draw 7 squares below the label as placeholders
        int squares = 7;
        float squareSize = 12f;
        float squareSpacing = 6f;
        float squaresTop = context.y - 6f; // slight offset under the label baseline
        float squaresBottom = squaresTop - squareSize;
        float squaresStartX = LEFT_MARGIN;
        context.content.setLineWidth(0.8f);
        for (int i = 0; i < squares; i++) {
            float x = squaresStartX + i * (squareSize + squareSpacing);
            context.content.addRect(x, squaresBottom, squareSize, squareSize);
        }
        context.content.stroke();

        // Top-left: Matrikelnummer label + 7 small squares
        context.content.setFont(BOLD_FONT, BODY_FONT_SIZE);
        // Use localized label for student id
        String studentIdLabel = uiTextCatalog.text(uiLanguage, "cover.studentId");
        // Keep a snapshot of y to compute square positions
        context.y = writeText(context, studentIdLabel + ":", LEFT_MARGIN, context.y, BOLD_FONT, BODY_FONT_SIZE);

        // increase spacing after the student id line
        context.y -= SECTION_GAP * 2f;

        // Exam type (centered) - uses localization key "cover.examType" if available
        String examType = uiTextCatalog.text(uiLanguage, "cover.examType");
        if (examType != null && !examType.isBlank()) {
            context.content.setFont(BOLD_FONT, SUBTITLE_FONT_SIZE);
            context.y = writeCenteredLine(context, context.y, examType, BOLD_FONT, SUBTITLE_FONT_SIZE);
            context.y -= SECTION_GAP;
        }

        // Exam name (centered)
        context.content.setFont(BODY_FONT, TITLE_FONT_SIZE);
        context.y = writeCenteredLine(context, context.y, oneLine(exam.getName()), BODY_FONT, SUBTITLE_FONT_SIZE);
        context.y -= CHAPTER_GAP;

        // Instruction text (centered under the cover header)
        context.content.setFont(BODY_FONT, BODY_FONT_SIZE);
        context.y = writeCenteredLine(context, context.y, uiTextCatalog.text(uiLanguage, "cover.instruction"), BODY_FONT, BODY_FONT_SIZE);
        context.y -= SECTION_GAP;

        // Header row for the points table
        context.content.setFont(BOLD_FONT, BODY_FONT_SIZE);
        context.y = writeText(context, "Aufgabe", LEFT_MARGIN, context.y, BOLD_FONT, BODY_FONT_SIZE);
        // reserve a box for reached points and print total on the right
        float pointsBoxWidth = 50f;
        float pointsBoxHeight = LINE_HEIGHT - 4f;
        context.content.setFont(BODY_FONT, BODY_FONT_SIZE);
        context.y -= SECTION_GAP;

        final int chapterCount = exam.chapterCount();
        for (int index = 0; index < chapterCount; index++) {
            Chapter chapter = exam.chapterAt(index);
            context.y = ensureSpace(context, LINE_HEIGHT * 2.0f);
            // Compose left text for chapter
            String chapterLabel = chapterHeading(index, chapterCount, chapter.getName(), totalPoints(chapter, context.mode));
            float beforeY = context.y;
            context.y = writeText(context, oneLine(chapterLabel), LEFT_MARGIN, context.y, BODY_FONT, BODY_FONT_SIZE);

            // Draw placeholder box for reached points (empty)
            float boxX = LEFT_MARGIN + CONTENT_WIDTH - pointsBoxWidth - 10f;
            context.content.addRect(boxX, beforeY - 1f, pointsBoxWidth, pointsBoxHeight);
            context.content.stroke();

            // Draw total points for chapter to the right of the box
            String totalPts = padLeft(formatPoints(totalPoints(chapter, context.mode)), 6);
            // write total points slightly above the boxBottom so they align
            writeText(context, totalPts, boxX + pointsBoxWidth + 6f, beforeY, BODY_FONT, BODY_FONT_SIZE);
        }

        context.y -= SECTION_GAP;
        float beforeY = context.y;
        float boxX = LEFT_MARGIN + CONTENT_WIDTH - pointsBoxWidth - 10f;
        writeText(context, uiTextCatalog.text(uiLanguage, "cover.bonus"), LEFT_MARGIN, beforeY, BOLD_FONT, BODY_FONT_SIZE);
        // Draw placeholder box for bonus points (empty)
        context.content.addRect(boxX, beforeY - 1f, pointsBoxWidth, pointsBoxHeight);
        context.content.stroke();
        
        context.y -= LINE_HEIGHT;
        beforeY = context.y;
        writeText(context, uiTextCatalog.text(uiLanguage, "cover.total"), LEFT_MARGIN, context.y, BOLD_FONT, BODY_FONT_SIZE);
        // Draw placeholder box for total points (empty)
        context.content.addRect(boxX, beforeY - 1f, pointsBoxWidth, pointsBoxHeight);
        context.content.stroke();
        // Draw total points for the exam to the right of the box
        String totalPts = padLeft(formatPoints(totalPoints(exam, context.mode)), 6);
        writeText(context, totalPts, boxX + pointsBoxWidth + 6f, beforeY, BODY_FONT, BODY_FONT_SIZE);
    }

    /**
     * Render all chapters of the exam. Starts a new page before each
     * subsequent chapter to ensure chapters begin on fresh pages.
     *
     * @return list of chapter bookmarks with their start pages and task anchors
     */
    /**
     * Render all chapters in order.
     *
     * Each chapter begins on a fresh page so the outline and visual layout are
     * predictable.
     */
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

    /**
     * Render chapter header and its tasks, returning a bookmark representing
     * the chapter start page and task anchors for the outline.
     */
    /**
     * Render one chapter heading and its tasks.
     */
    private ChapterBookmark writeChapter(final RenderContext context, final int chapterIndex, final Chapter chapter)
        throws IOException {
        PDPage chapterPage = context.currentPage;
        final int chapterCount = context.exam.chapterCount();
        final String heading = chapterHeading(chapterIndex, chapterCount, chapter.getName(), totalPoints(chapter, context.mode));
        context.y = writeSectionTitle(
            context,
            heading
        );
        context.y -= SECTION_GAP;
        List<TaskBookmark> taskBookmarks = writeTasks(context, chapter);
        return new ChapterBookmark(heading, chapterPage, taskBookmarks);
    }

    /**
     * Render all tasks for a chapter. For each task the method:
     * - estimates the vertical space required,
     * - ensures there is room on the page or starts a new one,
     * - writes the task title and variants, and
     * - collects task bookmarks for the outline.
     */
    /**
     * Render all tasks for a chapter and collect outline bookmarks.
     */
    private List<TaskBookmark> writeTasks(final RenderContext context, final Chapter chapter)
        throws IOException {
        List<Task> tasks = includedTasks(chapter, context.mode);
        boolean enumerateTasks = tasks.size() > 1;
        int displayTaskIndex = 0;
        List<TaskBookmark> taskBookmarks = new ArrayList<>();
        for (Task task : tasks) {

            displayTaskIndex++;

            float taskHeight = estimateTaskBlockHeight(task, context.mode);
            // If the whole task block fits on the remaining page use the
            // estimated height; otherwise ensure a small header space and let
            // the content flow to the next page as needed.
            if (taskHeight <= CONTENT_HEIGHT) {
                context.y = ensureSpace(context, taskHeight);
            } else {
                // Large blocks are split — reserve at least a couple of lines
                // so the title is not orphaned at the page bottom.
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

    /**
     * Render the variants of one task.
     *
     * Each variant writes the question text, reserves space for the answer
     * box, and fills the box with solution text when rendering solutions.
     */
    private void writeVariants(final RenderContext context, final Task task)
        throws IOException {
        for (int i = 0; i < task.getVariants().size(); i++) {
            Variant variant = task.getVariants().get(i);
            context.y = ensureSpace(context, estimateVariantBlockHeight(task, variant, context.mode));
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
            drawAnswerBox(context, variant, boxHeight);
        }
    }

    /**
     * Decide whether a task belongs into the selected generation mode.
     * - EXAM: only include tasks with scope="exam"
     * - MOCK_EXAM: only include tasks with scope="mock-exam"
     */
    private boolean shouldIncludeTask(final Task task, final GenerationMode mode) {
        if (mode == GenerationMode.EXAM) {
            return task.getScope() == Scope.EXAM;
        }
        if (mode == GenerationMode.MOCK_EXAM) {
            return task.getScope() == Scope.MOCK_EXAM;
        }
        return true;
    }

    /**
     * Write a single line at the left page margin.
     */
    private float writeLine(
        final RenderContext context,
        final float y,
        final String text
    ) throws IOException {
        return writeText(context, oneLine(text), LEFT_MARGIN, y, BODY_FONT, BODY_FONT_SIZE);
    }

    /**
     * Write a chapter or section title using the subtitle font.
     */
    private float writeSectionTitle(final RenderContext context, final String text) throws IOException {
        context.content.setFont(BOLD_FONT, SUBTITLE_FONT_SIZE);
        float result = writeText(context, oneLine(text), LEFT_MARGIN, context.y, BOLD_FONT, SUBTITLE_FONT_SIZE);
        context.content.setFont(BODY_FONT, BODY_FONT_SIZE);
        return result;
    }

    /**
     * Write raw text at a fixed position and advance the logical cursor.
     */
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

    /**
     * Write text centered within the content width.
     */
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

    /**
     * Write pre-wrapped lines with a custom logical line height.
     */
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

    /**
     * Ensure enough vertical room remains for a block.
     *
     * If the current page cannot fit the requested height the renderer starts
     * a new page and resets the cursor.
     */
    private float ensureSpace(final RenderContext context, final float requiredHeight) throws IOException {
        // If the required height would cross the bottom margin, start a new
        // page so the block is rendered cleanly at the top of the next page.
        if (context.y - requiredHeight < BOTTOM_MARGIN) {
            context.startNewPage();
        }
        return context.y;
    }

    /**
     * Draw the answer box for a variant.
     *
     * In solution mode the answer text is written inside the box; otherwise
     * the box remains blank for handwritten responses.
     */
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

    /**
     * Collapse multi-line text into a single trimmed line.
     */
    private String oneLine(final String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\n', ' ').replace('\r', ' ').trim();
    }

    /**
     * Calculate total points across all exam chapters for the active mode.
     */
    private float totalPoints(final Exam exam, final GenerationMode mode) {
        float total = 0f;
        for (Chapter chapter : exam.getChapters()) {
            total += totalPoints(chapter, mode);
        }
        return total;
    }

    /**
     * Calculate total points for a single chapter after mode filtering.
     */
    private float totalPoints(final Chapter chapter, final GenerationMode mode) {
        float total = 0f;
        for (Task task : includedTasks(chapter, mode)) {
            total += (float) task.getPoints();
        }
        return total;
    }

    /**
     * Return tasks that should be included for the selected generation mode.
     */
    private List<Task> includedTasks(final Chapter chapter, final GenerationMode mode) {
        List<Task> tasks = new ArrayList<>();
        for (Task task : chapter.getTasks()) {
            if (shouldIncludeTask(task, mode)) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * Format a point value using a stable one-decimal representation.
     */
    private String formatPoints(final double points) {
        return String.format(java.util.Locale.ROOT, "%.1f", points);
    }

    /**
     * Build the visible chapter heading with chapter number and point total.
     */
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

    /**
     * Convert a zero-based index to a lowercase alphabetical ordinal.
     */
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

    /**
     * Left-pad a string to the requested width.
     */
    private String padLeft(final String value, final int width) {
        if (value.length() >= width) {
            return value;
        }
        return " ".repeat(width - value.length()) + value;
    }

    /**
     * Estimate the height required for a full task block.
     */
    private float estimateTaskBlockHeight(final Task task, final GenerationMode mode) throws IOException {
        float totalHeight = LINE_HEIGHT + SECTION_GAP;
        for (Variant variant : task.getVariants()) {
            totalHeight += estimateVariantBlockHeight(task, variant, mode);
            totalHeight += SECTION_GAP;
        }
        return totalHeight;
    }

    /**
     * Estimate the vertical space needed for one variant.
     */
    private float estimateVariantBlockHeight(final Task task, final Variant variant, final GenerationMode mode)
        throws IOException {
        float questionHeight = wrapText(variant.getQuestion(), BODY_FONT, BODY_FONT_SIZE, CONTENT_WIDTH - 20).size() * LINE_HEIGHT;
        float answerHeight = estimateAnswerBoxHeight(variant.getAnswer(), task.getPoints());
        float answerTextHeight = mode == GenerationMode.SOLUTION
            ? wrapText(variant.getAnswer(), BODY_FONT, BODY_FONT_SIZE, CONTENT_WIDTH - 2 * ANSWER_PADDING).size() * ANSWER_LINE_HEIGHT
            : 0f;
        return LINE_HEIGHT + questionHeight + QUESTION_GAP + Math.max(answerHeight, answerTextHeight + ANSWER_PADDING * 2) + LINE_HEIGHT;
    }

    /**
     * Estimate the height of an answer box.
     *
     * The estimate is based on wrapped answer text and a handwritten-writing
     * allowance derived from the task points.
     */
    static float estimateAnswerBoxHeight(final String answerText, final double points) throws IOException {
        List<String> wrapped = wrapText(answerText, BODY_FONT, BODY_FONT_SIZE, CONTENT_WIDTH - 2 * ANSWER_PADDING);
        float textHeight = wrapped.size() * ANSWER_LINE_HEIGHT;
        float handwrittenPadding = Math.max(28f, (float) points * 10f);
        return Math.max(MIN_ANSWER_BOX_HEIGHT, textHeight + handwrittenPadding + (2 * ANSWER_PADDING));
    }

    /**
     * Wrap text to the given width using the provided font metrics.
     */
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
                // If the candidate fits add it to the current line.
                if (textWidth(font, fontSize, candidate) <= maxWidth) {
                    currentLine.setLength(0);
                    currentLine.append(candidate);
                    continue;
                }

                if (!currentLine.isEmpty()) {
                    // flush current line before handling the too-long candidate
                    lines.add(currentLine.toString());
                    currentLine.setLength(0);
                }

                // Word itself might be longer than the available width. In
                // that case split it into chunks that fit.
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

    /**
     * Split a single overlong word into smaller chunks that fit.
     */
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
            // Grow current chunk until adding the next character would exceed
            // the available width, then flush the chunk.
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

    /**
     * Measure text width in PDF user space units for the given font size.
     */
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

        /**
         * Localized label for the page footer (e.g. "Page" / "Seite").
         */
        private final String footerPageLabel;

        /**
         * Create a render context and initialize the first page.
         */
        private RenderContext(final PDDocument document, final GenerationMode currentMode, final Exam currentExam, final String footerPageLabel) throws IOException {
            this.document = document;
            this.mode = currentMode;
            this.exam = currentExam;
            this.footerPageLabel = footerPageLabel == null ? "Page" : footerPageLabel;
            this.pageNumber = 0;
            startNewPage();
        }

        /**
         * Close the current stream if needed and start a fresh page.
         */
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
        /**
         * Close the current content stream when rendering is finished.
         */
        public void close() throws IOException {
            if (content != null) {
                content.close();
            }
        }
    }

    /**
     * Draw a small footer on the current page.
     */
    private static void drawFooter(final RenderContext context) throws IOException {
        context.content.setFont(BODY_FONT, SMALL_FONT_SIZE);
        context.content.beginText();
        context.content.newLineAtOffset(LEFT_MARGIN, 24);
        context.content.showText("JExam");
        context.content.endText();

        context.content.beginText();
        context.content.newLineAtOffset(PAGE_WIDTH - RIGHT_MARGIN - 60, 24);
        context.content.showText(context.footerPageLabel + " " + context.pageNumber);
        context.content.endText();
    }

    private record ChapterBookmark(String title, PDPage page, List<TaskBookmark> taskBookmarks) {
    }

    private record TaskBookmark(String title, PDPage page) {
    }
}
