package com.jexam;

import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;

import java.util.List;

public final class TestFixtures {
    private TestFixtures() {
    }

    public static Exam validExam() {
        Variant variant = new Variant("What is 2+2?", "4");
        Task task = new Task("Simple math", 2.0, Difficulty.EASY, Scope.EXAM, List.of(variant));
        Chapter chapter = new Chapter("Basics", List.of(task));
        return new Exam("Demo Exam", List.of(chapter));
    }
}
