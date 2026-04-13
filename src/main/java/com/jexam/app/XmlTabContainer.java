package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
import com.jexam.app.ui.components.BreadcrumbNavigation;
import com.jexam.app.ui.components.DeleteConfirmationDialog;
import com.jexam.app.ui.components.TreeViewWithFilter;
import com.jexam.app.ui.components.XmlLoadingState;
import com.jexam.app.ui.components.xml.ChapterHeaderEditor;
import com.jexam.app.ui.components.xml.ChapterTableComponent;
import com.jexam.app.ui.components.xml.ExamHeaderEditor;
import com.jexam.app.ui.components.xml.TaskHeaderEditor;
import com.jexam.app.ui.components.xml.TaskTableComponent;
import com.jexam.app.ui.components.xml.VariantEditorComponent;
import com.jexam.app.ui.components.xml.VariantListComponent;
import com.jexam.model.Chapter;
import com.jexam.model.Exam;
import com.jexam.model.Task;
import com.jexam.model.Variant;
import com.jexam.model.enums.Difficulty;
import com.jexam.model.enums.Scope;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Orientation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * XML tab shell that wires the existing app service to chapter/task/variant editors.
 */
public final class XmlTabContainer extends BorderPane {
    private static final int MAX_TREE_LABEL_LENGTH = 42;

    private final ExamApplicationService appService;
    private final JExamUiSupport ui;
    private final JExamSelectionModel selectionModel;
    private final UiStateManager uiStateManager;

    private final XmlLoadingState loadingState = new XmlLoadingState();
    private final ExamHeaderEditor examHeaderEditor = new ExamHeaderEditor();
    private final BreadcrumbNavigation breadcrumbNavigation = new BreadcrumbNavigation();
    private final TreeViewWithFilter<NavigationNode> navigationTree = new TreeViewWithFilter<>(NavigationNode::label);
    private final ChapterTableComponent chapterTable = new ChapterTableComponent();
    private final ChapterHeaderEditor chapterHeaderEditor = new ChapterHeaderEditor();
    private final TaskTableComponent taskTable = new TaskTableComponent();
    private final TaskHeaderEditor taskHeaderEditor = new TaskHeaderEditor();
    private final VariantListComponent variantList = new VariantListComponent();
    private final VariantEditorComponent variantEditor = new VariantEditorComponent();

    private final VBox chapterEditorPane = new VBox(8);
    private final VBox taskEditorPane = new VBox(8);
    private final VBox variantEditorPane = new VBox(8);
    private final VBox leftColumn = new VBox(8, navigationTree);
    private final VBox rightColumn = new VBox(
        10,
        breadcrumbNavigation,
        examHeaderEditor,
        chapterHeaderEditor,
        taskHeaderEditor,
        variantEditor,
        chapterTable,
        taskTable,
        variantList
    );
    private final SplitPane contentSplit = new SplitPane(leftColumn, rightColumn);
    private final StackPane centerStack = new StackPane();

    private Consumer<Boolean> dirtyStateChangedHandler = value -> { };
    private Consumer<String> examNameChangedHandler = value -> { };
    private Runnable onCreateNewExam = () -> { };
    private Runnable onLoadXml = () -> { };

    private int selectedChapterIndex = -1;
    private int selectedTaskIndex = -1;
    private int selectedVariantIndex = -1;
    private boolean syncingNavigation;

