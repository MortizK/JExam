package com.jexam.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Chapter {
    private String name;
    private final List<Task> tasks;

    public Chapter(String name, List<Task> tasks) {
        this.name = name;
        this.tasks = new ArrayList<>(tasks);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(int index) {
        tasks.remove(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Chapter chapter)) {
            return false;
        }
        return Objects.equals(name, chapter.name) && Objects.equals(tasks, chapter.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tasks);
    }
}
