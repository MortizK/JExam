package com.jexam.model;

import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamStructureTest {
    @Test
    void examShouldManageChaptersTasksAndVariants() {
        Variant variant = new Variant("Q1", "A1");
        Task task = new Task("Task 1", 2.5, Difficulty.EASY, Scope.EXAM, new ArrayList<>(List.of(variant)));
        Chapter chapter = new Chapter("Chapter 1", new ArrayList<>(List.of(task)));
        Exam exam = new Exam("Exam A", new ArrayList<>(List.of(chapter)));

        assertEquals("Exam A", exam.getName());
        exam.setName("Exam B");
        assertEquals("Exam B", exam.getName());

        assertEquals(1, exam.chapterCount());
        assertEquals(chapter, exam.chapterAt(0));
        assertEquals(task, exam.taskAt(0, 0));
        assertEquals(variant, exam.variantAt(0, 0, 0));

        Chapter newChapter = new Chapter("Chapter 2", new ArrayList<>());
        exam.addChapter(newChapter);
        assertEquals(2, exam.chapterCount());
        exam.removeChapter(1);
        assertEquals(1, exam.chapterCount());

        assertThrows(UnsupportedOperationException.class, () -> exam.getChapters().add(newChapter));
    }

    @Test
    void chapterShouldManageTasksAndExposeImmutableView() {
        Task firstTask = new Task("Task 1", 1.0, Difficulty.MEDIUM, Scope.MOCK_EXAM, new ArrayList<>(
            List.of(new Variant("Q", "A"))
        ));
        Chapter chapter = new Chapter("Chapter", new ArrayList<>(List.of(firstTask)));

        assertEquals("Chapter", chapter.getName());
        chapter.setName("Chapter Updated");
        assertEquals("Chapter Updated", chapter.getName());
        assertEquals(1, chapter.taskCount());
        assertEquals(firstTask, chapter.taskAt(0));
        assertEquals(firstTask.variantAt(0), chapter.variantAt(0, 0));

        Task secondTask = new Task("Task 2", 3.0, Difficulty.HARD, Scope.EXAM, new ArrayList<>(
            List.of(new Variant("Q2", "A2"))
        ));
        chapter.addTask(secondTask);
        assertEquals(2, chapter.taskCount());
        chapter.removeTask(1);
        assertEquals(1, chapter.taskCount());

        assertThrows(UnsupportedOperationException.class, () -> chapter.getTasks().add(secondTask));
    }

    @Test
    void taskAndVariantShouldSupportMutationAndEquality() {
        Variant firstVariant = new Variant("Question", "Answer");
        Task task = new Task(
            "Task",
            4.0,
            Difficulty.EASY,
            Scope.EXAM,
            new ArrayList<>(List.of(firstVariant))
        );

        assertEquals("Task", task.getName());
        task.setName("Task Updated");
        task.setPoints(5.0);
        task.setDifficulty(Difficulty.HARD);
        task.setScope(Scope.MOCK_EXAM);
        assertEquals("Task Updated", task.getName());
        assertEquals(5.0, task.getPoints());
        assertEquals(Difficulty.HARD, task.getDifficulty());
        assertEquals(Scope.MOCK_EXAM, task.getScope());

        Variant secondVariant = new Variant("Q2", "A2");
        task.addVariant(secondVariant);
        assertEquals(2, task.variantCount());
        assertEquals(secondVariant, task.variantAt(1));
        task.removeVariant(1);
        assertEquals(1, task.variantCount());
        assertThrows(UnsupportedOperationException.class, () -> task.getVariants().add(secondVariant));

        Variant sameVariant = new Variant("Question", "Answer");
        assertEquals(firstVariant, sameVariant);
        assertEquals(firstVariant.hashCode(), sameVariant.hashCode());

        firstVariant.setQuestion("Question Updated");
        firstVariant.setAnswer("Answer Updated");
        assertEquals("Question Updated", firstVariant.getQuestion());
        assertEquals("Answer Updated", firstVariant.getAnswer());
    }

    @Test
    void modelEqualityShouldConsiderNestedState() {
        Exam left = sampleExam("Exam", "Chapter", "Task", "Q", "A");
        Exam right = sampleExam("Exam", "Chapter", "Task", "Q", "A");
        Exam different = sampleExam("Exam X", "Chapter", "Task", "Q", "A");

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertTrue(!left.equals(different));
        assertTrue(!left.equals(null));
        assertTrue(!left.equals("not-an-exam"));

        Chapter leftChapter = left.chapterAt(0);
        Chapter rightChapter = right.chapterAt(0);
        assertEquals(leftChapter, rightChapter);
        assertEquals(leftChapter.hashCode(), rightChapter.hashCode());

        Task leftTask = left.taskAt(0, 0);
        Task rightTask = right.taskAt(0, 0);
        assertEquals(leftTask, rightTask);
        assertEquals(leftTask.hashCode(), rightTask.hashCode());
    }

    private Exam sampleExam(
        String examName,
        String chapterName,
        String taskName,
        String question,
        String answer
    ) {
        Variant variant = new Variant(question, answer);
        Task task = new Task(taskName, 1.0, Difficulty.EASY, Scope.EXAM, new ArrayList<>(List.of(variant)));
        Chapter chapter = new Chapter(chapterName, new ArrayList<>(List.of(task)));
        return new Exam(examName, new ArrayList<>(List.of(chapter)));
    }
}
