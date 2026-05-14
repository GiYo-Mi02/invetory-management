package com.ccissc.inventory.service;

import com.ccissc.inventory.dao.LoginHistoryDAO;
import com.ccissc.inventory.dao.UserActivityDAO;
import com.ccissc.inventory.dao.UserDAO;
import com.ccissc.inventory.dao.impl.LoginHistoryDAOImpl;
import com.ccissc.inventory.dao.impl.UserActivityDAOImpl;
import com.ccissc.inventory.dao.impl.UserDAOImpl;
import com.ccissc.inventory.model.User;
import com.ccissc.inventory.model.UserActivity;
import com.ccissc.inventory.util.SessionManager;
import com.ccissc.inventory.util.ValidationUtil;
import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDAO userDao;
    private final LoginHistoryDAO loginHistoryDao;
    private final UserActivityDAO activityDao;

    public AuthService() {
        this.userDao = new UserDAOImpl();
        this.loginHistoryDao = new LoginHistoryDAOImpl();
        this.activityDao = new UserActivityDAOImpl();
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
            loginHistoryDao.logLogin(user.getId());
            logActivity(user.getId(), "USER_LOGIN", null);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public void logout() {
        SessionManager.clear();
    }

    private void logActivity(int userId, String action, String metadata) {
        UserActivity activity = new UserActivity();
        activity.setUserId(userId);
        activity.setAction(action);
        activity.setEntityType("USER");
        activity.setEntityId(userId);
        activity.setMetadata(metadata);
        activityDao.create(activity);
    }
}
