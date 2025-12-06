package dhbw.koehler.jexam.model;

import java.util.ArrayList;
import java.util.List;

public class Exam extends Item {
    private String name;
    private List<Chapter> chapters;

    public Exam(String name) {
        super(name);
        this.chapters = new ArrayList<>();
    }

    // Chapters
    public List<Chapter> getChapters() {
        return chapters;
    }
    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
    public void deleteChapter(Chapter chapter) {
        this.chapters.remove(chapter);
    }
    @Override
    public void addChapter(Chapter chapter) {
        this.chapters.add(chapter);
    }
    public Integer getNumberOfChapters() {
        return this.chapters.size();
    }

}
