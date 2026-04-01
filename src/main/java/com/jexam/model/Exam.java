package com.jexam.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Exam {
    private String name;
    private final List<Chapter> chapters;

    public Exam(String name, List<Chapter> chapters) {
        this.name = name;
        this.chapters = new ArrayList<>(chapters);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Chapter> getChapters() {
        return Collections.unmodifiableList(chapters);
    }

    public void addChapter(Chapter chapter) {
        chapters.add(chapter);
    }

    public void removeChapter(int index) {
        chapters.remove(index);
    }

    public int chapterCount() {
        return chapters.size();
    }

    public Chapter chapterAt(int chapterIndex) {
        return chapters.get(chapterIndex);
    }

    public Task taskAt(int chapterIndex, int taskIndex) {
        return chapterAt(chapterIndex).taskAt(taskIndex);
    }

    public Variant variantAt(int chapterIndex, int taskIndex, int variantIndex) {
        return taskAt(chapterIndex, taskIndex).variantAt(variantIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Exam exam)) {
            return false;
        }
        return Objects.equals(name, exam.name) && Objects.equals(chapters, exam.chapters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, chapters);
    }
}
