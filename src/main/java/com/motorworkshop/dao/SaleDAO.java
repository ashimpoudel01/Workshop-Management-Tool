package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.*;
import com.motorworkshop.util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Sales, Invoices, line items, exact COGS tracking, and Inventory decrements.
 */
public class SaleDAO {

    public List<Sale> getAllSales() throws SQLException {
        List<Sale> list = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY date DESC, sale_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapSaleSummary(rs));
            }
        }
        return list;
    }

    public Sale getSaleById(int saleId) throws SQLException {
        String sql = "SELECT * FROM sales WHERE sale_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapSaleSummary(rs);
                    sale.setItems(getSaleItems(saleId, conn));
                    return sale;
                }
            }
        }
        return null;
    }

    public List<SaleItem> getSaleItems(int saleId, Connection connOpt) throws SQLException {
        boolean autoClose = false;
        Connection conn = connOpt;
        if (conn == null) {
            conn = DatabaseManager.getConnection();
            autoClose = true;
        }

        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT * FROM sale_items WHERE sale_id = ? ORDER BY id ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem.ItemType type;
                    try {
                        type = SaleItem.ItemType.valueOf(rs.getString("item_type"));
                    } catch (Exception e) {
                        type = SaleItem.ItemType.PART;
                    }
                    items.add(new SaleItem(
                            rs.getInt("id"),
                            rs.getInt("sale_id"),
                            type,
                            rs.getInt("item_id"),
                            rs.getString("item_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("unit_cost"),
                            rs.getDouble("total_price"),
                            rs.getDouble("total_cost")
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

    public List<Sale> searchSales(String query, String startDate, String endDate, String paymentMethod) throws SQLException {
        List<Sale> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM sales WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String pattern = "%" + query.trim() + "%";
            sql.append("AND (invoice_number LIKE ? OR customer_name LIKE ? OR customer_phone LIKE ? OR vehicle_reg_no LIKE ? OR vehicle_model LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append("AND substr(date, 1, 10) >= ? ");
            params.add(startDate.trim());
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append("AND substr(date, 1, 10) <= ? ");
            params.add(endDate.trim());
        }

        if (paymentMethod != null && !paymentMethod.trim().isEmpty() && !paymentMethod.equalsIgnoreCase("All Methods")) {
            sql.append("AND payment_method = ? ");
            params.add(paymentMethod.trim());
        }

        sql.append("ORDER BY date DESC, sale_id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSaleSummary(rs));
                }
            }
        }
        return list;
    }

    /**
     * Atomically creates a Sale transaction:
     * - Verifies stock for parts
     * - Decrements product inventory
     * - Records inventory transactions
     * - Calculates accurate COGS using purchase_price of parts
     * - Inserts Sale and SaleItems
     */
    public boolean createSale(Sale sale, boolean allowNegativeStock) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1. Verify stock & calculate accurate COGS for parts
            String sqlCheck = "SELECT current_quantity, purchase_price, part_name FROM products WHERE item_id = ?";
            try (PreparedStatement pCheck = conn.prepareStatement(sqlCheck)) {
                for (SaleItem item : sale.getItems()) {
                    if (item.getItemType() == SaleItem.ItemType.PART) {
                        pCheck.setInt(1, item.getItemId());
                        try (ResultSet rs = pCheck.executeQuery()) {
                            if (rs.next()) {
                                int currentStock = rs.getInt("current_quantity");
                                double buyCost = rs.getDouble("purchase_price");
                                String name = rs.getString("part_name");

                                if (!allowNegativeStock && currentStock < item.getQuantity()) {
                                    throw new SQLException("Insufficient stock for '" + name + "'. Available: " + currentStock + ", Requested: " + item.getQuantity());
                                }

                                // Update unit cost with actual current purchase price
                                item.setUnitCost(buyCost);
                                item.setTotalCost(buyCost * item.getQuantity());
                            } else {
                                throw new SQLException("Product ID " + item.getItemId() + " not found!");
                            }
                        }
                    } else {
                        // Services have 0 COGS
                        item.setUnitCost(0.0);
                        item.setTotalCost(0.0);
                    }
                }
            }

            // Recalculate totals with exact COGS
            sale.recalculateTotals();

            // 2. Insert Sale
            String sqlSale = "INSERT INTO sales (invoice_number, date, customer_id, customer_name, customer_phone, " +
                             "vehicle_type, vehicle_brand, vehicle_model, vehicle_reg_no, " +
                             "service_charge, parts_total, discount, subtotal, total_amount, " +
                             "total_cogs, gross_profit, payment_method, notes) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int saleId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, sale.getInvoiceNumber());
                pstmt.setString(2, sale.getDate());
                if (sale.getCustomerId() > 0) pstmt.setInt(3, sale.getCustomerId()); else pstmt.setNull(3, Types.INTEGER);
                pstmt.setString(4, sale.getCustomerName());
                pstmt.setString(5, sale.getCustomerPhone());
                pstmt.setString(6, sale.getVehicleType());
                pstmt.setString(7, sale.getVehicleBrand());
                pstmt.setString(8, sale.getVehicleModel());
                pstmt.setString(9, sale.getVehicleRegNo());
                pstmt.setDouble(10, sale.getServiceCharge());
                pstmt.setDouble(11, sale.getPartsTotal());
                pstmt.setDouble(12, sale.getDiscount());
                pstmt.setDouble(13, sale.getSubtotal());
                pstmt.setDouble(14, sale.getTotalAmount());
                pstmt.setDouble(15, sale.getTotalCogs());
                pstmt.setDouble(16, sale.getGrossProfit());
                pstmt.setString(17, sale.getPaymentMethod());
                pstmt.setString(18, sale.getNotes());
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        saleId = keys.getInt(1);
                        sale.setSaleId(saleId);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 3. Insert Sale Items & Deduct Parts Stock
            String sqlItem = "INSERT INTO sale_items (sale_id, item_type, item_id, item_name, quantity, unit_price, unit_cost, total_price, total_cost) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlDeduct = "UPDATE products SET current_quantity = current_quantity - ? WHERE item_id = ?";
            String sqlGetStock = "SELECT current_quantity FROM products WHERE item_id = ?";
            String sqlTx = "INSERT INTO inventory_transactions (product_id, date, transaction_type, quantity, previous_stock, new_stock, reference_id, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pItem = conn.prepareStatement(sqlItem);
                 PreparedStatement pDeduct = conn.prepareStatement(sqlDeduct);
                 PreparedStatement pGetStock = conn.prepareStatement(sqlGetStock);
                 PreparedStatement pTx = conn.prepareStatement(sqlTx)) {

                for (SaleItem item : sale.getItems()) {
                    pItem.setInt(1, saleId);
                    pItem.setString(2, item.getItemType().name());
                    if (item.getItemId() > 0) pItem.setInt(3, item.getItemId()); else pItem.setNull(3, Types.INTEGER);
                    pItem.setString(4, item.getItemName());
                    pItem.setInt(5, item.getQuantity());
                    pItem.setDouble(6, item.getUnitPrice());
                    pItem.setDouble(7, item.getUnitCost());
                    pItem.setDouble(8, item.getTotalPrice());
                    pItem.setDouble(9, item.getTotalCost());
                    pItem.executeUpdate();

                    if (item.getItemType() == SaleItem.ItemType.PART && item.getItemId() > 0) {
                        // Deduct stock
                        pGetStock.setInt(1, item.getItemId());
                        int prevStock = 0;
                        try (ResultSet rs = pGetStock.executeQuery()) {
                            if (rs.next()) prevStock = rs.getInt("current_quantity");
                        }
                        int newStock = prevStock - item.getQuantity();

                        pDeduct.setInt(1, item.getQuantity());
                        pDeduct.setInt(2, item.getItemId());
                        pDeduct.executeUpdate();

                        // Log inventory transaction
                        pTx.setInt(1, item.getItemId());
                        pTx.setString(2, sale.getDate());
                        pTx.setString(3, InventoryTransaction.Type.SALE.name());
                        pTx.setInt(4, item.getQuantity());
                        pTx.setInt(5, prevStock);
                        pTx.setInt(6, newStock);
                        pTx.setString(7, sale.getInvoiceNumber());
                        pTx.setString(8, "Sold in Invoice: " + sale.getInvoiceNumber());
                        pTx.executeUpdate();
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

    public boolean deleteSale(int saleId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            List<SaleItem> items = getSaleItems(saleId, conn);
            Sale sale = getSaleById(saleId);

            String sqlRestock = "UPDATE products SET current_quantity = current_quantity + ? WHERE item_id = ?";
            String sqlTx = "INSERT INTO inventory_transactions (product_id, date, transaction_type, quantity, previous_stock, new_stock, reference_id, notes) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlGetStock = "SELECT current_quantity FROM products WHERE item_id = ?";

            try (PreparedStatement pRestock = conn.prepareStatement(sqlRestock);
                 PreparedStatement pTx = conn.prepareStatement(sqlTx);
                 PreparedStatement pGetStock = conn.prepareStatement(sqlGetStock)) {

                for (SaleItem item : items) {
                    if (item.getItemType() == SaleItem.ItemType.PART && item.getItemId() > 0) {
                        pGetStock.setInt(1, item.getItemId());
                        int prevStock = 0;
                        try (ResultSet rs = pGetStock.executeQuery()) {
                            if (rs.next()) prevStock = rs.getInt("current_quantity");
                        }
                        int newStock = prevStock + item.getQuantity();

                        pRestock.setInt(1, item.getQuantity());
                        pRestock.setInt(2, item.getItemId());
                        pRestock.executeUpdate();

                        pTx.setInt(1, item.getItemId());
                        pTx.setString(2, DateUtil.today());
                        pTx.setString(3, InventoryTransaction.Type.RETURN.name());
                        pTx.setInt(4, item.getQuantity());
                        pTx.setInt(5, prevStock);
                        pTx.setInt(6, newStock);
                        pTx.setString(7, sale != null ? sale.getInvoiceNumber() : "DEL-" + saleId);
                        pTx.setString(8, "Reversed stock due to deleted invoice");
                        pTx.executeUpdate();
                    }
                }
            }

            try (PreparedStatement pDel = conn.prepareStatement("DELETE FROM sales WHERE sale_id = ?")) {
                pDel.setInt(1, saleId);
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

    public String generateNextInvoiceNumber(String prefix) {
        String sql = "SELECT MAX(sale_id) FROM sales";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            return (prefix != null ? prefix : "INV-") + String.format("%05d", nextId);
        } catch (Exception e) {
            return (prefix != null ? prefix : "INV-") + System.currentTimeMillis() % 100000;
        }
    }

    public double getTotalSalesInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(total_amount) FROM sales WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getTotalPartsRevenueInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(parts_total) FROM sales WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getTotalServiceRevenueInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(service_charge) FROM sales WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getTotalCogsInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(total_cogs) FROM sales WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getTotalGrossProfitInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(gross_profit) FROM sales WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int getInvoiceCountInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sales WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<DashboardStats.TopItemMetric> getTopSellingParts(String startDate, String endDate, int limit) throws SQLException {
        List<DashboardStats.TopItemMetric> list = new ArrayList<>();
        String sql = "SELECT si.item_name, SUM(si.quantity) as total_qty, SUM(si.total_price) as total_rev " +
                     "FROM sale_items si " +
                     "JOIN sales s ON si.sale_id = s.sale_id " +
                     "WHERE si.item_type = 'PART' AND substr(s.date, 1, 10) >= ? AND substr(s.date, 1, 10) <= ? " +
                     "GROUP BY si.item_name ORDER BY total_qty DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            pstmt.setInt(3, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new DashboardStats.TopItemMetric(
                            rs.getString("item_name"),
                            rs.getInt("total_qty"),
                            rs.getDouble("total_rev")
                    ));
                }
            }
        }
        return list;
    }

    public List<DashboardStats.TopItemMetric> getTopPerformingServices(String startDate, String endDate, int limit) throws SQLException {
        List<DashboardStats.TopItemMetric> list = new ArrayList<>();
        String sql = "SELECT si.item_name, SUM(si.quantity) as total_qty, SUM(si.total_price) as total_rev " +
                     "FROM sale_items si " +
                     "JOIN sales s ON si.sale_id = s.sale_id " +
                     "WHERE si.item_type = 'SERVICE' AND substr(s.date, 1, 10) >= ? AND substr(s.date, 1, 10) <= ? " +
                     "GROUP BY si.item_name ORDER BY total_qty DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            pstmt.setInt(3, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new DashboardStats.TopItemMetric(
                            rs.getString("item_name"),
                            rs.getInt("total_qty"),
                            rs.getDouble("total_rev")
                    ));
                }
            }
        }
        return list;
    }

    private Sale mapSaleSummary(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setSaleId(rs.getInt("sale_id"));
        s.setInvoiceNumber(rs.getString("invoice_number"));
        s.setDate(rs.getString("date"));
        s.setCustomerId(rs.getInt("customer_id"));
        s.setCustomerName(rs.getString("customer_name"));
        s.setCustomerPhone(rs.getString("customer_phone"));
        s.setVehicleType(rs.getString("vehicle_type"));
        s.setVehicleBrand(rs.getString("vehicle_brand"));
        s.setVehicleModel(rs.getString("vehicle_model"));
        s.setVehicleRegNo(rs.getString("vehicle_reg_no"));
        s.setServiceCharge(rs.getDouble("service_charge"));
        s.setPartsTotal(rs.getDouble("parts_total"));
        s.setDiscount(rs.getDouble("discount"));
        s.setSubtotal(rs.getDouble("subtotal"));
        s.setTotalAmount(rs.getDouble("total_amount"));
        s.setTotalCogs(rs.getDouble("total_cogs"));
        s.setGrossProfit(rs.getDouble("gross_profit"));
        s.setPaymentMethod(rs.getString("payment_method"));
        s.setNotes(rs.getString("notes"));
        return s;
    }
}
