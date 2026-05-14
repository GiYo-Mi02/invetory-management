package com.ccissc.inventory.util;

import com.ccissc.inventory.model.Role;
import com.ccissc.inventory.model.User;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.EXECUTIVE;
    }

    public static void clear() {
        currentUser = null;
    }
}
