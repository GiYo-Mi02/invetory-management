package com.ccissc.inventory.dao.impl;

import com.ccissc.inventory.config.DatabaseConfig;
import com.ccissc.inventory.dao.ItemHistoryDAO;
import com.ccissc.inventory.model.ItemHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ItemHistoryDAOImpl implements ItemHistoryDAO {
    @Override
    public int create(ItemHistory history) {
        String sql = "INSERT INTO item_history (item_id, action, changed_by, old_name, new_name, old_description, "
                + "new_description, old_quantity, new_quantity, old_category_id, new_category_id, old_min_quantity, "
                + "new_min_quantity, old_is_archived, new_is_archived, note) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, history.getItemId());
            stmt.setString(2, history.getAction());
            stmt.setInt(3, history.getChangedBy());
            stmt.setString(4, history.getOldName());
            stmt.setString(5, history.getNewName());
            stmt.setString(6, history.getOldDescription());
            stmt.setString(7, history.getNewDescription());
            stmt.setObject(8, history.getOldQuantity());
            stmt.setObject(9, history.getNewQuantity());
            stmt.setObject(10, history.getOldCategoryId());
            stmt.setObject(11, history.getNewCategoryId());
            stmt.setObject(12, history.getOldMinQuantity());
            stmt.setObject(13, history.getNewMinQuantity());
            stmt.setObject(14, history.getOldArchived());
            stmt.setObject(15, history.getNewArchived());
            stmt.setString(16, history.getNote());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create item history", ex);
        }
        return 0;
    }

    @Override
    public List<ItemHistory> findByItemId(int itemId, int limit) {
        String sql = "SELECT id, item_id, action, changed_by, old_name, new_name, old_description, new_description, "
                + "old_quantity, new_quantity, old_category_id, new_category_id, old_min_quantity, new_min_quantity, "
                + "old_is_archived, new_is_archived, note, created_at FROM item_history WHERE item_id = ? "
                + "ORDER BY created_at DESC LIMIT ?";
        List<ItemHistory> history = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(mapHistory(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch item history", ex);
        }
        return history;
    }

    @Override
    public List<ItemHistory> findRecent(int limit) {
        String sql = "SELECT id, item_id, action, changed_by, old_name, new_name, old_description, new_description, "
                + "old_quantity, new_quantity, old_category_id, new_category_id, old_min_quantity, new_min_quantity, "
                + "old_is_archived, new_is_archived, note, created_at FROM item_history "
                + "ORDER BY created_at DESC LIMIT ?";
        List<ItemHistory> history = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getInstance().getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(mapHistory(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to fetch recent history", ex);
        }
        return history;
    }

    private ItemHistory mapHistory(ResultSet rs) throws SQLException {
        ItemHistory history = new ItemHistory();
        history.setId(rs.getInt("id"));
        history.setItemId(rs.getInt("item_id"));
        history.setAction(rs.getString("action"));
        history.setChangedBy(rs.getInt("changed_by"));
        history.setOldName(rs.getString("old_name"));
        history.setNewName(rs.getString("new_name"));
        history.setOldDescription(rs.getString("old_description"));
        history.setNewDescription(rs.getString("new_description"));
        history.setOldQuantity((Integer) rs.getObject("old_quantity"));
        history.setNewQuantity((Integer) rs.getObject("new_quantity"));
        history.setOldCategoryId((Integer) rs.getObject("old_category_id"));
        history.setNewCategoryId((Integer) rs.getObject("new_category_id"));
        history.setOldMinQuantity((Integer) rs.getObject("old_min_quantity"));
        history.setNewMinQuantity((Integer) rs.getObject("new_min_quantity"));
        history.setOldArchived((Boolean) rs.getObject("old_is_archived"));
        history.setNewArchived((Boolean) rs.getObject("new_is_archived"));
        history.setNote(rs.getString("note"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        history.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        return history;
    }
}
