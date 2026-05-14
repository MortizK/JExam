file_path = r"src/main/java/com/jexam/model/DifficultyDistributionSummary.java"

content = '''package com.jexam.model;

import java.util.List;
import java.util.Locale;

/**
 * Aggregates task difficulty distribution statistics (easy, medium, hard counts).
 * 
 * This immutable, thread-safe value class computes and provides access to difficulty
 * distribution metrics for a collection of tasks. Used by the exam generation service
 * to validate that generated exams match the target difficulty ratio and by the UI
 * to display difficulty distribution charts and statistics.
 * 
 * The class provides:
 * - Raw counts for each difficulty level
 * - Percentages (ratios) for each level
 * - Validation against target ratio with tolerance
 * - Human-readable text representations for display and export
 * 
 * Instance creation is through the {@link #fromTasks(List)} factory method, which
 * null-safely processes the task list and gracefully handles null task/difficulty values.
 * 
 * @author Moritz
 */
public final class DifficultyDistributionSummary {
    /**
     * Number of easy-difficulty tasks in this distribution.
     */
    private final int easyCount;

    /**
     * Number of medium-difficulty tasks in this distribution.
     */
    private final int mediumCount;

    /**
     * Number of hard-difficulty tasks in this distribution.
     */
    private final int hardCount;

    /**
     * Creates a distribution summary with specific difficulty counts.
     * 
     * This constructor is private; use {@link #fromTasks(List)} to create instances.
     * 
     * @param easyCount number of easy tasks (>= 0)
     * @param mediumCount number of medium tasks (>= 0)
     * @param hardCount number of hard tasks (>= 0)
     */
    private DifficultyDistributionSummary(final int easyCount, final int mediumCount, final int hardCount) {
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
    }

    /**
     * Aggregates difficulty distribution from a task list.
     * 
     * Safely processes the provided task list, ignoring null tasks or tasks with
     * missing difficulty classifications. Returns a summary with zero counts if
     * the list is null or empty.
     * 
     * @param tasks list of tasks to analyze (null-safe)
     * @return distribution summary with easy/medium/hard counts
     */
    public static DifficultyDistributionSummary fromTasks(final List<Task> tasks) {
        int easyCount = 0;
        int mediumCount = 0;
        int hardCount = 0;

        if (tasks != null) {
            for (Task task : tasks) {
                // Skip null tasks or tasks without difficulty classification
                if (task == null || task.getDifficulty() == null) {
                    continue;
                }

                // Increment count for the corresponding difficulty level
                switch (task.getDifficulty()) {
                    case EASY -> easyCount++;
                    case MEDIUM -> mediumCount++;
                    case HARD -> hardCount++;
                }
            }
        }

        return new DifficultyDistributionSummary(easyCount, mediumCount, hardCount);
    }

    /**
     * Gets the number of easy tasks in this distribution.
     * 
     * @return easy task count (>= 0)
     */
    public int easyCount() {
        return easyCount;
    }

    /**
     * Gets the number of medium tasks in this distribution.
     * 
     * @return medium task count (>= 0)
     */
    public int mediumCount() {
        return mediumCount;
    }

    /**
     * Gets the number of hard tasks in this distribution.
     * 
     * @return hard task count (>= 0)
     */
    public int hardCount() {
        return hardCount;
    }

    /**
     * Gets the total number of tasks (sum of all difficulty levels).
     * 
     * @return total task count (>= 0)
     */
    public int totalCount() {
        return easyCount + mediumCount + hardCount;
    }

    /**
     * Indicates whether this distribution has no tasks.
     * 
     * @return true if total count is zero, false otherwise
     */
    public boolean isEmpty() {
        return totalCount() == 0;
    }

    /**
     * Gets the proportion of easy tasks (easy count / total count).
     * 
     * @return ratio in range [0.0, 1.0], or 0.0 if no tasks
     */
    public double easyRatio() {
        return ratio(easyCount);
    }

    /**
     * Gets the proportion of medium tasks (medium count / total count).
     * 
     * @return ratio in range [0.0, 1.0], or 0.0 if no tasks
     */
    public double mediumRatio() {
        return ratio(mediumCount);
    }

    /**
     * Gets the proportion of hard tasks (hard count / total count).
     * 
     * @return ratio in range [0.0, 1.0], or 0.0 if no tasks
     */
    public double hardRatio() {
        return ratio(hardCount);
    }

    /**
     * Determines whether each difficulty level is within the specified tolerance of target ratio.
     * 
     * Returns false if:
     * - Total task count is less than 3 (insufficient for meaningful distribution)
     * - Any difficulty level has zero tasks (incomplete distribution)
     * 
     * Returns true only if all three difficulty levels are represented and each
     * level ratio falls within +-tolerance of the target ratio.
     * 
     * Example: isBalanced(0.5, 0.1) checks if all levels are within 40-60% when target is 50%.
     * 
     * @param targetRatio expected ratio for each difficulty level (0.0-1.0)
     * @param tolerance acceptable deviation from target (>= 0.0)
     * @return true if distribution is balanced within tolerance, false otherwise
     */
    public boolean isBalanced(final double targetRatio, final double tolerance) {
        final int total = totalCount();
        // Require at least 3 tasks (one per difficulty) and all difficulties present
        if (total < 3 || easyCount == 0 || mediumCount == 0 || hardCount == 0) {
            return false;
        }

        // Check if each difficulty level ratio is within tolerance of target
        return withinTolerance(easyCount, total, targetRatio, tolerance)
            && withinTolerance(mediumCount, total, targetRatio, tolerance)
            && withinTolerance(hardCount, total, targetRatio, tolerance);
    }

    /**
     * Generates human-readable distribution text with counts and percentages.
     * 
     * Format example: "easy 5 (25.0%), medium 10 (50.0%), hard 5 (25.0%)"
     * or "No tasks selected." if distribution is empty.
     * 
     * @return formatted distribution string suitable for UI display
     */
    public String toHumanReadableText() {
        // Return placeholder text if no tasks in distribution
        if (isEmpty()) {
            return "No tasks selected.";
        }

        // Build formatted string with counts and percentages for each level
        return "easy " + easyCount + " (" + formatPercent(easyRatio()) + ")"
            + ", medium " + mediumCount + " (" + formatPercent(mediumRatio()) + ")"
            + ", hard " + hardCount + " (" + formatPercent(hardRatio()) + ")";
    }

    /**
     * Generates distribution text with "Difficulty distribution:" prefix for metadata/export.
     * 
     * Format example: "Difficulty distribution: easy 5 (25.0%), medium 10 (50.0%), hard 5 (25.0%)"
     * 
     * @return formatted distribution string with metadata prefix
     */
    public String toMetadataText() {
        return "Difficulty distribution: " + toHumanReadableText();
    }

    /**
     * Computes the ratio of a count to the total.
     * 
     * @param count difficulty count to ratio
     * @return count / totalCount, or 0.0 if no tasks
     */
    private double ratio(final int count) {
        final int total = totalCount();
        // Prevent division by zero when no tasks present
        if (total == 0) {
            return 0d;
        }
        return count / (double) total;
    }

    /**
     * Checks if a ratio is within tolerance of a target value.
     * 
     * Calculates the absolute difference between (count / total) and targetRatio,
     * returns true if the difference is <= tolerance.
     * 
     * @param count numerator for ratio calculation
     * @param total denominator for ratio calculation
     * @param targetRatio expected ratio value
     * @param tolerance acceptable deviation (>= 0.0)
     * @return true if |ratio - targetRatio| <= tolerance, false otherwise
     */
    private boolean withinTolerance(
        final int count,
        final int total,
        final double targetRatio,
        final double tolerance
    ) {
        // Calculate actual ratio and compare against target with tolerance window
        final double ratio = count / (double) total;
        return Math.abs(ratio - targetRatio) <= tolerance;
    }

    /**
     * Formats a ratio as a percentage string with one decimal place.
     * 
     * @param ratio value in range [0.0, 1.0]
     * @return formatted string (e.g., "25.0%")
     */
    private String formatPercent(final double ratio) {
        return String.format(Locale.ROOT, "%.1f%%", ratio * 100d);
    }
}
'''

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("File rewritten successfully")
