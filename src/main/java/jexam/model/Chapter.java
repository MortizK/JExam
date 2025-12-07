package jexam.model;

import jexam.model.enums.Difficulty;

import java.util.*;

public class Chapter extends Item{
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
    public void deleteTask(Task task) {
        this.tasks.remove(task);
    }
    @Override
    public void addTask(Task task) {
        this.tasks.add(task);
    }
    public Integer getNumberOfTasks() {
        return this.tasks.size();
    }

    // Points
    public Double getNumberOfPoints() {
        Double numberOfPoints = 0.0;
        for (Task task : this.tasks) {
            numberOfPoints += task.getPoints();
        }
        return numberOfPoints;
    }

    public List<Double> getPossiblePointCombinations() {
        List<Double> points = new ArrayList<>();
        for (Task task : this.tasks) {
            points.add(task.getPoints());
        }

        if (points.isEmpty()) {
            points.add(0.0);
            return points;
        }

        // Working with Sets to avoid duplicates
        Set<Double> possiblePointsSet = new HashSet<>();
        possiblePointsSet.add(0.0);

        for (Double p : points) {
            Set<Double> newSums = new HashSet<>();
            for (Double existingSum : possiblePointsSet) {
                newSums.add(existingSum + p);
            }
            possiblePointsSet.addAll(newSums);
        }

        // Sorting
        List<Double> possiblePoints = new ArrayList<>(possiblePointsSet);
        possiblePoints.removeFirst(); // Removing the Zero
        Collections.sort(possiblePoints);
        return possiblePoints;
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
