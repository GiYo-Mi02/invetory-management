package com.ccissc.inventory.dao.impl;

import com.ccissc.inventory.config.DatabaseConfig;
import com.ccissc.inventory.dao.UserActivityDAO;
import com.ccissc.inventory.model.UserActivity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserActivityDAOImpl implements UserActivityDAO {
    @Override
    public int create(UserActivity activity) {
        String sql = "INSERT INTO user_activity (user_id, action, entity_type, entity_id, metadata) VALUES (?,?,?,?,?)";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, activity.getUserId());
            stmt.setString(2, activity.getAction());
            stmt.setString(3, activity.getEntityType());
            stmt.setObject(4, activity.getEntityId());
            stmt.setString(5, activity.getMetadata());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create user activity", ex);
        }
        return 0;
    }

    @Override
    public int countByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM user_activity WHERE user_id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to count user activity", ex);
        }
    }

    @Override
    public List<UserActivity> findRecent(int limit) {
        String sql = "SELECT id, user_id, action, entity_type, entity_id, metadata, created_at "
                + "FROM user_activity ORDER BY created_at DESC LIMIT ?";
        List<UserActivity> activities = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapActivity(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch recent activity", ex);
        }
        return activities;
    }

    private UserActivity mapActivity(ResultSet rs) throws SQLException {
        UserActivity activity = new UserActivity();
        activity.setId(rs.getInt("id"));
        activity.setUserId(rs.getInt("user_id"));
        activity.setAction(rs.getString("action"));
        activity.setEntityType(rs.getString("entity_type"));
        activity.setEntityId((Integer) rs.getObject("entity_id"));
        activity.setMetadata(rs.getString("metadata"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        activity.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        return activity;
    }
}
