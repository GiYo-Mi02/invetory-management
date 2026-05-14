package com.ccissc.inventory.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Non-blocking toast / snackbar notification that appears at the bottom
 * of the current scene and auto-dismisses after 3 seconds.
 * Supports typed variants (success, error, warning, info) with FontAwesome icons.
 */
public final class ToastUtil {
    private ToastUtil() {
    }

    /** Toast severity levels with associated icon literals and CSS classes. */
    public enum Level {
        SUCCESS("fas-circle-check",       "icon-success"),
        ERROR("fas-circle-exclamation",   "icon-danger"),
        WARNING("fas-triangle-exclamation","icon-warning"),
        INFO("fas-circle-info",           "icon-info");

        private final String iconLiteral;
        private final String iconStyleClass;

        Level(String iconLiteral, String iconStyleClass) {
            this.iconLiteral = iconLiteral;
            this.iconStyleClass = iconStyleClass;
        }
    }

    /**
     * Show a default success toast message at the bottom of the given scene.
     */
    public static void show(Scene scene, String message) {
        show(scene, message, Level.SUCCESS);
    }

    /**
     * Show a typed toast with an icon.
     */
    public static void show(Scene scene, String message, Level level) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }

        // Build icon
        FontIcon icon = new FontIcon(level.iconLiteral);
        icon.setIconSize(16);
        icon.getStyleClass().add(level.iconStyleClass);

        // Build label
        Label textLabel = new Label(message);
        textLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Container
        HBox toast = new HBox(8, icon, textLabel);
        toast.setAlignment(Pos.CENTER);
        toast.getStyleClass().add("toast");
        toast.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.92);"
                        + "-fx-padding: 12 24;"
                        + "-fx-background-radius: 8;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0.3, 0, 4);");
        toast.setMouseTransparent(true);
        toast.setOpacity(0);

        // We need a StackPane overlay. If the root is already a StackPane, use it;
        // otherwise wrap the existing root.
        StackPane overlay;
        if (scene.getRoot() instanceof StackPane) {
            overlay = (StackPane) scene.getRoot();
        } else {
            // Attach to whatever pane is the root by adding a floating StackPane layer
            overlay = findOrCreateOverlay(scene);
        }

        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        toast.setTranslateY(-24);
        overlay.getChildren().add(toast);

        // Animations: slide up + fade in → hold → fade out
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(250), toast);
        slideIn.setFromY(20);
        slideIn.setToY(-24);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition sequence = new SequentialTransition(fadeIn, hold, fadeOut);
        slideIn.play();
        sequence.setOnFinished(e -> overlay.getChildren().remove(toast));
        sequence.play();
    }

    /**
     * Attempts to wrap the scene root in a StackPane so we can overlay toasts.
     */
    private static StackPane findOrCreateOverlay(Scene scene) {
        // If the root is a Pane but not a StackPane, wrap it
        if (scene.getRoot() instanceof Pane) {
            Pane original = (Pane) scene.getRoot();
            StackPane wrapper = new StackPane();
            // Move children to wrapper isn't safe; instead just add an overlay pane on top
            // We'll add the toast directly to the existing pane, positioned at bottom
            // For simplicity, cast to StackPane by wrapping
            scene.setRoot(wrapper);
            wrapper.getChildren().add(original);
            return wrapper;
        }
        // Fallback: wrap in StackPane
        StackPane wrapper = new StackPane(scene.getRoot());
        scene.setRoot(wrapper);
        return wrapper;
    }
}
