package dhbw.koehler.jexam.model;

import dhbw.koehler.jexam.model.enums.Difficulty;

import java.util.ArrayList;
import java.util.List;

public class Chapter extends Item{
    private String name;
    private List<Task> tasks;

    public Chapter(String name) {
        super(name);
        this.tasks = new ArrayList<Task>();
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
