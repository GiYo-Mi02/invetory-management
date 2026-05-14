package com.ccissc.inventory.dao;

import com.ccissc.inventory.model.UserActivity;
import java.util.List;

public interface UserActivityDAO {
    int create(UserActivity activity);

    int countByUserId(int userId);

    List<UserActivity> findRecent(int limit);
}
