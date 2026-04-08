package com.jexam.app.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private final Button expandAllButton = new Button("Expand All");
    private final Button collapseAllButton = new Button("Collapse All");
    private final TreeView<T> treeView = new TreeView<>();
    private final Function<T, String> labelProvider;
    private final ObservableList<TreeItem<T>> sourceRoots = FXCollections.observableArrayList();

    private Consumer<T> itemSelectionHandler = value -> { };
    private T selectedItem;
    private boolean updatingSelection;

    public TreeViewWithFilter(final Function<T, String> itemLabelProvider) {
        this.labelProvider = Objects.requireNonNull(itemLabelProvider, "itemLabelProvider");

        filterField.setPromptText("Filter...");
        filterField.setAccessibleText("Filter exam chapters and tasks");
        filterField.setFocusTraversable(true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> refreshTree());

        expandAllButton.setOnAction(event -> expandAll());
        collapseAllButton.setOnAction(event -> collapseAll());

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingSelection && newValue != null) {
                selectedItem = newValue.getValue();
                itemSelectionHandler.accept(newValue.getValue());
            }
        });
        treeView.setAccessibleText("Exam hierarchy tree");
        treeView.setFocusTraversable(true);

        HBox treeActionBar = new HBox(6, expandAllButton, collapseAllButton);
        VBox header = new VBox(6, treeActionBar, filterField);
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
        selectedItem = item;
        updatingSelection = true;
        try {
            if (item == null) {
                treeView.getSelectionModel().clearSelection();
                return;
            }

            TreeItem<T> root = treeView.getRoot();
            TreeItem<T> match = findItem(root, item);
            if (match != null) {
                treeView.getSelectionModel().select(match);
            } else {
                treeView.getSelectionModel().clearSelection();
            }
        } finally {
            updatingSelection = false;
        }
    }

    public TreeView<T> getTreeView() {
        return treeView;
    }

    public void requestTreeFocus() {
        treeView.requestFocus();
    }

    public void setHeaderTexts(
        final String filterPrompt,
        final String filterAccessibleText,
        final String expandAllLabel,
        final String collapseAllLabel
    ) {
        filterField.setPromptText(filterPrompt);
        filterField.setAccessibleText(filterAccessibleText);
        expandAllButton.setText(expandAllLabel);
        collapseAllButton.setText(collapseAllLabel);
    }

    public void expandAll() {
        TreeItem<T> root = treeView.getRoot();
        if (root == null) {
            return;
        }
        setExpandedRecursive(root, true);
    }

    public void collapseAll() {
        TreeItem<T> root = treeView.getRoot();
        if (root == null) {
            return;
        }
        for (TreeItem<T> child : root.getChildren()) {
            setExpandedRecursive(child, false);
        }
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
        if (selectedItem != null) {
            setSelectedItem(selectedItem);
        }
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

    private TreeItem<T> findItem(final TreeItem<T> root, final T value) {
        if (root == null) {
            return null;
        }
        if (Objects.equals(root.getValue(), value)) {
            return root;
        }
        for (TreeItem<T> child : root.getChildren()) {
            TreeItem<T> match = findItem(child, value);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    private void setExpandedRecursive(final TreeItem<T> item, final boolean expanded) {
        item.setExpanded(expanded);
        for (TreeItem<T> child : item.getChildren()) {
            setExpandedRecursive(child, expanded);
        }
    }
}