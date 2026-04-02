package com.jexam.app;

import com.jexam.app.ui.UiStateManager;
import com.jexam.app.ui.components.DeleteConfirmationDialog;
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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * XML tab shell that wires the existing app service to chapter/task/variant editors.
 */
public final class XmlTabContainer extends BorderPane {
    private final ExamApplicationService appService;
    private final JExamUiSupport ui;
    private final JExamSelectionModel selectionModel;
    private final UiStateManager uiStateManager;

    private final XmlLoadingState loadingState = new XmlLoadingState();
    private final ExamHeaderEditor examHeaderEditor = new ExamHeaderEditor();
    private final ChapterTableComponent chapterTable = new ChapterTableComponent();
    private final ChapterHeaderEditor chapterHeaderEditor = new ChapterHeaderEditor();
    private final TaskTableComponent taskTable = new TaskTableComponent();
    private final TaskHeaderEditor taskHeaderEditor = new TaskHeaderEditor();
    private final VariantListComponent variantList = new VariantListComponent();
    private final VariantEditorComponent variantEditor = new VariantEditorComponent();

    private final VBox chapterEditorPane = new VBox(8);
    private final VBox taskEditorPane = new VBox(8);
    private final VBox variantEditorPane = new VBox(8);
    private final StackPane centerStack = new StackPane();

    private Consumer<Boolean> dirtyStateChangedHandler = value -> { };
    private Runnable onCreateNewExam = () -> { };
    private Runnable onLoadXml = () -> { };

    private int selectedChapterIndex = -1;
    private int selectedTaskIndex = -1;
    private int selectedVariantIndex = -1;

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

        setPadding(new Insets(8));

        configureLoadingState();
        configureHeaderEditors();
        configureTables();
        configureEditorPanes();

        VBox leftColumn = new VBox(10, chapterTable, taskTable, variantList);
        leftColumn.setPrefWidth(320);
        VBox centerColumn = new VBox(12, chapterEditorPane, taskEditorPane, variantEditorPane);
        VBox.setVgrow(centerColumn, Priority.ALWAYS);
        HBox content = new HBox(12, leftColumn, centerColumn);
        HBox.setHgrow(centerColumn, Priority.ALWAYS);
        centerStack.getChildren().addAll(content, loadingState);

        setTop(examHeaderEditor);
        setCenter(centerStack);

