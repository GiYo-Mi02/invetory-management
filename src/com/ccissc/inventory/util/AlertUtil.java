package com.ccissc.inventory.util;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.kordamp.ikonli.javafx.FontIcon;

public final class AlertUtil {
    private AlertUtil() {
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        setDialogIcon(alert, "fas-circle-info", "icon-info", 28);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        setDialogIcon(alert, "fas-circle-exclamation", "icon-danger", 28);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Choose icon based on context keywords
        String iconLiteral;
        String iconClass;
        if (title.toLowerCase().contains("delete") || title.toLowerCase().contains("archive")) {
            iconLiteral = "fas-trash-can";
            iconClass = "icon-danger";
        } else if (title.toLowerCase().contains("logout")) {
            iconLiteral = "fas-right-from-bracket";
            iconClass = "icon-warning";
        } else {
            iconLiteral = "fas-triangle-exclamation";
            iconClass = "icon-warning";
        }
        setDialogIcon(alert, iconLiteral, iconClass, 28);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Sets a FontIcon graphic on the alert dialog's header area.
     */
    private static void setDialogIcon(Alert alert, String iconLiteral, String styleClass, int size) {
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(size);
        icon.getStyleClass().add(styleClass);
        alert.setGraphic(icon);
    }
}
