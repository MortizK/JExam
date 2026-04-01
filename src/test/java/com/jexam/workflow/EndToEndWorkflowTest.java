package com.jexam.workflow;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;
import com.jexam.io.ExamPersistenceService;
import com.jexam.io.ExamXmlLoader;
import com.jexam.io.ExamXmlWriter;
import com.jexam.generation.GenerationMode;
import com.jexam.generation.PdfBoxGenerationService;
import com.jexam.generation.PdfGenerationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EndToEndWorkflowTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldCompleteWorkflowFromXmlToPdf() throws Exception {
        Exam exam = sampleExam();
        ExamValidator validator = new ExamValidator();

        ValidationResult beforeSave = validator.validate(exam);
        assertTrue(beforeSave.isValid());

        ExamPersistenceService persistenceService = new ExamPersistenceService(
            new ExamXmlLoader(),
            new ExamXmlWriter(),
            validator
        );

        Path xmlPath = tempDir.resolve("workflow.xml");
        persistenceService.saveValidated(exam, xmlPath);
        assertTrue(Files.exists(xmlPath));
        assertTrue(Files.size(xmlPath) > 0);

        Exam loaded = persistenceService.loadValidated(xmlPath);
        assertEquals(exam.getName(), loaded.getName());
        assertEquals(2, loaded.getChapters().size());

        PdfGenerationService pdfService = new PdfBoxGenerationService();
        Path examPdf = tempDir.resolve("workflow-exam.pdf");
        Path solutionPdf = tempDir.resolve("workflow-solution.pdf");
        Path mockPdf = tempDir.resolve("workflow-mock.pdf");

        pdfService.generate(loaded, GenerationMode.EXAM, examPdf);
        pdfService.generate(loaded, GenerationMode.SOLUTION, solutionPdf);
        pdfService.generate(loaded, GenerationMode.MOCK_EXAM, mockPdf);

        assertTrue(Files.exists(examPdf));
        assertTrue(Files.exists(solutionPdf));
        assertTrue(Files.exists(mockPdf));
        assertTrue(Files.size(examPdf) > 0);
        assertTrue(Files.size(solutionPdf) > 0);
        assertTrue(Files.size(mockPdf) > 0);
    }

    private Exam sampleExam() {
        Task examTaskOne = new Task(
            "Architecture",
            3.0,
            Difficulty.MEDIUM,
            Scope.EXAM,
            List.of(new Variant("Explain MVC.", "Model View Controller separation."))
        );

        Task examTaskTwo = new Task(
            "Validation",
            2.0,
            Difficulty.EASY,
            Scope.EXAM,
            List.of(new Variant("When should validation run?", "Before save and before generation."))
        );

        Task mockTask = new Task(
            "Warmup",
            1.0,
            Difficulty.EASY,
            Scope.MOCK_EXAM,
            List.of(new Variant("What is XML?", "A markup language for structured data."))
        );

        Chapter chapterOne = new Chapter("Software Engineering", List.of(examTaskOne, examTaskTwo));
        Chapter chapterTwo = new Chapter("Basics", List.of(mockTask));

        return new Exam("Workflow Demo", List.of(chapterOne, chapterTwo));
    }
}