    /**
     * Creates the XML tab container and wires all XML editing components.
     *
     * @param appService application service facade
     * @param ui UI support and localization helper
     * @param selectionModel hierarchical selection model
     * @param uiStateManager shared UI state manager
     */
    public XmlTabContainer(
        final ExamApplicationService appService,
        final JExamUiSupport ui,
        final JExamSelectionModel selectionModel,
        final UiStateManager uiStateManager
    ) {
        this.appService = Objects.requireNonNull(appService, "appService");
        this.ui = Objects.requireNonNull(ui, "ui");
        this.selectionModel = Objects.requireNonNull(selectionModel, "selectionModel");
        this.uiStateManager = Objects.requireNonNull(uiStateManager, "uiStateManager");

        getStyleClass().add("xml-tab");

        setPadding(new Insets(8));

        configureLoadingState();
        configureNavigationTreeTexts();
        configureHeaderEditors();
        configureTables();
        configureNavigation();
        ui.addLanguageChangeListener(() -> {
            configureLoadingState();
            configureNavigationTreeTexts();
        });
        VBox.setVgrow(navigationTree, Priority.ALWAYS);
        VBox.setVgrow(leftColumn, Priority.ALWAYS);
        VBox.setVgrow(chapterTable, Priority.ALWAYS);
        VBox.setVgrow(taskTable, Priority.ALWAYS);
        VBox.setVgrow(variantList, Priority.ALWAYS);
        rightColumn.setFillWidth(true);
        rightColumn.setPadding(new Insets(4, 6, 4, 6));
        leftColumn.setPrefWidth(320);
        rightColumn.setPrefWidth(760);
        rightColumn.setMinHeight(0);
        contentSplit.setDividerPositions(0.28);
        centerStack.getChildren().addAll(contentSplit, loadingState);

        setCenter(centerStack);

        refreshFromService();
    }

    /**
     * Switches the split-pane orientation and sizes based on available width.
     *
     * @param width current scene width in pixels
     */
    public void updateLayout(final double width) {
        if (width < 1024) {
            contentSplit.setOrientation(Orientation.VERTICAL);
            contentSplit.setDividerPositions(0.40);
            leftColumn.setPrefWidth(Double.MAX_VALUE);
            rightColumn.setPrefWidth(Double.MAX_VALUE);
        } else {
            contentSplit.setOrientation(Orientation.HORIZONTAL);
            contentSplit.setDividerPositions(width >= 1400 ? 0.24 : 0.28);
            leftColumn.setPrefWidth(320);
            rightColumn.setPrefWidth(760);
        }
    }

    /**
     * Registers a callback for dirty-state changes caused by edit operations.
     *
     * @param handler callback receiving dirty-state changes; {@code null} resets to no-op
     */
    public void setOnDirtyStateChanged(final Consumer<Boolean> handler) {
        dirtyStateChangedHandler = handler == null ? value -> { } : handler;
    }

    /**
     * Registers a callback for exam-name changes.
     *
     * @param handler callback receiving the current exam name; {@code null} resets to no-op
     */
    public void setOnExamNameChanged(final Consumer<String> handler) {
        examNameChangedHandler = handler == null ? value -> { } : handler;
    }

    /**
     * Registers the create-exam action used by the empty/loading state.
     *
     * @param handler callback invoked when the user requests a new exam; {@code null} resets to no-op
     */
    public void setOnCreateNewExam(final Runnable handler) {
        onCreateNewExam = handler == null ? () -> { } : handler;
        loadingState.setOnCreateNewExam(() -> {
            onCreateNewExam.run();
            appService.newExam();
            refreshFromService();
            markSaved();
        });
    }

    /**
     * Registers the load-XML action used by the empty/loading state.
     *
     * @param handler callback invoked when the user requests XML loading; {@code null} resets to no-op
     */
    public void setOnLoadXml(final Runnable handler) {
        onLoadXml = handler == null ? () -> { } : handler;
        loadingState.setOnLoadXml(() -> onLoadXml.run());
    }

    /**
     * Requests keyboard focus for the left navigation tree.
     */
    public void focusNavigationTree() {
        navigationTree.requestTreeFocus();
    }

    /**
     * Moves focus to the most specific editor matching the current selection depth.
     */
    private void focusActiveContent() {
        if (selectedVariantIndex >= 0) {
            variantEditor.requestEditorFocus();
            return;
        }
        if (selectedTaskIndex >= 0) {
            taskHeaderEditor.requestEditorFocus();
            return;
        }
        if (selectedChapterIndex >= 0) {
            chapterHeaderEditor.requestEditorFocus();
            return;
        }
        examHeaderEditor.requestEditorFocus();
    }

