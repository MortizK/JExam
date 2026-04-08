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
    /**
     * Prevents instantiation of this static utility class.
     */
    private DeleteConfirmationDialog() {
    }

    /**
     * Shows a confirmation dialog for destructive operations.
     *
     * @param owner owner window, or {@code null} for default ownership
     * @param title dialog title
     * @param message header message describing the operation
     * @param cascadeMessage detail message describing follow-up effects
     * @return {@code true} when user confirms, otherwise {@code false}
     */
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