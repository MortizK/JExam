package com.jexam.app;

import com.jexam.generation.GenerationMode;
import com.jexam.generation.PdfBoxGenerationService;
import com.jexam.generation.PdfGenerationService;
import com.jexam.io.ExamPersistenceService;
import com.jexam.io.ExamXmlException;
import com.jexam.io.ExamXmlLoader;
import com.jexam.io.ExamXmlWriter;
import com.jexam.model.Chapter;
import com.jexam.model.DifficultyDistributionSummary;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import com.jexam.validation.ExamValidator;
import com.jexam.validation.ValidationResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.nio.file.Files;

/**
 * Application service that centralizes exam use-case orchestration.
 *
 * <p>This class acts as the controller-side boundary between the JavaFX view
 * and domain/services. It keeps JExamApp focused on presentation concerns.</p>
 *
 * @author Moritz
 */
public class ExamApplicationService {
    private static final double DIFFICULTY_TARGET_RATIO = 1d / 3d;
    private static final double DIFFICULTY_TOLERANCE = 0.10d;
    private static final int POINT_SCALE = 2;

    private final ExamValidator validator;
    private final PdfGenerationService pdfGenerationService;
    private final ExamPersistenceService persistenceService;
    private final List<Integer> generationChapterIndices;
    private final Map<Integer, Double> generationChapterGoalPoints;
    private final List<String> lastGenerationWarnings;

    private Exam currentExam;
    private Exam lastPreviewExam;
    private Exam lastGeneratedExam;
    private GenerationMode lastPreviewMode;
    private GoalPointFallbackPreference goalPointFallbackPreference;
    private Long generationRandomSeed;

    /**
     * Preference for resolving infeasible chapter goal points.
     */
    public enum GoalPointFallbackPreference {
        /**
         * Prefer the nearest lower achievable point sum.
         */
        LOWER,

        /**
         * Prefer the nearest higher achievable point sum.
         */
        HIGHER
    }

    /**
     * <p>Constructor for ExamApplicationService.</p>
     */
    public ExamApplicationService() {
        this.validator = new ExamValidator();
        this.pdfGenerationService = new PdfBoxGenerationService();
        this.persistenceService = new ExamPersistenceService(
            new ExamXmlLoader(),
            new ExamXmlWriter(),
            validator
        );
        this.currentExam = createDefaultExam();
        this.generationChapterIndices = new ArrayList<>();
        this.generationChapterGoalPoints = new HashMap<>();
        this.lastGenerationWarnings = new ArrayList<>();
        this.lastPreviewExam = null;
        this.lastGeneratedExam = null;
        this.lastPreviewMode = null;
        this.goalPointFallbackPreference = GoalPointFallbackPreference.LOWER;
        this.generationRandomSeed = null;
        resetGenerationChapterSelection();
    }

    /**
     * Returns the exam currently managed by the application service.
     *
     * @return current exam state
     */
    public Exam getCurrentExam() {
        return currentExam;
    }

    /**
     * Replaces the current exam with a default empty template.
     */
    public void newExam() {
        currentExam = createDefaultExam();
        resetGenerationChapterSelection();
    }

    /**
     * Loads and validates an exam from disk.
     *
     * @param path XML input path
     * @throws com.jexam.io.ExamXmlException if loading or validation fails
     */
    public void openExam(Path path) throws ExamXmlException {
        currentExam = persistenceService.loadValidated(path);
        resetGenerationChapterSelection();
    }

    /**
     * Validates and saves the current exam to disk.
     *
     * @param path XML output path
     * @throws com.jexam.io.ExamXmlException if validation or writing fails
     */
    public void saveExam(Path path) throws ExamXmlException {
        persistenceService.saveValidated(currentExam, path);
    }

    /**
     * Runs full validation for the current exam.
     *
     * @return validation result with all detected errors
     */
    public ValidationResult validateCurrentExam() {
        return validator.validate(currentExam);
    }

