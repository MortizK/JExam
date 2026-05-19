package com.jexam.generation;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import com.jexam.app.UiLanguage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        // MOCK_EXAM mode should only include tasks with scope="mock-exam"
        assertTrue(readPdfText(out).contains("Scope MOCK task"));
        assertFalse(readPdfText(out).contains("Scope EXAM task"));
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
        if (service instanceof PdfBoxGenerationService) {
            ((PdfBoxGenerationService) service).setUiLanguage(UiLanguage.GERMAN);
        }
        Path out = tempDir.resolve("chapter-breaks.pdf");

        service.generate(twoChapterExam(), GenerationMode.EXAM, out);

        try (PDDocument document = Loader.loadPDF(out.toFile())) {
            assertEquals(3, document.getNumberOfPages());
        }

        String text = readPdfText(out);
        assertTrue(text.contains("Aufgabe 1: First Chapter (2.0 Punkte)"));
        assertTrue(text.contains("Aufgabe 2: Second Chapter (3.0 Punkte)"));
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
        if (service instanceof PdfBoxGenerationService) {
            ((PdfBoxGenerationService) service).setUiLanguage(UiLanguage.GERMAN);
        }
        Path out = tempDir.resolve("empty.pdf");

        service.generate(new Exam("Empty", List.of()), GenerationMode.EXAM, out);

        try (PDDocument document = Loader.loadPDF(out.toFile())) {
            assertEquals(1, document.getNumberOfPages());
        }

        String text = readPdfText(out);
        assertTrue(text.contains("Empty"));
        assertTrue(!text.contains("Aufgabe 1:"));
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

    @Test
    void shouldWritePdfMetadata() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("metadata.pdf");

        service.generate(twoChapterExam(), GenerationMode.EXAM, out);

        try (PDDocument document = Loader.loadPDF(out.toFile())) {
            assertEquals("Two Chapters", document.getDocumentInformation().getTitle());
            assertEquals("Exam PDF - EXAM", document.getDocumentInformation().getSubject());
            assertEquals("JExam", document.getDocumentInformation().getCreator());
            assertTrue(document.getDocumentInformation().getKeywords().contains("Difficulty distribution:"));
            assertTrue(document.getDocumentInformation().getKeywords().contains("easy 1"));
            assertTrue(document.getDocumentInformation().getKeywords().contains("hard 1"));
        }
    }

    @Test
    void shouldCreateOutlineWithChaptersAndTasks() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("outline.pdf");

        service.generate(twoChapterExam(), GenerationMode.EXAM, out);

        try (PDDocument document = Loader.loadPDF(out.toFile())) {
            PDDocumentOutline outline = document.getDocumentCatalog().getDocumentOutline();
            assertNotNull(outline);

            PDOutlineItem coverItem = outline.getFirstChild();
            assertNotNull(coverItem);
            assertEquals("Cover", coverItem.getTitle());

            PDOutlineItem firstChapter = coverItem.getNextSibling();
            assertNotNull(firstChapter);
            assertEquals("Aufgabe 1: First Chapter (2.0 Punkte)", firstChapter.getTitle());

            PDOutlineItem firstTask = firstChapter.getFirstChild();
            assertNotNull(firstTask);
            assertEquals("First Task (2.0 Punkte)", firstTask.getTitle());

            PDOutlineItem secondChapter = firstChapter.getNextSibling();
            assertNotNull(secondChapter);
            assertEquals("Aufgabe 2: Second Chapter (3.0 Punkte)", secondChapter.getTitle());

            PDOutlineItem secondTask = secondChapter.getFirstChild();
            assertNotNull(secondTask);
            assertEquals("Second Task (3.0 Punkte)", secondTask.getTitle());
        }
    }

    @Test
    void shouldGeneratePdfToRelativePathWithoutParentDirectory() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = Path.of("target", "tmp", "generation-no-parent.pdf").getFileName();

        try {
            service.generate(sampleExam(), GenerationMode.EXAM, out);

            assertTrue(Files.exists(out));
            assertTrue(Files.size(out) > 0);
        } finally {
            Files.deleteIfExists(out);
        }
    }

    @Test
    void shouldRenderNullTextValuesAsEmptyStrings() throws Exception {
        PdfGenerationService service = new PdfBoxGenerationService();
        Path out = tempDir.resolve("null-text-values.pdf");

        Exam exam = new Exam(
            null,
            List.of(
                new Chapter(
                    null,
                    List.of(
                        new Task(
                            null,
                            1.0,
                            Difficulty.EASY,
                            Scope.EXAM,
                            List.of(new Variant(null, null))
                        )
                    )
                )
            )
        );

        service.generate(exam, GenerationMode.SOLUTION, out);

        assertTrue(Files.exists(out));
        assertTrue(Files.size(out) > 0);
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
