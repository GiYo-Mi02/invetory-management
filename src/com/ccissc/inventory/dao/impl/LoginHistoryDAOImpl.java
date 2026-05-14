package com.ccissc.inventory.dao.impl;

import com.ccissc.inventory.config.DatabaseConfig;
import com.ccissc.inventory.dao.LoginHistoryDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class LoginHistoryDAOImpl implements LoginHistoryDAO {
    @Override
    public void logLogin(int userId) {
        String sql = "INSERT INTO login_history (user_id) VALUES (?)";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to log login", ex);
        }
    }

    @Override
    public Optional<LocalDateTime> findLastLogin(int userId) {
        String sql = "SELECT MAX(logged_in_at) AS last_login FROM login_history WHERE user_id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("last_login");
                    return Optional.ofNullable(ts != null ? ts.toLocalDateTime() : null);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch last login", ex);
        }
        return Optional.empty();
    }
}
