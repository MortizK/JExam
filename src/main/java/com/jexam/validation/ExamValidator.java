package com.jexam.validation;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;

public class ExamValidator {
    public ValidationResult validate(Exam exam) {
        ValidationResult result = new ValidationResult();
        if (exam == null) {
            result.addError("exam", "Exam must not be null.");
            return result;
        }

        if (isBlank(exam.getName())) {
            result.addError("exam.name", "Exam name must not be blank.");
        }

        for (int chapterIndex = 0; chapterIndex < exam.getChapters().size(); chapterIndex++) {
            Chapter chapter = exam.getChapters().get(chapterIndex);
            String chapterPath = "exam.chapters[" + chapterIndex + "]";
            validateChapter(chapter, chapterPath, result);
        }

        return result;
    }

    private void validateChapter(Chapter chapter, String path, ValidationResult result) {
        if (chapter == null) {
            result.addError(path, "Chapter must not be null.");
            return;
        }

        if (isBlank(chapter.getName())) {
            result.addError(path + ".name", "Chapter name must not be blank.");
        }

        for (int taskIndex = 0; taskIndex < chapter.getTasks().size(); taskIndex++) {
            Task task = chapter.getTasks().get(taskIndex);
            String taskPath = path + ".tasks[" + taskIndex + "]";
            validateTask(task, taskPath, result);
        }
    }

    private void validateTask(Task task, String path, ValidationResult result) {
        if (task == null) {
            result.addError(path, "Task must not be null.");
            return;
        }

        if (isBlank(task.getName())) {
            result.addError(path + ".name", "Task name must not be blank.");
        }

        if (task.getPoints() <= 0.0) {
            result.addError(path + ".points", "Task points must be greater than 0.");
        }

        if (task.getDifficulty() == null) {
            result.addError(path + ".difficulty", "Task difficulty must be set.");
        }

        if (task.getScope() == null) {
            result.addError(path + ".scope", "Task scope must be set.");
        }

        if (task.getVariants().isEmpty()) {
            result.addError(path + ".variants", "Task must contain at least one variant.");
        }

        for (int variantIndex = 0; variantIndex < task.getVariants().size(); variantIndex++) {
            Variant variant = task.getVariants().get(variantIndex);
            String variantPath = path + ".variants[" + variantIndex + "]";
            validateVariant(variant, variantPath, result);
        }
    }

    private void validateVariant(Variant variant, String path, ValidationResult result) {
        if (variant == null) {
            result.addError(path, "Variant must not be null.");
            return;
        }

        if (isBlank(variant.getQuestion())) {
            result.addError(path + ".question", "Variant question must not be blank.");
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }
}
