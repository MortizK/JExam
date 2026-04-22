package com.jexam.validation;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;

/**
 * Validates exam model instances and collects all detected violations.
 *
 * @author Moritz
 */
public final class ExamValidator {
    /**
     * Creates a validator instance.
     */
    public ExamValidator() {
    }

    /**
     * Validates the provided exam and returns all errors.
     *
     * @param exam exam to validate
     * @return validation result containing zero or more errors
     */
    public ValidationResult validate(final Exam exam) {
        final ValidationResult result = new ValidationResult();
        if (exam == null) {
            result.addError("exam", "Exam must not be null.");
            return result;
        }

        if (isBlank(exam.getName())) {
            result.addError("exam.name", "Exam name must not be blank.");
        }

        final int chapterCount = exam.getChapters().size();
        for (int chapterIndex = 0; chapterIndex < chapterCount; chapterIndex++) {
            final Chapter chapter = exam.getChapters().get(chapterIndex);
            final String chapterPath =
                "exam.chapters[" + chapterIndex + "]";
            validateChapter(chapter, chapterPath, result);
        }

        return result;
    }

    private void validateChapter(
        final Chapter chapter,
        final String path,
        final ValidationResult result
    ) {
        if (chapter == null) {
            result.addError(path, "Chapter must not be null.");
            return;
        }

        if (isBlank(chapter.getName())) {
            result.addError(path + ".name", "Chapter name must not be blank.");
        }

        final int taskCount = chapter.getTasks().size();
        for (int taskIndex = 0; taskIndex < taskCount; taskIndex++) {
            final Task task = chapter.getTasks().get(taskIndex);
            final String taskPath =
                path + ".tasks[" + taskIndex + "]";
            validateTask(task, taskPath, result);
        }
    }

    private void validateTask(
        final Task task,
        final String path,
        final ValidationResult result
    ) {
        if (task == null) {
            result.addError(path, "Task must not be null.");
            return;
        }

        if (isBlank(task.getName())) {
            result.addError(path + ".name", "Task name must not be blank.");
        }

        if (task.getPoints() <= 0.0) {
            result.addError(
                path + ".points",
                "Task points must be greater than 0."
            );
        } else if (!isHalfStep(task.getPoints())) {
            result.addError(
                path + ".points",
                "Task points must be in 0.5 increments."
            );
        }

        if (task.getDifficulty() == null) {
            result.addError(path + ".difficulty", "Task difficulty must be set.");
        }

        if (task.getScope() == null) {
            result.addError(path + ".scope", "Task scope must be set.");
        }

        if (task.getVariants().isEmpty()) {
            result.addError(
                path + ".variants",
                "Task must contain at least one variant."
            );
        }

        final int variantCount = task.getVariants().size();
        for (
            int variantIndex = 0;
            variantIndex < variantCount;
            variantIndex++
        ) {
            final Variant variant = task.getVariants().get(variantIndex);
            final String variantPath =
                path + ".variants[" + variantIndex + "]";
            validateVariant(variant, variantPath, result);
        }
    }

    private void validateVariant(
        final Variant variant,
        final String path,
        final ValidationResult result
    ) {
        if (variant == null) {
            result.addError(path, "Variant must not be null.");
            return;
        }

        if (isBlank(variant.getQuestion())) {
            result.addError(
                path + ".question",
                "Variant question must not be blank."
            );
        }
    }

    private boolean isBlank(final String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isHalfStep(final double points) {
        final double scaled = points * 2.0;
        return Math.abs(scaled - Math.rint(scaled)) < 1e-9;
    }
}
