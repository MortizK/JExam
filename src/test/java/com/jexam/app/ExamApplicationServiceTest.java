package com.jexam.app;

import com.jexam.generation.GenerationMode;
import com.jexam.model.Task;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExamApplicationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void newExamShouldUseDocumentedDefaultTaskValues() {
        ExamApplicationService service = new ExamApplicationService();

        Task task = service.getCurrentExam().taskAt(0, 0);
        assertEquals("New Subtask", task.getName());
        assertEquals("easy", task.getDifficulty().toXmlValue());
    }

    @Test
    void removeVariantShouldRejectDeletingLastVariant() {
        ExamApplicationService service = new ExamApplicationService();

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.removeVariant(0, 0, 0)
        );

        assertEquals(
            "A task must contain at least one variant.",
            exception.getMessage()
        );
    }

    @Test
    void generatePdfShouldWarnWhenDifficultyIsNotBalanced() {
        ExamApplicationService service = new ExamApplicationService();
        Path output = tempDir.resolve("difficulty-warning.pdf");

        assertDoesNotThrow(() -> service.generatePdf(GenerationMode.EXAM, output));

        assertTrue(Files.exists(output));
        assertTrue(service.getLastGenerationWarnings().stream().anyMatch(w -> w.contains("roughly balanced by difficulty")));
    }

    @Test
    void generatePdfShouldAllowBalancedDifficultyThirds() {
        ExamApplicationService service = new ExamApplicationService();

        service.addTask(0, "Medium Task");
        service.updateTaskDetails(0, 1, "Medium Task", 1.0, Difficulty.MEDIUM, Scope.EXAM);
        service.addTask(0, "Hard Task");
        service.updateTaskDetails(0, 2, "Hard Task", 1.0, Difficulty.HARD, Scope.EXAM);
        service.setGenerationChapterGoalPoints(0, 3.0);

        assertDoesNotThrow(
            () -> service.generatePdf(GenerationMode.EXAM, tempDir.resolve("valid-exam.pdf"))
        );
    }

    @Test
    void generatePdfShouldResolveInfeasibleGoalToNearestLowerWhenConfigured() {
        ExamApplicationService service = new ExamApplicationService();
        service.updateTaskDetails(0, 0, "A", 1.0, Difficulty.EASY, Scope.EXAM);
        service.addTask(0, "B");
        service.updateTaskDetails(0, 1, "B", 2.0, Difficulty.MEDIUM, Scope.EXAM);

        service.setGoalPointFallbackPreference(ExamApplicationService.GoalPointFallbackPreference.LOWER);
        service.setGenerationChapterGoalPoints(0, 2.5);

        Path output = tempDir.resolve("nearest-lower.pdf");
        service.generatePdf(GenerationMode.EXAM, output);

        String text = assertDoesNotThrow(() -> readPdfText(output));
        assertTrue(text.contains("Tasks: 1 | Points: 2.0"));
        assertTrue(text.contains("Task 1: B"));
    }

    @Test
    void generatePdfShouldResolveInfeasibleGoalToNearestHigherWhenConfigured() throws Exception {
        ExamApplicationService service = new ExamApplicationService();
        service.updateTaskDetails(0, 0, "A", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);
        service.addTask(0, "B");
        service.updateTaskDetails(0, 1, "B", 2.0, Difficulty.MEDIUM, Scope.MOCK_EXAM);

        service.setGoalPointFallbackPreference(ExamApplicationService.GoalPointFallbackPreference.HIGHER);
        service.setGenerationChapterGoalPoints(0, 2.5);

        Path output = tempDir.resolve("nearest-higher.pdf");
        service.generatePdf(GenerationMode.MOCK_EXAM, output);

        String text = readPdfText(output);
        assertTrue(text.contains("Tasks: 2 | Points: 3.0"));
        assertTrue(text.contains("Task 1: A") || text.contains("Task 2: A"));
        assertTrue(text.contains("Task 1: B") || text.contains("Task 2: B"));
    }

    @Test
    void generatePdfPairShouldCreatePrimaryAndSolutionFiles() {
        ExamApplicationService service = new ExamApplicationService();
        service.addTask(0, "Medium Task");
        service.updateTaskDetails(0, 1, "Medium Task", 1.0, Difficulty.MEDIUM, Scope.EXAM);
        service.addTask(0, "Hard Task");
        service.updateTaskDetails(0, 2, "Hard Task", 1.0, Difficulty.HARD, Scope.EXAM);
        service.setGenerationChapterGoalPoints(0, 3.0);

        Path output = tempDir.resolve("paired.pdf");
        List<Path> generated = service.generatePdfPair(GenerationMode.EXAM, output);

        assertEquals(2, generated.size());
        assertTrue(Files.exists(generated.get(0)));
        assertTrue(Files.exists(generated.get(1)));
        assertTrue(generated.get(1).getFileName().toString().endsWith("_solutions.pdf"));
    }

    @Test
    void generatePdfInMockModeShouldIncludeAllTasksWithRandomVariants() throws Exception {
        ExamApplicationService service = new ExamApplicationService();
        service.updateTaskDetails(0, 0, "Exam Scoped", 1.0, Difficulty.EASY, Scope.EXAM);
        service.updateVariantDetails(0, 0, 0, "Exam Q1", "Exam A1");
        service.addVariant(0, 0);
        service.updateVariantDetails(0, 0, 1, "Exam Q2", "Exam A2");

        service.addTask(0, "Mock Scoped");
        service.updateTaskDetails(0, 1, "Mock Scoped", 1.0, Difficulty.MEDIUM, Scope.MOCK_EXAM);
        service.updateVariantDetails(0, 1, 0, "Mock Q1", "Mock A1");
        service.addVariant(0, 1);
        service.updateVariantDetails(0, 1, 1, "Mock Q2", "Mock A2");

        Path output = tempDir.resolve("mock-all-tasks.pdf");
        service.generatePdf(GenerationMode.MOCK_EXAM, output);

        String text = readPdfText(output);
        assertTrue(text.contains("Exam Scoped"));
        assertTrue(text.contains("Mock Scoped"));
        assertFalse(text.contains("Variant 2"));
    }

    @Test
    void generatePdfShouldUseDeterministicSeedForVariantSelection() throws Exception {
        ExamApplicationService serviceA = new ExamApplicationService();
        serviceA.updateTaskDetails(0, 0, "Seeded Task", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);
        serviceA.updateVariantDetails(0, 0, 0, "Q1", "A1");
        serviceA.addVariant(0, 0);
        serviceA.updateVariantDetails(0, 0, 1, "Q2", "A2");
        serviceA.setGenerationRandomSeed(7L);

        ExamApplicationService serviceB = new ExamApplicationService();
        serviceB.updateTaskDetails(0, 0, "Seeded Task", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);
        serviceB.updateVariantDetails(0, 0, 0, "Q1", "A1");
        serviceB.addVariant(0, 0);
        serviceB.updateVariantDetails(0, 0, 1, "Q2", "A2");
        serviceB.setGenerationRandomSeed(7L);

        Path outA = tempDir.resolve("seed-a.pdf");
        Path outB = tempDir.resolve("seed-b.pdf");
        serviceA.generatePdf(GenerationMode.MOCK_EXAM, outA);
        serviceB.generatePdf(GenerationMode.MOCK_EXAM, outB);

        assertEquals(readPdfText(outA), readPdfText(outB));
    }

    @Test
    void generatePdfShouldRespectConfiguredChapterOrderAndExclusion()
        throws Exception {
        ExamApplicationService service = new ExamApplicationService();
        service.getCurrentExam().chapterAt(0).setName("Alpha");
        service.addChapter("Beta");
        service.addChapter("Gamma");

        service.updateTaskDetails(0, 0, "Alpha Task", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);
        service.updateTaskDetails(1, 0, "Beta Task", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);
        service.updateTaskDetails(2, 0, "Gamma Task", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);

        service.excludeGenerationChapter(1);
        service.moveGenerationChapterDown(0);

        Path output = tempDir.resolve("ordered-mock.pdf");
        service.generatePdf(GenerationMode.MOCK_EXAM, output);

        String text = readPdfText(output);
        int gammaIndex = text.indexOf("Chapter: Gamma");
        int alphaIndex = text.indexOf("Chapter: Alpha");

        assertTrue(gammaIndex >= 0);
        assertTrue(alphaIndex >= 0);
        assertTrue(gammaIndex < alphaIndex);
        assertFalse(text.contains("Chapter: Beta"));
    }

    @Test
    void generatePdfShouldRejectWhenNoGenerationChapterIsSelected() {
        ExamApplicationService service = new ExamApplicationService();
        service.excludeGenerationChapter(0);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.generatePdf(GenerationMode.MOCK_EXAM, tempDir.resolve("empty-selection.pdf"))
        );

        assertEquals("No chapters selected for PDF generation.", exception.getMessage());
    }

    @Test
    void addAndRemoveChapterShouldUpdateExamStructure() {
        ExamApplicationService service = new ExamApplicationService();

        int initial = service.getCurrentExam().chapterCount();
        service.addChapter("Networks");

        assertEquals(initial + 1, service.getCurrentExam().chapterCount());
        assertEquals("Networks", service.getCurrentExam().chapterAt(initial).getName());

        service.removeChapter(initial);
        assertEquals(initial, service.getCurrentExam().chapterCount());
    }

    @Test
    void addTaskAndUpdateTaskDetailsShouldPersistChanges() {
        ExamApplicationService service = new ExamApplicationService();

        int initialTasks = service.getCurrentExam().chapterAt(0).taskCount();
        service.addTask(0, "Heap Basics");

        assertEquals(initialTasks + 1, service.getCurrentExam().chapterAt(0).taskCount());
        assertEquals("Heap Basics", service.getCurrentExam().taskAt(0, initialTasks).getName());

        service.updateTaskDetails(
            0,
            initialTasks,
            "Heap Advanced",
            2.5,
            Difficulty.HARD,
            Scope.MOCK_EXAM
        );

        Task updated = service.getCurrentExam().taskAt(0, initialTasks);
        assertEquals("Heap Advanced", updated.getName());
        assertEquals(2.5, updated.getPoints());
        assertEquals(Difficulty.HARD, updated.getDifficulty());
        assertEquals(Scope.MOCK_EXAM, updated.getScope());
    }

    @Test
    void removeTaskShouldDeleteRequestedTask() {
        ExamApplicationService service = new ExamApplicationService();
        service.addTask(0, "Task A");
        service.addTask(0, "Task B");

        int countBefore = service.getCurrentExam().chapterAt(0).taskCount();
        service.removeTask(0, countBefore - 1);

        assertEquals(countBefore - 1, service.getCurrentExam().chapterAt(0).taskCount());
    }

    @Test
    void generatePreviewPdfShouldCreateReadableTemporaryPdf() throws Exception {
        ExamApplicationService service = new ExamApplicationService();
        service.updateTaskDetails(0, 0, "Preview Task", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);

        Path previewPath = service.generatePreviewPdf(GenerationMode.MOCK_EXAM);

        assertNotNull(previewPath);
        assertTrue(Files.exists(previewPath));
        assertTrue(Files.size(previewPath) > 0);
    }

    private String readPdfText(Path path) throws Exception {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }
}
