package com.jexam.model;

import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Task entity with metadata and variants.
 */
public final class Task {
    private String name;
    private double points;
    private Difficulty difficulty;
    private Scope scope;
    private final List<Variant> variants;

    /**
     * Creates a task.
     *
     * @param name task name
     * @param points task points
     * @param difficulty task difficulty
     * @param scope task scope
     * @param variants initial variants
     */
    public Task(
        final String name,
        final double points,
        final Difficulty difficulty,
        final Scope scope,
        final List<Variant> variants
    ) {
        this.name = name;
        this.points = points;
        this.difficulty = difficulty;
        this.scope = scope;
        this.variants = new ArrayList<>(variants);
    }

    /**
     * Gets task name.
     *
     * @return task name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets task name.
     *
     * @param name new name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets task points.
     *
     * @return points value
     */
    public double getPoints() {
        return points;
    }

    /**
     * Sets task points.
     *
     * @param points new points value
     */
    public void setPoints(final double points) {
        this.points = points;
    }

    /**
     * Gets difficulty.
     *
     * @return difficulty value
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Sets difficulty.
     *
     * @param difficulty new difficulty
     */
    public void setDifficulty(final Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Gets scope.
     *
     * @return scope value
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Sets scope.
     *
     * @param scope new scope
     */
    public void setScope(final Scope scope) {
        this.scope = scope;
    }

    /**
     * Returns variants as an immutable list.
     *
     * @return variant list view
     */
    public List<Variant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    /**
     * Adds a variant.
     *
     * @param variant variant to add
     */
    public void addVariant(final Variant variant) {
        variants.add(variant);
    }

    /**
     * Removes a variant by index.
     *
     * @param index variant index
     */
    public void removeVariant(final int index) {
        variants.remove(index);
    }

    /**
     * Counts variants.
     *
     * @return variant count
     */
    public int variantCount() {
        return variants.size();
    }

    /**
     * Gets a variant by index.
     *
     * @param variantIndex variant index
     * @return selected variant
     */
    public Variant variantAt(final int variantIndex) {
        return variants.get(variantIndex);
    }

    @Override
    public boolean equals(final Object o) {
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
