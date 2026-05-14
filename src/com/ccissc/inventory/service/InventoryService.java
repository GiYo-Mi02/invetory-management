package com.ccissc.inventory.service;

import com.ccissc.inventory.dao.CategoryDAO;
import com.ccissc.inventory.dao.ItemDAO;
import com.ccissc.inventory.dao.ItemHistoryDAO;
import com.ccissc.inventory.dao.UserActivityDAO;
import com.ccissc.inventory.dao.impl.CategoryDAOImpl;
import com.ccissc.inventory.dao.impl.ItemDAOImpl;
import com.ccissc.inventory.dao.impl.ItemHistoryDAOImpl;
import com.ccissc.inventory.dao.impl.UserActivityDAOImpl;
import com.ccissc.inventory.model.Category;
import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.model.ItemHistory;
import com.ccissc.inventory.model.UserActivity;
import com.ccissc.inventory.util.SessionManager;
import com.ccissc.inventory.util.ValidationUtil;
import java.util.List;
import java.util.Optional;

public class InventoryService {
    private final ItemDAO itemDao;
    private final CategoryDAO categoryDao;
    private final ItemHistoryDAO historyDao;
    private final UserActivityDAO activityDao;

    public InventoryService() {
        this.itemDao = new ItemDAOImpl();
        this.categoryDao = new CategoryDAOImpl();
        this.historyDao = new ItemHistoryDAOImpl();
        this.activityDao = new UserActivityDAOImpl();
    }

    public List<Category> getCategories() {
        return categoryDao.findAll();
    }

    public List<Item> getAllItems(boolean includeArchived) {
        return itemDao.findAll(includeArchived);
    }

    public List<Item> searchItems(String query, Integer categoryId, boolean includeArchived) {
        return itemDao.search(query, categoryId, includeArchived);
    }

    public int createItem(Item item) {
        requireAdmin();
        validateItem(item, true);
        int id = itemDao.create(item);
        item.setId(id);
        logHistory(null, item, "CREATE", null);
        logUserActivity("ITEM_CREATE", item.getId(), item.getItemName());
        return id;
    }

    public boolean updateItem(Item item) {
        requireAdmin();
        validateItem(item, false);
        Item existing = itemDao.findById(item.getId())
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        boolean updated = itemDao.update(item);
        if (updated) {
            logHistory(existing, item, "UPDATE", null);
            logUserActivity("ITEM_UPDATE", item.getId(), item.getItemName());
        }
        return updated;
    }

    public boolean adjustQuantity(int itemId, int delta, String note) {
        requireAdmin();
        Item existing = itemDao.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        int newQuantity = existing.getQuantity() + delta;
        ValidationUtil.requireNonNegative(newQuantity, "Quantity cannot be negative");
        boolean updated = itemDao.updateQuantity(itemId, newQuantity);
        if (updated) {
            Item updatedItem = itemDao.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));
            logHistory(existing, updatedItem, "ADJUST", note);
            logUserActivity("ITEM_ADJUST", itemId, note);
        }
        return updated;
    }

    public boolean setArchived(int itemId, boolean archived, String note) {
        requireAdmin();
        Item existing = itemDao.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        boolean updated = itemDao.setArchived(itemId, archived);
        if (updated) {
            Item updatedItem = itemDao.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));
            logHistory(existing, updatedItem, archived ? "ARCHIVE" : "RESTORE", note);
            logUserActivity(archived ? "ITEM_ARCHIVE" : "ITEM_RESTORE", itemId, existing.getItemName());
        }
        return updated;
    }

    public int getTotalItems() {
        return itemDao.getTotalItems();
    }

    public int getTotalStock() {
        return itemDao.getTotalStock();
    }

    public int getLowStockCount() {
        return itemDao.getLowStockCount();
    }

    public List<Item> getLowStockItems(int limit) {
        return itemDao.getLowStockItems(limit);
    }

    public List<Item> getTopItems(int limit) {
        return itemDao.getTopItems(limit);
    }

    public List<Item> getRecentItems(int limit) {
        return itemDao.getRecentItems(limit);
    }

    public List<ItemHistory> getItemHistory(int itemId, int limit) {
        return historyDao.findByItemId(itemId, limit);
    }

    public List<ItemHistory> getRecentHistory(int limit) {
        return historyDao.findRecent(limit);
    }

    private void validateItem(Item item, boolean isNew) {
        ValidationUtil.requireNotBlank(item.getItemName(), "Item name is required");
        ValidationUtil.requireNonNegative(item.getQuantity(), "Quantity cannot be negative");
        ValidationUtil.requireNonNegative(item.getMinQuantity(), "Minimum quantity cannot be negative");
        if (item.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Category is required");
        }

        Optional<Item> existing = itemDao.findByName(item.getItemName(), false);
        if (existing.isPresent() && (isNew || existing.get().getId() != item.getId())) {
            throw new IllegalArgumentException("Item name already exists");
        }
    }

    private void logHistory(Item oldItem, Item newItem, String action, String note) {
        ItemHistory history = new ItemHistory();
        history.setItemId(newItem != null ? newItem.getId() : oldItem.getId());
        history.setAction(action);
        history.setChangedBy(SessionManager.getCurrentUser().getId());
        history.setNote(note);

        if (oldItem != null) {
            history.setOldName(oldItem.getItemName());
            history.setOldDescription(oldItem.getDescription());
            history.setOldQuantity(oldItem.getQuantity());
            history.setOldCategoryId(oldItem.getCategoryId());
            history.setOldMinQuantity(oldItem.getMinQuantity());
            history.setOldArchived(oldItem.isArchived());
        }

        if (newItem != null) {
            history.setNewName(newItem.getItemName());
            history.setNewDescription(newItem.getDescription());
            history.setNewQuantity(newItem.getQuantity());
            history.setNewCategoryId(newItem.getCategoryId());
            history.setNewMinQuantity(newItem.getMinQuantity());
            history.setNewArchived(newItem.isArchived());
        }

        historyDao.create(history);
    }

    private void logUserActivity(String action, int itemId, String metadata) {
        UserActivity activity = new UserActivity();
        activity.setUserId(SessionManager.getCurrentUser().getId());
        activity.setAction(action);
        activity.setEntityType("ITEM");
        activity.setEntityId(itemId);
        activity.setMetadata(metadata);
        activityDao.create(activity);
    }

    private void requireAdmin() {
        if (!SessionManager.isAdmin()) {
            throw new IllegalStateException("Not authorized to perform this action");
        }
    }
}
