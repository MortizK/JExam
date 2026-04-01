package com.jexam.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Chapter containing a set of tasks.
 */
public final class Chapter {
    private String name;
    private final List<Task> tasks;

    /**
     * Creates a chapter.
     *
     * @param name chapter name
     * @param tasks initial task list
     */
    public Chapter(final String name, final List<Task> tasks) {
        this.name = name;
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Gets chapter name.
     *
     * @return chapter name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets chapter name.
     *
     * @param name new chapter name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns tasks as immutable list.
     *
     * @return task list view
     */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /**
     * Adds task.
     *
     * @param task task to add
     */
    public void addTask(final Task task) {
        tasks.add(task);
    }

    /**
     * Removes task by index.
     *
     * @param index task index
     */
    public void removeTask(final int index) {
        tasks.remove(index);
    }

    /**
     * Counts tasks.
     *
     * @return number of tasks
     */
    public int taskCount() {
        return tasks.size();
    }

    /**
     * Gets task by index.
     *
     * @param taskIndex task index
     * @return selected task
     */
    public Task taskAt(final int taskIndex) {
        return tasks.get(taskIndex);
    }

    /**
     * Gets task variant by indices.
     *
     * @param taskIndex task index
     * @param variantIndex variant index
     * @return selected variant
     */
    public Variant variantAt(final int taskIndex, final int variantIndex) {
        return taskAt(taskIndex).variantAt(variantIndex);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Chapter chapter)) {
            return false;
        }
        return Objects.equals(name, chapter.name)
            && Objects.equals(tasks, chapter.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tasks);
    }
}
