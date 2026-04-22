package com.jexam.app.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Tracks UI state that spans multiple components, such as unsaved changes and stale previews.
 *
 * @author Moritz
 */
public final class UiStateManager {
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final BooleanProperty previewStale = new SimpleBooleanProperty(false);

    /**
     * <p>dirtyProperty.</p>
     *
     * @return a {@link javafx.beans.property.BooleanProperty} object
     */
    public BooleanProperty dirtyProperty() {
        return dirty;
    }

    /**
     * <p>isDirty.</p>
     *
     * @return a boolean
     */
    public boolean isDirty() {
        return dirty.get();
    }

    /**
     * <p>Setter for the field <code>dirty</code>.</p>
     *
     * @param value a boolean
     */
    public void setDirty(final boolean value) {
        dirty.set(value);
    }

    /**
     * <p>markDirty.</p>
     */
    public void markDirty() {
        dirty.set(true);
    }

    /**
     * <p>markSaved.</p>
     */
    public void markSaved() {
        dirty.set(false);
    }

    /**
     * <p>previewStaleProperty.</p>
     *
     * @return a {@link javafx.beans.property.BooleanProperty} object
     */
    public BooleanProperty previewStaleProperty() {
        return previewStale;
    }

    /**
     * <p>isPreviewStale.</p>
     *
     * @return a boolean
     */
    public boolean isPreviewStale() {
        return previewStale.get();
    }

    /**
     * <p>Setter for the field <code>previewStale</code>.</p>
     *
     * @param value a boolean
     */
    public void setPreviewStale(final boolean value) {
        previewStale.set(value);
    }

    /**
     * <p>markPreviewStale.</p>
     */
    public void markPreviewStale() {
        previewStale.set(true);
    }

    /**
     * <p>clearPreviewStale.</p>
     */
    public void clearPreviewStale() {
        previewStale.set(false);
    }
}