    /**
     * Generates a PDF export for the current exam in the selected mode.
     *
     * @param mode export mode
     * @param outputPath destination PDF path
     */
    public void generatePdf(GenerationMode mode, Path outputPath) {
        lastGenerationWarnings.clear();
        Exam generationExam = buildExamForGeneration(mode, generationRandom(mode));
        ValidationResult result = validator.validate(generationExam);
        if (!result.isValid()) {
            throw new IllegalStateException("Exam is invalid: " + result.getErrors());
        }
        updateGenerationOutcome(mode, generationExam);
        pdfGenerationService.generate(generationExam, mode, outputPath);
    }

    /**
     * Sets whether the PDF generator should include the cover page.
     * This is a best-effort call and only affects PdfBoxGenerationService instances.
     */
    public void setCoverEnabled(final boolean enabled) {
        if (pdfGenerationService instanceof PdfBoxGenerationService) {
            ((PdfBoxGenerationService) pdfGenerationService).setCoverEnabled(enabled);
        }
    }

    /**
     * Sets the UI language for any PDF generator that can render localized texts.
     */
    public void setUiLanguage(final UiLanguage language) {
        if (pdfGenerationService instanceof PdfBoxGenerationService) {
            ((PdfBoxGenerationService) pdfGenerationService).setUiLanguage(language);
        }
    }

    /**
     * Generates both primary and solutions PDFs for the selected mode.
     *
     * @param mode generation mode (EXAM or MOCK_EXAM)
     * @param outputPath destination path for primary PDF
     * @return generated file paths [primary, solutions]
     */
    public List<Path> generatePdfPair(final GenerationMode mode, final Path outputPath) {
        lastGenerationWarnings.clear();

        if (mode != GenerationMode.EXAM && mode != GenerationMode.MOCK_EXAM) {
            throw new IllegalArgumentException("Only EXAM and MOCK_EXAM modes are supported.");
        }

        final Random random = generationRandom(mode);
        final Exam generationExam = buildExamForGeneration(mode, random);
        final ValidationResult result = validator.validate(generationExam);
        if (!result.isValid()) {
            throw new IllegalStateException("Exam is invalid: " + result.getErrors());
        }

        updateGenerationOutcome(mode, generationExam);

        final Path primaryPath = outputPath;
        final Path solutionPath = solutionOutputPath(outputPath);
        pdfGenerationService.generate(generationExam, mode, primaryPath);
        pdfGenerationService.generate(generationExam, GenerationMode.SOLUTION, solutionPath);
        return List.of(primaryPath, solutionPath);
    }

    /**
     * Generates primary and solutions PDFs using the last preview snapshot.
     * This keeps exported questions identical to the already rendered preview.
     *
     * @param mode generation mode that must match the preview mode
     * @param outputPath destination path for primary PDF
     * @return generated file paths [primary, solutions]
     */
    public List<Path> generatePdfPairFromLastPreview(final GenerationMode mode, final Path outputPath) {
        lastGenerationWarnings.clear();
        if (lastPreviewExam == null || lastPreviewMode != mode) {
            return generatePdfPair(mode, outputPath);
        }

        final ValidationResult result = validator.validate(lastPreviewExam);
        if (!result.isValid()) {
            throw new IllegalStateException("Exam is invalid: " + result.getErrors());
        }

        updateGenerationOutcome(mode, lastPreviewExam);

        final Path primaryPath = outputPath;
        final Path solutionPath = solutionOutputPath(outputPath);
        pdfGenerationService.generate(lastPreviewExam, mode, primaryPath);
        pdfGenerationService.generate(lastPreviewExam, GenerationMode.SOLUTION, solutionPath);
        return List.of(primaryPath, solutionPath);
    }

    /**
     * Returns warnings collected during the last preview/export generation run.
     *
     * @return immutable list of generation warnings
     */
    public List<String> getLastGenerationWarnings() {
        return Collections.unmodifiableList(lastGenerationWarnings);
    }

    /**
     * Returns the difficulty distribution from the most recently generated exam.
     *
     * @return summary for the most recent generation result, or null if none exists yet
     */
    public DifficultyDistributionSummary getLastGenerationDifficultySummary() {
        return lastGeneratedExam == null ? null : summarizeDifficultyDistribution(lastGeneratedExam);
    }

