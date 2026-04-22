package com.jexam.model;

import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Task entity with metadata and variants.
 *
 * @author Moritz
 */
public final class Task {
    /**
     * Task title.
     */
    private String name;

    /**
     * Maximum points for this task.
     */
    private double points;

    /**
     * Difficulty classification.
     */
    private Difficulty difficulty;

    /**
     * Scope in which this task appears.
     */
    private Scope scope;

    /**
     * Available variants for this task.
     */
    private final List<Variant> variants;

    /**
     * Creates a task.
     *
     * @param taskName task name
     * @param taskPoints task points
     * @param taskDifficulty task difficulty
     * @param taskScope task scope
     * @param variantList initial variants
     */
    public Task(
        final String taskName,
        final double taskPoints,
        final Difficulty taskDifficulty,
        final Scope taskScope,
        final List<Variant> variantList
    ) {
        this.name = taskName;
        this.points = taskPoints;
        this.difficulty = taskDifficulty;
        this.scope = taskScope;
        this.variants = new ArrayList<>(variantList);
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
     * @param newName new name
     */
    public void setName(final String newName) {
        this.name = newName;
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
     * @param newPoints new points value
     */
    public void setPoints(final double newPoints) {
        this.points = newPoints;
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
     * @param newDifficulty new difficulty
     */
    public void setDifficulty(final Difficulty newDifficulty) {
        this.difficulty = newDifficulty;
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
     * @param newScope new scope
     */
    public void setScope(final Scope newScope) {
        this.scope = newScope;
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(name, points, difficulty, scope, variants);
    }
}
