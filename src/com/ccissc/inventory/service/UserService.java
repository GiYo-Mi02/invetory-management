package com.ccissc.inventory.service;

import com.ccissc.inventory.dao.UserDAO;
import com.ccissc.inventory.dao.impl.UserDAOImpl;
import com.ccissc.inventory.model.Role;
import com.ccissc.inventory.model.User;
import com.ccissc.inventory.util.ValidationUtil;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDAO userDao;

    public UserService() {
        this.userDao = new UserDAOImpl();
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public int createUser(String username, String password, String fullName, Role role) {
        ValidationUtil.requireNotBlank(username, "Username is required");
        ValidationUtil.requireNotBlank(password, "Password is required");
        ValidationUtil.requireNotBlank(fullName, "Full name is required");

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hash);
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        return userDao.create(user);
    }

    public boolean updateRole(int userId, Role role) {
        ValidationUtil.requireNonNegative(userId, "User id is required");
        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);
        return userDao.update(user);
    }

    public boolean setActive(int userId, boolean active) {
        ValidationUtil.requireNonNegative(userId, "User id is required");
        return userDao.setActive(userId, active);
    }

    public boolean deleteUser(int userId) {
        ValidationUtil.requireNonNegative(userId, "User id is required");
        return userDao.delete(userId);
    }
}
