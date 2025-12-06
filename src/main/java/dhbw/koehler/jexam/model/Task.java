package dhbw.koehler.jexam.model;

import dhbw.koehler.jexam.model.enums.Difficulty;
import dhbw.koehler.jexam.model.enums.Scope;

import java.util.ArrayList;
import java.util.List;

public class Task extends Item{
    private String name;
    private List<Variant> variants;
    private Double points;
    private Difficulty difficulty;
    private Scope scope;

    public Task(String name, Variant variant, Double points, Difficulty difficulty, Scope scope) {
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
    public void deleteVariant(Variant variant) {
        if(variants.size() > 1) {
            this.variants.remove(variant);
        }
    }
    @Override
    public void addVariant(Variant variant) {
        this.variants.add(variant);
    }
    public Integer getNumberOfVariants() {
        return variants.size();
    }

    // Points
    public Double getPoints() {
        return points;
    }
    public void setPoints(Double points) {
        this.points = points;
    }

    // Difficulty
    public Difficulty getDifficulty() {
        return difficulty;
    }
    public String getFormatedDifficulty() {
        String diff = difficulty.toString();
        return diff.charAt(0) + diff.substring(1).toLowerCase();
    }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    // Scope
    public Scope getScope() {
        return scope;
    }
    public String getFormatedScope() {
        String sc = scope.toString();
        return sc.charAt(0) + sc.substring(1).toLowerCase();
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }
}
