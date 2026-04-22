package com.jexam.app.ui.base;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

import java.util.function.Consumer;

/**
 * Base class for navigation-oriented UI components that expose a selected item.
 *
 * @param <T> selected hierarchy element type
 * @author Moritz
 */
public abstract class HierarchyComponent<T> extends Region {
    private final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>();
    private Consumer<T> selectionHandler;

    /**
     * <p>selectedItemProperty.</p>
     *
     * @return a {@link javafx.beans.property.ObjectProperty} object
     */
    public final ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    /**
     * <p>Getter for the field <code>selectedItem</code>.</p>
     *
     * @return a T object
     */
    public final T getSelectedItem() {
        return selectedItem.get();
    }

    /**
     * <p>setOnSelectionChanged.</p>
     *
     * @param handler a {@link java.util.function.Consumer} object
     */
    public final void setOnSelectionChanged(final Consumer<T> handler) {
        this.selectionHandler = handler;
    }

    /**
     * <p>selectItem.</p>
     *
     * @param item a T object
     */
    protected final void selectItem(final T item) {
        selectedItem.set(item);
        if (selectionHandler != null) {
            selectionHandler.accept(item);
        }
    }
}
