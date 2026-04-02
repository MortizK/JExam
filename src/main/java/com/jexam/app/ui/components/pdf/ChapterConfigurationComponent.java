package com.jexam.app.ui.components.pdf;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Included/excluded chapter configuration with reorder actions.
 */
public final class ChapterConfigurationComponent extends VBox {
    private final ListView<String> includedList = new ListView<>();
    private final ListView<String> excludedList = new ListView<>();
    private final TextField goalPointsField = new TextField();
    private final List<Integer> includedChapterIndices = new ArrayList<>();
    private final List<Integer> excludedChapterIndices = new ArrayList<>();
    private final java.util.Map<Integer, Double> chapterGoalPoints = new java.util.HashMap<>();

    private Consumer<Integer> moveUpHandler = index -> { };
    private Consumer<Integer> moveDownHandler = index -> { };
    private Consumer<Integer> excludeHandler = index -> { };
    private Consumer<Integer> includeHandler = chapterIndex -> { };
    private BiConsumer<Integer, Double> goalChangedHandler = (chapterIndex, points) -> { };
    private Runnable resetHandler = () -> { };
    private BiConsumer<Integer, Integer> reorderHandler = (fromIndex, toIndex) -> { };

    public ChapterConfigurationComponent() {
        setSpacing(8);
        setPadding(new Insets(8));

        Button upButton = new Button("Up");
        Button downButton = new Button("Down");
        Button excludeButton = new Button("Exclude");
        Button includeButton = new Button("Include");
        Button resetButton = new Button("Reset");
        Button applyGoalButton = new Button("Set Goal");

        includedList.setAccessibleText("Included chapters list");
        excludedList.setAccessibleText("Excluded chapters list");
        goalPointsField.setAccessibleText("Goal points for selected included chapter");
        upButton.setAccessibleText("Move selected chapter up");
        downButton.setAccessibleText("Move selected chapter down");
        excludeButton.setAccessibleText("Exclude selected chapter from generation");
        includeButton.setAccessibleText("Include selected chapter in generation");
        resetButton.setAccessibleText("Reset chapter selection to all chapters");
        applyGoalButton.setAccessibleText("Apply goal points to selected chapter");

        goalPointsField.setPromptText("Goal points (0.5 steps)");

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
        applyGoalButton.setOnAction(event -> applyGoalFromInput());
        goalPointsField.setOnAction(event -> applyGoalFromInput());

        includedList.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            updateGoalInputForSelection(newValue == null ? -1 : newValue.intValue());
        });

        enableDragDropReorder();

        HBox includedActions = new HBox(6, upButton, downButton, excludeButton);
        HBox goalActions = new HBox(6, goalPointsField, applyGoalButton);
        HBox excludedActions = new HBox(6, includeButton, resetButton);
        getChildren().addAll(
            new Label("Included Chapters"),
            includedList,
            includedActions,
            new Label("Goal points for selected chapter"),
            goalActions,
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
        chapterGoalPoints.clear();
        if (configuredGoals != null) {
            chapterGoalPoints.putAll(configuredGoals);
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

        List<String> includedLabels = new ArrayList<>();
        for (int chapterIndex : includedChapterIndices) {
            if (chapterIndex >= 0 && chapterIndex < chapterNames.size()) {
                double points = chapterGoalPoints.getOrDefault(chapterIndex, 0d);
                includedLabels.add(chapterNames.get(chapterIndex) + " (goal " + formatPoints(points) + " pts)");
            }
        }

        List<String> excludedLabels = new ArrayList<>();
        for (int chapterIndex : excludedChapterIndices) {
            if (chapterIndex >= 0 && chapterIndex < chapterNames.size()) {
                excludedLabels.add(chapterNames.get(chapterIndex));
            }
        }

        includedList.setItems(FXCollections.observableArrayList(includedLabels));
        excludedList.setItems(FXCollections.observableArrayList(excludedLabels));

        if (!includedLabels.isEmpty()) {
            int targetSelection = Math.max(0, Math.min(previousSelection, includedLabels.size() - 1));
            includedList.getSelectionModel().select(targetSelection);
            updateGoalInputForSelection(targetSelection);
        } else {
            goalPointsField.clear();
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

    private void enableDragDropReorder() {
        includedList.setCellFactory(list -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(final String item, final boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }
            };

            cell.setOnDragDetected(event -> {
                if (cell.isEmpty()) {
                    return;
                }
                var dragboard = cell.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(Integer.toString(cell.getIndex()));
                dragboard.setContent(content);
                event.consume();
            });

            cell.setOnDragOver(event -> {
                if (event.getDragboard().hasString() && cell.getIndex() >= 0) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            cell.setOnDragDropped(event -> {
                if (!event.getDragboard().hasString()) {
                    return;
                }
                int fromIndex = Integer.parseInt(event.getDragboard().getString());
                int toIndex = cell.getIndex();
                if (fromIndex != toIndex && fromIndex >= 0 && toIndex >= 0) {
                    reorderHandler.accept(fromIndex, toIndex);
                }
                event.setDropCompleted(true);
                event.consume();
            });
            return cell;
        });
    }

    private void applyGoalFromInput() {
        int selectedIncludedIndex = includedList.getSelectionModel().getSelectedIndex();
        if (selectedIncludedIndex < 0 || selectedIncludedIndex >= includedChapterIndices.size()) {
            return;
        }

        String raw = goalPointsField.getText();
        if (raw == null || raw.isBlank()) {
            return;
        }

        try {
            double parsedPoints = Double.parseDouble(raw.trim());
            if (parsedPoints <= 0d) {
                return;
            }

            int chapterIndex = includedChapterIndices.get(selectedIncludedIndex);
            goalChangedHandler.accept(chapterIndex, parsedPoints);
        } catch (NumberFormatException ignored) {
            // Input validation is handled by accepting only valid numeric values.
        }
    }

    private void updateGoalInputForSelection(final int includedSelectionIndex) {
        if (includedSelectionIndex < 0 || includedSelectionIndex >= includedChapterIndices.size()) {
            goalPointsField.clear();
            return;
        }
        int chapterIndex = includedChapterIndices.get(includedSelectionIndex);
        double points = chapterGoalPoints.getOrDefault(chapterIndex, 0d);
        goalPointsField.setText(formatPoints(points));
    }

    private String formatPoints(final double points) {
        return String.format(java.util.Locale.ROOT, "%.1f", points);
    }
}