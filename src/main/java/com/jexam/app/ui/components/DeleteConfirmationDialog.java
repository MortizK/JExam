package com.jexam.app.ui.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Reusable delete confirmation dialog for destructive actions.
 */
public final class DeleteConfirmationDialog {
    private DeleteConfirmationDialog() {
    }

    public static boolean confirm(
        final Window owner,
        final String title,
        final String message,
        final String cascadeMessage
    ) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(cascadeMessage);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE;
    }
}