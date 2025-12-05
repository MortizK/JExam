package dhbw.koehler.jexaminer.model;

import dhbw.koehler.jexaminer.model.enums.Difficulty;

import java.util.ArrayList;
import java.util.List;

public class Chapter {
    private String name;
    private List<Task> tasks;

    public Chapter(String name) {
        this.name = name;
        this.tasks = new ArrayList<Task>();
    }

    // Name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Tasks
    public List<Task> getTasks() {
        return tasks;
    }
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
    // Needs: Delete Single one
    public void addTask(Task task) {
        this.tasks.add(task);
    }
    public Integer getNumberOfTasks() {
        return this.tasks.size();
    }

    // Points
    public Integer getNumberOfPoints() {
        Integer numberOfPoints = 0;
        for (Task task : this.tasks) {
            numberOfPoints += task.getPoints();
        }
        return numberOfPoints;
    }

    // Variants
    public Integer getNumberOfVariants() {
        Integer numberOfVariants = 0;
        for(Task task : this.tasks){
            numberOfVariants += task.getNumberOfVariants();
        }
        return numberOfVariants;
    }

    // Difficulties
    public Integer getNumberOfDifficultyTasks(Difficulty difficulty) {
        Integer numberOfTasks = 0;
        for(Task task : this.tasks){
            numberOfTasks += (task.getDifficulty() == difficulty) ? 1 : 0;
        }
        return numberOfTasks;
    }
}
