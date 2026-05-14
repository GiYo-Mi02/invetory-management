package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.model.Category;
import com.ccissc.inventory.service.InventoryService;
import com.ccissc.inventory.util.AlertUtil;
import com.ccissc.inventory.util.SessionManager;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class AddEditItemController {
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField minQuantityField;

    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private ImageView itemImageView;

    @FXML
    private Label imagePathLabel;

    @FXML
    private VBox uploadBox;

    @FXML
    private FontIcon dialogIcon;

    private final InventoryService inventoryService = new InventoryService();
    private Item currentItem;
    private String imagePath;

    public void setItem(Item item) {
        this.currentItem = item;
        loadCategories();
        if (item != null) {
            // Switch icon to edit mode
            if (dialogIcon != null) {
                dialogIcon.setIconLiteral("fas-pen-to-square");
            }
            nameField.setText(item.getItemName());
            descriptionArea.setText(item.getDescription());
            quantityField.setText(String.valueOf(item.getQuantity()));
            minQuantityField.setText(String.valueOf(item.getMinQuantity()));
            selectCategory(item.getCategoryId());
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
    private void initialize() {
        loadCategories();
        setupDragAndDrop();
        setupKeyboardShortcuts();
    }

    @FXML
    private void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            setImageFile(file);
        }
    }

    @FXML
    private void onSave() {
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            int minQuantity = Integer.parseInt(minQuantityField.getText().trim());
            Category category = categoryCombo.getSelectionModel().getSelectedItem();
            if (category == null) {
                throw new IllegalArgumentException("Category is required");
            }
            if (currentItem == null) {
                currentItem = new Item();
                currentItem.setCreatedBy(SessionManager.getCurrentUser().getId());
            }
            currentItem.setItemName(nameField.getText());
            currentItem.setDescription(descriptionArea.getText());
            currentItem.setQuantity(quantity);
            currentItem.setMinQuantity(minQuantity);
            currentItem.setCategoryId(category.getId());
            currentItem.setCategoryName(category.getName());
            currentItem.setImagePath(imagePath);

            if (currentItem.getId() == 0) {
                inventoryService.createItem(currentItem);
            } else {
                inventoryService.updateItem(currentItem);
            }

            close();
        } catch (NumberFormatException ex) {
            AlertUtil.showError("Validation Error", "Quantity and minimum quantity must be numbers.");
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

    private void loadCategories() {
        List<Category> categories = inventoryService.getCategories();
        categoryCombo.setItems(javafx.collections.FXCollections.observableArrayList(categories));
        if (!categories.isEmpty() && categoryCombo.getSelectionModel().isEmpty()) {
            categoryCombo.getSelectionModel().selectFirst();
        }
    }

    private void selectCategory(int categoryId) {
        if (categoryId <= 0) {
            return;
        }
        for (Category category : categoryCombo.getItems()) {
            if (category.getId() == categoryId) {
                categoryCombo.getSelectionModel().select(category);
                break;
            }
        }
    }

    private void setupDragAndDrop() {
        uploadBox.setOnDragOver(this::handleDragOver);
        uploadBox.setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            if (isImageFile(file)) {
                setImageFile(file);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
        }
        event.consume();
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private void setImageFile(File file) {
        imagePath = file.getAbsolutePath();
        imagePathLabel.setText(imagePath);
        itemImageView.setImage(new Image(file.toURI().toString()));
    }

    private void setupKeyboardShortcuts() {
        javafx.application.Platform.runLater(() -> {
            if (nameField.getScene() == null) {
                return;
            }
            nameField.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    onCancel();
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER) {
                    // Don't intercept Enter in the description TextArea
                    if (!(event.getTarget() instanceof TextArea)) {
                        onSave();
                        event.consume();
                    }
                }
            });
        });
    }
}
