package br.edu.ifba.inf008.plugins.ecommerce.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;

/**
 * Themed alert helpers. Dialogs own their scene, so the plugin stylesheet is
 * attached explicitly to keep them visually consistent with the views.
 */
final class Dialogs {

    private Dialogs() {
    }

    static void info(String message) {
        show(new Alert(Alert.AlertType.INFORMATION, message));
    }

    static void warn(String message) {
        show(new Alert(Alert.AlertType.WARNING, message));
    }

    static void error(String message) {
        show(new Alert(Alert.AlertType.ERROR, message));
    }

    /** Applies the theme to any dialog (also used by input dialogs). */
    static void applyTheme(Dialog<?> dialog) {
        Theme.apply(dialog.getDialogPane());
    }

    private static void show(Alert alert) {
        applyTheme(alert);
        alert.showAndWait();
    }
}