    /**
     * Returns the current difficulty distribution for the selected generation chapters.
     *
     * @param mode generation mode used to filter tasks
     * @return summary for the current generation selection
     */
    public DifficultyDistributionSummary getGenerationDifficultySummary(final GenerationMode mode) {
        return summarizeSelectedGenerationDifficulty(mode);
    }

    /**
     * Generates a preview PDF into a temporary file and returns its path.
     *
     * @param mode preview generation mode
     * @return absolute path to generated preview PDF
     */
    public Path generatePreviewPdf(GenerationMode mode) {
        try {
            Path previewPath = Files.createTempFile("jexam-preview-", ".pdf");
            previewPath.toFile().deleteOnExit();

            lastGenerationWarnings.clear();
            Exam generationExam = buildExamForGeneration(mode, generationRandom(mode));
            ValidationResult result = validator.validate(generationExam);
            if (!result.isValid()) {
                throw new IllegalStateException("Exam is invalid: " + result.getErrors());
            }
            updateGenerationOutcome(mode, generationExam);
            pdfGenerationService.generate(generationExam, mode, previewPath);

            lastPreviewExam = generationExam;
            lastPreviewMode = mode;
            return previewPath.toAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create preview file.", e);
        }
    }

    private List<String> collectGenerationWarnings(
        final GenerationMode mode,
        final Exam generationExam
    ) {
        List<String> warnings = new ArrayList<>();
        if (mode != GenerationMode.EXAM && mode != GenerationMode.SOLUTION) {
            return warnings;
        }

        DifficultyDistributionSummary summary = summarizeDifficultyDistribution(generationExam);
        if (!summary.isBalanced(DIFFICULTY_TARGET_RATIO, DIFFICULTY_TOLERANCE)) {
            warnings.add(
                "Generated exam difficulty distribution is " + summary.toHumanReadableText()
                    + " (recommended balance: 33% +/- 10%)."
            );
        }
        return warnings;
    }

    private void updateGenerationOutcome(final GenerationMode mode, final Exam generationExam) {
        lastGeneratedExam = generationExam;
        lastGenerationWarnings.clear();
        lastGenerationWarnings.addAll(collectGenerationWarnings(mode, generationExam));
    }

    private DifficultyDistributionSummary summarizeSelectedGenerationDifficulty(final GenerationMode mode) {
        List<Task> selectedTasks = new ArrayList<>();
        for (int chapterIndex : generationChapterIndices) {
            if (chapterIndex < 0 || chapterIndex >= currentExam.chapterCount()) {
                continue;
            }

            Chapter chapter = currentExam.chapterAt(chapterIndex);
            for (Task task : chapter.getTasks()) {
                if (isTaskIncludedForMode(task, mode)) {
                    selectedTasks.add(task);
                }
            }
        }
        return DifficultyDistributionSummary.fromTasks(selectedTasks);
    }

    private DifficultyDistributionSummary summarizeDifficultyDistribution(final Exam exam) {
        List<Task> tasks = new ArrayList<>();
        for (int chapterIndex = 0; chapterIndex < exam.chapterCount(); chapterIndex++) {
            tasks.addAll(exam.chapterAt(chapterIndex).getTasks());
        }
        return DifficultyDistributionSummary.fromTasks(tasks);
    }

    private boolean isTaskIncludedForMode(final Task task, final GenerationMode mode) {
        if (mode == GenerationMode.MOCK_EXAM) {
            return true;
        }
        return task.getScope() == Scope.EXAM;
    }

    /**
     * Adds a chapter initialized with one default task.
     *
     * @param name chapter title
     */
    public void addChapter(String name) {
        currentExam.addChapter(new Chapter(name, List.of(defaultTask())));
        resetGenerationChapterSelection();
    }

    /**
     * Removes a chapter by index.
     *
     * @param chapterIndex chapter index in current exam
     */
    public void removeChapter(int chapterIndex) {
        currentExam.removeChapter(chapterIndex);
        resetGenerationChapterSelection();
    }

