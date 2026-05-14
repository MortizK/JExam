package com.jexam.model;

import java.util.List;
import java.util.Locale;

/**
 * Compact easy/medium/hard distribution summary for a set of tasks.
 */
public final class DifficultyDistributionSummary {
    private final int easyCount;
    private final int mediumCount;
    private final int hardCount;

    private DifficultyDistributionSummary(final int easyCount, final int mediumCount, final int hardCount) {
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
    }

    public static DifficultyDistributionSummary fromTasks(final List<Task> tasks) {
        int easyCount = 0;
        int mediumCount = 0;
        int hardCount = 0;

        if (tasks != null) {
            for (Task task : tasks) {
                if (task == null || task.getDifficulty() == null) {
                    continue;
                }

                switch (task.getDifficulty()) {
                    case EASY -> easyCount++;
                    case MEDIUM -> mediumCount++;
                    case HARD -> hardCount++;
                }
            }
        }

        return new DifficultyDistributionSummary(easyCount, mediumCount, hardCount);
    }

    public int easyCount() {
        return easyCount;
    }

    public int mediumCount() {
        return mediumCount;
    }

    public int hardCount() {
        return hardCount;
    }

    public int totalCount() {
        return easyCount + mediumCount + hardCount;
    }

    public boolean isEmpty() {
        return totalCount() == 0;
    }

    public double easyRatio() {
        return ratio(easyCount);
    }

    public double mediumRatio() {
        return ratio(mediumCount);
    }

    public double hardRatio() {
        return ratio(hardCount);
    }

    public boolean isBalanced(final double targetRatio, final double tolerance) {
        final int total = totalCount();
        if (total < 3 || easyCount == 0 || mediumCount == 0 || hardCount == 0) {
            return false;
        }

        return withinTolerance(easyCount, total, targetRatio, tolerance)
            && withinTolerance(mediumCount, total, targetRatio, tolerance)
            && withinTolerance(hardCount, total, targetRatio, tolerance);
    }

    public String toHumanReadableText() {
        if (isEmpty()) {
            return "No tasks selected.";
        }

        return "easy " + easyCount + " (" + formatPercent(easyRatio()) + ")"
            + ", medium " + mediumCount + " (" + formatPercent(mediumRatio()) + ")"
            + ", hard " + hardCount + " (" + formatPercent(hardRatio()) + ")";
    }

    public String toMetadataText() {
        return "Difficulty distribution: " + toHumanReadableText();
    }

    private double ratio(final int count) {
        final int total = totalCount();
        if (total == 0) {
            return 0d;
        }
        return count / (double) total;
    }

    private boolean withinTolerance(
        final int count,
        final int total,
        final double targetRatio,
        final double tolerance
    ) {
        final double ratio = count / (double) total;
        return Math.abs(ratio - targetRatio) <= tolerance;
    }

    private String formatPercent(final double ratio) {
        return String.format(Locale.ROOT, "%.1f%%", ratio * 100d);
    }
}
