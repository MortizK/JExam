package com.jexam.app.ui.base;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

/**
 * Base class for navigation-oriented UI components that expose a selected item.
 *
 * @param <T> selected hierarchy element type
 */
public abstract class HierarchyComponent<T> extends Region {
    private final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>();
    private Consumer<T> selectionHandler;

    public final ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    public final T getSelectedItem() {
        return selectedItem.get();
    }

    public final void setOnSelectionChanged(final Consumer<T> handler) {
        this.selectionHandler = handler;
    }

    protected final void selectItem(final T item) {
        selectedItem.set(item);
        if (selectionHandler != null) {
            selectionHandler.accept(item);
        }
    }
}