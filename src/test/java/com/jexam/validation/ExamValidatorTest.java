package com.jexam.validation;

import com.jexam.TestFixtures;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExamValidatorTest {
    private final ExamValidator validator = new ExamValidator();

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
}
