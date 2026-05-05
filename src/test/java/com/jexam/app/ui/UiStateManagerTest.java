package com.jexam.app.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiStateManagerTest {
    @Test
    void dirtyStateShouldToggleAndExposeProperty() {
        UiStateManager state = new UiStateManager();

        assertFalse(state.isDirty());
        assertFalse(state.dirtyProperty().get());

        state.markDirty();
        assertTrue(state.isDirty());
        assertTrue(state.dirtyProperty().get());

        state.setDirty(false);
        assertFalse(state.isDirty());

        assertSame(state.dirtyProperty(), state.dirtyProperty());
    }

    @Test
    void previewStaleStateShouldToggleAndExposeProperty() {
        UiStateManager state = new UiStateManager();

        assertFalse(state.isPreviewStale());
        assertFalse(state.previewStaleProperty().get());

        state.markPreviewStale();
        assertTrue(state.isPreviewStale());
        assertTrue(state.previewStaleProperty().get());

        state.clearPreviewStale();
        assertFalse(state.isPreviewStale());

        state.setPreviewStale(true);
        assertTrue(state.isPreviewStale());
    }
}