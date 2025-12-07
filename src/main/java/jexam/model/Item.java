package jexam.model;

public class Item {
    private String name;

    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addChapter(Chapter chapter) {};

    public void addTask(Task task) {}

    public void addVariant(Variant variant) {};
}
