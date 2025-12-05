package dhbw.koehler.jexaminer.model;

import java.util.ArrayList;
import java.util.List;

public class Exam {
    private String name;
    private List<Chapter> chapters;

    public Exam(String name) {
        this.name = name;
        this.chapters = new ArrayList<>();
    }

    // Name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // Chapters
    public List<Chapter> getChapters() {
        return chapters;
    }
    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
    // Needs: Delete Single one
    public void addChapter(Chapter chapter) {
        this.chapters.add(chapter);
    }
    public Integer getNumberOfChapters() {
        return this.chapters.size();
    }

}
