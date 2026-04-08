package com.jexam.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Displays the current hierarchy path and allows navigation to ancestors.
 */
public final class BreadcrumbNavigation extends HBox {
    private static final int MAX_SEGMENT_LENGTH = 30;

    private final List<String> segments = new ArrayList<>();
    private IntConsumer selectionHandler = index -> { };

    /**
     * Creates the breadcrumb container used for hierarchy navigation.
     */
    public BreadcrumbNavigation() {
        getStyleClass().add("breadcrumb");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(6);
        setPadding(new Insets(6, 10, 6, 10));
    }

    /**
     * Replaces the breadcrumb path and re-renders visible segments.
     *
     * @param pathSegments ordered path segments from root to current item
     */
    public void setPath(final List<String> pathSegments) {
        segments.clear();
        if (pathSegments != null) {
            segments.addAll(pathSegments);
        }
        render();
    }

    /**
     * Registers a callback for ancestor-segment clicks.
     *
     * @param handler callback receiving clicked segment index; {@code null} resets to no-op
     */
    public void setOnSegmentClicked(final IntConsumer handler) {
        selectionHandler = handler == null ? index -> { } : handler;
    }

    /**
     * Rebuilds visual breadcrumb controls from current path segments.
     */
    private void render() {
        getChildren().clear();
        for (int i = 0; i < segments.size(); i++) {
            final int segmentIndex = i;
            final String segment = segments.get(i);
            final String displaySegment = shorten(segment);
            if (i > 0) {
                Label separator = new Label(">");
                separator.getStyleClass().add("breadcrumb-separator");
                getChildren().add(separator);
            }

            if (i == segments.size() - 1) {
                Label current = new Label(displaySegment);
                current.getStyleClass().add("breadcrumb-current");
                getChildren().add(current);
            } else {
                Button button = new Button(displaySegment);
                button.setOnAction(event -> selectionHandler.accept(segmentIndex));
                button.getStyleClass().add("breadcrumb-link");
                getChildren().add(button);
            }
        }
    }

    /**
     * Shortens a segment to a stable maximum display length.
     *
     * @param segment raw segment text
     * @return shortened display text
     */
    private String shorten(final String segment) {
        if (segment == null) {
            return "";
        }
        String normalized = segment.trim();
        if (normalized.length() <= MAX_SEGMENT_LENGTH) {
            return normalized;
        }

        String candidate = normalized.substring(0, MAX_SEGMENT_LENGTH).trim();
        int lastWhitespace = lastWhitespaceIndex(candidate);
        if (lastWhitespace > 0) {
            candidate = candidate.substring(0, lastWhitespace).trim();
        }
        if (candidate.isEmpty()) {
            candidate = normalized.substring(0, MAX_SEGMENT_LENGTH).trim();
        }
        return candidate + "...";
    }

    /**
     * Finds the last whitespace index in a value.
     *
     * @param value input string
     * @return index of last whitespace, or -1 when absent
     */
    private int lastWhitespaceIndex(final String value) {
        for (int i = value.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}