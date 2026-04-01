package com.jexam.app;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;

/**
 * Encapsulates read-only navigation over the currently active exam hierarchy.
 */
class JExamSelectionModel {
    private Exam exam;

    void setExam(Exam exam) {
        this.exam = exam;
    }

    Chapter chapterAt(int chapterIndex) {
        if (exam == null || chapterIndex < 0 || chapterIndex >= chapterCount()) {
            return null;
        }
        return exam.chapterAt(chapterIndex);
    }

    Task taskAt(int chapterIndex, int taskIndex) {
        Chapter chapter = chapterAt(chapterIndex);
        if (chapter == null || taskIndex < 0 || taskIndex >= chapter.taskCount()) {
            return null;
        }
        return chapter.taskAt(taskIndex);
    }

    Variant variantAt(int chapterIndex, int taskIndex, int variantIndex) {
        Task task = taskAt(chapterIndex, taskIndex);
        if (task == null || variantIndex < 0 || variantIndex >= task.variantCount()) {
            return null;
        }
        return task.variantAt(variantIndex);
    }

    int chapterCount() {
        if (exam == null) {
            return 0;
        }
        return exam.chapterCount();
    }

    int taskCount(int chapterIndex) {
        Chapter chapter = chapterAt(chapterIndex);
        if (chapter == null) {
            return 0;
        }
        return chapter.taskCount();
    }

    int variantCount(int chapterIndex, int taskIndex) {
        Task task = taskAt(chapterIndex, taskIndex);
        if (task == null) {
            return 0;
        }
        return task.variantCount();
    }

    int lastChapterIndex() {
        return chapterCount() - 1;
    }

    int lastTaskIndex(int chapterIndex) {
        return taskCount(chapterIndex) - 1;
    }

    int lastVariantIndex(int chapterIndex, int taskIndex) {
        return variantCount(chapterIndex, taskIndex) - 1;
    }

    String currentExamName() {
        if (exam == null) {
            return "";
        }
        return exam.getName();
    }

    String taskLabel(Task task) {
        return task.getName() + " (" + task.getPoints() + " pts, " + task.getDifficulty().toXmlValue() + ", "
            + task.getScope().toXmlValue() + ")";
    }
}