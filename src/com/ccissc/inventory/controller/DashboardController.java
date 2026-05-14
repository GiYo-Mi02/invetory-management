package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.service.InventoryService;
import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.SessionManager;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DashboardController {
    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label totalStockLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private TableView<Item> recentItemsTable;

    @FXML
    private TableColumn<Item, String> itemNameColumn;

    @FXML
    private TableColumn<Item, Integer> quantityColumn;

    @FXML
    private Button manageUsersButton;

    private final InventoryService inventoryService = new InventoryService();

    @FXML
    private void initialize() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        manageUsersButton.setVisible(SessionManager.isAdmin());
        loadStats();
    }

    @FXML
    private void onManageUsers() {
        NavigationUtil.switchTo("UserManagement.fxml");
    }

    private void loadStats() {
        totalItemsLabel.setText(String.valueOf(inventoryService.getTotalItems()));
        totalStockLabel.setText(String.valueOf(inventoryService.getTotalStock()));
        lowStockLabel.setText(String.valueOf(inventoryService.getLowStockCount()));

        List<Item> recentItems = inventoryService.getRecentItems(5);
        recentItemsTable.setItems(FXCollections.observableArrayList(recentItems));
    }
}
