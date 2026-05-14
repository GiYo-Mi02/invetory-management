package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.service.InventoryService;
import com.ccissc.inventory.util.AlertUtil;
import com.ccissc.inventory.util.SessionManager;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddEditItemController {
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField quantityField;

    @FXML
    private ImageView itemImageView;

    @FXML
    private Label imagePathLabel;

    private final InventoryService inventoryService = new InventoryService();
    private Item currentItem;
    private String imagePath;

    public void setItem(Item item) {
        this.currentItem = item;
        if (item != null) {
            nameField.setText(item.getItemName());
            descriptionArea.setText(item.getDescription());
            quantityField.setText(String.valueOf(item.getQuantity()));
            imagePath = item.getImagePath();
            imagePathLabel.setText(imagePath != null ? imagePath : "No image selected");
            if (imagePath != null) {
                File file = new File(imagePath);
                if (file.exists()) {
                    itemImageView.setImage(new Image(file.toURI().toString()));
                }
            }
        }
    }

    @FXML
    private void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            imagePath = file.getAbsolutePath();
            imagePathLabel.setText(imagePath);
            itemImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void onSave() {
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (currentItem == null) {
                currentItem = new Item();
                currentItem.setCreatedBy(SessionManager.getCurrentUser().getId());
            }
            currentItem.setItemName(nameField.getText());
            currentItem.setDescription(descriptionArea.getText());
            currentItem.setQuantity(quantity);
            currentItem.setImagePath(imagePath);

            if (currentItem.getId() == 0) {
                inventoryService.createItem(currentItem);
            } else {
                inventoryService.updateItem(currentItem);
            }

            close();
        } catch (NumberFormatException ex) {
            AlertUtil.showError("Validation Error", "Quantity must be a number.");
        } catch (IllegalArgumentException ex) {
            AlertUtil.showError("Validation Error", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Save Error", "Unable to save item.");
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
