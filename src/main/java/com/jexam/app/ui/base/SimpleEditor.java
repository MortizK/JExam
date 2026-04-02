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
 */
public abstract class SimpleEditor<T> extends VBox {
    private final ObjectProperty<T> value = new SimpleObjectProperty<>();
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private Consumer<T> saveHandler;

    protected SimpleEditor() {
    }

    public final void setValue(final T newValue) {
        value.set(newValue);
        loadValue(newValue);
        dirty.set(false);
    }

    public final T getValue() {
        return value.get();
    }

    public final ObjectProperty<T> valueProperty() {
        return value;
    }

    public final BooleanProperty dirtyProperty() {
        return dirty;
    }

    public final boolean isDirty() {
        return dirty.get();
    }

    public final void setOnSave(final Consumer<T> handler) {
        this.saveHandler = handler;
    }

    protected final void markDirty() {
        dirty.set(true);
    }

    protected final void markSaved(final T savedValue) {
        value.set(savedValue);
        dirty.set(false);
    }

    protected final void saveDraft(final T draftValue) {
        if (saveHandler != null) {
            saveHandler.accept(draftValue);
        }
        value.set(draftValue);
        dirty.set(false);
    }

    protected final void saveIfChanged(final T draftValue) {
        if (!Objects.equals(value.get(), draftValue)) {
            saveDraft(draftValue);
        } else {
            dirty.set(false);
        }
    }

    protected abstract void loadValue(T value);
}