    /**
     * Reloads tab state from the application service and updates editor visibility and selection.
     */
    public void refreshFromService() {
        Exam currentExam = appService.getCurrentExam();
        selectionModel.setExam(currentExam);

        boolean hasExam = currentExam != null;
        loadingState.setVisible(!hasExam);
        loadingState.setManaged(!hasExam);
        centerStack.getChildren().get(0).setVisible(hasExam);
        centerStack.getChildren().get(0).setManaged(hasExam);

        if (!hasExam) {
            examNameChangedHandler.accept("");
            return;
        }

        examHeaderEditor.setExamName(currentExam.getName());
        examNameChangedHandler.accept(currentExam.getName());

        chapterTable.setItems(currentExam.getChapters().stream().map(this::chapterRowLabel).toList());
        chapterTable.setSelectedIndex(selectedChapterIndex);
        refreshNavigationTree();
        refreshChapterSelection();
        refreshBreadcrumb();
        refreshContentVisibility();
        focusActiveContent();
    }

    /**
     * Marks the current state as saved and notifies dirty-state listeners.
     */
    public void markSaved() {
        uiStateManager.markSaved();
        dirtyStateChangedHandler.accept(false);
    }

    /**
     * Navigates to the nearest valid chapter/task/variant selection indices.
     *
     * @param chapterIndex target chapter index
     * @param taskIndex target task index
     * @param variantIndex target variant index
     */
    public void navigateToSelection(final int chapterIndex, final int taskIndex, final int variantIndex) {
        if (selectionModel.chapterCount() == 0) {
            return;
        }

        int boundedChapter = Math.max(0, Math.min(chapterIndex, selectionModel.lastChapterIndex()));
        selectedChapterIndex = boundedChapter;

        int boundedTask = -1;
        if (taskIndex >= 0 && selectionModel.taskCount(boundedChapter) > 0) {
            boundedTask = Math.min(taskIndex, selectionModel.lastTaskIndex(boundedChapter));
        }
        selectedTaskIndex = boundedTask;

        int boundedVariant = -1;
        if (boundedTask >= 0 && variantIndex >= 0 && selectionModel.variantCount(boundedChapter, boundedTask) > 0) {
            boundedVariant = Math.min(variantIndex, selectionModel.lastVariantIndex(boundedChapter, boundedTask));
        }
        selectedVariantIndex = boundedVariant;

        refreshFromService();
    }

    /**
     * Navigates one hierarchy level up within the XML tab selection.
     */
    public void navigateOneLevelUp() {
        if (selectedVariantIndex >= 0) {
            selectedVariantIndex = -1;
        } else if (selectedTaskIndex >= 0) {
            selectedTaskIndex = -1;
        } else if (selectedChapterIndex >= 0) {
            selectedChapterIndex = -1;
        } else {
            return;
        }
        refreshFromService();
    }

    /**
     * Binds breadcrumb and navigation-tree selection handlers.
     */
    private void configureNavigation() {
        breadcrumbNavigation.setOnSegmentClicked(segmentIndex -> {
            if (segmentIndex <= 0) {
                selectedChapterIndex = -1;
                selectedTaskIndex = -1;
                selectedVariantIndex = -1;
                refreshFromService();
                return;
            }
            if (segmentIndex == 1) {
                selectedTaskIndex = -1;
                selectedVariantIndex = -1;
                refreshFromService();
                return;
            }
            if (segmentIndex == 2) {
                selectedVariantIndex = -1;
                refreshFromService();
            }
        });

        navigationTree.setOnItemSelected(node -> {
            if (syncingNavigation || node == null) {
                return;
            }

            syncingNavigation = true;
            try {
                if (node.type == NavigationType.EXAM) {
                    selectedChapterIndex = -1;
                    selectedTaskIndex = -1;
                    selectedVariantIndex = -1;
                } else if (node.type == NavigationType.CHAPTER) {
                    selectedChapterIndex = node.chapterIndex;
                    selectedTaskIndex = -1;
                    selectedVariantIndex = -1;
                } else if (node.type == NavigationType.TASK) {
                    selectedChapterIndex = node.chapterIndex;
                    selectedTaskIndex = node.taskIndex;
                    selectedVariantIndex = -1;
                }
                refreshFromService();
                focusActiveContent();
            } finally {
                syncingNavigation = false;
            }
        });
    }

    /**
     * Applies localized strings to the XML empty/loading state panel.
     */
    private void configureLoadingState() {
        loadingState.setTitleText(ui.text("label.xml.empty.title"));
        loadingState.setSubtitleText(ui.text("label.xml.empty.subtitle"));
        loadingState.setCreateButtonText(ui.text("button.create.exam"));
        loadingState.setLoadButtonText(ui.text("button.load.xml"));
    }

