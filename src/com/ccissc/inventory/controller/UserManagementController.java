package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Role;
import com.ccissc.inventory.model.User;
import com.ccissc.inventory.service.UserService;
import com.ccissc.inventory.util.AlertUtil;
import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.SessionManager;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

public class UserManagementController {
    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> fullNameColumn;

    @FXML
    private TableColumn<User, Role> roleColumn;

    @FXML
    private TableColumn<User, Boolean> activeColumn;

    private final UserService userService = new UserService();

    @FXML
    private void initialize() {
        if (!SessionManager.isAdmin()) {
            NavigationUtil.switchTo("Dashboard.fxml");
            return;
        }

        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));

        refreshTable();
    }

    @FXML
    private void onAddUser() {
        Dialog<User> dialog = buildUserDialog();
        dialog.showAndWait().ifPresent(user -> {
            try {
                userService.createUser(user.getUsername(), user.getPasswordHash(), user.getFullName(), user.getRole());
                refreshTable();
            } catch (Exception ex) {
                AlertUtil.showError("Create Error", "Unable to create user.");
            }
        });
    }

    @FXML
    private void onChangeRole() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select a user to update.");
            return;
        }

        Role newRole = selected.getRole() == Role.EXECUTIVE ? Role.COMMITTEE : Role.EXECUTIVE;
        userService.updateRole(selected.getId(), newRole);
        refreshTable();
    }

    @FXML
    private void onToggleActive() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select a user to update.");
            return;
        }

        userService.setActive(selected.getId(), !selected.isActive());
        refreshTable();
    }

    @FXML
    private void onDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select a user to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirm("Delete User", "Are you sure you want to delete this user?");
        if (confirmed) {
            userService.deleteUser(selected.getId());
            refreshTable();
        }
    }

    private void refreshTable() {
        List<User> users = userService.getAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
    }

    private Dialog<User> buildUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        TextField fullNameField = new TextField();
        TextField passwordField = new TextField();
        ComboBox<Role> roleCombo = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        roleCombo.getSelectionModel().select(Role.COMMITTEE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Password"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Role"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                User user = new User();
                user.setUsername(usernameField.getText());
                user.setFullName(fullNameField.getText());
                user.setPasswordHash(passwordField.getText());
                user.setRole(roleCombo.getValue());
                return user;
            }
            return null;
        });
        return dialog;
    }
}
