package com.jexam.app;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class JExamSelectionModelTest {
    @Test
    void shouldReturnSafeDefaultsWhenExamIsMissing() {
        JExamSelectionModel model = new JExamSelectionModel();

        assertNull(model.chapterAt(0));
        assertNull(model.taskAt(0, 0));
        assertNull(model.variantAt(0, 0, 0));
        assertEquals(0, model.chapterCount());
        assertEquals(0, model.taskCount(0));
        assertEquals(0, model.variantCount(0, 0));
        assertEquals(-1, model.lastChapterIndex());
        assertEquals(-1, model.lastTaskIndex(0));
        assertEquals(-1, model.lastVariantIndex(0, 0));
        assertEquals("", model.currentExamName());
    }

    @Test
    void shouldNavigateThroughValidHierarchy() {
        JExamSelectionModel model = new JExamSelectionModel();
        model.setExam(sampleExam());

        assertEquals(1, model.chapterCount());
        assertEquals(1, model.taskCount(0));
        assertEquals(1, model.variantCount(0, 0));
        assertEquals(0, model.lastChapterIndex());
        assertEquals(0, model.lastTaskIndex(0));
        assertEquals(0, model.lastVariantIndex(0, 0));
        assertEquals("Algorithms", model.currentExamName());

        assertNotNull(model.chapterAt(0));
        Task selectedTask = model.taskAt(0, 0);
        assertNotNull(selectedTask);
        assertNotNull(model.variantAt(0, 0, 0));

        assertEquals(
            "Graph Basics (4.0 pts, medium, exam)",
            model.taskLabel(selectedTask)
        );
    }

    @Test
    void shouldReturnNullOrZeroForOutOfRangeIndices() {
        JExamSelectionModel model = new JExamSelectionModel();
        model.setExam(sampleExam());

        assertNull(model.chapterAt(-1));
        assertNull(model.chapterAt(2));
        assertNull(model.taskAt(0, -1));
        assertNull(model.taskAt(0, 2));
        assertNull(model.variantAt(0, 0, -1));
        assertNull(model.variantAt(0, 0, 2));
        assertEquals(0, model.taskCount(5));
        assertEquals(0, model.variantCount(5, 0));
    }

    private Exam sampleExam() {
        Variant variant = new Variant("What is BFS?", "Breadth-first search");
        Task task = new Task(
            "Graph Basics",
            4.0,
            Difficulty.MEDIUM,
            Scope.EXAM,
            List.of(variant)
        );
        Chapter chapter = new Chapter("Graph Theory", List.of(task));
        return new Exam("Algorithms", List.of(chapter));
    }
}
