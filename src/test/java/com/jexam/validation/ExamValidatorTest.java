package com.jexam.validation;

import com.jexam.TestFixtures;
import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamValidatorTest {
    private final ExamValidator validator = new ExamValidator();

    @Test
    void nullExamShouldFail() {
        ValidationResult result = validator.validate(null);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("exam", result.getErrors().get(0).getPath());
    }

    @Test
    void validExamShouldPass() {
        ValidationResult result = validator.validate(TestFixtures.validExam());
        assertTrue(result.isValid());
    }

    @Test
    void taskWithoutVariantShouldFail() {
        Exam exam = TestFixtures.validExam();
        Task broken = new Task("Broken", 1.0, Difficulty.EASY, Scope.EXAM, List.of());
        exam.getChapters().get(0).addTask(broken);

        ValidationResult result = validator.validate(exam);
        assertFalse(result.isValid());
    }

    @Test
    void blankVariantQuestionShouldFail() {
        Exam exam = TestFixtures.validExam();
        Task task = exam.getChapters().get(0).getTasks().get(0);
        task.addVariant(new Variant(" ", "answer"));

        ValidationResult result = validator.validate(exam);
        assertFalse(result.isValid());
    }

    @Test
    void taskPointsMustUseHalfStepIncrements() {
        Exam exam = TestFixtures.validExam();
        Task task = exam.getChapters().get(0).getTasks().get(0);
        task.setPoints(1.3);

        ValidationResult result = validator.validate(exam);
        assertFalse(result.isValid());
    }

    @Test
    void nullAndBlankFieldsShouldBeReported() {
        Exam exam = new Exam(
            " ",
            new ArrayList<>(List.of(
                new Chapter(
                    null,
                    new ArrayList<>(List.of(
                        new Task(
                            null,
                            0.0,
                            null,
                            null,
                            new ArrayList<>()
                        )
                    ))
                )
            ))
        );

        exam.getChapters().get(0).getTasks().get(0).addVariant(null);
        exam.getChapters().get(0).getTasks().get(0).addVariant(new Variant(" ", "answer"));

        ValidationResult result = validator.validate(exam);

        assertFalse(result.isValid());
        assertEquals(8, result.getErrors().size());
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.name".equals(error.getPath())));
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[0].name".equals(error.getPath())));
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[0].tasks[0].name".equals(error.getPath())));
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[0].tasks[0].points".equals(error.getPath())));
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[0].tasks[0].difficulty".equals(error.getPath())));
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[0].tasks[0].scope".equals(error.getPath())));
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[0].tasks[0].variants[0]".equals(error.getPath())));
    }

    @Test
    void nullChapterAndNullTaskShouldBeReported() {
        List<Task> tasks = new ArrayList<>();
        tasks.add(null);
        List<Chapter> chapters = new ArrayList<>();
        chapters.add(null);
        chapters.add(new Chapter("Second", tasks));

        Exam exam = new Exam(
            "Demo",
            chapters
        );

        ValidationResult result = validator.validate(exam);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[0]".equals(error.getPath())));
        assertTrue(result.getErrors().stream().anyMatch(error -> "exam.chapters[1].tasks[0]".equals(error.getPath())));
    }
}
