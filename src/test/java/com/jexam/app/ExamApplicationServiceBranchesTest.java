package com.jexam.app;

import com.jexam.generation.GenerationMode;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
}
