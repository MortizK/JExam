package dhbw.koehler.jexam.model;

import dhbw.koehler.jexam.model.enums.Difficulty;
import dhbw.koehler.jexam.model.enums.Scope;

import java.util.ArrayList;
import java.util.List;

public class Task extends Item{
    private String name;
    private List<Variant> variants;
    private Integer points;
    private Difficulty difficulty;
    private Scope scope;

    public Task(String name, Variant variant, Integer points, Difficulty difficulty, Scope scope) {
        super(name);
        this.points = points;
        this.variants = new ArrayList<>();
        this.variants.add(variant);
        this.difficulty = difficulty;
        this.scope = scope;
    }

    // Variants Handling
    public List<Variant> getVariants() {
        return variants;
    }
    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }
    // Needs: Delete Single one, but leave at least one
    public void addVariant(Variant variant) {
        this.variants.add(variant);
    }
    public Integer getNumberOfVariants() {
        return variants.size();
    }

    // Points
    public Integer getPoints() {
        return points;
    }
    public void setPoints(Integer points) {
        this.points = points;
    }

    // Difficulty
    public Difficulty getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    // Scope
    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
