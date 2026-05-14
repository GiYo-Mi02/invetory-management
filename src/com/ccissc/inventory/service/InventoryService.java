package com.ccissc.inventory.service;

import com.ccissc.inventory.dao.ItemDAO;
import com.ccissc.inventory.dao.impl.ItemDAOImpl;
import com.ccissc.inventory.model.Item;
import com.ccissc.inventory.util.ValidationUtil;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private static final int LOW_STOCK_THRESHOLD = 5;
    private final ItemDAO itemDao;

    public InventoryService() {
        this.itemDao = new ItemDAOImpl();
    }

    public List<Item> getAllItems() {
        return itemDao.findAll();
    }

    public List<Item> searchItems(String query, String filter) {
        List<Item> items;
        if (query == null || query.isBlank()) {
            items = new ArrayList<>(itemDao.findAll());
        } else {
            items = new ArrayList<>(itemDao.search(query));
        }

        if (filter == null || filter.isBlank() || "ALL".equalsIgnoreCase(filter)) {
            return items;
        }

        if ("LOW_STOCK".equalsIgnoreCase(filter)) {
            items.removeIf(item -> item.getQuantity() > LOW_STOCK_THRESHOLD);
        } else if ("OUT_OF_STOCK".equalsIgnoreCase(filter)) {
            items.removeIf(item -> item.getQuantity() > 0);
        }

        return items;
    }

    public int createItem(Item item) {
        validateItem(item);
        return itemDao.create(item);
    }

    public boolean updateItem(Item item) {
        validateItem(item);
        return itemDao.update(item);
    }

    public boolean deleteItem(int itemId) {
        return itemDao.delete(itemId);
    }

    public int getTotalItems() {
        return itemDao.getTotalItems();
    }

    public int getTotalStock() {
        return itemDao.getTotalStock();
    }

    public int getLowStockCount() {
        return itemDao.getLowStockCount(LOW_STOCK_THRESHOLD);
    }

    public List<Item> getRecentItems(int limit) {
        return itemDao.getRecentItems(limit);
    }

    private void validateItem(Item item) {
        ValidationUtil.requireNotBlank(item.getItemName(), "Item name is required");
        ValidationUtil.requireNonNegative(item.getQuantity(), "Quantity cannot be negative");
    }
}
