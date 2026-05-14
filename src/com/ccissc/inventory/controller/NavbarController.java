package com.ccissc.inventory.controller;

import com.ccissc.inventory.service.AuthService;
import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class NavbarController {
    @FXML
    private Label userLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            userLabel.setText(SessionManager.getCurrentUser().getFullName());
        }
    }

    @FXML
    private void onLogout() {
        authService.logout();
        NavigationUtil.switchTo("Login.fxml");
    }
}
