package com.jexam.app.ui.components.pdf;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.nio.file.Path;

/**
 * Right-hand preview region with lifecycle states.
 */
public final class PreviewRegionComponent extends VBox {
    private final Label stateLabel = new Label("No preview generated yet.");
    private final Label pathLabel = new Label();
    private final Button refreshButton = new Button("Refresh Preview");
    private final Button openExternalButton = new Button("Open External");

    private Runnable refreshHandler = () -> { };
    private Runnable openExternalHandler = () -> { };
    private Path previewPath;

    public PreviewRegionComponent() {
        setSpacing(8);
        setPadding(new Insets(8));
        refreshButton.setOnAction(event -> refreshHandler.run());
        openExternalButton.setOnAction(event -> openExternalHandler.run());
        openExternalButton.setDisable(true);
        getChildren().addAll(new Label("Preview"), stateLabel, pathLabel, refreshButton, openExternalButton);
    }

    public void setIdle() {
        stateLabel.setText("No preview generated yet.");
        pathLabel.setText("");
        previewPath = null;
        openExternalButton.setDisable(true);
    }

    public void setLoading() {
        stateLabel.setText("Generating preview...");
    }

    public void setReady(final Path path) {
        previewPath = path;
        stateLabel.setText("Preview ready");
        pathLabel.setText(path == null ? "" : path.toString());
        openExternalButton.setDisable(path == null);
    }

    public void setStale(final boolean stale) {
        if (stale) {
            stateLabel.setText("Preview is stale. Refresh required.");
        }
    }

    public void setError(final String message) {
        stateLabel.setText("Preview failed");
        pathLabel.setText(message == null ? "" : message);
    }

    public Path getPreviewPath() {
        return previewPath;
    }

    public void setOnRefresh(final Runnable handler) {
        refreshHandler = handler == null ? () -> { } : handler;
    }

    public void setOnOpenExternal(final Runnable handler) {
        openExternalHandler = handler == null ? () -> { } : handler;
    }
}