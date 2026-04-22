package com.jexam.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Chapter containing a set of tasks.
 *
 * @author Moritz
 */
public final class Chapter {
    /**
     * Chapter title.
     */
    private String name;

    /**
     * Tasks in this chapter.
     */
    private final List<Task> tasks;

    /**
     * Creates a chapter.
     *
     * @param chapterName chapter name
     * @param taskList initial task list
     */
    public Chapter(final String chapterName, final List<Task> taskList) {
        this.name = chapterName;
        this.tasks = new ArrayList<>(taskList);
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
     * @param newName new chapter name
     */
    public void setName(final String newName) {
        this.name = newName;
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(name, tasks);
    }
}
