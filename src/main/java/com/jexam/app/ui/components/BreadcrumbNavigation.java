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

    public BreadcrumbNavigation() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(6);
        setPadding(new Insets(6, 10, 6, 10));
    }

    public void setPath(final List<String> pathSegments) {
        segments.clear();
        if (pathSegments != null) {
            segments.addAll(pathSegments);
        }
        render();
    }

    public void setOnSegmentClicked(final IntConsumer handler) {
        selectionHandler = handler == null ? index -> { } : handler;
    }

    private void render() {
        getChildren().clear();
        for (int i = 0; i < segments.size(); i++) {
            final int segmentIndex = i;
            final String segment = segments.get(i);
            final String displaySegment = shorten(segment);
            if (i > 0) {
                getChildren().add(new Label(">"));
            }

            if (i == segments.size() - 1) {
                getChildren().add(new Label(displaySegment));
            } else {
                Button button = new Button(displaySegment);
                button.setOnAction(event -> selectionHandler.accept(segmentIndex));
                button.getStyleClass().add("breadcrumb-link");
                getChildren().add(button);
            }
        }
    }

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

    private int lastWhitespaceIndex(final String value) {
        for (int i = value.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(value.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}