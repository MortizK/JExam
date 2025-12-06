package dhbw.koehler.jexam.service;

import dhbw.koehler.jexam.model.*;
import dhbw.koehler.jexam.model.enums.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataService {
    public Exam exam;
    public Item selectedItem;
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
        createRandomExampleData();
    }

    public void updateSelectedFromPath(List<Integer> path) {
        this.path = path;

        // Also update the breadCrumbs
        this.breadCrumbs = new ArrayList<>();
        breadCrumbs.add(this.exam.getName());

        Chapter selectedChapter;

        // Logic for nested Structure, limited to Tasks
        switch (path.size()) {
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
    }

    public void createRandomExampleData() {
        int numChapters = 4;
        int maxTasks = 10; // Can be zero
        int maxVariants = 3; // Always at least 1

        for (int c = 0; c < numChapters; c++) {
            Chapter chapter = new Chapter("Chapter " + c);

            int nTasks = new Random().nextInt(maxTasks + 1);
            for (int t = 0; t < nTasks; t++) {
                Variant startVariant = new Variant("Question 0", "Answer 0");

                Difficulty[] difficultys = Difficulty.values();
                Difficulty difficulty = difficultys[new Random().nextInt(difficultys.length)];

                Scope[]  scopes = Scope.values();
                Scope scope = scopes[new Random().nextInt(scopes.length)];

                Double points = (double) (new Random().nextInt(15) + 1);

                Task task = new Task("Task " + t, startVariant, points, difficulty, scope);

                int nVariants = new Random().nextInt(maxVariants + 1);
                for (int v = 1; v < nVariants; v++) {
                    Variant variant = new Variant("Question " + v, "Answer " + v);
                    task.addVariant(variant);
                }

                chapter.addTask(task);
            }

            this.exam.addChapter(chapter);
        }
    }
}
