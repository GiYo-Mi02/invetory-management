package com.ccissc.inventory.controller;

import com.ccissc.inventory.model.Category;
import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.model.ItemHistory;
import com.ccissc.inventory.service.InventoryService;
import com.ccissc.inventory.util.AlertUtil;
import com.ccissc.inventory.util.NavigationUtil;
import com.ccissc.inventory.util.ToastUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.beans.property.ReadOnlyStringWrapper;
import org.kordamp.ikonli.javafx.FontIcon;

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
    private TableColumn<Item, String> categoryColumn;

    @FXML
    private TableColumn<Item, Item> adjustColumn;

    @FXML
    private ImageView previewImageView;

    @FXML
    private TextArea previewDescription;

    @FXML
    private Label previewPlaceholder;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button restoreButton;

    @FXML
    private Button exportButton;

    @FXML
    private TableView<ItemHistory> historyTable;

    @FXML
    private TableColumn<ItemHistory, String> historyActionColumn;

    @FXML
    private TableColumn<ItemHistory, String> historyNoteColumn;

    @FXML
    private TableColumn<ItemHistory, String> historyTimeColumn;

    private final InventoryService inventoryService = new InventoryService();
    private final Map<String, Integer> categoryLookup = new HashMap<>();
    private static final String FILTER_ALL = "All";
    private static final String FILTER_ARCHIVED = "Archived";
    private static final DateTimeFormatter HISTORY_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        adjustColumn.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        adjustColumn.setCellFactory(buildAdjustCell());

        // Column sorting comparators
        nameColumn.setComparator(String.CASE_INSENSITIVE_ORDER);
        quantityColumn.setComparator(Comparator.naturalOrder());
        categoryColumn.setComparator(String.CASE_INSENSITIVE_ORDER);
        adjustColumn.setSortable(false);

        historyActionColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getAction()));
        historyNoteColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getNote()));
        historyTimeColumn.setCellValueFactory(param -> {
            if (param.getValue().getCreatedAt() == null) {
                return new ReadOnlyStringWrapper("");
            }
            return new ReadOnlyStringWrapper(param.getValue().getCreatedAt().format(HISTORY_TIME_FORMAT));
        });

        setupFilters();
        setupRowStyling();

        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, newItem) -> {
            updatePreview(newItem);
            updateActionButtons(newItem != null);
            updateHistory(newItem);
        });

        updateActionButtons(false);
        refreshTable();

        // Keyboard shortcuts — deferred to after scene is available
        Platform.runLater(this::setupKeyboardShortcuts);
    }

    @FXML
    private void onSearch() {
        String query = searchField.getText();
        String selectedFilter = filterCombo.getValue();
        boolean archivedOnly = FILTER_ARCHIVED.equalsIgnoreCase(selectedFilter);
        boolean includeArchived = archivedOnly;
        Integer categoryId = categoryLookup.get(selectedFilter);

        List<Item> items = inventoryService.searchItems(query, categoryId, includeArchived);
        if (archivedOnly) {
            items.removeIf(item -> !item.isArchived());
        }
        inventoryTable.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    private void onAdd() {
        if (openItemDialog(null)) {
            refreshTable();
            ToastUtil.show(inventoryTable.getScene(), "Item saved ✓");
        }
    }

    @FXML
    private void onEdit() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select an item to edit.");
            return;
        }

        if (openItemDialog(selected)) {
            refreshTable();
            ToastUtil.show(inventoryTable.getScene(), "Item updated ✓");
        }
    }

    @FXML
    private void onDelete() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select an item to delete.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirm("Archive Item",
                "Are you sure you want to archive " + selected.getItemName() + "? This can be restored.");
        if (!confirmed) {
            return;
        }
        inventoryService.setArchived(selected.getId(), true, "Archived via inventory");
        refreshTable();
        ToastUtil.show(inventoryTable.getScene(), "Item archived ✓");
    }

    @FXML
    private void onRestore() {
        Item selected = inventoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("No Selection", "Select an item to restore.");
            return;
        }
        boolean confirmed = AlertUtil.showConfirm("Restore Item",
                "Restore " + selected.getItemName() + " to active inventory?");
        if (!confirmed) {
            return;
        }
        inventoryService.setArchived(selected.getId(), false, "Restored via inventory");
        refreshTable();
        ToastUtil.show(inventoryTable.getScene(), "Item restored ✓");
    }

    @FXML
    private void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Inventory CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(inventoryTable.getScene().getWindow());
        if (file == null) {
            return;
        }
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writer.println("Item,Category,Quantity,Min Quantity,Archived,Description");
            for (Item item : inventoryTable.getItems()) {
                writer.printf("\"%s\",\"%s\",%d,%d,%s,\"%s\"%n",
                        safeCsv(item.getItemName()),
                        safeCsv(item.getCategoryName()),
                        item.getQuantity(),
                        item.getMinQuantity(),
                        item.isArchived(),
                        safeCsv(item.getDescription()));
            }
            ToastUtil.show(inventoryTable.getScene(), "CSV exported ✓");
        } catch (IOException ex) {
            AlertUtil.showError("Export Error", "Unable to export CSV.");
        }
    }

    private void refreshTable() {
        onSearch();
    }

    private void updatePreview(Item item) {
        if (item == null) {
            previewImageView.setImage(null);
            previewDescription.setText("");
            previewPlaceholder.setVisible(true);
            previewPlaceholder.setManaged(true);
            deleteButton.setDisable(true);
            restoreButton.setDisable(true);
            return;
        }

        if (item.getImagePath() != null && !item.getImagePath().isBlank()) {
            File file = new File(item.getImagePath());
            previewImageView.setImage(file.exists() ? new Image(file.toURI().toString()) : null);
        } else {
            previewImageView.setImage(null);
        }

        previewDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        previewPlaceholder.setVisible(false);
        previewPlaceholder.setManaged(false);
        deleteButton.setDisable(item.isArchived());
        restoreButton.setDisable(!item.isArchived());
    }

    private void updateActionButtons(boolean hasSelection) {
        editButton.setDisable(!hasSelection);
    }

    private void updateHistory(Item item) {
        if (item == null) {
            historyTable.setItems(FXCollections.observableArrayList());
            return;
        }
        List<ItemHistory> history = inventoryService.getItemHistory(item.getId(), 20);
        historyTable.setItems(FXCollections.observableArrayList(history));
    }

    private void setupFilters() {
        filterCombo.getItems().clear();
        filterCombo.getItems().add(FILTER_ALL);
        filterCombo.getItems().add(FILTER_ARCHIVED);
        categoryLookup.clear();
        for (Category category : inventoryService.getCategories()) {
            filterCombo.getItems().add(category.getName());
            categoryLookup.put(category.getName(), category.getId());
        }
        filterCombo.getSelectionModel().selectFirst();
    }

    private void setupRowStyling() {
        inventoryTable.setRowFactory(table -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.isArchived() && item.getQuantity() <= item.getMinQuantity()) {
                    setStyle("-fx-background-color: #fee2e2;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void setupKeyboardShortcuts() {
        Scene scene = inventoryTable.getScene();
        if (scene == null) {
            return;
        }

        KeyCodeCombination ctrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        scene.setOnKeyPressed(event -> {
            if (ctrlN.match(event)) {
                onAdd();
                event.consume();
            } else if (event.getCode() == KeyCode.DELETE) {
                Item selected = inventoryTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    onDelete();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                inventoryTable.getSelectionModel().clearSelection();
                event.consume();
            }
        });
    }

    private Callback<TableColumn<Item, Item>, TableCell<Item, Item>> buildAdjustCell() {
        return column -> new TableCell<>() {
            private final Button minusButton = new Button();
            private final Button plusButton = new Button();
            private final HBox container = new HBox(6, minusButton, plusButton);

            {
                minusButton.setGraphic(new FontIcon("fas-minus"));
                plusButton.setGraphic(new FontIcon("fas-plus"));
                minusButton.getStyleClass().add("ghost-button");
                plusButton.getStyleClass().add("ghost-button");
                minusButton.setOnAction(event -> handleAdjust(-1));
                plusButton.setOnAction(event -> handleAdjust(1));
            }

            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }

            private void handleAdjust(int delta) {
                Item item = getTableView().getItems().get(getIndex());
                if (item == null) {
                    return;
                }
                if (item.isArchived()) {
                    AlertUtil.showError("Not Allowed", "Archived items cannot be adjusted.");
                    return;
                }
                String note = promptAdjustmentNote(delta, item);
                if (note == null) {
                    return;
                }
                try {
                    inventoryService.adjustQuantity(item.getId(), delta, note);
                    refreshTable();
                    ToastUtil.show(inventoryTable.getScene(), "Quantity adjusted ✓");
                } catch (Exception ex) {
                    AlertUtil.showError("Adjustment Error", ex.getMessage());
                }
            }
        };
    }

    private String promptAdjustmentNote(int delta, Item item) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adjust Quantity");
        dialog.setHeaderText((delta > 0 ? "Increase" : "Decrease") + " quantity for " + item.getItemName());
        dialog.setContentText("Reason:");
        return dialog.showAndWait().orElse(null);
    }

    private boolean openItemDialog(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ccissc/inventory/fxml/AddEditItem.fxml"));
            Parent root = loader.load();

            AddEditItemController controller = loader.getController();
            controller.setItem(item);

            Scene scene = new Scene(root);
            NavigationUtil.applyGlobalStyles(scene);
            if (NavigationUtil.isDarkMode()) {
                NavigationUtil.applyDarkStylesheet(scene, true);
            }

            Stage owner = (Stage) inventoryTable.getScene().getWindow();
            Stage modal = new Stage();
            modal.initOwner(owner);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.setTitle(item == null ? "Add Item" : "Edit Item");
            modal.setScene(scene);
            modal.centerOnScreen();
            modal.showAndWait();
            return true;
        } catch (IOException | RuntimeException ex) {
            // Surface the root error to help pinpoint FXML or resource issues.
            ex.printStackTrace();
            String message = ex.getMessage() != null ? ex.getMessage() : "Unknown error";
            AlertUtil.showError("Open Error", "Unable to open item dialog: " + message);
            return false;
        }
    }

    private String safeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }
}
