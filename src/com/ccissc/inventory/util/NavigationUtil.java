package com.ccissc.inventory.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public final class NavigationUtil {
    private static final String FXML_BASE = "/com/ccissc/inventory/fxml/";
    private static final String GLOBAL_CSS = "/com/ccissc/inventory/css/global.css";
    private static final String DARK_CSS = "/com/ccissc/inventory/css/dark.css";
    private static final String PREFS_FILE = System.getProperty("user.home")
            + File.separator + ".ccissc-inventory-prefs.properties";
    private static Stage primaryStage;
    private static boolean darkMode = false;

    private NavigationUtil() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        loadPreferences();
    }

    public static void switchTo(String fxmlFile) {
        try {
            Parent root = loadFXML(fxmlFile);
            Scene scene = new Scene(root, primaryStage.getWidth(), primaryStage.getHeight());
            applyGlobalStyles(scene);
            if (darkMode) {
                applyDarkStylesheet(scene, true);
            }
            primaryStage.setScene(scene);

            // Session timeout: start on non-login screens, stop on login
            if (fxmlFile.equalsIgnoreCase("Login.fxml")) {
                SessionTimeoutManager.getInstance().stop();
            } else {
                SessionTimeoutManager.getInstance().start(scene);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load view: " + fxmlFile, ex);
        }
    }

    public static void openModal(String fxmlFile, String title) {
        try {
            Parent root = loadFXML(fxmlFile);
            Scene scene = new Scene(root);
            applyGlobalStyles(scene);
            if (darkMode) {
                applyDarkStylesheet(scene, true);
            }

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

    // ---- Dark Mode ----

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggleDarkMode(Scene scene) {
        darkMode = !darkMode;
        applyDarkStylesheet(scene, darkMode);
        savePreferences();
    }

    public static void applyDarkStylesheet(Scene scene, boolean apply) {
        String stylesheet = NavigationUtil.class.getResource(DARK_CSS).toExternalForm();
        if (apply) {
            if (!scene.getStylesheets().contains(stylesheet)) {
                scene.getStylesheets().add(stylesheet);
            }
        } else {
            scene.getStylesheets().remove(stylesheet);
        }
    }

    // ---- Preferences persistence ----

    private static void loadPreferences() {
        File file = new File(PREFS_FILE);
        if (!file.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            darkMode = Boolean.parseBoolean(props.getProperty("dark_mode", "false"));
        } catch (IOException ignored) {
            // default to light mode
        }
    }

    private static void savePreferences() {
        try (FileOutputStream fos = new FileOutputStream(PREFS_FILE)) {
            Properties props = new Properties();
            props.setProperty("dark_mode", String.valueOf(darkMode));
            props.store(fos, "CCIS SC Inventory Preferences");
        } catch (IOException ignored) {
            // best-effort save
        }
    }

    private static Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(FXML_BASE + fxmlFile));
        return loader.load();
    }
}
