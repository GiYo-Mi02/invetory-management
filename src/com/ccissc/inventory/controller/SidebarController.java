package com.ccissc.inventory.controller;

import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SidebarController {
    @FXML
    private Button userManagementButton;

    @FXML
    private void initialize() {
        userManagementButton.setVisible(SessionManager.isAdmin());
    }

    @FXML
    private void onDashboard() {
        NavigationUtil.switchTo("Dashboard.fxml");
    }

    @FXML
    private void onInventory() {
        NavigationUtil.switchTo("Inventory.fxml");
    }

    @FXML
    private void onUserManagement() {
        NavigationUtil.switchTo("UserManagement.fxml");
    }
}
