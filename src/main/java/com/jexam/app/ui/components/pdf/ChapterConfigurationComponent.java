package com.jexam.app.ui.components.pdf;

import com.jexam.model.Chapter;
import com.jexam.model.Task;
import com.jexam.model.enums.Scope;
import com.jexam.app.UiTextCatalog;
import com.jexam.app.UiLanguage;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Included/excluded chapter configuration with reorder actions.
 *
 * <p>The component keeps the generation chapter order, chapter goal points,
 * and the included/excluded lists synchronized. It also provides a compact
 * inline control for editing the goal point of each included chapter.</p>
 *
 * @author Moritz
 */
public final class ChapterConfigurationComponent extends VBox {
    private static final int POINT_SCALE = 2;

    private final ListView<ChapterRow> includedList = new ListView<>();
    private final ListView<ChapterRow> excludedList = new ListView<>();
    private final List<Integer> includedChapterIndices = new ArrayList<>();
    private final List<Integer> excludedChapterIndices = new ArrayList<>();
    private final Map<Integer, Double> chapterGoalPoints = new HashMap<>();
    private final Map<Integer, Chapter> chapterByIndex = new HashMap<>();
    private final Map<Integer, List<Double>> achievablePointsByChapter = new HashMap<>();
    private final Map<Integer, GoalPointField> goalFieldByChapter = new HashMap<>();
    private final Label includedLabel = new Label();
    private final Label excludedLabel = new Label();
    private final Button upButton = new Button();
    private final Button downButton = new Button();
    private final Button excludeButton = new Button();
    private final Button includeButton = new Button();
    private final Button resetButton = new Button();
    private final HBox excludedActions;

    // Localization
    private UiTextCatalog uiTextCatalog = UiTextCatalog.loadDefault();
    private UiLanguage uiLanguage = UiLanguage.ENGLISH;

    private Consumer<Integer> moveUpHandler = index -> { };
    private Consumer<Integer> moveDownHandler = index -> { };
    private Consumer<Integer> excludeHandler = index -> { };
    private Consumer<Integer> includeHandler = chapterIndex -> { };
    private BiConsumer<Integer, Double> goalChangedHandler = (chapterIndex, points) -> { };
    private Runnable resetHandler = () -> { };
    private BiConsumer<Integer, Integer> reorderHandler = (fromIndex, toIndex) -> { };

    private record ChapterRow(int chapterIndex, String chapterName) {
    }

    private record PointOption(double value, String display) {
        @Override
        public String toString() {
            return display;
        }
    }

    /**
     * Creates the chapter configuration panel with included/excluded lists, reorder actions, and goal point fields.
     * Initializes UI controls for managing included chapters, excluded chapters, and their goal points.
     */
    public ChapterConfigurationComponent() {
        getStyleClass().add("chapter-configuration");
        setSpacing(8);
        setPadding(new Insets(8));

        includedList.setFixedCellSize(36);
        excludedList.setFixedCellSize(36);

        includedLabel.getStyleClass().add("section-label");
        excludedLabel.getStyleClass().add("section-label");
        includedList.getStyleClass().add("included-chapter-list");
        excludedList.getStyleClass().add("excluded-chapter-list");
        upButton.getStyleClass().add("secondary-action");
        downButton.getStyleClass().add("secondary-action");
        excludeButton.getStyleClass().add("secondary-action");
        includeButton.getStyleClass().add("secondary-action");
        resetButton.getStyleClass().add("secondary-action");

        includedList.setAccessibleText(uiTextCatalog.text(uiLanguage, "chapterConfiguration.includedList"));
        excludedList.setAccessibleText(uiTextCatalog.text(uiLanguage, "chapterConfiguration.excludedList"));
        upButton.setAccessibleText(uiTextCatalog.text(uiLanguage, "chapterConfiguration.moveUp"));
        downButton.setAccessibleText(uiTextCatalog.text(uiLanguage, "chapterConfiguration.moveDown"));
        excludeButton.setAccessibleText(uiTextCatalog.text(uiLanguage, "chapterConfiguration.excludeChapter"));
        includeButton.setAccessibleText(uiTextCatalog.text(uiLanguage, "chapterConfiguration.includeChapter"));
        resetButton.setAccessibleText(uiTextCatalog.text(uiLanguage, "chapterConfiguration.reset"));

        upButton.setOnAction(event -> moveUpHandler.accept(includedList.getSelectionModel().getSelectedIndex()));
        downButton.setOnAction(event -> moveDownHandler.accept(includedList.getSelectionModel().getSelectedIndex()));
        excludeButton.setOnAction(event -> excludeHandler.accept(includedList.getSelectionModel().getSelectedIndex()));
        includeButton.setOnAction(event -> {
            ChapterRow selected = excludedList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                includeHandler.accept(selected.chapterIndex());
            }
        });
        resetButton.setOnAction(event -> resetHandler.run());

