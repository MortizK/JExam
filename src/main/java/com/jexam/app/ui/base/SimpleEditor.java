package com.jexam.app.ui.base;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base class for inline editors that keep a draft value and can save on change.
 *
 * @param <T> edited value type
 * @author Moritz
 */
public abstract class SimpleEditor<T> extends VBox {
    private final ObjectProperty<T> value = new SimpleObjectProperty<>();
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private Consumer<T> saveHandler;

    /**
     * <p>Constructor for SimpleEditor.</p>
     */
    protected SimpleEditor() {
    }

    /**
     * <p>Setter for the field <code>value</code>.</p>
     *
     * @param newValue a T object
     */
    public final void setValue(final T newValue) {
        value.set(newValue);
        loadValue(newValue);
        dirty.set(false);
    }

    /**
     * <p>Getter for the field <code>value</code>.</p>
     *
     * @return a T object
     */
    public final T getValue() {
        return value.get();
    }

    /**
     * <p>valueProperty.</p>
     *
     * @return a {@link javafx.beans.property.ObjectProperty} object
     */
    public final ObjectProperty<T> valueProperty() {
        return value;
    }

    /**
     * <p>dirtyProperty.</p>
     *
     * @return a {@link javafx.beans.property.BooleanProperty} object
     */
    public final BooleanProperty dirtyProperty() {
        return dirty;
    }

    /**
     * <p>isDirty.</p>
     *
     * @return a boolean
     */
    public final boolean isDirty() {
        return dirty.get();
    }

    /**
     * <p>setOnSave.</p>
     *
     * @param handler a {@link java.util.function.Consumer} object
     */
    public final void setOnSave(final Consumer<T> handler) {
        this.saveHandler = handler;
    }

    /**
     * <p>markDirty.</p>
     */
    protected final void markDirty() {
        dirty.set(true);
    }

    /**
     * <p>markSaved.</p>
     *
     * @param savedValue a T object
     */
    protected final void markSaved(final T savedValue) {
        value.set(savedValue);
        dirty.set(false);
    }

    /**
     * <p>saveDraft.</p>
     *
     * @param draftValue a T object
     */
    protected final void saveDraft(final T draftValue) {
        if (saveHandler != null) {
            saveHandler.accept(draftValue);
        }
        value.set(draftValue);
        dirty.set(false);
    }

    /**
     * <p>saveIfChanged.</p>
     *
     * @param draftValue a T object
     */
    protected final void saveIfChanged(final T draftValue) {
        if (!Objects.equals(value.get(), draftValue)) {
            saveDraft(draftValue);
        } else {
            dirty.set(false);
        }
    }

    /**
     * <p>loadValue.</p>
     *
     * @param value a T object
     */
    protected abstract void loadValue(T value);
}
