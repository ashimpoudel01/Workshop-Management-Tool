package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.*;
import com.motorworkshop.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Purchases and purchase line items with atomic inventory updates.
 */
public class PurchaseDAO {

    public List<Purchase> getAllPurchases() throws SQLException {
        List<Purchase> list = new ArrayList<>();
        String sql = "SELECT p.*, s.name as supplier_name FROM purchases p " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                     "ORDER BY p.date DESC, p.purchase_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapPurchaseSummary(rs));
            }
        }
        return list;
    }

    public Purchase getPurchaseById(int id) throws SQLException {
        String sql = "SELECT p.*, s.name as supplier_name FROM purchases p " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                     "WHERE p.purchase_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Purchase p = mapPurchaseSummary(rs);
                    p.setItems(getPurchaseItems(id, conn));
                    return p;
                }
            }
        }
        return null;
    }

    public List<PurchaseItem> getPurchaseItems(int purchaseId, Connection connOpt) throws SQLException {
        boolean autoClose = false;
        Connection conn = connOpt;
        if (conn == null) {
            conn = DatabaseManager.getConnection();
            autoClose = true;
        }

        List<PurchaseItem> items = new ArrayList<>();
        String sql = "SELECT pi.*, pr.part_name, pr.part_number " +
                     "FROM purchase_items pi " +
                     "JOIN products pr ON pi.product_id = pr.item_id " +
                     "WHERE pi.purchase_id = ? ORDER BY pi.id ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, purchaseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new PurchaseItem(
                            rs.getInt("id"),
                            rs.getInt("purchase_id"),
                            rs.getInt("product_id"),
                            rs.getString("part_name"),
                            rs.getString("part_number"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_purchase_price"),
                            rs.getDouble("total_price")
                    ));
                }
            }
            return items;
        } finally {
            if (autoClose && conn != null) {
                conn.close();
            }
        }
    }

    public List<Purchase> searchPurchases(String query, String startDate, String endDate, Integer supplierId) throws SQLException {
        List<Purchase> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, s.name as supplier_name FROM purchases p " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String pattern = "%" + query.trim() + "%";
            sql.append("AND (p.invoice_number LIKE ? OR s.name LIKE ? OR p.notes LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append("AND substr(p.date, 1, 10) >= ? ");
            params.add(startDate.trim());
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append("AND substr(p.date, 1, 10) <= ? ");
            params.add(endDate.trim());
        }

        if (supplierId != null && supplierId > 0) {
            sql.append("AND p.supplier_id = ? ");
            params.add(supplierId);
        }

        sql.append("ORDER BY p.date DESC, p.purchase_id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPurchaseSummary(rs));
                }
            }
        }
        return list;
    }

    /**
     * Atomically creates a purchase record, adds purchase items, increases product stock,
     * logs inventory transactions, and logs price history if purchase price has changed.
     */
    public boolean createPurchase(Purchase purchase) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert Purchase
            String sqlPurchase = "INSERT INTO purchases (invoice_number, date, supplier_id, total_amount, payment_status, notes) " +
                                 "VALUES (?, ?, ?, ?, ?, ?)";
            int purchaseId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPurchase, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, purchase.getInvoiceNumber());
                pstmt.setString(2, purchase.getDate());
                if (purchase.getSupplierId() > 0) pstmt.setInt(3, purchase.getSupplierId()); else pstmt.setNull(3, Types.INTEGER);
                pstmt.setDouble(4, purchase.getTotalAmount());
                pstmt.setString(5, purchase.getPaymentStatus());
                pstmt.setString(6, purchase.getNotes());
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        purchaseId = keys.getInt(1);
                        purchase.setPurchaseId(purchaseId);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. Insert Items & Update Products
            String sqlItem = "INSERT INTO purchase_items (purchase_id, product_id, quantity, unit_purchase_price, total_price) " +
                             "VALUES (?, ?, ?, ?, ?)";
            String sqlGetProd = "SELECT current_quantity, purchase_price, selling_price FROM products WHERE item_id = ?";
            String sqlUpdateProd = "UPDATE products SET current_quantity = current_quantity + ?, purchase_price = ?, last_purchase_date = ? WHERE item_id = ?";
            String sqlTx = "INSERT INTO inventory_transactions (product_id, date, transaction_type, quantity, previous_stock, new_stock, reference_id, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlPriceHist = "INSERT INTO price_history (product_id, old_purchase_price, new_purchase_price, old_selling_price, new_selling_price, change_date, reason) " +
                                  "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pItem = conn.prepareStatement(sqlItem);
                 PreparedStatement pGetProd = conn.prepareStatement(sqlGetProd);
                 PreparedStatement pUpdateProd = conn.prepareStatement(sqlUpdateProd);
                 PreparedStatement pTx = conn.prepareStatement(sqlTx);
                 PreparedStatement pPriceHist = conn.prepareStatement(sqlPriceHist)) {

                for (PurchaseItem item : purchase.getItems()) {
                    // Save item
                    pItem.setInt(1, purchaseId);
                    pItem.setInt(2, item.getProductId());
                    pItem.setInt(3, item.getQuantity());
                    pItem.setDouble(4, item.getUnitPurchasePrice());
                    pItem.setDouble(5, item.getTotalPrice());
                    pItem.executeUpdate();

                    // Read previous product stock and price
                    pGetProd.setInt(1, item.getProductId());
                    int prevStock = 0;
                    double oldBuyPrice = 0.0;
                    double currSellPrice = 0.0;
                    try (ResultSet rsP = pGetProd.executeQuery()) {
                        if (rsP.next()) {
                            prevStock = rsP.getInt("current_quantity");
                            oldBuyPrice = rsP.getDouble("purchase_price");
                            currSellPrice = rsP.getDouble("selling_price");
                        }
                    }

                    int newStock = prevStock + item.getQuantity();

                    // Update product
                    pUpdateProd.setInt(1, item.getQuantity());
                    pUpdateProd.setDouble(2, item.getUnitPurchasePrice());
                    pUpdateProd.setString(3, purchase.getDate());
                    pUpdateProd.setInt(4, item.getProductId());
                    pUpdateProd.executeUpdate();

                    // Inventory Transaction
                    pTx.setInt(1, item.getProductId());
                    pTx.setString(2, purchase.getDate());
                    pTx.setString(3, InventoryTransaction.Type.PURCHASE.name());
                    pTx.setInt(4, item.getQuantity());
                    pTx.setInt(5, prevStock);
                    pTx.setInt(6, newStock);
                    pTx.setString(7, purchase.getInvoiceNumber());
                    pTx.setString(8, "Purchase Invoice: " + purchase.getInvoiceNumber());
                    pTx.executeUpdate();

                    // Record price change if buy price changed
                    if (Math.abs(oldBuyPrice - item.getUnitPurchasePrice()) > 0.001) {
                        pPriceHist.setInt(1, item.getProductId());
                        pPriceHist.setDouble(2, oldBuyPrice);
                        pPriceHist.setDouble(3, item.getUnitPurchasePrice());
                        pPriceHist.setDouble(4, currSellPrice);
                        pPriceHist.setDouble(5, currSellPrice);
                        pPriceHist.setString(6, purchase.getDate());
                        pPriceHist.setString(7, "Updated via Purchase Inv: " + purchase.getInvoiceNumber());
                        pPriceHist.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deletePurchase(int purchaseId) throws SQLException {
        // Rollback stock when deleting purchase
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            List<PurchaseItem> items = getPurchaseItems(purchaseId, conn);
            Purchase p = getPurchaseById(purchaseId);

            String sqlDeduct = "UPDATE products SET current_quantity = current_quantity - ? WHERE item_id = ?";
            String sqlTx = "INSERT INTO inventory_transactions (product_id, date, transaction_type, quantity, previous_stock, new_stock, reference_id, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlGetProd = "SELECT current_quantity FROM products WHERE item_id = ?";

            try (PreparedStatement pDeduct = conn.prepareStatement(sqlDeduct);
                 PreparedStatement pTx = conn.prepareStatement(sqlTx);
                 PreparedStatement pGetProd = conn.prepareStatement(sqlGetProd)) {

                for (PurchaseItem item : items) {
                    pGetProd.setInt(1, item.getProductId());
                    int prevStock = 0;
                    try (ResultSet rsP = pGetProd.executeQuery()) {
                        if (rsP.next()) prevStock = rsP.getInt("current_quantity");
                    }
                    int newStock = prevStock - item.getQuantity();

                    pDeduct.setInt(1, item.getQuantity());
                    pDeduct.setInt(2, item.getProductId());
                    pDeduct.executeUpdate();

                    pTx.setInt(1, item.getProductId());
                    pTx.setString(2, DateUtil.today());
                    pTx.setString(3, InventoryTransaction.Type.STOCK_ADJUSTMENT.name());
                    pTx.setInt(4, -item.getQuantity());
                    pTx.setInt(5, prevStock);
                    pTx.setInt(6, newStock);
                    pTx.setString(7, p != null ? p.getInvoiceNumber() : "DEL-" + purchaseId);
                    pTx.setString(8, "Reversal due to purchase deletion");
                    pTx.executeUpdate();
                }
            }

            try (PreparedStatement pDel = conn.prepareStatement("DELETE FROM purchases WHERE purchase_id = ?")) {
                pDel.setInt(1, purchaseId);
                pDel.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public double getTotalPurchasesInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM purchases WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    private Purchase mapPurchaseSummary(ResultSet rs) throws SQLException {
        Purchase p = new Purchase();
        p.setPurchaseId(rs.getInt("purchase_id"));
        p.setInvoiceNumber(rs.getString("invoice_number"));
        p.setDate(rs.getString("date"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setSupplierName(rs.getString("supplier_name"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setPaymentStatus(rs.getString("payment_status"));
        p.setNotes(rs.getString("notes"));
        return p;
    }
}
