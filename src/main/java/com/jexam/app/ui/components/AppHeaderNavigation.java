package com.jexam.app.ui.components;

import com.jexam.app.UiLanguage;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Top-level app header with tab switching, global actions, language selection, and dirty state.
 */
public final class AppHeaderNavigation extends HBox {
    private final Label titleLabel = new Label();
    private final Label dirtyIndicator = new Label();
    private final Button newButton = new Button();
    private final Button openButton = new Button();
    private final Button saveButton = new Button();
    private final Button validateButton = new Button();
    private final Button previewButton = new Button();
    private final ComboBox<UiLanguage> languageBox = new ComboBox<>();

    private Consumer<UiLanguage> languageHandler = language -> { };
    private Runnable newHandler = () -> { };
    private Runnable openHandler = () -> { };
    private Runnable saveHandler = () -> { };
    private Runnable validateHandler = () -> { };
    private Runnable previewHandler = () -> { };

    public AppHeaderNavigation() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(10));
        setMinHeight(Region.USE_PREF_SIZE);

        titleLabel.getStyleClass().add("app-header-title");
        dirtyIndicator.setVisible(false);
        dirtyIndicator.managedProperty().bind(dirtyIndicator.visibleProperty());

        newButton.setOnAction(event -> newHandler.run());
        openButton.setOnAction(event -> openHandler.run());
        saveButton.setOnAction(event -> saveHandler.run());
        validateButton.setOnAction(event -> validateHandler.run());
        previewButton.setOnAction(event -> previewHandler.run());

        titleLabel.setAccessibleText("Application title");
        dirtyIndicator.setAccessibleText("Unsaved changes indicator");
        newButton.setAccessibleText("Create a new exam in memory");
        openButton.setAccessibleText("Open an existing exam XML file");
        saveButton.setAccessibleText("Save the current exam to XML");
        validateButton.setAccessibleText("Validate the current exam");
        previewButton.setAccessibleText("Generate or open the PDF preview");
        languageBox.setAccessibleText("Language selector");

        languageBox.setItems(FXCollections.observableArrayList(UiLanguage.values()));
        languageBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !Objects.equals(oldValue, newValue)) {
                languageHandler.accept(newValue);
            }
        });

        getChildren().addAll(
            titleLabel,
            dirtyIndicator,
            newButton,
            openButton,
            saveButton,
            validateButton,
            previewButton,
            languageBox
        );
    }

    public void setTitle(final String value) {
        titleLabel.setText(value);
    }

    public void setDirty(final boolean dirty) {
        dirtyIndicator.setText(dirty ? "Unsaved changes" : "");
        dirtyIndicator.setVisible(dirty);
    }

    public void setButtonText(
        final String newText,
        final String openText,
        final String saveText,
        final String validateText,
        final String previewText
    ) {
        newButton.setText(newText);
        openButton.setText(openText);
        saveButton.setText(saveText);
        validateButton.setText(validateText);
        previewButton.setText(previewText);
    }

    public void setLanguageLabel(final String value) {
        languageBox.setPromptText(value);
    }

    public void setSelectedLanguage(final UiLanguage value) {
        languageBox.setValue(value);
    }

    public void setOnLanguageChanged(final Consumer<UiLanguage> handler) {
        languageHandler = handler == null ? language -> { } : handler;
    }

    public void setOnNew(final Runnable handler) {
        newHandler = handler == null ? () -> { } : handler;
    }

    public void setOnOpen(final Runnable handler) {
        openHandler = handler == null ? () -> { } : handler;
    }

    public void setOnSave(final Runnable handler) {
        saveHandler = handler == null ? () -> { } : handler;
    }

    public void setOnValidate(final Runnable handler) {
        validateHandler = handler == null ? () -> { } : handler;
    }

    public void setOnPreview(final Runnable handler) {
        previewHandler = handler == null ? () -> { } : handler;
    }
}