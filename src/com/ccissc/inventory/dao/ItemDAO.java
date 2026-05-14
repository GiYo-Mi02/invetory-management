package com.ccissc.inventory.dao;

import com.ccissc.inventory.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemDAO {
    Optional<Item> findById(int id);

    Optional<Item> findByName(String name, boolean includeArchived);

    List<Item> findAll(boolean includeArchived);

    List<Item> search(String query, Integer categoryId, boolean includeArchived);

    int create(Item item);

    boolean update(Item item);

    boolean updateQuantity(int itemId, int newQuantity);

    boolean setArchived(int itemId, boolean archived);

    int getTotalItems();

    int getTotalStock();

    int getLowStockCount();

    List<Item> getLowStockItems(int limit);

    List<Item> getTopItems(int limit);

    List<Item> getRecentItems(int limit);
}
