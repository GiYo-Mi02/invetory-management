package com.ccissc.inventory.util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class NavigationUtil {
    private static final String FXML_BASE = "/com/ccissc/inventory/fxml/";
    private static final String GLOBAL_CSS = "/com/ccissc/inventory/css/global.css";
    private static Stage primaryStage;

    private NavigationUtil() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void switchTo(String fxmlFile) {
        try {
            Parent root = loadFXML(fxmlFile);
            Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
            applyGlobalStyles(scene);
            primaryStage.setScene(scene);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load view: " + fxmlFile, ex);
        }
    }

    public static void openModal(String fxmlFile, String title) {
        try {
            Parent root = loadFXML(fxmlFile);
            Scene scene = new Scene(root);
            applyGlobalStyles(scene);

            Stage modal = new Stage();
            modal.initOwner(primaryStage);
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(title);
            modal.setScene(scene);
            modal.showAndWait();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to open modal: " + fxmlFile, ex);
        }
    }

    public static void applyGlobalStyles(Scene scene) {
        String stylesheet = NavigationUtil.class.getResource(GLOBAL_CSS).toExternalForm();
        if (!scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
        }
    }

    private static Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(FXML_BASE + fxmlFile));
        return loader.load();
    }
}
