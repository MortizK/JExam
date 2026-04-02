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

    public XmlLoadingState() {
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(24));

        createButton.setOnAction(event -> createHandler.accept(null));
        loadButton.setOnAction(event -> loadHandler.accept(null));

        getChildren().addAll(titleLabel, subtitleLabel, createButton, loadButton);
    }

    public void setTitleText(final String value) {
        titleLabel.setText(value);
    }

    public void setSubtitleText(final String value) {
        subtitleLabel.setText(value);
    }

    public void setCreateButtonText(final String value) {
        createButton.setText(value);
    }

    public void setLoadButtonText(final String value) {
        loadButton.setText(value);
    }

    public void setOnCreateNewExam(final Runnable handler) {
        createHandler = ignored -> {
            if (handler != null) {
                handler.run();
            }
        };
    }

    public void setOnLoadXml(final Runnable handler) {
        loadHandler = ignored -> {
            if (handler != null) {
                handler.run();
            }
        };
    }
}