    /**
     * Applies localized texts for tree header controls and filter prompt.
     */
    private void configureNavigationTreeTexts() {
        navigationTree.setHeaderTexts(
            ui.text("tree.filter.prompt"),
            ui.text("tree.filter.accessible"),
            ui.text("button.expand.all"),
            ui.text("button.collapse.all")
        );
    }

    /**
     * Connects exam/chapter/task/variant editor change events to service updates.
     */
    private void configureHeaderEditors() {
        examHeaderEditor.setOnChange(() -> {
            appService.getCurrentExam().setName(examHeaderEditor.getExamName());
            examNameChangedHandler.accept(examHeaderEditor.getExamName());
            refreshNavigationTree();
            refreshBreadcrumb();
            markDirty();
        });

        chapterHeaderEditor.setOnChange(() -> {
            if (selectedChapterIndex >= 0) {
                appService.getCurrentExam().chapterAt(selectedChapterIndex).setName(chapterHeaderEditor.getChapterName());
                chapterTable.setItems(appService.getCurrentExam().getChapters().stream().map(this::chapterRowLabel).toList());
                refreshNavigationTree();
                refreshBreadcrumb();
                markDirty();
            }
        });

        taskHeaderEditor.setOnChange(() -> {
            if (selectedChapterIndex >= 0 && selectedTaskIndex >= 0) {
                try {
                    double points = Double.parseDouble(taskHeaderEditor.getPointsText().trim());
                    if (points <= 0 || !isHalfStep(points)) {
                        return;
                    }
                    appService.updateTaskDetails(
                        selectedChapterIndex,
                        selectedTaskIndex,
                        taskHeaderEditor.getTaskName(),
                        points,
                        taskHeaderEditor.getDifficulty(),
                        taskHeaderEditor.getScope()
                    );
                    refreshTaskSelection();
                    refreshNavigationTree();
                    refreshBreadcrumb();
                    markDirty();
                } catch (NumberFormatException ignored) {
                    // keep invalid draft visible; save-on-change waits for valid input
                }
            }
        });

        variantEditor.setOnChange(() -> {
            if (selectedChapterIndex >= 0 && selectedTaskIndex >= 0 && selectedVariantIndex >= 0) {
                appService.updateVariantDetails(
                    selectedChapterIndex,
                    selectedTaskIndex,
                    selectedVariantIndex,
                    variantEditor.getQuestionText(),
                    variantEditor.getAnswerText()
                );
                refreshVariantSelection();
                refreshBreadcrumb();
                markDirty();
            }
        });
    }