    /**
     * Adds a task to a chapter.
     *
     * @param chapterIndex target chapter index
     * @param taskName task title
     */
    public void addTask(int chapterIndex, String taskName) {
        Task task = defaultTask();
        task.setName(taskName);
        chapterAt(chapterIndex).addTask(task);
    }

    /**
     * Returns configured chapter indices used for generation.
     *
     * @return ordered generation chapter indices
     */
    public List<Integer> generationChapterOrder() {
        return Collections.unmodifiableList(generationChapterIndices);
    }

    /**
     * Returns configured chapter point goals used during random generation.
     *
     * @return immutable chapter index to goal points map
     */
    public Map<Integer, Double> generationChapterGoalPoints() {
        return Collections.unmodifiableMap(generationChapterGoalPoints);
    }

    /**
     * Configures chapter goal points used during random generation.
     *
     * @param chapterIndex chapter index in current exam
     * @param points desired chapter points, normalized to 0.5 increments
     */
    public void setGenerationChapterGoalPoints(final int chapterIndex, final double points) {
        if (chapterIndex < 0 || chapterIndex >= currentExam.chapterCount()) {
            return;
        }
        final double normalizedPoints = normalizeHalfPoint(points);
        if (normalizedPoints <= 0d) {
            throw new IllegalArgumentException("Chapter goal points must be greater than 0.");
        }
        generationChapterGoalPoints.put(chapterIndex, normalizedPoints);
    }

    /**
     * Returns the current infeasible-goal fallback preference.
     *
     * @return fallback preference
     */
    public GoalPointFallbackPreference getGoalPointFallbackPreference() {
        return goalPointFallbackPreference;
    }

    /**
     * Updates how infeasible chapter goal points are resolved.
     *
     * @param preference fallback preference
     */
    public void setGoalPointFallbackPreference(final GoalPointFallbackPreference preference) {
        if (preference != null) {
            goalPointFallbackPreference = preference;
        }
    }

    /**
     * Returns the deterministic random seed used during generation.
     *
     * @return seed value, or null if non-deterministic generation is enabled
     */
    public Long getGenerationRandomSeed() {
        return generationRandomSeed;
    }

    /**
     * Updates deterministic random seed used during generation.
     *
     * @param seed seed value, or null to use non-deterministic random generation
     */
    public void setGenerationRandomSeed(final Long seed) {
        generationRandomSeed = seed;
    }

    /**
     * Moves a generation chapter one step up in the order.
     *
     * @param orderIndex index in generation order list
     */
    public void moveGenerationChapterUp(int orderIndex) {
        if (orderIndex <= 0 || orderIndex >= generationChapterIndices.size()) {
            return;
        }
        Collections.swap(generationChapterIndices, orderIndex, orderIndex - 1);
    }

    /**
     * Moves a generation chapter one step down in the order.
     *
     * @param orderIndex index in generation order list
     */
    public void moveGenerationChapterDown(int orderIndex) {
        if (orderIndex < 0 || orderIndex >= generationChapterIndices.size() - 1) {
            return;
        }
        Collections.swap(generationChapterIndices, orderIndex, orderIndex + 1);
    }

    /**
     * Excludes a chapter from generation by its position in generation order.
     *
     * @param orderIndex index in generation order list
     */
    public void excludeGenerationChapter(int orderIndex) {
        if (orderIndex < 0 || orderIndex >= generationChapterIndices.size()) {
            return;
        }
        generationChapterIndices.remove(orderIndex);
    }

    /**
     * Includes a chapter for generation by chapter index.
     *
     * @param chapterIndex chapter index in current exam
     */
    public void includeGenerationChapter(int chapterIndex) {
        if (chapterIndex < 0 || chapterIndex >= currentExam.chapterCount()) {
            return;
        }
        if (!generationChapterIndices.contains(chapterIndex)) {
            generationChapterIndices.add(chapterIndex);
        }
    }

    /**
     * Restores generation selection to all chapters in natural order.
     */
    public void resetGenerationChapterSelection() {
        generationChapterIndices.clear();
        for (int chapterIndex = 0; chapterIndex < currentExam.chapterCount(); chapterIndex++) {
            generationChapterIndices.add(chapterIndex);
        }
        resetGenerationGoalPoints();
    }

