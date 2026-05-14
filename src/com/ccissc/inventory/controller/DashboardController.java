package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.model.ItemHistory;
import com.ccissc.inventory.service.InventoryService;
import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.SessionManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
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
    private BarChart<String, Number> topItemsChart;

    @FXML
    private TableView<Item> lowStockTable;

    @FXML
    private TableColumn<Item, String> lowStockNameColumn;

    @FXML
    private TableColumn<Item, Integer> lowStockQtyColumn;

    @FXML
    private TableView<Item> recentItemsTable;

    @FXML
    private TableColumn<Item, String> itemNameColumn;

    @FXML
    private TableColumn<Item, Integer> quantityColumn;

    @FXML
    private TableView<ItemHistory> activityTable;

    @FXML
    private TableColumn<ItemHistory, String> activityActionColumn;

    @FXML
    private TableColumn<ItemHistory, String> activityTimeColumn;

    @FXML
    private Button manageUsersButton;

    private final InventoryService inventoryService = new InventoryService();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @FXML
    private void initialize() {
        // Recent items table
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Low stock table
        lowStockNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        lowStockQtyColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Activity table
        activityActionColumn.setCellValueFactory(param -> {
            ItemHistory h = param.getValue();
            String description = formatActivityAction(h);
            return new ReadOnlyStringWrapper(description);
        });
        activityTimeColumn.setCellValueFactory(param -> {
            if (param.getValue().getCreatedAt() == null) {
                return new ReadOnlyStringWrapper("");
            }
            return new ReadOnlyStringWrapper(formatRelativeTime(param.getValue().getCreatedAt()));
        });

        manageUsersButton.setVisible(SessionManager.isAdmin());
        loadStats();
    }

    @FXML
    private void onManageUsers() {
        NavigationUtil.switchTo("UserManagement.fxml");
    }

    private void loadStats() {
        // Stat cards
        totalItemsLabel.setText(String.valueOf(inventoryService.getTotalItems()));
        totalStockLabel.setText(String.valueOf(inventoryService.getTotalStock()));
        lowStockLabel.setText(String.valueOf(inventoryService.getLowStockCount()));

        // Recent items
        List<Item> recentItems = inventoryService.getRecentItems(5);
        recentItemsTable.setItems(FXCollections.observableArrayList(recentItems));

        // Bar chart — top 10 items by quantity
        loadBarChart();

        // Low stock alert list
        List<Item> lowStockItems = inventoryService.getLowStockItems(10);
        lowStockTable.setItems(FXCollections.observableArrayList(lowStockItems));

        // Recent activity feed
        List<ItemHistory> recentActivity = inventoryService.getRecentHistory(5);
        activityTable.setItems(FXCollections.observableArrayList(recentActivity));
    }

    @SuppressWarnings("unchecked")
    private void loadBarChart() {
        topItemsChart.getData().clear();
        topItemsChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantity");

        List<Item> topItems = inventoryService.getTopItems(10);
        for (Item item : topItems) {
            String label = item.getItemName().length() > 18
                    ? item.getItemName().substring(0, 16) + "…"
                    : item.getItemName();
            series.getData().add(new XYChart.Data<>(label, item.getQuantity()));
        }

        topItemsChart.getData().add(series);
    }

    private String formatActivityAction(ItemHistory h) {
        String itemName = h.getNewName() != null ? h.getNewName()
                : (h.getOldName() != null ? h.getOldName() : "Item #" + h.getItemId());
        switch (h.getAction()) {
            case "CREATE":
                return "Added " + itemName;
            case "UPDATE":
                return "Updated " + itemName;
            case "ADJUST":
                int delta = 0;
                if (h.getNewQuantity() != null && h.getOldQuantity() != null) {
                    delta = h.getNewQuantity() - h.getOldQuantity();
                }
                String sign = delta >= 0 ? "+" : "";
                String note = h.getNote() != null ? " — " + h.getNote() : "";
                return "Adjusted " + itemName + " (" + sign + delta + ")" + note;
            case "ARCHIVE":
                return "Archived " + itemName;
            case "RESTORE":
                return "Restored " + itemName;
            default:
                return h.getAction() + " " + itemName;
        }
    }

    private String formatRelativeTime(LocalDateTime time) {
        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else {
            return time.format(TIME_FORMAT);
        }
    }
}