    /**
     * Connects chapter/task/variant table and list actions to CRUD operations.
     */
    private void configureTables() {
        chapterTable.setOnSelect(index -> {
            selectedChapterIndex = index;
            selectedTaskIndex = -1;
            selectedVariantIndex = -1;
            refreshChapterSelection();
            refreshNavigationSelection();
            refreshBreadcrumb();
            refreshContentVisibility();
        });
        chapterTable.setOnCreate(() -> {
            appService.addChapter("New Chapter");
            selectedChapterIndex = appService.getCurrentExam().chapterCount() - 1;
            refreshFromService();
            markDirty();
        });
        chapterTable.setOnDelete(index -> {
            if (appService.getCurrentExam().chapterCount() <= 1) {
                ui.showError("Cannot remove chapter", "At least one chapter must remain.");
                return;
            }
            if (DeleteConfirmationDialog.confirm(null, "Remove chapter", "Remove selected chapter?", "This will remove all tasks in the chapter.")) {
                appService.removeChapter(index);
                selectedChapterIndex = -1;
                selectedTaskIndex = -1;
                selectedVariantIndex = -1;
                refreshFromService();
                markDirty();
            }
        });

        taskTable.setOnSelect(index -> {
            selectedTaskIndex = index;
            selectedVariantIndex = -1;
            refreshTaskSelection();
            refreshNavigationSelection();
            refreshBreadcrumb();
            refreshContentVisibility();
        });
        taskTable.setOnCreate(() -> {
            if (selectedChapterIndex < 0) {
                ui.showError("No chapter selected", "Select a chapter before adding a task.");
                return;
            }
            appService.addTask(selectedChapterIndex, "New Task");
            selectedTaskIndex = appService.getCurrentExam().chapterAt(selectedChapterIndex).taskCount() - 1;
            refreshFromService();
            markDirty();
        });
        taskTable.setOnDelete(index -> {
            if (selectedChapterIndex < 0) {
                return;
            }
            if (appService.getCurrentExam().chapterAt(selectedChapterIndex).taskCount() <= 1) {
                ui.showError("Cannot remove task", "At least one task must remain per chapter.");
                return;
            }
            if (DeleteConfirmationDialog.confirm(null, "Remove task", "Remove selected task?", "This will remove all variants in the task.")) {
                appService.removeTask(selectedChapterIndex, index);
                selectedTaskIndex = -1;
                selectedVariantIndex = -1;
                refreshFromService();
                markDirty();
            }
        });

        variantList.setOnSelect(index -> {
            selectedVariantIndex = index;
            refreshVariantSelection();
            refreshNavigationSelection();
            refreshBreadcrumb();
            refreshContentVisibility();
            focusActiveContent();
        });
        variantList.setOnCreate(() -> {
            if (selectedChapterIndex < 0 || selectedTaskIndex < 0) {
                ui.showError("No task selected", "Select a task before adding a variant.");
                return;
            }
            appService.addVariant(selectedChapterIndex, selectedTaskIndex);
            selectedVariantIndex = appService.getCurrentExam().taskAt(selectedChapterIndex, selectedTaskIndex).variantCount() - 1;
            refreshFromService();
            markDirty();
        });
        variantList.setOnDelete(index -> {
            if (selectedChapterIndex < 0 || selectedTaskIndex < 0) {
                return;
            }
            if (appService.getCurrentExam().taskAt(selectedChapterIndex, selectedTaskIndex).variantCount() <= 1) {
                ui.showError("Cannot remove variant", "Each task must have at least one variant.");
                return;
            }
            if (DeleteConfirmationDialog.confirm(null, "Remove variant", "Remove selected variant?", "This will remove the variant text immediately.")) {
                appService.removeVariant(selectedChapterIndex, selectedTaskIndex, index);
                selectedVariantIndex = -1;
                refreshFromService();
                markDirty();
            }
        });

        chapterTable.setOnEnter(this::focusActiveContent);
        chapterTable.setOnTabNavigation(this::focusActiveContent, this::focusNavigationTree);
        taskTable.setOnEnter(this::focusActiveContent);
        taskTable.setOnTabNavigation(this::focusActiveContent, chapterTable::requestTableFocus);
        variantList.setOnEnter(this::focusActiveContent);
        variantList.setOnTabNavigation(this::focusActiveContent, taskTable::requestTableFocus);
    }

    /**
     * Refreshes UI state for the currently selected chapter and dependent task content.
     */
    private void refreshChapterSelection() {
        Chapter chapter = selectionModel.chapterAt(selectedChapterIndex);
        if (chapter == null) {
            taskTable.setItems(List.of());
            taskTable.setSelectedIndex(-1);
            chapterHeaderEditor.clear();
            taskHeaderEditor.clear();
            variantList.setItems(List.of());
            variantList.setSelectedIndex(-1);
            variantEditor.clear();
            refreshNavigationSelection();
            refreshContentVisibility();
            return;
        }

        chapterHeaderEditor.setChapterName(chapter.getName());
        taskTable.setItems(chapter.getTasks().stream().map(this::taskRowLabel).toList());
        taskTable.setSelectedIndex(selectedTaskIndex);
        refreshTaskSelection();
        refreshNavigationSelection();
        refreshContentVisibility();
    }

    /**
     * Refreshes UI state for the currently selected task and dependent variant content.
     */
    private void refreshTaskSelection() {
        Task task = selectionModel.taskAt(selectedChapterIndex, selectedTaskIndex);
        if (task == null) {
            taskHeaderEditor.clear();
            variantList.setItems(List.of());
            variantList.setSelectedIndex(-1);
            variantEditor.clear();
            refreshNavigationSelection();
            refreshContentVisibility();
            return;
        }

        taskHeaderEditor.setTaskName(task.getName());
        taskHeaderEditor.setPoints(task.getPoints());
        taskHeaderEditor.setDifficulty(task.getDifficulty());
        taskHeaderEditor.setScope(task.getScope());
        variantList.setItems(task.getVariants().stream().map(Variant::getQuestion).toList());
        variantList.setSelectedIndex(selectedVariantIndex);
        refreshVariantSelection();
        refreshNavigationSelection();
        refreshContentVisibility();
    }

