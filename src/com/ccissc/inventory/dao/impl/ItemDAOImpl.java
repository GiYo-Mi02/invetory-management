package com.ccissc.inventory.dao.impl;

import com.ccissc.inventory.config.DatabaseConfig;
import com.ccissc.inventory.dao.ItemDAO;
import com.ccissc.inventory.model.Item;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemDAOImpl implements ItemDAO {
    private static final String BASE_SELECT = "SELECT id, item_name, description, quantity, image_path, created_by, created_at, updated_at FROM items";

    @Override
    public Optional<Item> findById(int id) {
        String sql = BASE_SELECT + " WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapItem(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find item", ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll() {
        List<Item> items = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(BASE_SELECT + " ORDER BY created_at DESC")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to list items", ex);
        }
        return items;
    }

    @Override
    public List<Item> search(String query) {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE item_name LIKE ? OR description LIKE ? ORDER BY created_at DESC";
        String likeQuery = "%" + query + "%";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, likeQuery);
            stmt.setString(2, likeQuery);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to search items", ex);
        }
        return items;
    }

    @Override
    public int create(Item item) {
        String sql = "INSERT INTO items (item_name, description, quantity, image_path, created_by) VALUES (?,?,?,?,?)";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getQuantity());
            stmt.setString(4, item.getImagePath());
            stmt.setInt(5, item.getCreatedBy());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create item", ex);
        }
        return 0;
    }

    @Override
    public boolean update(Item item) {
        String sql = "UPDATE items SET item_name = ?, description = ?, quantity = ?, image_path = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getQuantity());
            stmt.setString(4, item.getImagePath());
            stmt.setInt(5, item.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update item", ex);
        }
    }

    @Override
    public boolean delete(int itemId) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete item", ex);
        }
    }

    @Override
    public int getTotalItems() {
        String sql = "SELECT COUNT(*) FROM items";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch total items", ex);
        }
    }

    @Override
    public int getTotalStock() {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM items";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch total stock", ex);
        }
    }

    @Override
    public int getLowStockCount(int threshold) {
        String sql = "SELECT COUNT(*) FROM items WHERE quantity <= ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, threshold);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch low stock count", ex);
        }
    }

    @Override
    public List<Item> getRecentItems(int limit) {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + " ORDER BY created_at DESC LIMIT ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch recent items", ex);
        }
        return items;
    }

    private Item mapItem(ResultSet rs) throws SQLException {
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        return new Item(
                rs.getInt("id"),
                rs.getString("item_name"),
                rs.getString("description"),
                rs.getInt("quantity"),
                rs.getString("image_path"),
                rs.getInt("created_by"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null);
    }
}
