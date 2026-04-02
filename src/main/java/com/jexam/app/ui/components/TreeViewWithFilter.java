package com.jexam.app.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Tree navigation with text filtering and selection forwarding.
 *
 * @param <T> hierarchy item type
 */
public final class TreeViewWithFilter<T> extends BorderPane {
    private final TextField filterField = new TextField();
    private final TreeView<T> treeView = new TreeView<>();
    private final Function<T, String> labelProvider;
    private final ObservableList<TreeItem<T>> sourceRoots = FXCollections.observableArrayList();

    private Consumer<T> itemSelectionHandler = value -> { };
    private T selectedItem;

    public TreeViewWithFilter(final Function<T, String> itemLabelProvider) {
        this.labelProvider = Objects.requireNonNull(itemLabelProvider, "itemLabelProvider");

        filterField.setPromptText("Filter...");
        filterField.textProperty().addListener((observable, oldValue, newValue) -> refreshTree());

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                selectedItem = newValue.getValue();
                itemSelectionHandler.accept(newValue.getValue());
            }
        });

        VBox header = new VBox(6, filterField);
        header.setPadding(new Insets(8));
        treeView.setShowRoot(false);
        treeView.setCellFactory(createCellFactory());

        setTop(header);
        setCenter(treeView);
    }

    public void setOnItemSelected(final Consumer<T> handler) {
        itemSelectionHandler = handler == null ? value -> { } : handler;
    }

    public void setRootItems(final List<T> items) {
        sourceRoots.clear();
        if (items != null) {
            for (T item : items) {
                sourceRoots.add(buildTreeItem(item));
            }
        }
        refreshTree();
    }

    public void setRootItem(final TreeItem<T> rootItem) {
        sourceRoots.clear();
        if (rootItem != null) {
            sourceRoots.add(rootItem);
        }
        refreshTree();
    }

    public void setSelectedItem(final T item) {
        if (item == null) {
            treeView.getSelectionModel().clearSelection();
        }
        selectedItem = item;
    }

    public TreeView<T> getTreeView() {
        return treeView;
    }

    private Callback<TreeView<T>, TreeCell<T>> createCellFactory() {
        return view -> new TreeCell<>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labelProvider.apply(item));
            }
        };
    }

    private void refreshTree() {
        String filterText = filterField.getText();
        TreeItem<T> root = new TreeItem<>();
        root.setExpanded(true);

        for (TreeItem<T> sourceRoot : sourceRoots) {
            TreeItem<T> filteredRoot = filterTree(sourceRoot, filterText == null ? "" : filterText.trim().toLowerCase());
            if (filteredRoot != null) {
                root.getChildren().add(filteredRoot);
            }
        }

        treeView.setRoot(root);
    }

    private TreeItem<T> buildTreeItem(final T value) {
        TreeItem<T> item = new TreeItem<>(value);
        return item;
    }

    private TreeItem<T> filterTree(final TreeItem<T> sourceItem, final String filterText) {
        if (sourceItem == null || sourceItem.getValue() == null) {
            return null;
        }

        boolean matches = filterText.isEmpty()
            || labelProvider.apply(sourceItem.getValue()).toLowerCase().contains(filterText);

        List<TreeItem<T>> filteredChildren = new ArrayList<>();
        for (TreeItem<T> child : sourceItem.getChildren()) {
            TreeItem<T> filteredChild = filterTree(child, filterText);
            if (filteredChild != null) {
                filteredChildren.add(filteredChild);
            }
        }

        if (!matches && filteredChildren.isEmpty()) {
            return null;
        }

        TreeItem<T> copy = new TreeItem<>(sourceItem.getValue());
        copy.getChildren().addAll(filteredChildren);
        copy.setExpanded(true);
        return copy;
    }
}