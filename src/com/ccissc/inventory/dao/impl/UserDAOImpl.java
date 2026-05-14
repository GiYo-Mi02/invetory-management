package com.ccissc.inventory.dao.impl;

import com.ccissc.inventory.config.DatabaseConfig;
import com.ccissc.inventory.dao.UserDAO;
import com.ccissc.inventory.model.Role;
import com.ccissc.inventory.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private static final String BASE_SELECT = "SELECT u.id, u.username, u.password_hash, u.full_name, u.role, u.is_active, "
            + "u.created_at, u.updated_at, "
            + "(SELECT MAX(logged_in_at) FROM login_history WHERE user_id = u.id) AS last_login, "
            + "(SELECT COUNT(*) FROM user_activity WHERE user_id = u.id) AS action_count "
            + "FROM users u";

    @Override
    public Optional<User> findById(int id) {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find user by id", ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = BASE_SELECT + " WHERE username = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find user by username", ex);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(BASE_SELECT + " ORDER BY created_at DESC")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list users", ex);
        }
        return users;
    }

    @Override
    public int create(User user) {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, is_active) VALUES (?,?,?,?,?)";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole().name());
            stmt.setBoolean(5, user.isActive());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create user", ex);
        }
        return 0;
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, full_name = ?, role = ?, is_active = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getRole().name());
            stmt.setBoolean(4, user.isActive());
            stmt.setInt(5, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update user", ex);
        }
    }

    @Override
    public boolean updatePassword(int userId, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update password", ex);
        }
    }

    @Override
    public boolean setActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update user status", ex);
        }
    }

    @Override
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete user", ex);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        Timestamp lastLogin = rs.getTimestamp("last_login");
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("full_name"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_active"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
            updatedAt != null ? updatedAt.toLocalDateTime() : null,
            lastLogin != null ? lastLogin.toLocalDateTime() : null,
            rs.getInt("action_count"));
    }
}
