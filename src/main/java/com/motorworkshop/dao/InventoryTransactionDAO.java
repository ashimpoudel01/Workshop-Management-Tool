package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.InventoryTransaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Inventory Movement Audit Logs.
 */
public class InventoryTransactionDAO {

    public List<InventoryTransaction> getTransactionsByProduct(int productId) throws SQLException {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.*, p.part_name FROM inventory_transactions t " +
                     "JOIN products p ON t.product_id = p.item_id " +
                     "WHERE t.product_id = ? " +
                     "ORDER BY t.date DESC, t.transaction_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTransaction(rs));
                }
            }
        }
        return list;
    }

    public List<InventoryTransaction> getRecentTransactions(int limit) throws SQLException {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT t.*, p.part_name FROM inventory_transactions t " +
                     "JOIN products p ON t.product_id = p.item_id " +
                     "ORDER BY t.date DESC, t.transaction_id DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTransaction(rs));
                }
            }
        }
        return list;
    }

    public boolean insertTransaction(InventoryTransaction tx, Connection connOpt) throws SQLException {
        boolean autoClose = false;
        Connection conn = connOpt;
        if (conn == null) {
            conn = DatabaseManager.getConnection();
            autoClose = true;
        }

        String sql = "INSERT INTO inventory_transactions (product_id, date, transaction_type, quantity, " +
                     "previous_stock, new_stock, reference_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tx.getProductId());
            pstmt.setString(2, tx.getDate());
            pstmt.setString(3, tx.getTransactionType().name());
            pstmt.setInt(4, tx.getQuantity());
            pstmt.setInt(5, tx.getPreviousStock());
            pstmt.setInt(6, tx.getNewStock());
            pstmt.setString(7, tx.getReferenceId());
            pstmt.setString(8, tx.getNotes());
            return pstmt.executeUpdate() > 0;
        } finally {
            if (autoClose && conn != null) {
                conn.close();
            }
        }
    }

    private InventoryTransaction mapTransaction(ResultSet rs) throws SQLException {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setTransactionId(rs.getInt("transaction_id"));
        tx.setProductId(rs.getInt("product_id"));
        tx.setProductName(rs.getString("part_name"));
        tx.setDate(rs.getString("date"));
        try {
            tx.setTransactionType(InventoryTransaction.Type.valueOf(rs.getString("transaction_type")));
        } catch (Exception e) {
            tx.setTransactionType(InventoryTransaction.Type.STOCK_ADJUSTMENT);
        }
        tx.setQuantity(rs.getInt("quantity"));
        tx.setPreviousStock(rs.getInt("previous_stock"));
        tx.setNewStock(rs.getInt("new_stock"));
        tx.setReferenceId(rs.getString("reference_id"));
        tx.setNotes(rs.getString("notes"));
        return tx;
    }
}
