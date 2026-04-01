package com.jexam.app;

import com.jexam.generation.GenerationMode;
import com.jexam.generation.PdfBoxGenerationService;
import com.jexam.generation.PdfGenerationService;
import com.jexam.io.ExamPersistenceService;
import com.jexam.io.ExamXmlException;
import com.jexam.io.ExamXmlLoader;
import com.jexam.io.ExamXmlWriter;
import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.nio.file.Files;

/**
 * Application service that centralizes exam use-case orchestration.
 *
 * <p>This class acts as the controller-side boundary between the JavaFX view
 * and domain/services. It keeps JExamApp focused on presentation concerns.</p>
 */
public class ExamApplicationService {
    private final ExamValidator validator;
    private final PdfGenerationService pdfGenerationService;
    private final ExamPersistenceService persistenceService;
    private final List<Integer> generationChapterIndices;

    private Exam currentExam;

    public ExamApplicationService() {
        this.validator = new ExamValidator();
        this.pdfGenerationService = new PdfBoxGenerationService();
        this.persistenceService = new ExamPersistenceService(
            new ExamXmlLoader(),
            new ExamXmlWriter(),
            validator
        );
        this.currentExam = createDefaultExam();
        this.generationChapterIndices = new ArrayList<>();
        resetGenerationChapterSelection();
    }

    /**
     * Returns the exam currently managed by the application service.
     *
     * @return current exam state
     */
    public Exam getCurrentExam() {
        return currentExam;
    }

    /**
     * Replaces the current exam with a default empty template.
     */
    public void newExam() {
        currentExam = createDefaultExam();
        resetGenerationChapterSelection();
    }

    /**
     * Loads and validates an exam from disk.
     *
     * @param path XML input path
     * @throws ExamXmlException if loading or validation fails
     */
    public void openExam(Path path) throws ExamXmlException {
        currentExam = persistenceService.loadValidated(path);
        resetGenerationChapterSelection();
    }

    /**
     * Validates and saves the current exam to disk.
     *
     * @param path XML output path
     * @throws ExamXmlException if validation or writing fails
     */
    public void saveExam(Path path) throws ExamXmlException {
        persistenceService.saveValidated(currentExam, path);
    }

    /**
     * Runs full validation for the current exam.
     *
     * @return validation result with all detected errors
     */
    public ValidationResult validateCurrentExam() {
        return validator.validate(currentExam);
    }

    /**
     * Generates a PDF export for the current exam in the selected mode.
     *
     * @param mode export mode
     * @param outputPath destination PDF path
     */
    public void generatePdf(GenerationMode mode, Path outputPath) {
        Exam generationExam = buildExamForGeneration();
        ValidationResult result = validator.validate(generationExam);
        appendGenerationRuleErrors(result, mode, generationExam);
        if (!result.isValid()) {
            throw new IllegalStateException("Exam is invalid: " + result.getErrors());
        }
        pdfGenerationService.generate(generationExam, mode, outputPath);
    }

    /**
     * Generates a preview PDF into a temporary file and returns its path.
     *
     * @param mode preview generation mode
     * @return absolute path to generated preview PDF
     */
    public Path generatePreviewPdf(GenerationMode mode) {
        try {
            Path previewPath = Files.createTempFile("jexam-preview-", ".pdf");
            previewPath.toFile().deleteOnExit();
            generatePdf(mode, previewPath);
            return previewPath.toAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create preview file.", e);
        }
    }

    private void appendGenerationRuleErrors(
        final ValidationResult result,
        final GenerationMode mode,
        final Exam generationExam
    ) {
        if (mode != GenerationMode.EXAM && mode != GenerationMode.SOLUTION) {
            return;
        }

        final int chapterCount = generationExam.chapterCount();
        for (int chapterIndex = 0; chapterIndex < chapterCount; chapterIndex++) {
            final Chapter chapter = generationExam.chapterAt(chapterIndex);
            if (!hasDifficultyThirdsForExamScope(chapter)) {
                result.addError(
                    "exam.chapters[" + chapterIndex + "].tasks.difficultyDistribution",
                    "Exam tasks must be distributed by difficulty in exact thirds with at least one easy, medium, and hard task."
                );
            }
        }
    }

    private boolean hasDifficultyThirdsForExamScope(final Chapter chapter) {
        int easyCount = 0;
        int mediumCount = 0;
        int hardCount = 0;

        for (Task task : chapter.getTasks()) {
            if (task.getScope() != Scope.EXAM) {
                continue;
            }

            Difficulty difficulty = task.getDifficulty();
            if (difficulty == Difficulty.EASY) {
                easyCount++;
            } else if (difficulty == Difficulty.MEDIUM) {
                mediumCount++;
            } else if (difficulty == Difficulty.HARD) {
                hardCount++;
            }
        }

        final int totalExamTasks = easyCount + mediumCount + hardCount;
        if (totalExamTasks < 3 || totalExamTasks % 3 != 0) {
            return false;
        }

        final int target = totalExamTasks / 3;
        return easyCount == target
            && mediumCount == target
            && hardCount == target;
    }

    /**
     * Adds a chapter initialized with one default task.
     *
     * @param name chapter title
     */
    public void addChapter(String name) {
        currentExam.addChapter(new Chapter(name, List.of(defaultTask())));
        resetGenerationChapterSelection();
    }

    /**
     * Removes a chapter by index.
     *
     * @param chapterIndex chapter index in current exam
     */
    public void removeChapter(int chapterIndex) {
        currentExam.removeChapter(chapterIndex);
        resetGenerationChapterSelection();
    }

