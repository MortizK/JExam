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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertTrue(readPdfText(out).contains("Scope EXAM task"));
    }

    @Test
    void shouldIncludeAnswersInSolutionPdf() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("solution.pdf");

        service.generate(sampleExam(), GenerationMode.SOLUTION, out);

        String text = readPdfText(out);
        assertTrue(text.contains("Scope EXAM answer"));
    }

    @Test
    void shouldAddCoverPageAndStartEachChapterOnItsOwnPage() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("chapter-breaks.pdf");

        service.generate(twoChapterExam(), GenerationMode.EXAM, out);

        try (PDDocument document = Loader.loadPDF(out.toFile())) {
            assertEquals(3, document.getNumberOfPages());
        }

        String text = readPdfText(out);
        assertTrue(text.contains("Deckblatt"));
        assertTrue(text.contains("Chapter 1: First Chapter"));
        assertTrue(text.contains("Chapter 2: Second Chapter"));
    }

    @Test
    void shouldIncreaseAnswerBoxHeightForLongerAnswers() throws Exception {
        float shortBox = PdfBoxGenerationService.estimateAnswerBoxHeight("Short answer.", 1.0);
        float longBox = PdfBoxGenerationService.estimateAnswerBoxHeight(
            "This is a much longer answer that should require a taller handwritten box because it wraps across multiple lines.",
            1.0
        );

        assertTrue(longBox > shortBox);
    }

    @Test
    void shouldRejectNullExam() {
        PdfGenerationService service = new PdfBoxGenerationService();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.generate(null, GenerationMode.EXAM, tempDir.resolve("null.pdf"))
        );

        assertEquals("Exam must not be null.", exception.getMessage());
    }

    @Test
    void shouldGenerateCoverOnlyPdfForEmptyExam() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("empty.pdf");

        service.generate(new Exam("Empty", List.of()), GenerationMode.EXAM, out);

        try (PDDocument document = Loader.loadPDF(out.toFile())) {
            assertEquals(1, document.getNumberOfPages());
        }

        String text = readPdfText(out);
        assertTrue(text.contains("Exam: Empty"));
        assertTrue(!text.contains("Chapter:"));
    }

    @Test
    void shouldWrapLongUnbrokenWordsInSolutionPdf() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("long-word.pdf");
        String longToken = "SupercalifragilisticexpialidociousSupercalifragilisticexpialidocious";

        Exam exam = new Exam(
            "Long Text",
            List.of(
                new Chapter(
                    "Long Chapter",
                    List.of(
                        new Task(
                            "Long Task",
                            2.0,
                            Difficulty.MEDIUM,
                            Scope.EXAM,
                            List.of(new Variant(longToken, longToken))
                        )
                    )
                )
            )
        );

        service.generate(exam, GenerationMode.SOLUTION, out);

        assertTrue(Files.exists(out));
        assertTrue(Files.size(out) > 0);
        assertTrue(readPdfText(out).contains("Long Task"));
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

    private Exam twoChapterExam() {
        Task firstTask = new Task(
            "First Task",
            2.0,
            Difficulty.EASY,
            Scope.EXAM,
            List.of(new Variant("First question", "First answer"))
        );

        Task secondTask = new Task(
            "Second Task",
            3.0,
            Difficulty.HARD,
            Scope.EXAM,
            List.of(new Variant("Second question", "Second answer"))
        );

        Chapter firstChapter = new Chapter("First Chapter", List.of(firstTask));
        Chapter secondChapter = new Chapter("Second Chapter", List.of(secondTask));
        return new Exam("Two Chapters", List.of(firstChapter, secondChapter));
    }
}