    /**
     * Removes a task from a chapter.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex task index in chapter
     */
    public void removeTask(int chapterIndex, int taskIndex) {
        chapterAt(chapterIndex).removeTask(taskIndex);
    }

    /**
     * Adds a default variant to a task.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     */
    public void addVariant(int chapterIndex, int taskIndex) {
        taskAt(chapterIndex, taskIndex).addVariant(new Variant("New Question", "New Answer"));
    }

    /**
     * Removes a variant from a task.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     * @param variantIndex target variant index
     */
    public void removeVariant(int chapterIndex, int taskIndex, int variantIndex) {
        Task task = taskAt(chapterIndex, taskIndex);
        if (task.variantCount() <= 1) {
            throw new IllegalStateException(
                "A task must contain at least one variant."
            );
        }
        task.removeVariant(variantIndex);
    }

    /**
     * Updates editable task properties.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     * @param name task name
     * @param points task points
     * @param difficulty task difficulty
     * @param scope task scope
     */
    public void updateTaskDetails(int chapterIndex, int taskIndex, String name, double points, Difficulty difficulty, Scope scope) {
        Task task = taskAt(chapterIndex, taskIndex);
        task.setName(name);
        task.setPoints(points);
        task.setDifficulty(difficulty);
        task.setScope(scope);
    }

    /**
     * Updates editable variant content.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     * @param variantIndex target variant index
     * @param question question text
     * @param answer answer text
     */
    public void updateVariantDetails(int chapterIndex, int taskIndex, int variantIndex, String question, String answer) {
        Variant variant = variantAt(chapterIndex, taskIndex, variantIndex);
        variant.setQuestion(question);
        variant.setAnswer(answer);
    }

    /**
     * Convenience wrapper for accessing a chapter from the current exam.
     */
    private Chapter chapterAt(int chapterIndex) {
        return currentExam.chapterAt(chapterIndex);
    }

    /**
     * Convenience wrapper for accessing a task from the current exam.
     */
    private Task taskAt(int chapterIndex, int taskIndex) {
        return currentExam.taskAt(chapterIndex, taskIndex);
    }

    /**
     * Convenience wrapper for accessing a variant from the current exam.
     */
    private Variant variantAt(int chapterIndex, int taskIndex, int variantIndex) {
        return currentExam.variantAt(chapterIndex, taskIndex, variantIndex);
    }

    /**
     * Create the default exam template used when starting a new document.
     */
    private Exam createDefaultExam() {
        return new Exam("New Exam", List.of(new Chapter("New Chapter", List.of(defaultTask()))));
    }

    /**
     * Build a new Exam instance containing only the selected chapters and
     * tasks suitable for the given {@link GenerationMode}.
     *
     * <p>The method clones chapter/task content as necessary and applies mode
     * filtering (e.g. excludes non-exam scoped tasks for EXAM mode).</p>
     */
    private Exam buildExamForGeneration(final GenerationMode mode, final Random random) {
        if (generationChapterIndices.isEmpty()) {
            throw new IllegalStateException("No chapters selected for PDF generation.");
        }

        List<Chapter> chapters = new ArrayList<>();
        for (int chapterIndex : generationChapterIndices) {
            if (chapterIndex >= 0 && chapterIndex < currentExam.chapterCount()) {
                Chapter sourceChapter = currentExam.chapterAt(chapterIndex);
                // if chapter has no tasks, skip it to avoid generation failing due to infeasible goals
                // error when chapter has no task for specified mode are still possible
                if (sourceChapter.getTasks().isEmpty()) {
                    continue;
                }
                chapters.add(buildChapterForGeneration(sourceChapter, chapterIndex, mode, random));
            }
        }

        if (chapters.isEmpty()) {
            throw new IllegalStateException("No chapters selected for PDF generation.");
        }
        return new Exam(currentExam.getName(), chapters);
    }

