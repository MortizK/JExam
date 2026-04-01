package com.jexam.generation;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfBoxGenerationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldGenerateExamPdf() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("exam.pdf");

        service.generate(sampleExam(), GenerationMode.EXAM, out);

        assertTrue(Files.exists(out));
        assertTrue(Files.size(out) > 0);
        assertTrue(readPdfText(out).contains("Scope EXAM task"));
        assertTrue(!readPdfText(out).contains("Scope MOCK task"));
    }

    @Test
    void shouldGenerateMockExamPdf() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("mock.pdf");

        service.generate(sampleExam(), GenerationMode.MOCK_EXAM, out);

        assertTrue(Files.exists(out));
        assertTrue(Files.size(out) > 0);
        assertTrue(readPdfText(out).contains("Scope MOCK task"));
        assertTrue(!readPdfText(out).contains("Scope EXAM task"));
    }

    @Test
    void shouldIncludeAnswersInSolutionPdf() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("solution.pdf");

        service.generate(sampleExam(), GenerationMode.SOLUTION, out);

        String text = readPdfText(out);
        assertTrue(text.contains("Scope EXAM answer"));
    }

    private String readPdfText(Path path) throws Exception {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private Exam sampleExam() {
        Task examTask = new Task(
            "Scope EXAM task",
            2.0,
            Difficulty.MEDIUM,
            Scope.EXAM,
            List.of(new Variant("Scope EXAM question", "Scope EXAM answer"))
        );

        Task mockTask = new Task(
            "Scope MOCK task",
            1.0,
            Difficulty.EASY,
            Scope.MOCK_EXAM,
            List.of(new Variant("Scope MOCK question", "Scope MOCK answer"))
        );

        Chapter chapter = new Chapter("Demo Chapter", List.of(examTask, mockTask));
        return new Exam("Demo", List.of(chapter));
    }
}
