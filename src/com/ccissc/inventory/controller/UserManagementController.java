package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Role;
import com.ccissc.inventory.model.User;
import com.ccissc.inventory.service.UserService;
import com.ccissc.inventory.util.AlertUtil;
import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.SessionManager;
import com.ccissc.inventory.util.ToastUtil;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.javafx.FontIcon;

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

    @FXML
    private TableColumn<User, String> lastLoginColumn;

    @FXML
    private TableColumn<User, Integer> actionCountColumn;

    @FXML
    private Label userCountLabel;

    @FXML
    private Button changeRoleButton;

    @FXML
    private Button toggleActiveButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button resetPasswordButton;

    private final UserService userService = new UserService();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

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
        activeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    FontIcon icon = new FontIcon(active ? "fas-circle-check" : "fas-circle-xmark");
                    icon.setIconSize(16);
                    icon.getStyleClass().add(active ? "icon-success" : "icon-danger");
                    setGraphic(icon);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        actionCountColumn.setCellValueFactory(new PropertyValueFactory<>("actionCount"));
        lastLoginColumn.setCellValueFactory(param -> {
            User user = param.getValue();
            if (user.getLastLoginAt() == null) {
                return new ReadOnlyStringWrapper("Never");
            }
            return new ReadOnlyStringWrapper(user.getLastLoginAt().format(DATE_FORMAT));
        });

        // Selection-driven button enable/disable
        updateActionButtons(false);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateActionButtons(newVal != null);
        });

        refreshTable();
    }

    @FXML
    private void onAddUser() {
        Dialog<User> dialog = buildUserDialog();
        dialog.showAndWait().ifPresent(user -> {
            try {
                userService.createUser(user.getUsername(), user.getPasswordHash(), user.getFullName(), user.getRole());
                refreshTable();
                ToastUtil.show(userTable.getScene(), "User created ✓");
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
        boolean confirmed = AlertUtil.showConfirm("Change Role",
                "Change " + selected.getFullName() + "'s role to " + newRole + "?");
        if (!confirmed) {
            return;
        }
        userService.updateRole(selected.getId(), newRole);
        refreshTable();
        ToastUtil.show(userTable.getScene(), "Role updated to " + newRole + " ✓");
    }

    @FXML
    private void onToggleActive() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select a user to update.");
            return;
        }

        String action = selected.isActive() ? "deactivate" : "activate";
        boolean confirmed = AlertUtil.showConfirm("Toggle Active",
                "Are you sure you want to " + action + " " + selected.getFullName() + "?");
        if (!confirmed) {
            return;
        }
        userService.setActive(selected.getId(), !selected.isActive());
        refreshTable();
        ToastUtil.show(userTable.getScene(), "User " + action + "d ✓");
    }

    @FXML
    private void onDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select a user to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirm("Delete User",
                "Are you sure you want to delete " + selected.getFullName() + "? This cannot be undone.");
        if (confirmed) {
            userService.deleteUser(selected.getId());
            refreshTable();
            ToastUtil.show(userTable.getScene(), "User deleted ✓");
        }
    }

    @FXML
    private void onResetPassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select a user to reset password.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirm("Reset Password",
                "Reset password for " + selected.getFullName() + "? A temporary password will be generated.");
        if (!confirmed) {
            return;
        }

        // Generate random 8-char temp password
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        try {
            userService.resetPassword(selected.getId(), tempPassword);
            AlertUtil.showInfo("Password Reset",
                    "Temporary password for " + selected.getUsername() + ":\n\n" + tempPassword
                            + "\n\nPlease share this with the user securely.");
            ToastUtil.show(userTable.getScene(), "Password reset ✓");
        } catch (Exception ex) {
            AlertUtil.showError("Reset Error", "Unable to reset password.");
        }
    }

    private void updateActionButtons(boolean hasSelection) {
        changeRoleButton.setDisable(!hasSelection);
        toggleActiveButton.setDisable(!hasSelection);
        deleteUserButton.setDisable(!hasSelection);
        resetPasswordButton.setDisable(!hasSelection);
    }

    private void refreshTable() {
        List<User> users = userService.getAllUsers();
        userTable.setItems(FXCollections.observableArrayList(users));
        userCountLabel.setText(users.size() + " user" + (users.size() != 1 ? "s" : ""));
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
