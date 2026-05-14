package com.ccissc.inventory.dao;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoginHistoryDAO {
    void logLogin(int userId);

    Optional<LocalDateTime> findLastLogin(int userId);
}