    /**
     * Refreshes the variant editor from the currently selected variant.
     */
    private void refreshVariantSelection() {
        Variant variant = selectionModel.variantAt(selectedChapterIndex, selectedTaskIndex, selectedVariantIndex);
        if (variant == null) {
            variantEditor.clear();
            return;
        }

        variantEditor.setQuestionText(variant.getQuestion());
        variantEditor.setAnswerText(variant.getAnswer());
    }

    /**
     * Marks XML state as dirty and preview as stale, then notifies listeners.
     */
    private void markDirty() {
        uiStateManager.markDirty();
        uiStateManager.markPreviewStale();
        dirtyStateChangedHandler.accept(true);
    }

    private boolean isHalfStep(final double points) {
        final double scaled = points * 2.0;
        return Math.abs(scaled - Math.rint(scaled)) < 1e-9;
    }

    /**
     * Rebuilds the navigation tree from the current exam hierarchy.
     */
    private void refreshNavigationTree() {
        Exam exam = appService.getCurrentExam();
        if (exam == null) {
            navigationTree.setRootItem(null);
            return;
        }

        TreeItem<NavigationNode> root = new TreeItem<>(NavigationNode.exam(shortenTreeLabel(exam.getName())));
        root.setExpanded(true);

        for (int chapterIndex = 0; chapterIndex < exam.chapterCount(); chapterIndex++) {
            Chapter chapter = exam.chapterAt(chapterIndex);
            TreeItem<NavigationNode> chapterNode = new TreeItem<>(
                NavigationNode.chapter(chapterIndex, shortenTreeLabel(chapter.getName()))
            );
            chapterNode.setExpanded(true);
            for (int taskIndex = 0; taskIndex < chapter.taskCount(); taskIndex++) {
                Task task = chapter.taskAt(taskIndex);
                chapterNode.getChildren().add(
                    new TreeItem<>(NavigationNode.task(chapterIndex, taskIndex, shortenTreeLabel(task.getName())))
                );
            }
            root.getChildren().add(chapterNode);
        }

        navigationTree.setRootItem(root);
        refreshNavigationSelection();
    }

    /**
     * Updates navigation tree selection to mirror current chapter/task selection.
     */
    private void refreshNavigationSelection() {
        if (selectedTaskIndex >= 0) {
            navigationTree.setSelectedItem(NavigationNode.task(selectedChapterIndex, selectedTaskIndex, ""));
            return;
        }
        if (selectedChapterIndex >= 0) {
            navigationTree.setSelectedItem(NavigationNode.chapter(selectedChapterIndex, ""));
            return;
        }
        navigationTree.setSelectedItem(NavigationNode.exam(""));
    }

    /**
     * Builds and applies breadcrumb labels for exam, chapter, task, and variant levels.
     */
    private void refreshBreadcrumb() {
        List<String> segments = new ArrayList<>();
        segments.add("Exam");

        Chapter chapter = selectionModel.chapterAt(selectedChapterIndex);
        if (chapter != null) {
            segments.add(chapter.getName());
        }
        Task task = selectionModel.taskAt(selectedChapterIndex, selectedTaskIndex);
        if (task != null) {
            segments.add(task.getName());
        }
        Variant variant = selectionModel.variantAt(selectedChapterIndex, selectedTaskIndex, selectedVariantIndex);
        if (variant != null) {
            String label = variant.getQuestion();
            if (label == null || label.isBlank()) {
                label = "Variant " + (selectedVariantIndex + 1);
            }
            segments.add(label);
        }
        breadcrumbNavigation.setPath(segments);
    }

