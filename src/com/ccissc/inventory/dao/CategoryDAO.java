package com.ccissc.inventory.dao;

import com.ccissc.inventory.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryDAO {
    List<Category> findAll();

    Optional<Category> findById(int id);

    Optional<Category> findByName(String name);
}
