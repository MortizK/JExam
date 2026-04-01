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

    public Exam getCurrentExam() {
        return currentExam;
    }

    public void newExam() {
        currentExam = createDefaultExam();
    }

    public void openExam(Path path) throws ExamXmlException {
        currentExam = persistenceService.loadValidated(path);
    }

    public void saveExam(Path path) throws ExamXmlException {
        persistenceService.saveValidated(currentExam, path);
    }

    public ValidationResult validateCurrentExam() {
        return validator.validate(currentExam);
    }

    public void generatePdf(GenerationMode mode, Path outputPath) {
        ValidationResult result = validateCurrentExam();
        if (!result.isValid()) {
            throw new IllegalStateException("Exam is invalid: " + result.getErrors());
        }
        pdfGenerationService.generate(currentExam, mode, outputPath);
    }

    public void addChapter(String name) {
        currentExam.addChapter(new Chapter(name, List.of(defaultTask())));
    }

    public void removeChapter(int chapterIndex) {
        currentExam.removeChapter(chapterIndex);
    }

    public void addTask(int chapterIndex, String taskName) {
        Task task = defaultTask();
        task.setName(taskName);
        currentExam.getChapters().get(chapterIndex).addTask(task);
    }

    public void removeTask(int chapterIndex, int taskIndex) {
        currentExam.getChapters().get(chapterIndex).removeTask(taskIndex);
    }

    public void addVariant(int chapterIndex, int taskIndex) {
        currentExam.getChapters().get(chapterIndex).getTasks().get(taskIndex)
            .addVariant(new Variant("New Question", "New Answer"));
    }

    public void removeVariant(int chapterIndex, int taskIndex, int variantIndex) {
        currentExam.getChapters().get(chapterIndex).getTasks().get(taskIndex).removeVariant(variantIndex);
    }

    public void updateTaskDetails(int chapterIndex, int taskIndex, String name, double points, Difficulty difficulty, Scope scope) {
        Task task = currentExam.getChapters().get(chapterIndex).getTasks().get(taskIndex);
        task.setName(name);
        task.setPoints(points);
        task.setDifficulty(difficulty);
        task.setScope(scope);
    }

    public void updateVariantDetails(int chapterIndex, int taskIndex, int variantIndex, String question, String answer) {
        Variant variant = currentExam.getChapters().get(chapterIndex).getTasks().get(taskIndex).getVariants().get(variantIndex);
        variant.setQuestion(question);
        variant.setAnswer(answer);
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
