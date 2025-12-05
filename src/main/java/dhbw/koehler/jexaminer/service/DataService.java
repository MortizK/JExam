package dhbw.koehler.jexaminer.service;

import dhbw.koehler.jexaminer.model.*;
import dhbw.koehler.jexaminer.model.enums.Difficulty;
import dhbw.koehler.jexaminer.model.enums.Scope;

public class DataService {
    public Exam exam;

    public DataService(String name) {
        this.exam = new Exam((name == null) ? "Exam" : name);
        createExampleData();
    }

    public void createExampleData() {
        Variant variant1 = new Variant("question1", "answer1");
        Variant variant2 = new Variant("question2", "answer2");
        Variant variant3 = new Variant("question3", "answer3");
        Variant variant4 = new Variant("question4", "answer4");

        Task task1 = new Task("task1", variant1, 5, Difficulty.EASY, Scope.EXAM);
        task1.addVariant(variant4);
        Task task2 = new Task("task1", variant2, 5, Difficulty.EASY, Scope.EXAM);
        Task task3 = new Task("task1", variant3, 5, Difficulty.EASY, Scope.EXAM);

        Chapter chapter1 = new Chapter("chapter1");
        chapter1.addTask(task1);
        chapter1.addTask(task2);
        Chapter chapter2 = new Chapter("chapter2");
        chapter2.addTask(task3);

        this.exam.addChapter(chapter1);
        this.exam.addChapter(chapter2);
    }
}
