package com.ccissc.inventory.dao;

import com.ccissc.inventory.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findById(int id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    int create(User user);

    boolean update(User user);

    boolean updatePassword(int userId, String passwordHash);

    boolean setActive(int userId, boolean active);

    boolean delete(int userId);
}
