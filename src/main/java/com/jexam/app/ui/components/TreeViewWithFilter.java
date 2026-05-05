package com.jexam.app.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
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
 * @author Moritz
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

    /**
     * Creates a filterable tree view using the provided item label function.
     *
     * @param itemLabelProvider label provider used for rendering and filtering items
     */
    public TreeViewWithFilter(final Function<T, String> itemLabelProvider) {
        this.labelProvider = Objects.requireNonNull(itemLabelProvider, "itemLabelProvider");

        getStyleClass().add("tree-filter-panel");
        filterField.getStyleClass().add("tree-filter-input");
        expandAllButton.getStyleClass().add("secondary-action");
        collapseAllButton.getStyleClass().add("secondary-action");
        treeView.getStyleClass().add("tree-filter-view");

        filterField.setPromptText("Filter...");
        filterField.setAccessibleText("Filter exam chapters and tasks");
        filterField.setFocusTraversable(true);
        // ELEGANCE: Novelty - Real-time tree filtering enables interactive, responsive navigation
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
        treeActionBar.getStyleClass().add("tree-filter-actions");
        VBox header = new VBox(6, treeActionBar, filterField);
        header.getStyleClass().add("tree-filter-header");
        header.setPadding(new Insets(8));
        treeView.setShowRoot(false);
        treeView.setCellFactory(createCellFactory());

        setTop(header);
        setCenter(treeView);
    }

    /**
     * Registers a callback for tree-item selection changes.
     *
     * @param handler callback receiving selected item value; {@code null} resets to no-op
     */
    public void setOnItemSelected(final Consumer<T> handler) {
        itemSelectionHandler = handler == null ? value -> { } : handler;
    }

    /**
     * Replaces root items with a flat list converted to root-level nodes.
     *
     * @param items root-level item values
     */
    public void setRootItems(final List<T> items) {
        sourceRoots.clear();
        if (items != null) {
            for (T item : items) {
                sourceRoots.add(buildTreeItem(item));
            }
        }
        refreshTree();
    }

    /**
     * Replaces the source root with a pre-built tree item.
     *
     * @param rootItem root item, or {@code null} to clear
     */
    public void setRootItem(final TreeItem<T> rootItem) {
        sourceRoots.clear();
        if (rootItem != null) {
            sourceRoots.add(rootItem);
        }
        refreshTree();
    }

    /**
     * Programmatically selects a tree item by value when present.
     *
     * @param item value to select
     */
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

    /**
     * Exposes the underlying JavaFX tree view for advanced configuration.
     *
     * @return backing tree view instance
     */
    public TreeView<T> getTreeView() {
        return treeView;
    }

    /**
     * Requests keyboard focus for the tree control.
     */
    public void requestTreeFocus() {
        treeView.requestFocus();
    }

    /**
     * Applies localized texts for filter and expansion controls.
     *
     * @param filterPrompt prompt shown in the filter input
     * @param filterAccessibleText accessible text for the filter input
     * @param expandAllLabel label for expand-all action
     * @param collapseAllLabel label for collapse-all action
     */
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

    /**
     * Expands the full visible tree hierarchy.
     */
    public void expandAll() {
        TreeItem<T> root = treeView.getRoot();
        if (root == null) {
            return;
        }
        setExpandedRecursive(root, true);
    }

    /**
     * Collapses all visible top-level branches.
     */
    public void collapseAll() {
        TreeItem<T> root = treeView.getRoot();
        if (root == null) {
            return;
        }
        for (TreeItem<T> child : root.getChildren()) {
            setExpandedRecursive(child, false);
        }
    }

    /**
     * Creates a tree-cell factory that renders item labels via the configured label provider.
     *
     * @return tree-cell callback
     */
    private Callback<TreeView<T>, TreeCell<T>> createCellFactory() {
        return view -> new TreeCell<>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labelProvider.apply(item));
            }
        };
    }

    /**
     * Rebuilds the visible tree from source roots and current filter text.
     */
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

    /**
     * Wraps a raw value into a tree item.
     *
     * @param value item value
     * @return tree item wrapping value
     */
    private TreeItem<T> buildTreeItem(final T value) {
        TreeItem<T> item = new TreeItem<>(value);
        return item;
    }

    /**
     * Creates a filtered tree copy keeping matching nodes and ancestor paths.
     *
     * @param sourceItem source tree node
     * @param filterText normalized filter text
     * @return filtered node copy, or {@code null} when fully filtered out
     */
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

    /**
     * Finds the first tree node matching the provided value.
     *
     * @param root subtree root to search
     * @param value value to match
     * @return matching tree item, or {@code null} when absent
     */
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

    /**
     * Sets expansion state recursively for a subtree.
     *
     * @param item subtree root item
     * @param expanded desired expansion state
     */
    private void setExpandedRecursive(final TreeItem<T> item, final boolean expanded) {
        item.setExpanded(expanded);
        for (TreeItem<T> child : item.getChildren()) {
            setExpandedRecursive(child, expanded);
        }
    }
}
