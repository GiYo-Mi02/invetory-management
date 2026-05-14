package com.ccissc.inventory.controller;

import com.ccissc.inventory.service.AuthService;
import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.SessionManager;
import com.ccissc.inventory.util.SessionTimeoutManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.kordamp.ikonli.javafx.FontIcon;

public class NavbarController {
    @FXML
    private Label userLabel;

    @FXML
    private Button themeToggleButton;

    @FXML
    private FontIcon themeIcon;

    @FXML
    private ImageView logoImage;

    @FXML
    private FontIcon logoFallbackIcon;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            userLabel.setText(SessionManager.getCurrentUser().getFullName());
        }
        updateThemeIcon();
        setupLogoFallback();
    }

    @FXML
    private void onLogout() {
        SessionTimeoutManager.getInstance().stop();
        authService.logout();
        NavigationUtil.switchTo("Login.fxml");
    }

    @FXML
    private void onToggleTheme() {
        if (themeToggleButton.getScene() != null) {
            NavigationUtil.toggleDarkMode(themeToggleButton.getScene());
            updateThemeIcon();
        }
    }

    private void updateThemeIcon() {
        if (themeIcon != null) {
            themeIcon.setIconLiteral(NavigationUtil.isDarkMode() ? "fas-sun" : "fas-moon");
        }
    }

    /**
     * If the logo image failed to load (e.g. resource missing), hide the ImageView
     * and show the fallback FontIcon (fas-store) instead.
     */
    private void setupLogoFallback() {
        if (logoImage != null && logoImage.getImage() != null) {
            logoImage.getImage().errorProperty().addListener((obs, wasError, isError) -> {
                if (isError) {
                    showLogoFallback();
                }
            });
            // Check if it already errored before the listener was added
            if (logoImage.getImage().isError()) {
                showLogoFallback();
            }
        } else if (logoImage != null && logoImage.getImage() == null) {
            showLogoFallback();
        }
    }

    private void showLogoFallback() {
        logoImage.setVisible(false);
        logoImage.setManaged(false);
        if (logoFallbackIcon != null) {
            logoFallbackIcon.setVisible(true);
            logoFallbackIcon.setManaged(true);
        }
    }
}
