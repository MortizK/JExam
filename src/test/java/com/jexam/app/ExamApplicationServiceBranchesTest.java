package com.jexam.app;

import com.jexam.generation.GenerationMode;
import com.jexam.model.DifficultyDistributionSummary;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamApplicationServiceBranchesTest {
    @TempDir
    Path tempDir;

    @Test
    void generationChapterSelectionShouldIgnoreInvalidIndicesAndDuplicates() {
        ExamApplicationService service = new ExamApplicationService();

        service.excludeGenerationChapter(0);
        service.includeGenerationChapter(-1);
        service.includeGenerationChapter(99);
        service.includeGenerationChapter(0);
        service.includeGenerationChapter(0);

        assertEquals(List.of(0), service.generationChapterOrder());
        assertEquals(1, service.generationChapterOrder().size());

        service.moveGenerationChapterUp(0);
        service.moveGenerationChapterDown(0);
        service.excludeGenerationChapter(5);
        assertEquals(List.of(0), service.generationChapterOrder());
    }

    @Test
    void goalPointConfigurationShouldValidateInputAndNormalizeValues() {
        ExamApplicationService service = new ExamApplicationService();

        service.setGenerationChapterGoalPoints(-1, 5.0);
        service.setGenerationChapterGoalPoints(99, 5.0);
        assertFalse(service.generationChapterGoalPoints().isEmpty());

        service.setGenerationChapterGoalPoints(0, 1.24);
        assertEquals(1.0, service.generationChapterGoalPoints().get(0));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.setGenerationChapterGoalPoints(0, 0.0)
        );
        assertEquals("Chapter goal points must be greater than 0.", exception.getMessage());

        ExamApplicationService.GoalPointFallbackPreference initial = service.getGoalPointFallbackPreference();
        service.setGoalPointFallbackPreference(null);
        assertEquals(initial, service.getGoalPointFallbackPreference());
        service.setGoalPointFallbackPreference(ExamApplicationService.GoalPointFallbackPreference.HIGHER);
        assertEquals(ExamApplicationService.GoalPointFallbackPreference.HIGHER, service.getGoalPointFallbackPreference());
    }

    @Test
    void generatePdfPairShouldRejectUnsupportedModesAndUsePreviewSnapshotWhenAvailable() {
        ExamApplicationService service = new ExamApplicationService();

        assertThrows(
            IllegalArgumentException.class,
            () -> service.generatePdfPair(GenerationMode.SOLUTION, tempDir.resolve("unsupported.pdf"))
        );

        service.setGenerationRandomSeed(7L);
        Path preview = service.generatePreviewPdf(GenerationMode.MOCK_EXAM);
        assertTrue(preview.toFile().exists());

        assertDoesNotThrow(() -> service.generatePdfPairFromLastPreview(
            GenerationMode.MOCK_EXAM,
            tempDir.resolve("preview-pair.pdf")
        ));

        assertDoesNotThrow(() -> service.generatePdfPairFromLastPreview(
            GenerationMode.EXAM,
            tempDir.resolve("fallback-pair.pdf")
        ));
    }

    @Test
    void mutatingExamShouldResetGenerationSelection() {
        ExamApplicationService service = new ExamApplicationService();

        service.excludeGenerationChapter(0);
        assertTrue(service.generationChapterOrder().isEmpty());

        service.newExam();
        assertEquals(1, service.generationChapterOrder().size());

        service.addChapter("Extra");
        assertEquals(2, service.generationChapterOrder().size());

        service.removeChapter(1);
        assertEquals(1, service.generationChapterOrder().size());
    }

    @Test
    void resetGenerationGoalsShouldUseAllTaskPointsWhenChapterHasNoExamScopeTasks() {
        ExamApplicationService service = new ExamApplicationService();

        service.addChapter("Mock only");
        service.updateTaskDetails(1, 0, "Mock task", 2.5, Difficulty.EASY, Scope.MOCK_EXAM);
        service.resetGenerationChapterSelection();

        assertEquals(2.5, service.generationChapterGoalPoints().get(1));
    }

    @Test
    void resetGenerationGoalsShouldPreferDifficultyBalancedDefault() {
        ExamApplicationService service = new ExamApplicationService();

        service.updateTaskDetails(0, 0, "Easy A", 1.0, Difficulty.EASY, Scope.EXAM);
        service.addTask(0, "Easy B");
        service.updateTaskDetails(0, 1, "Easy B", 1.0, Difficulty.EASY, Scope.EXAM);
        service.addTask(0, "Medium");
        service.updateTaskDetails(0, 2, "Medium", 1.0, Difficulty.MEDIUM, Scope.EXAM);
        service.addTask(0, "Hard");
        service.updateTaskDetails(0, 3, "Hard", 1.0, Difficulty.HARD, Scope.EXAM);

        service.resetGenerationChapterSelection();

        assertEquals(3.0, service.generationChapterGoalPoints().get(0));
    }

    @Test
    void mockModeGenerationShouldNotCollectDifficultyWarnings() {
        ExamApplicationService service = new ExamApplicationService();

        service.updateTaskDetails(0, 0, "Mock task", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);
        service.generatePdf(GenerationMode.MOCK_EXAM, tempDir.resolve("mock.pdf"));

        assertTrue(service.getLastGenerationWarnings().isEmpty());
    }

    @Test
    void generationDifficultySummaryShouldReflectSelectedMode() {
        ExamApplicationService service = new ExamApplicationService();

        service.updateTaskDetails(0, 0, "Exam task", 1.0, Difficulty.EASY, Scope.EXAM);
        service.addTask(0, "Mock task");
        service.updateTaskDetails(0, 1, "Mock task", 1.0, Difficulty.HARD, Scope.MOCK_EXAM);

        DifficultyDistributionSummary examSummary = service.getGenerationDifficultySummary(GenerationMode.EXAM);
        assertEquals(1, examSummary.easyCount());
        assertEquals(0, examSummary.mediumCount());
        assertEquals(0, examSummary.hardCount());

        DifficultyDistributionSummary mockSummary = service.getGenerationDifficultySummary(GenerationMode.MOCK_EXAM);
        assertEquals(1, mockSummary.easyCount());
        assertEquals(0, mockSummary.mediumCount());
        assertEquals(1, mockSummary.hardCount());
    }

    @Test
    void examModeShouldRejectChaptersWithoutExamScopeTasks() {
        ExamApplicationService service = new ExamApplicationService();
        service.updateTaskDetails(0, 0, "Mock only", 1.0, Difficulty.EASY, Scope.MOCK_EXAM);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.generatePdf(GenerationMode.EXAM, tempDir.resolve("exam-fail.pdf"))
        );

        assertEquals(
            "Chapter 'New Chapter' has no tasks available for mode EXAM.",
            exception.getMessage()
        );
    }

    @Test
    void generationShouldRejectTasksWithoutVariants() {
        ExamApplicationService service = new ExamApplicationService();
        service.getCurrentExam().taskAt(0, 0).removeVariant(0);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.generatePdf(GenerationMode.EXAM, tempDir.resolve("missing-variant.pdf"))
        );

        assertTrue(exception.getMessage().contains("has no variants."));
    }

    @Test
    void pairGenerationShouldSupportOutputWithoutPdfExtension() {
        ExamApplicationService service = new ExamApplicationService();
        Path output = tempDir.resolve("pair-output");

        List<Path> generated = service.generatePdfPair(GenerationMode.EXAM, output);

        assertEquals(2, generated.size());
        assertTrue(generated.get(0).getFileName().toString().equals("pair-output"));
        assertTrue(generated.get(1).getFileName().toString().equals("pair-output_solutions.pdf"));
        assertTrue(Files.exists(generated.get(0)));
        assertTrue(Files.exists(generated.get(1)));
    }

    @Test
    void pairGenerationShouldSupportOutputWithoutParentPath() throws Exception {
        ExamApplicationService service = new ExamApplicationService();
        Path output = Path.of("pair-no-parent.pdf");

        try {
            List<Path> generated = service.generatePdfPair(GenerationMode.EXAM, output);

            assertEquals(2, generated.size());
            assertEquals("pair-no-parent.pdf", generated.get(0).toString());
            assertEquals("pair-no-parent_solutions.pdf", generated.get(1).toString());
            assertTrue(Files.exists(generated.get(0)));
            assertTrue(Files.exists(generated.get(1)));
        } finally {
            Files.deleteIfExists(Path.of("pair-no-parent.pdf"));
            Files.deleteIfExists(Path.of("pair-no-parent_solutions.pdf"));
        }
    }

    @Test
    void lowerFallbackShouldUseHigherSumWhenNoLowerSumExists() throws Exception {
        ExamApplicationService service = new ExamApplicationService();
        service.updateTaskDetails(0, 0, "Only", 1.0, Difficulty.EASY, Scope.EXAM);
        service.addTask(0, "Second");
        service.updateTaskDetails(0, 1, "Second", 2.0, Difficulty.MEDIUM, Scope.EXAM);

        service.setGoalPointFallbackPreference(ExamApplicationService.GoalPointFallbackPreference.LOWER);
        service.setGenerationChapterGoalPoints(0, 0.5);

        Path output = tempDir.resolve("lower-fallback-no-lower.pdf");
        service.generatePdf(GenerationMode.EXAM, output);

        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }

    @Test
    void generationShouldRejectChapterWithOnlyZeroPointTasks() {
        ExamApplicationService service = new ExamApplicationService();
        service.updateTaskDetails(0, 0, "Zero", 0.0, Difficulty.EASY, Scope.EXAM);
        service.setGenerationChapterGoalPoints(0, 0.5);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.generatePdf(GenerationMode.EXAM, tempDir.resolve("zero-point.pdf"))
        );

        assertTrue(exception.getMessage().contains("has no achievable positive point total"));
    }
}
