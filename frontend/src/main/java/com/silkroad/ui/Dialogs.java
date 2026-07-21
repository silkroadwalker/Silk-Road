package com.silkroad.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Small helper so every controller doesn't have to rebuild
 * the same Alert boilerplate.
 */
public final class Dialogs {

    private Dialogs() {}

    /**
     * displays an error alert dialog with the given message.
     * the dialog has no header and waits for the user to close it.
     *
     * @param message the error message to show
     */
    public static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /**
     * displays an information alert dialog with the given message.
     * the dialog has no header and waits for the user to close it.
     *
     * @param message the information message to show
     */
    public static void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    /**
     * displays a confirmation dialog with yes and no buttons and returns
     * the user's choice. the dialog has no header.
     *
     * @param message the confirmation prompt text
     * @return true if the user clicked yes, false otherwise
     */
    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    /**
     * displays a text input dialog with a title, header, and prompt text.
     *
     * @param title      the dialog window title
     * @param header     the dialog header text
     * @param promptText the label next to the input field
     * @return an optional containing the user's input, or empty if cancelled
     */
    public static Optional<String> prompt(String title, String header, String promptText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(promptText);
        return dialog.showAndWait();
    }
}