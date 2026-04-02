package com.jexam.app.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Tracks UI state that spans multiple components, such as unsaved changes and stale previews.
 */
public final class UiStateManager {
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final BooleanProperty previewStale = new SimpleBooleanProperty(false);

    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void setDirty(final boolean value) {
        dirty.set(value);
    }

    public void markDirty() {
        dirty.set(true);
    }

    public void markSaved() {
        dirty.set(false);
    }

    public BooleanProperty previewStaleProperty() {
        return previewStale;
    }

    public boolean isPreviewStale() {
        return previewStale.get();
    }

    public void setPreviewStale(final boolean value) {
        previewStale.set(value);
    }

    public void markPreviewStale() {
        previewStale.set(true);
    }

    public void clearPreviewStale() {
        previewStale.set(false);
    }
}