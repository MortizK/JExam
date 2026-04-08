package com.jexam.app.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Empty state shown when no XML has been loaded yet.
 */
public final class XmlLoadingState extends VBox {
    private final Label titleLabel = new Label("No XML loaded");
    private final Label subtitleLabel = new Label("Create a new exam or load an XML file to begin.");
    private final Button createButton = new Button("Create New Exam");
    private final Button loadButton = new Button("Load XML");

    private Consumer<Void> createHandler = ignored -> { };
    private Consumer<Void> loadHandler = ignored -> { };

    /**
     * Creates the XML empty/loading state panel with create and load actions.
     */
    public XmlLoadingState() {
        getStyleClass().add("xml-loading-state");
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(24));

        titleLabel.getStyleClass().add("loading-title");
        subtitleLabel.getStyleClass().add("loading-subtitle");
        createButton.getStyleClass().add("primary-action");
        loadButton.getStyleClass().add("secondary-action");
        createButton.setMaxWidth(Double.MAX_VALUE);
        loadButton.setMaxWidth(Double.MAX_VALUE);

        createButton.setOnAction(event -> createHandler.accept(null));
        loadButton.setOnAction(event -> loadHandler.accept(null));

        titleLabel.setAccessibleText("Empty XML title");
        subtitleLabel.setAccessibleText("Empty XML instructions");
        createButton.setAccessibleText("Create a new exam");
        loadButton.setAccessibleText("Load an exam from XML");

        getChildren().addAll(titleLabel, subtitleLabel, createButton, loadButton);
    }

    /**
     * Sets the loading-state title text.
     *
     * @param value title text
     */
    public void setTitleText(final String value) {
        titleLabel.setText(value);
    }

    /**
     * Sets the loading-state subtitle text.
     *
     * @param value subtitle text
     */
    public void setSubtitleText(final String value) {
        subtitleLabel.setText(value);
    }

    /**
     * Sets the create button label.
     *
     * @param value button text
     */
    public void setCreateButtonText(final String value) {
        createButton.setText(value);
    }

    /**
     * Sets the load button label.
     *
     * @param value button text
     */
    public void setLoadButtonText(final String value) {
        loadButton.setText(value);
    }

    /**
     * Registers callback for the create-new-exam action.
     *
     * @param handler action callback; {@code null} clears action behavior
     */
    public void setOnCreateNewExam(final Runnable handler) {
        createHandler = ignored -> {
            if (handler != null) {
                handler.run();
            }
        };
    }

    /**
     * Registers callback for the load-XML action.
     *
     * @param handler action callback; {@code null} clears action behavior
     */
    public void setOnLoadXml(final Runnable handler) {
        loadHandler = ignored -> {
            if (handler != null) {
                handler.run();
            }
        };
    }
}