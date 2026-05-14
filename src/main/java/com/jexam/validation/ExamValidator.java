package com.jexam.validation;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;

/**
 * Validates exam model instances and collects all detected violations.
 * 
 * This class performs comprehensive bottom-up validation of the entire exam hierarchy:
 * Exam -> Chapters -> Tasks -> Variants. Validation errors are collected into a single
 * {@link ValidationResult} with descriptive error paths (e.g., "exam.chapters[0].tasks[1].points")
 * for easy UI error highlighting and reporting.
 * 
 * Validation Rules:
 * - Exam: Must have non-blank name
 * - Chapter: Must have non-blank name
 * - Task: Must have non-blank name, points > 0 (in 0.5 increments), difficulty set, scope set, >= 1 variant
 * - Variant: Must have non-blank question
 * 
 * The validator is stateless and thread-safe.
 * 
 * @author Moritz
 */
public final class ExamValidator {
    /**
     * Creates a new validator instance.
     * 
     * Validator instances are lightweight and stateless; safe to reuse or discard.
     */
    public ExamValidator() {
    }

    /**
     * Validates the provided exam and returns all errors.
     * 
     * Performs a complete traversal of the exam tree structure and collects all validation
     * errors. If the exam is null, returns a result with a single error. If no errors are
     * detected, returns an empty (but valid) result.
     * 
     * Validation is performed in order: exam name -> chapters -> chapter name -> tasks ->
     * task properties -> variants -> variant properties. This ensures errors are reported
     * in a predictable, traversal order.
     * 
     * @param exam exam to validate (null-safe)
     * @return validation result containing zero or more errors; never null
     */
    public ValidationResult validate(final Exam exam) {
        final ValidationResult result = new ValidationResult();
        // Null check: early return if exam is missing
        if (exam == null) {
            result.addError("exam", "Exam must not be null.");
            return result;
        }

        // Validate exam-level properties
        if (isBlank(exam.getName())) {
            result.addError("exam.name", "Exam name must not be blank.");
        }

        // Validate each chapter and its contents
        final int chapterCount = exam.getChapters().size();
        for (int chapterIndex = 0; chapterIndex < chapterCount; chapterIndex++) {
            final Chapter chapter = exam.getChapters().get(chapterIndex);
            // Build path like "exam.chapters[0]" for error reporting
            final String chapterPath =
                "exam.chapters[" + chapterIndex + "]";
            validateChapter(chapter, chapterPath, result);
        }

        return result;
    }

    /**
     * Validates a chapter and all its tasks.
     * 
     * @param chapter chapter to validate (null-safe)
     * @param path error path prefix for this chapter (e.g., "exam.chapters[0]")
     * @param result accumulator for validation errors
     */
    private void validateChapter(
        final Chapter chapter,
        final String path,
        final ValidationResult result
    ) {
        // Null check: report error and return to prevent NPE
        if (chapter == null) {
            result.addError(path, "Chapter must not be null.");
            return;
        }

        // Validate chapter-level properties
        if (isBlank(chapter.getName())) {
            result.addError(path + ".name", "Chapter name must not be blank.");
        }

        // Validate each task in the chapter
        final int taskCount = chapter.getTasks().size();
        for (int taskIndex = 0; taskIndex < taskCount; taskIndex++) {
            final Task task = chapter.getTasks().get(taskIndex);
            // Build path like "exam.chapters[0].tasks[1]" for error reporting
            final String taskPath =
                path + ".tasks[" + taskIndex + "]";
            validateTask(task, taskPath, result);
        }
    }

    /**
     * Validates a task and all its variants.
     * 
     * Checks: name (non-blank), points (>0 and in 0.5 increments), difficulty (non-null),
     * scope (non-null), and at least 1 variant.
     * 
     * @param task task to validate (null-safe)
     * @param path error path prefix for this task (e.g., "exam.chapters[0].tasks[1]")
     * @param result accumulator for validation errors
     */
    private void validateTask(
        final Task task,
        final String path,
        final ValidationResult result
    ) {
        // Null check: report error and return to prevent NPE
        if (task == null) {
            result.addError(path, "Task must not be null.");
            return;
        }

        // Validate task name
        if (isBlank(task.getName())) {
            result.addError(path + ".name", "Task name must not be blank.");
        }

        // Validate task points: must be positive and in 0.5 increments
        if (task.getPoints() <= 0.0) {
            result.addError(
                path + ".points",
                "Task points must be greater than 0."
            );
        } else if (!isHalfStep(task.getPoints())) {
            // Check if points follow 0.5 increments (e.g., 1.0, 1.5, 2.0, 2.5...)
            result.addError(
                path + ".points",
                "Task points must be in 0.5 increments."
            );
        }

        // Validate task difficulty is set
        if (task.getDifficulty() == null) {
            result.addError(path + ".difficulty", "Task difficulty must be set.");
        }

        // Validate task scope is set
        if (task.getScope() == null) {
            result.addError(path + ".scope", "Task scope must be set.");
        }

        // Validate task has at least one variant (PDF generation requires this)
        if (task.getVariants().isEmpty()) {
            result.addError(
                path + ".variants",
                "Task must contain at least one variant."
            );
        }

        // Validate each variant in the task
        final int variantCount = task.getVariants().size();
        for (
            int variantIndex = 0;
            variantIndex < variantCount;
            variantIndex++
        ) {
            final Variant variant = task.getVariants().get(variantIndex);
            // Build path like "exam.chapters[0].tasks[1].variants[0]" for error reporting
            final String variantPath =
                path + ".variants[" + variantIndex + "]";
            validateVariant(variant, variantPath, result);
        }
    }

    /**
     * Validates a variant (question-answer pair).
     * 
     * @param variant variant to validate (null-safe)
     * @param path error path prefix for this variant (e.g., "exam.chapters[0].tasks[1].variants[0]")
     * @param result accumulator for validation errors
     */
    private void validateVariant(
        final Variant variant,
        final String path,
        final ValidationResult result
    ) {
        // Null check: report error and return to prevent NPE
        if (variant == null) {
            result.addError(path, "Variant must not be null.");
            return;
        }

        // Validate variant question is not blank
        if (isBlank(variant.getQuestion())) {
            result.addError(
                path + ".question",
                "Variant question must not be blank."
            );
        }
    }

    /**
     * Determines if a string is blank (null, empty, or whitespace-only).
     * 
     * @param text string to check (null-safe)
     * @return true if text is null, empty, or contains only whitespace
     */
    private boolean isBlank(final String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Determines if a points value is in 0.5 increments (e.g., 1.0, 1.5, 2.0, 2.5).
     * 
     * Implementation: Multiply by 2 and check if result is close to an integer.
     * Uses epsilon comparison (1e-9) to handle floating-point rounding.
     * 
     * @param points points value to check
     * @return true if points is a valid half-step value, false otherwise
     */
    private boolean isHalfStep(final double points) {
        // Scale by 2: if result is integer (e.g., 3.0), then original was 0.5 step (e.g., 1.5)
        final double scaled = points * 2.0;
        // Compare to nearest integer using epsilon tolerance for floating-point safety
        return Math.abs(scaled - Math.rint(scaled)) < 1e-9;
    }
}
