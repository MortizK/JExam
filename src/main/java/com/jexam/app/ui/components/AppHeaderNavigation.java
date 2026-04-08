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
    private final Label dirtyIndicator = new Label();
    private final Button newButton = new Button();
    private final Button openButton = new Button();
    private final Button saveButton = new Button();
    private final ComboBox<UiLanguage> languageBox = new ComboBox<>();

    private Consumer<UiLanguage> languageHandler = language -> { };
    private Runnable newHandler = () -> { };
    private Runnable openHandler = () -> { };
    private Runnable saveHandler = () -> { };
    private Runnable validateHandler = () -> { };

    /**
     * Creates the application header with global actions and language selector.
     */
    public AppHeaderNavigation() {
        getStyleClass().add("app-header");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(10));
        setMinHeight(Region.USE_PREF_SIZE);

        dirtyIndicator.getStyleClass().add("dirty-indicator");
        newButton.getStyleClass().add("secondary-action");
        openButton.getStyleClass().add("secondary-action");
        saveButton.getStyleClass().add("primary-action");
        languageBox.getStyleClass().add("language-selector");

        dirtyIndicator.setVisible(false);
        dirtyIndicator.managedProperty().bind(dirtyIndicator.visibleProperty());

        newButton.setOnAction(event -> newHandler.run());
        openButton.setOnAction(event -> openHandler.run());
        saveButton.setOnAction(event -> saveHandler.run());

        dirtyIndicator.setAccessibleText("Unsaved changes indicator");
        newButton.setAccessibleText("Create a new exam in memory");
        openButton.setAccessibleText("Open an existing exam XML file");
        saveButton.setAccessibleText("Save the current exam to XML");
        languageBox.setAccessibleText("Language selector");

        languageBox.setItems(FXCollections.observableArrayList(UiLanguage.values()));
        languageBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !Objects.equals(oldValue, newValue)) {
                languageHandler.accept(newValue);
            }
        });

        getChildren().addAll(
            newButton,
            openButton,
            saveButton,
            languageBox,
            dirtyIndicator
        );
    }

    /**
     * Toggles the unsaved-changes indicator.
     *
     * @param dirty whether unsaved changes are present
     */
    public void setDirty(final boolean dirty) {
        dirtyIndicator.setText(dirty ? "Unsaved changes" : "");
        dirtyIndicator.setVisible(dirty);
    }

    /**
     * Applies localized labels for the global action buttons.
     *
     * @param newText label for the create action
     * @param openText label for the open action
     * @param saveText label for the save action
     * @param validateText label for the validate action
     */
    public void setButtonText(
        final String newText,
        final String openText,
        final String saveText,
        final String validateText
    ) {
        newButton.setText(newText);
        openButton.setText(openText);
        saveButton.setText(saveText);
    }

    /**
     * Sets the prompt text shown for language selection.
     *
     * @param value localized prompt value
     */
    public void setLanguageLabel(final String value) {
        languageBox.setPromptText(value);
    }

    /**
     * Updates the currently selected language in the dropdown.
     *
     * @param value selected language value
     */
    public void setSelectedLanguage(final UiLanguage value) {
        languageBox.setValue(value);
    }

    /**
     * Registers a callback for language changes.
     *
     * @param handler callback receiving the new language; {@code null} resets to no-op
     */
    public void setOnLanguageChanged(final Consumer<UiLanguage> handler) {
        languageHandler = handler == null ? language -> { } : handler;
    }

    /**
     * Registers the new-exam action.
     *
     * @param handler callback for create action; {@code null} resets to no-op
     */
    public void setOnNew(final Runnable handler) {
        newHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Registers the open-exam action.
     *
     * @param handler callback for open action; {@code null} resets to no-op
     */
    public void setOnOpen(final Runnable handler) {
        openHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Registers the save-exam action.
     *
     * @param handler callback for save action; {@code null} resets to no-op
     */
    public void setOnSave(final Runnable handler) {
        saveHandler = handler == null ? () -> { } : handler;
    }

    /**
     * Registers the validate action.
     *
     * @param handler callback for validation action; {@code null} resets to no-op
     */
    public void setOnValidate(final Runnable handler) {
        validateHandler = handler == null ? () -> { } : handler;
    }
}