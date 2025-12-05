package dhbw.koehler.jexaminer.service;

import dhbw.koehler.jexaminer.model.*;
import dhbw.koehler.jexaminer.model.enums.*;

import java.util.ArrayList;
import java.util.List;

public class DataService {
    public Exam exam;
    public Object selectedItem;
    public Type type;
    public List<Integer> path;
    public List<String> breadCrumbs;

    public DataService(String name) {
        this.exam = new Exam((name == null) ? "Exam" : name);
        this.selectedItem = this.exam;
        this.type = Type.EXAM;
        this.path = new ArrayList<>();
        this.breadCrumbs = new ArrayList<>();
        this.breadCrumbs.add(this.exam.getName());
        createExampleData();
    }

    public void updateSelectedFromPath(List<Integer> path) {
        this.path = path;

        // Also update the breadCrumbs
        this.breadCrumbs = new ArrayList<>();
        breadCrumbs.add(this.exam.getName());

        Chapter selectedChapter;

        // Logic for nested Structure, limited to Tasks
        switch(path.size()) {
            case 0:
                this.type = Type.EXAM;
                this.selectedItem = this.exam;
                break;
            case 1:
                this.type = Type.CHAPTER;
                selectedChapter = this.exam.getChapters().get(path.getFirst());
                breadCrumbs.add(selectedChapter.getName());
                this.selectedItem = selectedChapter;
                break;
            case 2:
                this.type = Type.TASK;
                selectedChapter = this.exam.getChapters().get(path.getFirst());
                breadCrumbs.add(selectedChapter.getName());
                Task selectedTask = selectedChapter.getTasks().get(path.get(1));
                breadCrumbs.add(selectedTask.getName());
                this.selectedItem = selectedTask;
                break;
        }

        System.out.println(breadCrumbs.toString());
    }

    public void createExampleData() {
        Variant variant1 = new Variant("question1", "answer1");
        Variant variant2 = new Variant("question2", "answer2");
        Variant variant3 = new Variant("question3", "answer3");
        Variant variant4 = new Variant("question4", "answer4");

        Task task1 = new Task("task1", variant1, 5, Difficulty.EASY, Scope.EXAM);
        task1.addVariant(variant4);
        Task task2 = new Task("task2", variant2, 5, Difficulty.EASY, Scope.EXAM);
        Task task3 = new Task("task3", variant3, 5, Difficulty.EASY, Scope.EXAM);

        Chapter chapter1 = new Chapter("chapter1");
        chapter1.addTask(task1);
        chapter1.addTask(task2);
        Chapter chapter2 = new Chapter("chapter2");
        chapter2.addTask(task3);

        this.exam.addChapter(chapter1);
        this.exam.addChapter(chapter2);
    }
}
