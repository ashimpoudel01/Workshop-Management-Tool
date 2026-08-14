package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.PriceHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Price History tracking.
 */
public class PriceHistoryDAO {

    public List<PriceHistory> getHistoryByProduct(int productId) throws SQLException {
        List<PriceHistory> list = new ArrayList<>();
        String sql = "SELECT ph.*, p.part_name FROM price_history ph " +
                     "JOIN products p ON ph.product_id = p.item_id " +
                     "WHERE ph.product_id = ? " +
                     "ORDER BY ph.change_date DESC, ph.history_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PriceHistory(
                            rs.getInt("history_id"),
                            rs.getInt("product_id"),
                            rs.getString("part_name"),
                            rs.getDouble("old_purchase_price"),
                            rs.getDouble("new_purchase_price"),
                            rs.getDouble("old_selling_price"),
                            rs.getDouble("new_selling_price"),
                            rs.getString("change_date"),
                            rs.getString("reason")
                    ));
                }
            }
        }
        return list;
    }

    public List<PriceHistory> getAllRecentHistory(int limit) throws SQLException {
        List<PriceHistory> list = new ArrayList<>();
        String sql = "SELECT ph.*, p.part_name FROM price_history ph " +
                     "JOIN products p ON ph.product_id = p.item_id " +
                     "ORDER BY ph.change_date DESC, ph.history_id DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PriceHistory(
                            rs.getInt("history_id"),
                            rs.getInt("product_id"),
                            rs.getString("part_name"),
                            rs.getDouble("old_purchase_price"),
                            rs.getDouble("new_purchase_price"),
                            rs.getDouble("old_selling_price"),
                            rs.getDouble("new_selling_price"),
                            rs.getString("change_date"),
                            rs.getString("reason")
                    ));
                }
            }
        }
        return list;
    }

    public boolean insertPriceHistory(PriceHistory ph, Connection connOpt) throws SQLException {
        boolean autoClose = false;
        Connection conn = connOpt;
        if (conn == null) {
            conn = DatabaseManager.getConnection();
            autoClose = true;
        }

        String sql = "INSERT INTO price_history (product_id, old_purchase_price, new_purchase_price, " +
                     "old_selling_price, new_selling_price, change_date, reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ph.getProductId());
            pstmt.setDouble(2, ph.getOldPurchasePrice());
            pstmt.setDouble(3, ph.getNewPurchasePrice());
            pstmt.setDouble(4, ph.getOldSellingPrice());
            pstmt.setDouble(5, ph.getNewSellingPrice());
            pstmt.setString(6, ph.getChangeDate());
            pstmt.setString(7, ph.getReason());
            return pstmt.executeUpdate() > 0;
        } finally {
            if (autoClose && conn != null) {
                conn.close();
            }
        }
    }
}