    /**
     * Adds a task to a chapter.
     *
     * @param chapterIndex target chapter index
     * @param taskName task title
     */
    public void addTask(int chapterIndex, String taskName) {
        Task task = defaultTask();
        task.setName(taskName);
        chapterAt(chapterIndex).addTask(task);
    }

    /**
     * Returns configured chapter indices used for generation.
     *
     * @return ordered generation chapter indices
     */
    public List<Integer> generationChapterOrder() {
        return Collections.unmodifiableList(generationChapterIndices);
    }

    /**
     * Moves a generation chapter one step up in the order.
     *
     * @param orderIndex index in generation order list
     */
    public void moveGenerationChapterUp(int orderIndex) {
        if (orderIndex <= 0 || orderIndex >= generationChapterIndices.size()) {
            return;
        }
        Collections.swap(generationChapterIndices, orderIndex, orderIndex - 1);
    }

    /**
     * Moves a generation chapter one step down in the order.
     *
     * @param orderIndex index in generation order list
     */
    public void moveGenerationChapterDown(int orderIndex) {
        if (orderIndex < 0 || orderIndex >= generationChapterIndices.size() - 1) {
            return;
        }
        Collections.swap(generationChapterIndices, orderIndex, orderIndex + 1);
    }

    /**
     * Excludes a chapter from generation by its position in generation order.
     *
     * @param orderIndex index in generation order list
     */
    public void excludeGenerationChapter(int orderIndex) {
        if (orderIndex < 0 || orderIndex >= generationChapterIndices.size()) {
            return;
        }
        generationChapterIndices.remove(orderIndex);
    }

    /**
     * Includes a chapter for generation by chapter index.
     *
     * @param chapterIndex chapter index in current exam
     */
    public void includeGenerationChapter(int chapterIndex) {
        if (chapterIndex < 0 || chapterIndex >= currentExam.chapterCount()) {
            return;
        }
        if (!generationChapterIndices.contains(chapterIndex)) {
            generationChapterIndices.add(chapterIndex);
        }
    }

    /**
     * Restores generation selection to all chapters in natural order.
     */
    public void resetGenerationChapterSelection() {
        generationChapterIndices.clear();
        for (int chapterIndex = 0; chapterIndex < currentExam.chapterCount(); chapterIndex++) {
            generationChapterIndices.add(chapterIndex);
        }
    }

    /**
     * Removes a task from a chapter.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex task index in chapter
     */
    public void removeTask(int chapterIndex, int taskIndex) {
        chapterAt(chapterIndex).removeTask(taskIndex);
    }

    /**
     * Adds a default variant to a task.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     */
    public void addVariant(int chapterIndex, int taskIndex) {
        taskAt(chapterIndex, taskIndex).addVariant(new Variant("New Question", "New Answer"));
    }

    /**
     * Removes a variant from a task.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     * @param variantIndex target variant index
     */
    public void removeVariant(int chapterIndex, int taskIndex, int variantIndex) {
        Task task = taskAt(chapterIndex, taskIndex);
        if (task.variantCount() <= 1) {
            throw new IllegalStateException(
                "A task must contain at least one variant."
            );
        }
        task.removeVariant(variantIndex);
    }

    /**
     * Updates editable task properties.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     * @param name task name
     * @param points task points
     * @param difficulty task difficulty
     * @param scope task scope
     */
    public void updateTaskDetails(int chapterIndex, int taskIndex, String name, double points, Difficulty difficulty, Scope scope) {
        Task task = taskAt(chapterIndex, taskIndex);
        task.setName(name);
        task.setPoints(points);
        task.setDifficulty(difficulty);
        task.setScope(scope);
    }

    /**
     * Updates editable variant content.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     * @param variantIndex target variant index
     * @param question question text
     * @param answer answer text
     */
    public void updateVariantDetails(int chapterIndex, int taskIndex, int variantIndex, String question, String answer) {
        Variant variant = variantAt(chapterIndex, taskIndex, variantIndex);
        variant.setQuestion(question);
        variant.setAnswer(answer);
    }

    private Chapter chapterAt(int chapterIndex) {
        return currentExam.chapterAt(chapterIndex);
    }

    private Task taskAt(int chapterIndex, int taskIndex) {
        return currentExam.taskAt(chapterIndex, taskIndex);
    }

    private Variant variantAt(int chapterIndex, int taskIndex, int variantIndex) {
        return currentExam.variantAt(chapterIndex, taskIndex, variantIndex);
    }

    private Exam createDefaultExam() {
        return new Exam("New Exam", List.of(new Chapter("New Chapter", List.of(defaultTask()))));
    }

    private Exam buildExamForGeneration() {
        if (generationChapterIndices.isEmpty()) {
            throw new IllegalStateException("No chapters selected for PDF generation.");
        }

        List<Chapter> chapters = new ArrayList<>();
        for (int chapterIndex : generationChapterIndices) {
            if (chapterIndex >= 0 && chapterIndex < currentExam.chapterCount()) {
                chapters.add(currentExam.chapterAt(chapterIndex));
            }
        }

        if (chapters.isEmpty()) {
            throw new IllegalStateException("No chapters selected for PDF generation.");
        }
        return new Exam(currentExam.getName(), chapters);
    }

    private Task defaultTask() {
        return new Task(
            "New Subtask",
            1.0,
            Difficulty.EASY,
            Scope.EXAM,
            List.of(new Variant("New Question", "New Answer"))
        );
    }
}
