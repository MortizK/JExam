package com.jexam.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Root exam aggregate containing chapters.
 *
 * @author Moritz
 */
public final class Exam {
    /**
     * Exam display name.
     */
    private String name;

    /**
     * Ordered chapter list.
     */
    private final List<Chapter> chapters;

    /**
     * Creates an exam.
     *
     * @param examName exam name
     * @param chapterList initial chapter list
     */
    public Exam(final String examName, final List<Chapter> chapterList) {
        this.name = examName;
        this.chapters = new ArrayList<>(chapterList);
    }

    /**
     * Gets exam name.
     *
     * @return exam name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets exam name.
     *
     * @param newName new exam name
     */
    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * Returns chapters as immutable list.
     *
     * @return chapter list view
     */
    public List<Chapter> getChapters() {
        return Collections.unmodifiableList(chapters);
    }

    /**
     * Adds chapter.
     *
     * @param chapter chapter to add
     */
    public void addChapter(final Chapter chapter) {
        chapters.add(chapter);
    }

    /**
     * Removes chapter by index.
     *
     * @param index chapter index
     */
    public void removeChapter(final int index) {
        chapters.remove(index);
    }

    /**
     * Counts chapters.
     *
     * @return chapter count
     */
    public int chapterCount() {
        return chapters.size();
    }

    /**
     * Gets chapter by index.
     *
     * @param chapterIndex chapter index
     * @return selected chapter
     */
    public Chapter chapterAt(final int chapterIndex) {
        return chapters.get(chapterIndex);
    }

    /**
     * Gets task by chapter/task index.
     *
     * @param chapterIndex chapter index
     * @param taskIndex task index
     * @return selected task
     */
    public Task taskAt(final int chapterIndex, final int taskIndex) {
        return chapterAt(chapterIndex).taskAt(taskIndex);
    }

    /**
     * Gets variant by chapter/task/variant indices.
     *
     * @param chapterIndex chapter index
     * @param taskIndex task index
     * @param variantIndex variant index
     * @return selected variant
     */
    public Variant variantAt(
        final int chapterIndex,
        final int taskIndex,
        final int variantIndex
    ) {
        return taskAt(chapterIndex, taskIndex).variantAt(variantIndex);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Exam exam)) {
            return false;
        }
        return Objects.equals(name, exam.name)
            && Objects.equals(chapters, exam.chapters);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(name, chapters);
    }
}
