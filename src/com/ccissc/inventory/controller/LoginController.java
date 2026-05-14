package com.ccissc.inventory.controller;

import com.ccissc.inventory.service.AuthService;
import com.ccissc.inventory.util.AlertUtil;
import com.ccissc.inventory.util.NavigationUtil;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        try {
            Optional<?> user = authService.login(usernameField.getText(), passwordField.getText());
            if (user.isPresent()) {
                NavigationUtil.switchTo("Dashboard.fxml");
            } else {
                AlertUtil.showError("Login Failed", "Invalid credentials or inactive account.");
            }
        } catch (IllegalArgumentException ex) {
            AlertUtil.showError("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Login Error", "Unable to log in. Please try again.");
        }
    }
}
