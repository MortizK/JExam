package com.jexam.app.ui.components.xml;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Chapter navigation table with inline add/delete actions.
 */
public final class ChapterTableComponent extends VBox {
    private final ListView<String> listView = new ListView<>();
    private Consumer<Integer> selectHandler = index -> { };
    private Runnable createHandler = () -> { };
    private Consumer<Integer> deleteHandler = index -> { };
    private boolean updating;

    public ChapterTableComponent() {
        setSpacing(6);
        setPadding(new Insets(0, 0, 0, 0));

        Button addButton = new Button("Add Chapter");
        Button deleteButton = new Button("Delete Chapter");
        addButton.setOnAction(event -> createHandler.run());
        deleteButton.setOnAction(event -> deleteHandler.accept(listView.getSelectionModel().getSelectedIndex()));
        listView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (!updating && newValue != null) {
                selectHandler.accept(newValue.intValue());
            }
        });
        getChildren().addAll(new Label("Chapters"), listView, new HBox(6, addButton, deleteButton));
    }

    public void setItems(final List<String> values) {
        updating = true;
        try {
            listView.setItems(FXCollections.observableArrayList(values));
        } finally {
            updating = false;
        }
    }

    public void setSelectedIndex(final int index) {
        updating = true;
        try {
            if (index >= 0 && index < listView.getItems().size()) {
                listView.getSelectionModel().select(index);
            } else {
                listView.getSelectionModel().clearSelection();
            }
        } finally {
            updating = false;
        }
    }

    public void setOnSelect(final Consumer<Integer> handler) {
        selectHandler = handler == null ? index -> { } : handler;
    }

    public void setOnCreate(final Runnable handler) {
        createHandler = handler == null ? () -> { } : handler;
    }

    public void setOnDelete(final Consumer<Integer> handler) {
        deleteHandler = handler == null ? index -> { } : handler;
    }
}