package com.ccissc.inventory.dao;

import com.ccissc.inventory.model.ItemHistory;
import java.util.List;

public interface ItemHistoryDAO {
    int create(ItemHistory history);

    List<ItemHistory> findByItemId(int itemId, int limit);

    List<ItemHistory> findRecent(int limit);
}
