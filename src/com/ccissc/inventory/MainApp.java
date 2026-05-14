package com.ccissc.inventory;

import com.ccissc.inventory.config.DatabaseConfig;
import com.ccissc.inventory.util.NavigationUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static final String APP_TITLE = "CCIS SC Inventory Management System";
    private static final String LOGIN_FXML = "/com/ccissc/inventory/fxml/Login.fxml";
    private static final String APP_ICON = "/com/ccissc/inventory/images/ccis-sc-logo.png";

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        NavigationUtil.init(primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
        Scene scene = new Scene(loader.load(), 1100, 700);
        NavigationUtil.applyGlobalStyles(scene);

        primaryStage.setTitle(APP_TITLE);
        Image icon = loadIcon();
        if (icon != null) {
            primaryStage.getIcons().add(icon);
        }
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            DatabaseConfig.getInstance().shutdown();
            Platform.exit();
        });
    }

    private Image loadIcon() {
        try {
            return new Image(getClass().getResourceAsStream(APP_ICON));
        } catch (Exception ex) {
            return null;
        }
    }
}