        refreshFromService();
    }

    public void setOnDirtyStateChanged(final Consumer<Boolean> handler) {
        dirtyStateChangedHandler = handler == null ? value -> { } : handler;
    }

    public void setOnCreateNewExam(final Runnable handler) {
        onCreateNewExam = handler == null ? () -> { } : handler;
        loadingState.setOnCreateNewExam(() -> {
            onCreateNewExam.run();
            appService.newExam();
            refreshFromService();
            markSaved();
        });
    }

    public void setOnLoadXml(final Runnable handler) {
        onLoadXml = handler == null ? () -> { } : handler;
        loadingState.setOnLoadXml(() -> onLoadXml.run());
    }

    public void refreshFromService() {
        Exam currentExam = appService.getCurrentExam();
        selectionModel.setExam(currentExam);

        boolean hasExam = currentExam != null;
        loadingState.setVisible(!hasExam);
        loadingState.setManaged(!hasExam);
        centerStack.getChildren().get(0).setVisible(hasExam);
        centerStack.getChildren().get(0).setManaged(hasExam);

        if (!hasExam) {
            return;
        }

        examHeaderEditor.setExamName(currentExam.getName());

        chapterTable.setItems(currentExam.getChapters().stream().map(Chapter::getName).toList());
        if (selectedChapterIndex < 0 && currentExam.chapterCount() > 0) {
            selectedChapterIndex = 0;
        }
        chapterTable.setSelectedIndex(selectedChapterIndex);
        refreshChapterSelection();
    }

    public void markSaved() {
        uiStateManager.markSaved();
        dirtyStateChangedHandler.accept(false);
    }

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

    private void configureLoadingState() {
        loadingState.setTitleText(ui.text("label.xml.empty.title"));
        loadingState.setSubtitleText(ui.text("label.xml.empty.subtitle"));
        loadingState.setCreateButtonText(ui.text("button.create.exam"));
        loadingState.setLoadButtonText(ui.text("button.load.xml"));
    }

    private void configureHeaderEditors() {
        examHeaderEditor.setOnChange(() -> {
            appService.getCurrentExam().setName(examHeaderEditor.getExamName());
            markDirty();
        });

        chapterHeaderEditor.setOnChange(() -> {
            if (selectedChapterIndex >= 0) {
                appService.getCurrentExam().chapterAt(selectedChapterIndex).setName(chapterHeaderEditor.getChapterName());
                chapterTable.setItems(appService.getCurrentExam().getChapters().stream().map(Chapter::getName).toList());
                markDirty();
            }
        });

        taskHeaderEditor.setOnChange(() -> {
            if (selectedChapterIndex >= 0 && selectedTaskIndex >= 0) {
                try {
                    double points = Double.parseDouble(taskHeaderEditor.getPointsText().trim());
                    if (points <= 0) {
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
                markDirty();
            }
        });
    }

    private void configureTables() {
        chapterTable.setOnSelect(index -> {
            selectedChapterIndex = index;
            selectedTaskIndex = -1;
            selectedVariantIndex = -1;
            refreshChapterSelection();
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
                selectedChapterIndex = Math.min(index, appService.getCurrentExam().chapterCount() - 1);
                refreshFromService();
                markDirty();
            }
        });

        taskTable.setOnSelect(index -> {
            selectedTaskIndex = index;
            selectedVariantIndex = -1;
            refreshTaskSelection();
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
                selectedTaskIndex = Math.min(index, appService.getCurrentExam().chapterAt(selectedChapterIndex).taskCount() - 1);
                refreshFromService();
                markDirty();
            }
        });

        variantList.setOnSelect(index -> {
            selectedVariantIndex = index;
            refreshVariantSelection();
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
                selectedVariantIndex = Math.min(index, appService.getCurrentExam().taskAt(selectedChapterIndex, selectedTaskIndex).variantCount() - 1);
                refreshFromService();
                markDirty();
            }
        });
    }

    private void configureEditorPanes() {
        chapterEditorPane.getChildren().addAll(new Label("Chapter"), chapterHeaderEditor);
        taskEditorPane.getChildren().addAll(new Label("Task"), taskHeaderEditor);
        variantEditorPane.getChildren().addAll(new Label("Variant"), variantEditor);
    }

    private void refreshChapterSelection() {
        Chapter chapter = selectionModel.chapterAt(selectedChapterIndex);
        if (chapter == null) {
            taskTable.setItems(List.of());
            chapterHeaderEditor.clear();
            taskHeaderEditor.clear();
            variantList.setItems(List.of());
            variantEditor.clear();
            return;
        }

        chapterHeaderEditor.setChapterName(chapter.getName());
        taskTable.setItems(chapter.getTasks().stream().map(selectionModel::taskLabel).toList());
        if (selectedTaskIndex < 0 && chapter.taskCount() > 0) {
            selectedTaskIndex = 0;
        }
        taskTable.setSelectedIndex(selectedTaskIndex);
        refreshTaskSelection();
    }

    private void refreshTaskSelection() {
        Task task = selectionModel.taskAt(selectedChapterIndex, selectedTaskIndex);
        if (task == null) {
            taskHeaderEditor.clear();
            variantList.setItems(List.of());
            variantEditor.clear();
            return;
        }

        taskHeaderEditor.setTaskName(task.getName());
        taskHeaderEditor.setPoints(task.getPoints());
        taskHeaderEditor.setDifficulty(task.getDifficulty());
        taskHeaderEditor.setScope(task.getScope());
        variantList.setItems(task.getVariants().stream().map(Variant::getQuestion).toList());
        if (selectedVariantIndex < 0 && task.variantCount() > 0) {
            selectedVariantIndex = 0;
        }
        variantList.setSelectedIndex(selectedVariantIndex);
        refreshVariantSelection();
    }

    private void refreshVariantSelection() {
        Variant variant = selectionModel.variantAt(selectedChapterIndex, selectedTaskIndex, selectedVariantIndex);
        if (variant == null) {
            variantEditor.clear();
            return;
        }

        variantEditor.setQuestionText(variant.getQuestion());
        variantEditor.setAnswerText(variant.getAnswer());
    }

    private void markDirty() {
        uiStateManager.markDirty();
        uiStateManager.markPreviewStale();
        dirtyStateChangedHandler.accept(true);
    }
}