    /**
     * Shows only the editor sections relevant to the current selection depth.
     */
    private void refreshContentVisibility() {
        boolean hasExam = appService.getCurrentExam() != null;

        if (!hasExam) {
            setSectionVisible(examHeaderEditor, false);
            setSectionVisible(chapterHeaderEditor, false);
            setSectionVisible(taskHeaderEditor, false);
            setSectionVisible(variantEditor, false);
            setSectionVisible(chapterTable, false);
            setSectionVisible(taskTable, false);
            setSectionVisible(variantList, false);
            return;
        }

        if (selectedVariantIndex >= 0) {
            setSectionVisible(examHeaderEditor, false);
            setSectionVisible(chapterHeaderEditor, false);
            setSectionVisible(taskHeaderEditor, false);
            setSectionVisible(variantEditor, true);
            setSectionVisible(chapterTable, false);
            setSectionVisible(taskTable, false);
            setSectionVisible(variantList, false);
            return;
        }

        if (selectedTaskIndex >= 0) {
            setSectionVisible(examHeaderEditor, false);
            setSectionVisible(chapterHeaderEditor, false);
            setSectionVisible(taskHeaderEditor, true);
            setSectionVisible(variantEditor, false);
            setSectionVisible(chapterTable, false);
            setSectionVisible(taskTable, false);
            setSectionVisible(variantList, true);
            return;
        }

        if (selectedChapterIndex >= 0) {
            setSectionVisible(examHeaderEditor, false);
            setSectionVisible(chapterHeaderEditor, true);
            setSectionVisible(taskHeaderEditor, false);
            setSectionVisible(variantEditor, false);
            setSectionVisible(chapterTable, false);
            setSectionVisible(taskTable, true);
            setSectionVisible(variantList, false);
            return;
        }

        setSectionVisible(examHeaderEditor, true);
        setSectionVisible(chapterHeaderEditor, false);
        setSectionVisible(taskHeaderEditor, false);
        setSectionVisible(variantEditor, false);
        setSectionVisible(chapterTable, true);
        setSectionVisible(taskTable, false);
        setSectionVisible(variantList, false);
    }