        configureIncludedCellFactory();
        excludedList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(final ChapterRow item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.chapterName());
            }
        });

        HBox includedActions = new HBox(6, upButton, downButton, excludeButton);
        excludedActions = new HBox(6, includeButton, resetButton);
        includedActions.getStyleClass().add("list-actions");
        excludedActions.getStyleClass().add("list-actions");
        setExcludedSectionVisible(false);

        getChildren().addAll(
            includedLabel,
            includedList,
            includedActions,
            excludedLabel,
            excludedList,
            excludedActions
        );

        setLocalizedTexts(
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.includedLabel"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.excludedLabel"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.up"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.down"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.exclude"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.include"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.reset"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.includedList"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.excludedList"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.moveUp"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.moveDown"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.excludeChapter"),
            uiTextCatalog.text(uiLanguage, "chapterConfiguration.includeChapter")
        );
    }

    /**
     * Applies localized visible texts and accessibility labels.
     *
     * @param includedLabelText text for the included section label
     * @param excludedLabelText text for the excluded section label
     * @param upButtonText text for the move-up button
     * @param downButtonText text for the move-down button
     * @param excludeButtonText text for the exclude button
     * @param includeButtonText text for the include button
     * @param resetButtonText text for the reset button
     * @param includedListAccessibleText accessible text for the included list
     * @param excludedListAccessibleText accessible text for the excluded list
     * @param moveUpAccessibleText accessible text for the move-up button
     * @param moveDownAccessibleText accessible text for the move-down button
     * @param excludeChapterAccessibleText accessible text for the exclude button
     * @param includeChapterAccessibleText accessible text for the include button
     */
    public void setLocalizedTexts(
        final String includedLabelText,
        final String excludedLabelText,
        final String upButtonText,
        final String downButtonText,
        final String excludeButtonText,
        final String includeButtonText,
        final String resetButtonText,
        final String includedListAccessibleText,
        final String excludedListAccessibleText,
        final String moveUpAccessibleText,
        final String moveDownAccessibleText,
        final String excludeChapterAccessibleText,
        final String includeChapterAccessibleText
    ) {
        if (includedLabelText != null) {
            includedLabel.setText(includedLabelText);
        }
        if (excludedLabelText != null) {
            excludedLabel.setText(excludedLabelText);
        }
        if (upButtonText != null) {
            upButton.setText(upButtonText);
        }
        if (downButtonText != null) {
            downButton.setText(downButtonText);
        }
        if (excludeButtonText != null) {
            excludeButton.setText(excludeButtonText);
        }
        if (includeButtonText != null) {
            includeButton.setText(includeButtonText);
        }
        if (resetButtonText != null) {
            resetButton.setText(resetButtonText);
        }

        includedList.setAccessibleText(includedListAccessibleText);
        excludedList.setAccessibleText(excludedListAccessibleText);
        upButton.setAccessibleText(moveUpAccessibleText);
        downButton.setAccessibleText(moveDownAccessibleText);
        excludeButton.setAccessibleText(excludeChapterAccessibleText);
        includeButton.setAccessibleText(includeChapterAccessibleText);
        resetButton.setAccessibleText(resetButtonText);
    }

    /**
     * Sets chapter data with default excluded chapters list.
     *
     * @param chapterNames names of all chapters
     * @param includedOrder indices of chapters to include in order
     * @param configuredGoals map of chapter index to goal points
     */
    public void setChapterData(
        final List<String> chapterNames,
        final List<Integer> includedOrder,
        final Map<Integer, Double> configuredGoals
    ) {
        setChapterData(chapterNames, includedOrder, configuredGoals, List.of());
    }

    /**
     * Sets complete chapter data including chapters for point calculation.
     *
     * @param chapterNames names of all chapters
     * @param includedOrder indices of chapters to include in order
     * @param configuredGoals map of chapter index to goal points
     * @param chapters the full chapter list for achievable points computation
     */
    public void setChapterData(
        final List<String> chapterNames,
        final List<Integer> includedOrder,
        final Map<Integer, Double> configuredGoals,
        final List<Chapter> chapters
    ) {
        goalFieldByChapter.clear();
        chapterGoalPoints.clear();
        if (configuredGoals != null) {
            chapterGoalPoints.putAll(configuredGoals);
        }

        chapterByIndex.clear();
        if (chapters != null) {
            for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
                chapterByIndex.put(chapterIndex, chapters.get(chapterIndex));
            }
        }

        achievablePointsByChapter.clear();
        for (int chapterIndex = 0; chapterIndex < chapterNames.size(); chapterIndex++) {
            achievablePointsByChapter.put(chapterIndex, computeAchievablePoints(chapterByIndex.get(chapterIndex)));
        }

        ChapterRow previousIncludedSelection = includedList.getSelectionModel().getSelectedItem();
        ChapterRow previousExcludedSelection = excludedList.getSelectionModel().getSelectedItem();
        int previousIncludedIndex = includedList.getSelectionModel().getSelectedIndex();
        int previousExcludedIndex = excludedList.getSelectionModel().getSelectedIndex();
        includedChapterIndices.clear();
        includedChapterIndices.addAll(includedOrder);

        excludedChapterIndices.clear();
        for (int chapterIndex = 0; chapterIndex < chapterNames.size(); chapterIndex++) {
            if (!includedChapterIndices.contains(chapterIndex)) {
                excludedChapterIndices.add(chapterIndex);
            }
        }

        List<ChapterRow> includedRows = new ArrayList<>();
        for (int chapterIndex : includedChapterIndices) {
            if (chapterIndex >= 0 && chapterIndex < chapterNames.size()) {
                Chapter chapter = chapterByIndex.get(chapterIndex);
                if (chapter == null || chapter.getTasks().isEmpty()) {
                    continue;
                }

                includedRows.add(new ChapterRow(chapterIndex, chapterNames.get(chapterIndex)));
                GoalPointField field = goalFieldByChapter.computeIfAbsent(chapterIndex, GoalPointField::new);
                field.setAvailablePoints(achievablePointsByChapter.getOrDefault(chapterIndex, List.of()));
                field.setCommittedValue(chapterGoalPoints.getOrDefault(chapterIndex, 0d));
            }
        }

        List<ChapterRow> excludedRows = new ArrayList<>();
        for (int chapterIndex : excludedChapterIndices) {
            if (chapterIndex >= 0 && chapterIndex < chapterNames.size()) {
                excludedRows.add(new ChapterRow(chapterIndex, chapterNames.get(chapterIndex)));
            }
        }

        includedList.setItems(FXCollections.observableArrayList(includedRows));
        excludedList.setItems(FXCollections.observableArrayList(excludedRows));
        setListHeight(includedList, includedRows.size());
        setListHeight(excludedList, excludedRows.size());
        setExcludedSectionVisible(!excludedRows.isEmpty());

        if (!includedRows.isEmpty()) {
            int targetSelection = previousIncludedSelection == null
                ? Math.max(0, Math.min(previousIncludedIndex, includedRows.size() - 1))
                : indexOfChapter(includedRows, previousIncludedSelection.chapterIndex());
            if (targetSelection < 0) {
                targetSelection = Math.max(0, Math.min(previousIncludedIndex, includedRows.size() - 1));
            }
            includedList.getSelectionModel().select(targetSelection);
        } else {
            includedList.getSelectionModel().clearSelection();
        }

        if (!excludedRows.isEmpty()) {
            int targetSelection = previousExcludedSelection == null
                ? Math.max(0, Math.min(previousExcludedIndex, excludedRows.size() - 1))
                : indexOfChapter(excludedRows, previousExcludedSelection.chapterIndex());
            if (targetSelection < 0) {
                targetSelection = Math.max(0, Math.min(previousExcludedIndex, excludedRows.size() - 1));
            }
            excludedList.getSelectionModel().select(targetSelection);
        } else {
            excludedList.getSelectionModel().clearSelection();
        }
    }

    private void setExcludedSectionVisible(final boolean visible) {
        // Hide the entire excluded section when there is nothing to include
        // later, which keeps the panel compact for small exams.
        excludedLabel.setManaged(visible);
        excludedLabel.setVisible(visible);
        excludedList.setManaged(visible);
        excludedList.setVisible(visible);
        excludedList.setDisable(!visible);
        excludedActions.setManaged(visible);
        excludedActions.setVisible(visible);
        excludedActions.setDisable(!visible);
    }

    private static void setListHeight(final ListView<ChapterRow> listView, final int itemCount) {
        // Keep list heights stable so the included/excluded panels do not jump
        // as chapters are moved between them.
        double rowHeight = listView.getFixedCellSize() <= 0 ? 36 : listView.getFixedCellSize();
        double height = Math.max(rowHeight, itemCount * rowHeight + 2);
        listView.setMinHeight(Region.USE_PREF_SIZE);
        listView.setPrefHeight(height);
        listView.setMaxHeight(height);
    }

    /**
     * Returns currently selected included chapter index, or -1 when no selection exists.
     *
     * @return a int
     */
    public int selectedIncludedChapterIndex() {
        ChapterRow selected = includedList.getSelectionModel().getSelectedItem();
        return selected == null ? -1 : selected.chapterIndex();
    }

    /**
     * Returns currently selected excluded chapter index, or -1 when no selection exists.
     *
     * @return a int
     */
    public int selectedExcludedChapterIndex() {
        ChapterRow selected = excludedList.getSelectionModel().getSelectedItem();
        return selected == null ? -1 : selected.chapterIndex();
    }

    /**
     * Selects an included chapter row by chapter index if present.
     *
     * @param chapterIndex a int
     */
    public void selectIncludedChapterByChapterIndex(final int chapterIndex) {
        if (chapterIndex < 0) {
            includedList.getSelectionModel().clearSelection();
            return;
        }
        int index = indexOfChapter(includedList.getItems(), chapterIndex);
        if (index >= 0) {
            includedList.getSelectionModel().select(index);
            includedList.scrollTo(index);
        }
    }

    /**
     * Selects an excluded chapter row by chapter index if present.
     *
     * @param chapterIndex a int
     */
    public void selectExcludedChapterByChapterIndex(final int chapterIndex) {
        if (chapterIndex < 0) {
            excludedList.getSelectionModel().clearSelection();
            return;
        }
        int index = indexOfChapter(excludedList.getItems(), chapterIndex);
        if (index >= 0) {
            excludedList.getSelectionModel().select(index);
            excludedList.scrollTo(index);
        }
    }

    private int indexOfChapter(final List<ChapterRow> rows, final int chapterIndex) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).chapterIndex() == chapterIndex) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sets callback handler when user requests moving selected chapter up in included list.
     *
     * @param handler consumer accepting the chapter index to move up; null clears the handler
     */
    public void setOnMoveUp(final Consumer<Integer> handler) {
        moveUpHandler = handler == null ? index -> { } : handler;
    }

    /**
     * Sets callback handler when user requests moving selected chapter down in included list.
     *
     * @param handler consumer accepting the chapter index to move down; null clears the handler
     */
    public void setOnMoveDown(final Consumer<Integer> handler) {
        moveDownHandler = handler == null ? index -> { } : handler;
    }

    /**
     * Sets callback handler when user excludes a chapter from generation.
     *
     * @param handler consumer accepting the chapter index to exclude; null clears the handler
     */
    public void setOnExclude(final Consumer<Integer> handler) {
        excludeHandler = handler == null ? index -> { } : handler;
    }

    /**
     * Sets callback handler when user includes an excluded chapter in generation.
     *
     * @param handler consumer accepting the chapter index to include; null clears the handler
     */
    public void setOnInclude(final Consumer<Integer> handler) {
        includeHandler = handler == null ? chapterIndex -> { } : handler;
    }

    /**
     * Sets callback handler when user changes goal points for a chapter.
     *
     * @param handler bi-consumer accepting (chapter index, goal points); null clears the handler
     */
    public void setOnGoalChanged(final BiConsumer<Integer, Double> handler) {
        goalChangedHandler = handler == null ? (chapterIndex, points) -> { } : handler;
    }

    /**
     * Sets callback handler when user requests reset of chapter selection to all chapters.
     *
     * @param handler runnable to execute on reset request; null clears the handler
     */
    public void setOnReset(final Runnable handler) {
        resetHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Sets callback handler when user reorders chapters in the included list via drag-drop.
     *
     * @param handler bi-consumer accepting (from index, to index); null clears the handler
     */
    public void setOnReorder(final BiConsumer<Integer, Integer> handler) {
        reorderHandler = handler == null ? (fromIndex, toIndex) -> { } : handler;
    }

    /**
     * Configures the cell factory for the included chapters list with drag-drop reordering support.
     * Each cell displays chapter name and goal point field with keyboard event handling.
     */
    private void configureIncludedCellFactory() {
        includedList.setCellFactory(listView -> new ListCell<>() {
            {
                setOnDragDetected(event -> {
                    if (isEmpty()) {
                        return;
                    }
                    var dragboard = startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(Integer.toString(getIndex()));
                    dragboard.setContent(content);
                    event.consume();
                });

                setOnDragOver(event -> {
                    if (event.getDragboard().hasString() && getIndex() >= 0) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                setOnDragDropped(event -> {
                    if (!event.getDragboard().hasString()) {
                        return;
                    }
                    int fromIndex = Integer.parseInt(event.getDragboard().getString());
                    int toIndex = getIndex();
                    if (fromIndex != toIndex && fromIndex >= 0 && toIndex >= 0) {
                        // Reordering is delegated to the controller so the model
                        // and all selection state can be updated together.
                        reorderHandler.accept(fromIndex, toIndex);
                    }
                    event.setDropCompleted(true);
                    event.consume();
                });
            }

            @Override
            protected void updateItem(final ChapterRow item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                GoalPointField goalField = goalFieldByChapter.computeIfAbsent(item.chapterIndex(), GoalPointField::new);
                Label nameLabel = new Label(item.chapterName());
                nameLabel.setMinWidth(220);
                nameLabel.setPrefWidth(220);
                nameLabel.setMaxWidth(220);

                HBox row = new HBox(8, nameLabel, goalField);
                row.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(goalField, Priority.ALWAYS);
                // The row combines the chapter label with the inline goal field
                // so users can reorder and adjust points without leaving the list.
                setGraphic(row);
            }
        });
    }

    /**
     * Computes achievable point totals for a chapter based on its exam-scoped tasks.
     * Uses dynamic programming to find all possible sums from exam task points.
     * @param chapter the chapter to compute points for
     * @return sorted list of achievable point values, or empty list if chapter is null
     */
    private List<Double> computeAchievablePoints(final Chapter chapter) {
        if (chapter == null) {
            return List.of();
        }

        Set<Integer> sums = new HashSet<>();
        sums.add(0);

        for (Task task : chapter.getTasks()) {
            if (task.getScope() != Scope.EXAM) {
                continue;
            }
            int taskUnits = toPointUnits(task.getPoints());
            if (taskUnits <= 0) {
                continue;
            }

            Set<Integer> next = new HashSet<>(sums);
            for (Integer currentSum : sums) {
                next.add(currentSum + taskUnits);
            }
            sums = next;
        }

        TreeSet<Double> result = new TreeSet<>();
        for (Integer sum : sums) {
            if (sum > 0) {
                result.add(fromPointUnits(sum));
            }
        }
        return List.copyOf(result);
    }

    private final class GoalPointField extends ComboBox<PointOption> {
        private final int chapterIndex;
        private List<Double> availablePoints = List.of();
        private boolean updatingFromCode;

        /**
         * Initializes a goal point field for a specific chapter.
         * @param chapterIndex the chapter index this field manages
         */
        private GoalPointField(final int chapterIndex) {
            this.chapterIndex = chapterIndex;
            setEditable(true);
            setMinWidth(160);
            setPrefWidth(190);
            setMaxWidth(Double.MAX_VALUE);
            setPromptText("Goal points");

            setConverter(new StringConverter<>() {
                @Override
                public String toString(final PointOption object) {
                    return object == null ? "" : object.display();
                }

                @Override
                public PointOption fromString(final String string) {
                    if (string == null || string.isBlank()) {
                        return null;
                    }
                    OptionalDouble parsed = parseInput(string);
                    if (parsed.isEmpty()) {
                        return null;
                    }
                    return optionForValue(parsed.getAsDouble());
                }
            });

            setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(final PointOption item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.display());
                }
            });

            setCellFactory(listView -> new ListCell<>() {
                @Override
                protected void updateItem(final PointOption item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.display());
                }
            });

            getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                if (updatingFromCode) {
                    return;
                }

                // Keep typed text untouched; dropdown remains a static list.
            });

            getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    commitCurrentValue();
                }
            });

            addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    commitCurrentValue();
                    event.consume();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    revertToCommittedValue();
                    hide();
                    event.consume();
                }
            });

            setOnShowing(event -> { });

            // Keep user typing stable: selecting from popup only fills the editor; commit stays on Enter/blur.
            valueProperty().addListener((observable, oldValue, newValue) -> {
                if (updatingFromCode || newValue == null) {
                    return;
                }
                updatingFromCode = true;
                try {
                    getEditor().setText(newValue.display());
                } finally {
                    updatingFromCode = false;
                }
            });
        }

        /**
         * Sets the list of achievable point values for this chapter based on task points.
         * @param points list of valid point values to display in dropdown
         */
        private void setAvailablePoints(final List<Double> points) {
            availablePoints = points == null ? List.of() : List.copyOf(points);
            // Rebuild the dropdown each time the underlying chapter changes so
            // the options always match the current achievable sums.
            setItems(FXCollections.observableArrayList(availablePoints.stream().map(this::toOption).toList()));
        }

        /**
         * Sets the initial committed value for this field's chapter goal points.
         * Updates editor display and fires goalChangedHandler callback.
         * @param value the goal points value to commit
         */
        private void setCommittedValue(final double value) {
            PointOption option = optionForValue(value);
            updatingFromCode = true;
            try {
                setValue(option);
                getEditor().setText(option == null ? "" : option.display());
            } finally {
                updatingFromCode = false;
            }
        }

        /**
         * Parses editor text input and commits the resolved value.
         * Performs fuzzy matching to nearest achievable point and signals goalChangedHandler.
         */
        private void commitCurrentValue() {
            // Commit from free-form text so typed values still work when they
            // match an achievable point sum.
            String rawText = getEditor().getText();
            double resolvedValue = resolveValueFromInput(rawText);
            commitResolvedValue(resolvedValue);
        }

        /**
         * Commits a resolved point value, performing option matching and fuzzy nearest-value selection.
         * @param resolvedValue the point value to commit
         */
        private void commitResolvedValue(final double resolvedValue) {
            PointOption resolvedOption = optionForValue(resolvedValue);
            if (resolvedOption == null && availablePoints.isEmpty()) {
                return;
            }

            double committedValue = resolvedOption == null ? resolvedValue : resolvedOption.value();
            updatingFromCode = true;
            try {
                setValue(resolvedOption);
                getEditor().setText(resolvedOption == null ? formatPoints(committedValue) : resolvedOption.display());
            } finally {
                updatingFromCode = false;
            }
            hide();
            goalChangedHandler.accept(chapterIndex, committedValue);
        }

        /**
         * Reverts editor text to the last committed value for this chapter.
         * Used on Escape key to undo uncommitted edits.
         */
        private void revertToCommittedValue() {
            PointOption option = optionForValue(chapterGoalPoints.getOrDefault(chapterIndex, 0d));
            updatingFromCode = true;
            try {
                setValue(option);
                getEditor().setText(option == null ? "" : option.display());
            } finally {
                updatingFromCode = false;
            }
        }

        /**
         * Resolves user input text to a goal point value using fallback precedence:
         * 1. Exact match in available points
         * 2. Fuzzy nearest-match to available points
         * 3. Raw parsed input value
         * @param rawText user-entered text from editor
         * @return resolved point value
         */
        private double resolveValueFromInput(final String rawText) {
            if (rawText == null || rawText.isBlank()) {
                return chapterGoalPoints.getOrDefault(chapterIndex, 0d);
            }

            OptionalDouble parsed = parseInput(rawText);
            if (parsed.isEmpty()) {
                return chapterGoalPoints.getOrDefault(chapterIndex, 0d);
            }

            double parsedValue = parsed.getAsDouble();
            PointOption exact = optionForValue(parsedValue);
            if (exact != null) {
                return exact.value();
            }

            // Fallback to the closest achievable value before accepting the raw
            // typed number to keep the generation goal feasible.
            PointOption nearest = nearestOption(parsedValue);
            if (nearest != null) {
                return nearest.value();
            }

            return parsedValue;
        }

        /**
         * Finds achievable point value nearest to target, with tie-breaking by higher value preference.
         * @param target desired point value
         * @return nearest PointOption, or null if no achievable points available
         */
        private PointOption nearestOption(final double target) {
            if (availablePoints.isEmpty()) {
                return null;
            }

            Double nearest = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Double candidate : availablePoints) {
                double distance = Math.abs(candidate - target);
                if (distance < nearestDistance || (Math.abs(distance - nearestDistance) < 0.0001 && (nearest == null || candidate > nearest))) {
                    nearest = candidate;
                    nearestDistance = distance;
                }
            }
            return nearest == null ? null : toOption(nearest);
        }

        /**
         * Finds a PointOption matching a target value via display string formatted comparison.
         * @param value the point value to match
         * @return matching PointOption, or null if value not in achievable points
         */
        private PointOption optionForValue(final double value) {
            String formatted = formatPoints(value);
            for (Double availablePoint : availablePoints) {
                if (formatPoints(availablePoint).equals(formatted)) {
                    return toOption(availablePoint);
                }
            }
            return null;
        }

        /**
         * Converts a point value to a PointOption with formatted display text.
         * @param value the point value to convert
         * @return new PointOption with formatted display
         */
        private PointOption toOption(final double value) {
            return new PointOption(value, formatPoints(value));
        }

        /**
         * Parses user input string to optional double value.
         * Handles locale-specific decimal separators (comma or period).
         * @param rawText user-entered text
         * @return OptionalDouble with parsed value, or empty on invalid input
         */
        private OptionalDouble parseInput(final String rawText) {
            try {
                if (rawText == null || rawText.isBlank()) {
                    return OptionalDouble.empty();
                }
                return OptionalDouble.of(Double.parseDouble(rawText.trim().replace(',', '.')));
            } catch (NumberFormatException ignored) {
                return OptionalDouble.empty();
            }
        }
    }

    /**
     * Converts point values to integer units for internal computation (multiplied by POINT_SCALE).
     * @param points point value to convert
     * @return integer units (points * POINT_SCALE, rounded)
     */
    private int toPointUnits(final double points) {
        return (int) Math.round(points * POINT_SCALE);
    }

    /**
     * Converts integer units (from POINT_SCALE) back to point values.
     * Rounds result to match POINT_SCALE precision.
     * @param units integer units value
     * @return converted point value
     */
    private double fromPointUnits(final int units) {
        return Math.round((units / (double) POINT_SCALE) * POINT_SCALE) / (double) POINT_SCALE;
    }

    /**
     * Formats a double point value to locale-independent string (one decimal place, Locale.ROOT).
     * @param points point value to format
     * @return formatted string representation
     */
    private String formatPoints(final double points) {
        return String.format(Locale.ROOT, "%.1f", points);
    }
}
