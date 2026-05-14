package com.ccissc.inventory.service;

import com.ccissc.inventory.dao.UserDAO;
import com.ccissc.inventory.dao.impl.UserDAOImpl;
import com.ccissc.inventory.model.User;
import com.ccissc.inventory.util.SessionManager;
import com.ccissc.inventory.util.ValidationUtil;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDAO userDao;

    public AuthService() {
        this.userDao = new UserDAOImpl();
    }

    public Optional<User> login(String username, String password) {
        ValidationUtil.requireNotBlank(username, "Username is required");
        ValidationUtil.requireNotBlank(password, "Password is required");

        Optional<User> userOptional = userDao.findByUsername(username);
        if (userOptional.isEmpty()) {
            return Optional.empty();
        }

        User user = userOptional.get();
        if (!user.isActive()) {
            return Optional.empty();
        }

        if (BCrypt.checkpw(password, user.getPasswordHash())) {
            SessionManager.setCurrentUser(user);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public void logout() {
        SessionManager.clear();
    }
}