    /**
     * Build a chapter tailored for random generation.
     *
     * <p>For MOCK_EXAM mode this returns all tasks cloned with a single
     * randomized variant. For EXAM mode the method resolves a chapter
     * point goal and selects a subset of tasks whose summed points match
     * the resolved target (with fallback preferences applied).</p>
     */
    private Chapter buildChapterForGeneration(
        final Chapter sourceChapter,
        final int chapterIndex,
        final GenerationMode mode,
        final Random random
    ) {
        List<Task> candidates = modeFilteredTasks(sourceChapter, mode);
        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                "Chapter '" + sourceChapter.getName() + "' has no tasks available for mode " + mode + "."
            );
        }

        if (mode == GenerationMode.MOCK_EXAM) {
            return new Chapter(sourceChapter.getName(), cloneAllTasksWithSingleVariant(candidates, random));
        }

        double chapterGoalPoints = resolveChapterGoalPoints(chapterIndex, candidates);
        List<Task> selectedTasks = selectTasksForGoal(candidates, chapterGoalPoints, random, sourceChapter.getName());
        if (selectedTasks.isEmpty()) {
            throw new IllegalStateException(
                "Chapter '" + sourceChapter.getName() + "' has no selectable tasks for the configured goal points."
            );
        }

        return new Chapter(sourceChapter.getName(), selectedTasks);
    }

    /**
     * Filter the chapter tasks according to the active generation mode.
     */
    private List<Task> modeFilteredTasks(final Chapter chapter, final GenerationMode mode) {
        List<Task> result = new ArrayList<>();
        for (Task task : chapter.getTasks()) {
            if (isTaskIncludedForMode(task, mode)) {
                result.add(task);
            }
        }
        return result;
    }

    /**
     * Clone all tasks and reduce each one to a single randomly selected variant.
     */
    private List<Task> cloneAllTasksWithSingleVariant(final List<Task> sourceTasks, final Random random) {
        List<Task> cloned = new ArrayList<>();
        for (Task task : sourceTasks) {
            cloned.add(cloneTaskWithSingleVariant(task, random));
        }
        return cloned;
    }

    /**
     * Resolve the target chapter points using either a configured override or
     * a default based on the available candidate tasks.
     */
    private double resolveChapterGoalPoints(final int chapterIndex, final List<Task> candidates) {
        Double configured = generationChapterGoalPoints.get(chapterIndex);
        if (configured != null && configured > 0d) {
            return configured;
        }
        return Math.max(0.5d, normalizeHalfPoint(totalPoints(candidates)));
    }

    /**
     * Select a subset of tasks from candidates whose total points best
     * match the requested goalPoints. The algorithm treats points as
     * discrete units (half-point granularity) and computes reachable
     * sums using a dynamic programming style subset enumeration.
     */
    private List<Task> selectTasksForGoal(
        final List<Task> candidates,
        final double goalPoints,
        final Random random,
        final String chapterName
    ) {
        List<Task> shuffledCandidates = new ArrayList<>(candidates);
        Collections.shuffle(shuffledCandidates, random);

        // Map achievable sum (in point units) -> example subset producing that sum
        Map<Integer, List<Task>> subsetByUnits = new HashMap<>();
        subsetByUnits.put(0, List.of());

        for (Task task : shuffledCandidates) {
            final int taskUnits = toPointUnits(task.getPoints());
            if (taskUnits <= 0) {
                continue;
            }

            Map<Integer, List<Task>> next = new HashMap<>(subsetByUnits);
            for (Map.Entry<Integer, List<Task>> entry : subsetByUnits.entrySet()) {
                int sumUnits = entry.getKey() + taskUnits;
                if (next.containsKey(sumUnits)) {
                    continue;
                }
                List<Task> subset = new ArrayList<>(entry.getValue());
                subset.add(task);
                next.put(sumUnits, subset);
            }
            subsetByUnits = next;
        }

        final int targetUnits = toPointUnits(goalPoints);
        final Integer resolvedUnits = resolveTargetUnits(subsetByUnits.keySet(), targetUnits);
        if (resolvedUnits == null || resolvedUnits == 0) {
            throw new IllegalStateException(
                "Chapter '" + chapterName + "' has no achievable positive point total for goal " + formatPoints(goalPoints) + "."
            );
        }

        List<Task> subset = subsetByUnits.get(resolvedUnits);
        if (subset == null || subset.isEmpty()) {
            throw new IllegalStateException(
                "Chapter '" + chapterName + "' could not resolve tasks for goal " + formatPoints(goalPoints) + "."
            );
        }

        List<Task> selected = new ArrayList<>();
        for (Task task : subset) {
            selected.add(cloneTaskWithSingleVariant(task, random));
        }
        return selected;
    }

    private Integer resolveTargetUnits(final java.util.Set<Integer> sums, final int targetUnits) {
        // Prefer an exact match when available
        if (sums.contains(targetUnits)) {
            return targetUnits;
        }

        Integer nearestLower = null;
        Integer nearestHigher = null;

        for (Integer sum : sums) {
            if (sum <= 0) {
                continue;
            }
            if (sum < targetUnits && (nearestLower == null || sum > nearestLower)) {
                nearestLower = sum;
            }
            if (sum > targetUnits && (nearestHigher == null || sum < nearestHigher)) {
                nearestHigher = sum;
            }
        }

        // Apply configured fallback preference when no exact sum is found.
        if (goalPointFallbackPreference == GoalPointFallbackPreference.LOWER) {
            return nearestLower != null ? nearestLower : nearestHigher;
        }
        return nearestHigher != null ? nearestHigher : nearestLower;
    }

    /**
     * Clone a task and retain only one variant selected at random.
     */
    private Task cloneTaskWithSingleVariant(final Task sourceTask, final Random random) {
        if (sourceTask.getVariants().isEmpty()) {
            throw new IllegalStateException("Task '" + sourceTask.getName() + "' has no variants.");
        }

        int variantIndex = random.nextInt(sourceTask.variantCount());
        Variant selectedVariant = sourceTask.variantAt(variantIndex);
        Variant variantCopy = new Variant(selectedVariant.getQuestion(), selectedVariant.getAnswer());
        return new Task(
            sourceTask.getName(),
            sourceTask.getPoints(),
            sourceTask.getDifficulty(),
            sourceTask.getScope(),
            List.of(variantCopy)
        );
    }

    /**
     * Recompute default chapter point targets for the current generation
     * selection.
     */
    private void resetGenerationGoalPoints() {
        generationChapterGoalPoints.clear();
        for (int chapterIndex = 0; chapterIndex < currentExam.chapterCount(); chapterIndex++) {
            Chapter chapter = currentExam.chapterAt(chapterIndex);
            List<Task> examScopeTasks = new ArrayList<>();
            List<Task> allTasks = new ArrayList<>();
            for (Task task : chapter.getTasks()) {
                allTasks.add(task);
                if (task.getScope() == Scope.EXAM) {
                    examScopeTasks.add(task);
                }
            }
            double defaultPoints = !examScopeTasks.isEmpty()
                ? balancedDefaultGoalPoints(examScopeTasks)
                : balancedDefaultGoalPoints(allTasks);
            generationChapterGoalPoints.put(chapterIndex, Math.max(0.5d, normalizeHalfPoint(defaultPoints)));
        }
    }

    /**
     * Determine a reasonable default goal point total for a chapter.
     *
     * For small task sets the method enumerates subsets and prefers a subset
     * with the most balanced difficulty distribution.
     */
    private double balancedDefaultGoalPoints(final List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0.5d;
        }

        if (tasks.size() > 20) {
            return Math.max(0.5d, normalizeHalfPoint(totalPoints(tasks)));
        }

        final int taskCount = tasks.size();
        final int subsetLimit = 1 << taskCount;

        double bestScore = Double.MAX_VALUE;
        int bestPointsUnits = 0;

        // Evaluate every non-empty subset of tasks (bitmask enumeration).
        // For each subset compute a simple imbalance score based on the
        // deviation from the target difficulty ratio (easy/medium/hard).
        // Keep the subset with minimal imbalance; in tie cases prefer the
        // subset with the larger point sum to maximize coverage.
        for (int mask = 1; mask < subsetLimit; mask++) {
            int easy = 0;
            int medium = 0;
            int hard = 0;
            int selected = 0;
            int pointsUnits = 0;

            for (int i = 0; i < taskCount; i++) {
                if ((mask & (1 << i)) == 0) {
                    continue;
                }

                Task task = tasks.get(i);
                selected++;
                pointsUnits += toPointUnits(task.getPoints());

                Difficulty difficulty = task.getDifficulty();
                if (difficulty == Difficulty.EASY) {
                    easy++;
                } else if (difficulty == Difficulty.MEDIUM) {
                    medium++;
                } else if (difficulty == Difficulty.HARD) {
                    hard++;
                }
            }

            if (selected == 0 || pointsUnits <= 0) {
                continue;
            }

            double score = difficultyImbalanceScore(easy, medium, hard, selected);
            if (score < bestScore || (Math.abs(score - bestScore) < 1e-9 && pointsUnits > bestPointsUnits)) {
                bestScore = score;
                bestPointsUnits = pointsUnits;
            }
        }

        if (bestPointsUnits <= 0) {
            return Math.max(0.5d, normalizeHalfPoint(totalPoints(tasks)));
        }
        return bestPointsUnits / (double) POINT_SCALE;
    }

    /**
     * Score how far a subset deviates from the target difficulty ratio.
     */
    private double difficultyImbalanceScore(final int easy, final int medium, final int hard, final int total) {
        if (total <= 0) {
            return Double.MAX_VALUE;
        }
        double easyRatio = easy / (double) total;
        double mediumRatio = medium / (double) total;
        double hardRatio = hard / (double) total;
        return Math.abs(easyRatio - DIFFICULTY_TARGET_RATIO)
            + Math.abs(mediumRatio - DIFFICULTY_TARGET_RATIO)
            + Math.abs(hardRatio - DIFFICULTY_TARGET_RATIO);
    }

    /**
     * Sum the points of the provided tasks.
     */
    private double totalPoints(final List<Task> tasks) {
        double total = 0d;
        for (Task task : tasks) {
            total += task.getPoints();
        }
        return total;
    }

    /**
     * Convert a point value to integer half-point units.
     */
    private int toPointUnits(final double points) {
        return (int) Math.round(points * POINT_SCALE);
    }

    /**
     * Round points to the nearest half point.
     */
    private double normalizeHalfPoint(final double points) {
        return Math.round(points * POINT_SCALE) / (double) POINT_SCALE;
    }

    /**
     * Format points using one decimal place for user-facing text.
     */
    private String formatPoints(final double points) {
        return String.format(java.util.Locale.ROOT, "%.1f", points);
    }

    /**
     * Create the random source used for generation.
     *
     * If a seed is configured the result is deterministic; otherwise a fresh
     * seed from the current time is used. A mode-specific offset keeps exam and
     * mock-exam exports different even with the same base seed.
     */
    private Random generationRandom(final GenerationMode mode) {
        long baseSeed = generationRandomSeed == null ? System.nanoTime() : generationRandomSeed;
        long modeOffset = mode == GenerationMode.MOCK_EXAM ? 31L : 17L;
        return new Random(baseSeed + modeOffset);
    }

    /**
     * Derive the output path for the solution PDF from the primary path.
     */
    private Path solutionOutputPath(final Path primaryPath) {
        String fileName = primaryPath.getFileName() == null ? "output.pdf" : primaryPath.getFileName().toString();
        String solutionName;
        if (fileName.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            solutionName = fileName.substring(0, fileName.length() - 4) + "_solutions.pdf";
        } else {
            solutionName = fileName + "_solutions.pdf";
        }

        Path parent = primaryPath.getParent();
        return parent == null ? Path.of(solutionName) : parent.resolve(solutionName);
    }

    /**
     * Create the default placeholder task used in new exams and chapters.
     */
    private Task defaultTask() {
        return new Task(
            "New Subtask",
            1.0,
            Difficulty.EASY,
            Scope.EXAM,
            List.of(new Variant("New Question", "New Answer"))
        );
    }
}
