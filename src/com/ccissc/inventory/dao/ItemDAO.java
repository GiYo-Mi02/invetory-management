package com.ccissc.inventory.dao;

import com.ccissc.inventory.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemDAO {
    Optional<Item> findById(int id);

    List<Item> findAll();

    List<Item> search(String query);

    int create(Item item);

    boolean update(Item item);

    boolean delete(int itemId);

    int getTotalItems();

    int getTotalStock();

    int getLowStockCount(int threshold);

    List<Item> getRecentItems(int limit);
}
