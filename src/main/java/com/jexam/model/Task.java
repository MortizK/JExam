package com.jexam.model;

import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Task {
    private String name;
    private double points;
    private Difficulty difficulty;
    private Scope scope;
    private final List<Variant> variants;

    public Task(String name, double points, Difficulty difficulty, Scope scope, List<Variant> variants) {
        this.name = name;
        this.points = points;
        this.difficulty = difficulty;
        this.scope = scope;
        this.variants = new ArrayList<>(variants);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public List<Variant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    public void addVariant(Variant variant) {
        variants.add(variant);
    }

    public void removeVariant(int index) {
        variants.remove(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task task)) {
            return false;
        }
        return Double.compare(points, task.points) == 0
            && Objects.equals(name, task.name)
            && difficulty == task.difficulty
            && scope == task.scope
            && Objects.equals(variants, task.variants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, points, difficulty, scope, variants);
    }
}