    /**
     * Applies visibility and layout-management flags for a section node.
     *
     * @param node UI node to toggle
     * @param visible whether the node should be visible and managed
     */
    private void setSectionVisible(final javafx.scene.Node node, final boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Builds a chapter row label including task/variant counts, points, and distribution stats.
     *
     * @param chapter chapter to summarize
     * @return multi-line chapter label used in the chapter table
     */
    private String chapterRowLabel(final Chapter chapter) {
        List<Task> tasks = chapter.getTasks();
        int taskCount = tasks.size();
        int variantCount = tasks.stream().mapToInt(Task::variantCount).sum();
        double totalPoints = tasks.stream().mapToDouble(Task::getPoints).sum();

        Map<Difficulty, Integer> difficulties = new EnumMap<>(Difficulty.class);
        Map<Scope, Integer> scopes = new EnumMap<>(Scope.class);
        for (Task task : tasks) {
            difficulties.merge(task.getDifficulty(), 1, Integer::sum);
            scopes.merge(task.getScope(), 1, Integer::sum);
        }

        String stats = "Children " + taskCount
            + " tasks, " + variantCount + " variants"
            + " | Total points " + formatPoints(totalPoints)
            + " | Difficulty " + formatDifficultyDistribution(difficulties)
            + " | Scope " + formatScopeDistribution(scopes);
        return chapter.getName() + "\n" + stats;
    }

    /**
     * Builds a task row label including variant count, points, and taxonomy fields.
     *
     * @param task task to summarize
     * @return multi-line task label used in the task table
     */
    private String taskRowLabel(final Task task) {
        String stats = "Children " + task.variantCount()
            + " variants"
            + " | Total points " + formatPoints(task.getPoints())
            + " | Difficulty " + task.getDifficulty().toXmlValue()
            + " | Scope " + task.getScope().toXmlValue();
        return task.getName() + "\n" + stats;
    }

    /**
     * Formats numeric points consistently with one decimal place.
     *
     * @param points points value
     * @return localized-independent one-decimal string
     */
    private String formatPoints(final double points) {
        return String.format(Locale.ROOT, "%.1f", points);
    }

    /**
     * Formats difficulty distribution as easy/medium/hard counts.
     *
     * @param difficulties map of difficulty to count
     * @return compact distribution label
     */
    private String formatDifficultyDistribution(final Map<Difficulty, Integer> difficulties) {
        return Difficulty.EASY.toXmlValue() + " " + difficulties.getOrDefault(Difficulty.EASY, 0)
            + "/" + Difficulty.MEDIUM.toXmlValue() + " " + difficulties.getOrDefault(Difficulty.MEDIUM, 0)
            + "/" + Difficulty.HARD.toXmlValue() + " " + difficulties.getOrDefault(Difficulty.HARD, 0);
    }

    /**
     * Formats scope distribution as exam/mock-exam counts.
     *
     * @param scopes map of scope to count
     * @return compact distribution label
     */
    private String formatScopeDistribution(final Map<Scope, Integer> scopes) {
        return Scope.EXAM.toXmlValue() + " " + scopes.getOrDefault(Scope.EXAM, 0)
            + "/" + Scope.MOCK_EXAM.toXmlValue() + " " + scopes.getOrDefault(Scope.MOCK_EXAM, 0);
    }

    private String shortenTreeLabel(final String label) {
        if (label == null) {
            return "";
        }
        String normalized = label.trim();
        if (normalized.length() <= MAX_TREE_LABEL_LENGTH) {
            return normalized;
        }

        String candidate = normalized.substring(0, MAX_TREE_LABEL_LENGTH).trim();
        int lastWhitespace = lastWhitespaceIndex(candidate);
        if (lastWhitespace > 0) {
            candidate = candidate.substring(0, lastWhitespace).trim();
        }
        if (candidate.isEmpty()) {
            candidate = normalized.substring(0, MAX_TREE_LABEL_LENGTH).trim();
        }
        return candidate + "...";
    }

    private int lastWhitespaceIndex(final String value) {
        for (int i = value.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private enum NavigationType {
        EXAM,
        CHAPTER,
        TASK
    }

    private static final class NavigationNode {
        private final NavigationType type;
        private final int chapterIndex;
        private final int taskIndex;
        private final String label;

        /**
         * Creates a navigation node for the tree hierarchy.
         *
         * @param type navigation depth type
         * @param chapterIndex chapter index for chapter/task nodes
         * @param taskIndex task index for task nodes
         * @param label display label
         */
        private NavigationNode(
            final NavigationType type,
            final int chapterIndex,
            final int taskIndex,
            final String label
        ) {
            this.type = type;
            this.chapterIndex = chapterIndex;
            this.taskIndex = taskIndex;
            this.label = label;
        }

        /**
         * Creates the root exam navigation node.
         *
         * @param label preferred display label
         * @return exam node
         */
        private static NavigationNode exam(final String label) {
            return new NavigationNode(NavigationType.EXAM, -1, -1, label == null || label.isBlank() ? "Exam" : label);
        }

        /**
         * Creates a chapter navigation node.
         *
         * @param chapterIndex chapter index represented by this node
         * @param label preferred display label
         * @return chapter node
         */
        private static NavigationNode chapter(final int chapterIndex, final String label) {
            return new NavigationNode(
                NavigationType.CHAPTER,
                chapterIndex,
                -1,
                label == null || label.isBlank() ? "Chapter" : label
            );
        }

        /**
         * Creates a task navigation node.
         *
         * @param chapterIndex chapter index for the task
         * @param taskIndex task index represented by this node
         * @param label preferred display label
         * @return task node
         */
        private static NavigationNode task(final int chapterIndex, final int taskIndex, final String label) {
            return new NavigationNode(
                NavigationType.TASK,
                chapterIndex,
                taskIndex,
                label == null || label.isBlank() ? "Task" : label
            );
        }

        /**
         * Returns the rendered label text for tree display.
         *
         * @return node label
         */
        private String label() {
            return label;
        }

        /**
         * Compares nodes by semantic navigation identity.
         *
         * @param o object to compare
         * @return {@code true} when type and indices match
         */
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NavigationNode that)) {
                return false;
            }
            return type == that.type
                && chapterIndex == that.chapterIndex
                && taskIndex == that.taskIndex;
        }

        /**
         * Computes hash code based on node type and hierarchy indices.
         *
         * @return stable hash for collection lookups
         */
        @Override
        public int hashCode() {
            return Objects.hash(type, chapterIndex, taskIndex);
        }
    }
}