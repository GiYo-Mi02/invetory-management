package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.service.InventoryService;
import com.ccissc.inventory.util.AlertUtil;
import com.ccissc.inventory.util.SessionManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class InventoryController {
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterCombo;

    @FXML
    private TableView<Item> inventoryTable;

    @FXML
    private TableColumn<Item, String> nameColumn;

    @FXML
    private TableColumn<Item, Integer> quantityColumn;

    @FXML
    private ImageView previewImageView;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    private final InventoryService inventoryService = new InventoryService();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        filterCombo.setItems(FXCollections.observableArrayList("ALL", "LOW_STOCK", "OUT_OF_STOCK"));
        filterCombo.getSelectionModel().selectFirst();

        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null && newItem.getImagePath() != null) {
                File file = new File(newItem.getImagePath());
                if (file.exists()) {
                    previewImageView.setImage(new Image(file.toURI().toString()));
                }
            }
        });

        boolean admin = SessionManager.isAdmin();
        addButton.setVisible(admin);
        editButton.setVisible(admin);
        deleteButton.setVisible(admin);

        refreshTable();
    }

    @FXML
    private void onSearch() {
        refreshTable();
    }

    @FXML
    private void onAdd() {
        openAddEditModal(null);
    }

    @FXML
    private void onEdit() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select an item to edit.");
            return;
        }
        openAddEditModal(selected);
    }

    @FXML
    private void onDelete() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select an item to delete.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirm("Delete Item", "Are you sure you want to delete this item?");
        if (confirmed) {
            inventoryService.deleteItem(selected.getId());
            refreshTable();
        }
    }

    private void refreshTable() {
        String query = searchField.getText();
        String filter = filterCombo.getValue();
        List<Item> items = inventoryService.searchItems(query, filter);
        inventoryTable.setItems(FXCollections.observableArrayList(items));
    }

    private void openAddEditModal(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccissc/inventory/fxml/AddEditItem.fxml"));
            Parent root = loader.load();
            AddEditItemController controller = loader.getController();
            controller.setItem(item);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(item == null ? "Add Item" : "Edit Item");
            modal.setScene(new Scene(root));
            modal.showAndWait();
            refreshTable();
        } catch (IOException ex) {
            AlertUtil.showError("Error", "Unable to open item form.");
        }
    }
}
