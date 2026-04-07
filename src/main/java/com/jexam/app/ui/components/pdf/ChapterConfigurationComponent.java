package com.jexam.app.ui.components.pdf;

import com.jexam.model.Chapter;
import com.jexam.model.Task;
import com.jexam.model.enums.Scope;
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
 */
public final class ChapterConfigurationComponent extends VBox {
    private static final int POINT_SCALE = 2;

    private final ListView<ChapterRow> includedList = new ListView<>();
    private final ListView<String> excludedList = new ListView<>();
    private final List<Integer> includedChapterIndices = new ArrayList<>();
    private final List<Integer> excludedChapterIndices = new ArrayList<>();
    private final Map<Integer, Double> chapterGoalPoints = new HashMap<>();
    private final Map<Integer, Chapter> chapterByIndex = new HashMap<>();
    private final Map<Integer, List<Double>> achievablePointsByChapter = new HashMap<>();
    private final Map<Integer, GoalPointField> goalFieldByChapter = new HashMap<>();

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

    public ChapterConfigurationComponent() {
        setSpacing(8);
        setPadding(new Insets(8));

        Button upButton = new Button("Up");
        Button downButton = new Button("Down");
        Button excludeButton = new Button("Exclude");
        Button includeButton = new Button("Include");
        Button resetButton = new Button("Reset");

        includedList.setAccessibleText("Included chapters list");
        excludedList.setAccessibleText("Excluded chapters list");
        upButton.setAccessibleText("Move selected chapter up");
        downButton.setAccessibleText("Move selected chapter down");
        excludeButton.setAccessibleText("Exclude selected chapter from generation");
        includeButton.setAccessibleText("Include selected chapter in generation");
        resetButton.setAccessibleText("Reset chapter selection to all chapters");

        upButton.setOnAction(event -> moveUpHandler.accept(includedList.getSelectionModel().getSelectedIndex()));
        downButton.setOnAction(event -> moveDownHandler.accept(includedList.getSelectionModel().getSelectedIndex()));
        excludeButton.setOnAction(event -> excludeHandler.accept(includedList.getSelectionModel().getSelectedIndex()));
        includeButton.setOnAction(event -> {
            int excludedRowIndex = excludedList.getSelectionModel().getSelectedIndex();
            if (excludedRowIndex >= 0 && excludedRowIndex < excludedChapterIndices.size()) {
                includeHandler.accept(excludedChapterIndices.get(excludedRowIndex));
            }
        });
        resetButton.setOnAction(event -> resetHandler.run());

        configureIncludedCellFactory();

        HBox includedActions = new HBox(6, upButton, downButton, excludeButton);
        HBox excludedActions = new HBox(6, includeButton, resetButton);

        getChildren().addAll(
            new Label("Included Chapters"),
            includedList,
            includedActions,
            new Label("Excluded Chapters"),
            excludedList,
            excludedActions
        );
    }

    public void setChapterData(
        final List<String> chapterNames,
        final List<Integer> includedOrder,
        final Map<Integer, Double> configuredGoals
    ) {
        setChapterData(chapterNames, includedOrder, configuredGoals, List.of());
    }

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

        int previousSelection = includedList.getSelectionModel().getSelectedIndex();
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
                includedRows.add(new ChapterRow(chapterIndex, chapterNames.get(chapterIndex)));
                GoalPointField field = goalFieldByChapter.computeIfAbsent(chapterIndex, GoalPointField::new);
                field.setAvailablePoints(achievablePointsByChapter.getOrDefault(chapterIndex, List.of()));
                field.setCommittedValue(chapterGoalPoints.getOrDefault(chapterIndex, 0d));
            }
        }

        List<String> excludedLabels = new ArrayList<>();
        for (int chapterIndex : excludedChapterIndices) {
            if (chapterIndex >= 0 && chapterIndex < chapterNames.size()) {
                excludedLabels.add(chapterNames.get(chapterIndex));
            }
        }

        includedList.setItems(FXCollections.observableArrayList(includedRows));
        excludedList.setItems(FXCollections.observableArrayList(excludedLabels));

        if (!includedRows.isEmpty()) {
            int targetSelection = Math.max(0, Math.min(previousSelection, includedRows.size() - 1));
            includedList.getSelectionModel().select(targetSelection);
        } else {
            includedList.getSelectionModel().clearSelection();
        }
    }

    public void setOnMoveUp(final Consumer<Integer> handler) {
        moveUpHandler = handler == null ? index -> { } : handler;
    }

    public void setOnMoveDown(final Consumer<Integer> handler) {
        moveDownHandler = handler == null ? index -> { } : handler;
    }

    public void setOnExclude(final Consumer<Integer> handler) {
        excludeHandler = handler == null ? index -> { } : handler;
    }

    public void setOnInclude(final Consumer<Integer> handler) {
        includeHandler = handler == null ? chapterIndex -> { } : handler;
    }

    public void setOnGoalChanged(final BiConsumer<Integer, Double> handler) {
        goalChangedHandler = handler == null ? (chapterIndex, points) -> { } : handler;
    }

    public void setOnReset(final Runnable handler) {
        resetHandler = handler == null ? () -> { } : handler;
    }

    public void setOnReorder(final BiConsumer<Integer, Integer> handler) {
        reorderHandler = handler == null ? (fromIndex, toIndex) -> { } : handler;
    }

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
                setGraphic(row);
            }
        });
    }

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

        private void setAvailablePoints(final List<Double> points) {
            availablePoints = points == null ? List.of() : List.copyOf(points);
            setItems(FXCollections.observableArrayList(availablePoints.stream().map(this::toOption).toList()));
        }

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

        private void commitCurrentValue() {
            String rawText = getEditor().getText();
            double resolvedValue = resolveValueFromInput(rawText);
            commitResolvedValue(resolvedValue);
        }

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

            PointOption nearest = nearestOption(parsedValue);
            if (nearest != null) {
                return nearest.value();
            }

            return parsedValue;
        }

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

        private PointOption optionForValue(final double value) {
            String formatted = formatPoints(value);
            for (Double availablePoint : availablePoints) {
                if (formatPoints(availablePoint).equals(formatted)) {
                    return toOption(availablePoint);
                }
            }
            return null;
        }

        private PointOption toOption(final double value) {
            return new PointOption(value, formatPoints(value));
        }

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

    private int toPointUnits(final double points) {
        return (int) Math.round(points * POINT_SCALE);
    }

    private double fromPointUnits(final int units) {
        return Math.round((units / (double) POINT_SCALE) * POINT_SCALE) / (double) POINT_SCALE;
    }

    private String formatPoints(final double points) {
        return String.format(Locale.ROOT, "%.1f", points);
    }
}