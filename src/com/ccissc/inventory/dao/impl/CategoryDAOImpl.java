package com.ccissc.inventory.dao.impl;

import com.ccissc.inventory.config.DatabaseConfig;
import com.ccissc.inventory.dao.CategoryDAO;
import com.ccissc.inventory.model.Category;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDAOImpl implements CategoryDAO {
    private static final String BASE_SELECT = "SELECT id, name, created_at FROM categories";

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(BASE_SELECT + " ORDER BY name ASC")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(mapCategory(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list categories", ex);
        }
        return categories;
    }

    @Override
    public Optional<Category> findById(int id) {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCategory(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find category", ex);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = BASE_SELECT + " WHERE name = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCategory(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find category by name", ex);
        }
        return Optional.empty();
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                createdAt != null ? createdAt.toLocalDateTime() : null);
    }
}
