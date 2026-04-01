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

import java.nio.file.Path;
import java.util.List;

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
    }

    /**
     * Loads and validates an exam from disk.
     *
     * @param path XML input path
     * @throws ExamXmlException if loading or validation fails
     */
    public void openExam(Path path) throws ExamXmlException {
        currentExam = persistenceService.loadValidated(path);
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
        ValidationResult result = validateCurrentExam();
        if (!result.isValid()) {
            throw new IllegalStateException("Exam is invalid: " + result.getErrors());
        }
        pdfGenerationService.generate(currentExam, mode, outputPath);
    }

    /**
     * Adds a chapter initialized with one default task.
     *
     * @param name chapter title
     */
    public void addChapter(String name) {
        currentExam.addChapter(new Chapter(name, List.of(defaultTask())));
    }

    /**
     * Removes a chapter by index.
     *
     * @param chapterIndex chapter index in current exam
     */
    public void removeChapter(int chapterIndex) {
        currentExam.removeChapter(chapterIndex);
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
        taskAt(chapterIndex, taskIndex).removeVariant(variantIndex);
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
        return currentExam.getChapters().get(chapterIndex);
    }

    private Task taskAt(int chapterIndex, int taskIndex) {
        return chapterAt(chapterIndex).getTasks().get(taskIndex);
    }

    private Variant variantAt(int chapterIndex, int taskIndex, int variantIndex) {
        return taskAt(chapterIndex, taskIndex).getVariants().get(variantIndex);
    }

    private Exam createDefaultExam() {
        return new Exam("New Exam", List.of(new Chapter("New Chapter", List.of(defaultTask()))));
    }

    private Task defaultTask() {
        return new Task(
            "New Task",
            1.0,
            Difficulty.MEDIUM,
            Scope.EXAM,
            List.of(new Variant("New Question", "New Answer"))
        );
    }
}
