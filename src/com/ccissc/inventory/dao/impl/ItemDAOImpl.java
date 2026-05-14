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
    private static final String BASE_SELECT = "SELECT i.id, i.item_name, i.description, i.quantity, i.min_quantity, "
            + "i.image_path, i.category_id, c.name AS category_name, i.is_archived, i.created_by, "
            + "i.created_at, i.updated_at FROM items i JOIN categories c ON i.category_id = c.id";

    @Override
    public Optional<Item> findById(int id) {
        String sql = BASE_SELECT + " WHERE i.id = ?";
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
    public Optional<Item> findByName(String name, boolean includeArchived) {
        String sql = BASE_SELECT + " WHERE i.item_name = ?" + (includeArchived ? "" : " AND i.is_archived = 0");
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapItem(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to find item by name", ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> findAll(boolean includeArchived) {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + (includeArchived ? "" : " WHERE i.is_archived = 0")
                + " ORDER BY i.created_at DESC";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
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
    public List<Item> search(String query, Integer categoryId, boolean includeArchived) {
        List<Item> items = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;

        if (!includeArchived) {
            sql.append(" WHERE i.is_archived = 0");
            hasWhere = true;
        }

        if (query != null && !query.isBlank()) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" (i.item_name LIKE ? OR i.description LIKE ?)");
            String likeQuery = "%" + query + "%";
            params.add(likeQuery);
            params.add(likeQuery);
            hasWhere = true;
        }

        if (categoryId != null && categoryId > 0) {
            sql.append(hasWhere ? " AND" : " WHERE");
            sql.append(" i.category_id = ?");
            params.add(categoryId);
        }

        sql.append(" ORDER BY i.created_at DESC");
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
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
        String sql = "INSERT INTO items (item_name, description, quantity, min_quantity, image_path, category_id, "
                + "is_archived, created_by) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getQuantity());
            stmt.setInt(4, item.getMinQuantity());
            stmt.setString(5, item.getImagePath());
            stmt.setInt(6, item.getCategoryId());
            stmt.setBoolean(7, item.isArchived());
            stmt.setInt(8, item.getCreatedBy());
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
        String sql = "UPDATE items SET item_name = ?, description = ?, quantity = ?, min_quantity = ?, image_path = ?, "
                + "category_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getQuantity());
            stmt.setInt(4, item.getMinQuantity());
            stmt.setString(5, item.getImagePath());
            stmt.setInt(6, item.getCategoryId());
            stmt.setInt(7, item.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update item", ex);
        }
    }

    @Override
    public boolean updateQuantity(int itemId, int newQuantity) {
        String sql = "UPDATE items SET quantity = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update item quantity", ex);
        }
    }

    @Override
    public boolean setArchived(int itemId, boolean archived) {
        String sql = "UPDATE items SET is_archived = ? WHERE id = ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, archived);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update item archive state", ex);
        }
    }

    @Override
    public int getTotalItems() {
        String sql = "SELECT COUNT(*) FROM items WHERE is_archived = 0";
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
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM items WHERE is_archived = 0";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch total stock", ex);
        }
    }

    @Override
    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM items WHERE is_archived = 0 AND quantity <= min_quantity";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch low stock count", ex);
        }
    }

    @Override
    public List<Item> getLowStockItems(int limit) {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE i.is_archived = 0 AND i.quantity <= i.min_quantity "
                + "ORDER BY i.quantity ASC LIMIT ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch low stock items", ex);
        }
        return items;
    }

    @Override
    public List<Item> getTopItems(int limit) {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE i.is_archived = 0 ORDER BY i.quantity DESC LIMIT ?";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch top items", ex);
        }
        return items;
    }

    @Override
    public List<Item> getRecentItems(int limit) {
        List<Item> items = new ArrayList<>();
        String sql = BASE_SELECT + " WHERE i.is_archived = 0 ORDER BY i.created_at DESC LIMIT ?";
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
                rs.getInt("min_quantity"),
                rs.getString("image_path"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getBoolean("is_archived"),
                rs.getInt("created_by"),
                createdAt != null ? createdAt.toLocalDateTime() : null,
                updatedAt != null ? updatedAt.toLocalDateTime() : null);
    }
}
