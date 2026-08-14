package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Products / Inventory.
 */
public class ProductDAO {

    public List<Product> getAllProducts(boolean activeOnly) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                     (activeOnly ? "WHERE p.is_active = 1 " : "") +
                     "ORDER BY p.part_name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        }
        return list;
    }

    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM products p " +
                     "LEFT JOIN categories c ON p.category_id = c.category_id " +
                     "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                     "WHERE p.item_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }
        }
        return null;
    }

    public List<Product> searchProducts(String query, Integer categoryId, String brand, Boolean lowStockOnly, Boolean outOfStockOnly) throws SQLException {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.category_id " +
                "LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
                "WHERE p.is_active = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String pattern = "%" + query.trim() + "%";
            sql.append("AND (p.part_name LIKE ? OR p.part_number LIKE ? OR p.brand LIKE ? OR s.name LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }

        if (brand != null && !brand.trim().isEmpty() && !brand.equalsIgnoreCase("All Brands")) {
            sql.append("AND p.brand = ? ");
            params.add(brand.trim());
        }

        if (lowStockOnly != null && lowStockOnly) {
            sql.append("AND p.current_quantity <= p.min_stock_level AND p.current_quantity > 0 ");
        }

        if (outOfStockOnly != null && outOfStockOnly) {
            sql.append("AND p.current_quantity <= 0 ");
        }

        sql.append("ORDER BY p.part_name ASC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapProduct(rs));
                }
            }
        }
        return list;
    }

    public List<String> getAllBrands() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT brand FROM products WHERE brand IS NOT NULL AND brand != '' AND is_active = 1 ORDER BY brand ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("brand"));
            }
        }
        return list;
    }

    public boolean insertProduct(Product p, Connection connOpt) throws SQLException {
        boolean autoClose = false;
        Connection conn = connOpt;
        if (conn == null) {
            conn = DatabaseManager.getConnection();
            autoClose = true;
        }

        String sql = "INSERT INTO products (part_name, part_number, category_id, brand, supplier_id, " +
                "purchase_price, selling_price, workshop_price, current_quantity, min_stock_level, unit, date_added, last_purchase_date, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, p.getPartName());
            pstmt.setString(2, p.getPartNumber());
            if (p.getCategoryId() > 0) pstmt.setInt(3, p.getCategoryId()); else pstmt.setNull(3, Types.INTEGER);
            pstmt.setString(4, p.getBrand());
            if (p.getSupplierId() > 0) pstmt.setInt(5, p.getSupplierId()); else pstmt.setNull(5, Types.INTEGER);
            pstmt.setDouble(6, p.getPurchasePrice());
            pstmt.setDouble(7, p.getSellingPrice());
            pstmt.setDouble(8, p.getWorkshopPrice());
            pstmt.setInt(9, p.getCurrentQuantity());
            pstmt.setInt(10, p.getMinStockLevel());
            pstmt.setString(11, p.getUnit());
            pstmt.setString(12, p.getDateAdded());
            pstmt.setString(13, p.getLastPurchaseDate());
            pstmt.setInt(14, p.isActive() ? 1 : 0);

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        p.setItemId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } finally {
            if (autoClose && conn != null) {
                conn.close();
            }
        }
    }

    public boolean updateProduct(Product p, Connection connOpt) throws SQLException {
        boolean autoClose = false;
        Connection conn = connOpt;
        if (conn == null) {
            conn = DatabaseManager.getConnection();
            autoClose = true;
        }

        String sql = "UPDATE products SET part_name = ?, part_number = ?, category_id = ?, brand = ?, " +
                "supplier_id = ?, purchase_price = ?, selling_price = ?, workshop_price = ?, " +
                "min_stock_level = ?, unit = ?, is_active = ? WHERE item_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getPartName());
            pstmt.setString(2, p.getPartNumber());
            if (p.getCategoryId() > 0) pstmt.setInt(3, p.getCategoryId()); else pstmt.setNull(3, Types.INTEGER);
            pstmt.setString(4, p.getBrand());
            if (p.getSupplierId() > 0) pstmt.setInt(5, p.getSupplierId()); else pstmt.setNull(5, Types.INTEGER);
            pstmt.setDouble(6, p.getPurchasePrice());
            pstmt.setDouble(7, p.getSellingPrice());
            pstmt.setDouble(8, p.getWorkshopPrice());
            pstmt.setInt(9, p.getMinStockLevel());
            pstmt.setString(10, p.getUnit());
            pstmt.setInt(11, p.isActive() ? 1 : 0);
            pstmt.setInt(12, p.getItemId());

            return pstmt.executeUpdate() > 0;
        } finally {
            if (autoClose && conn != null) {
                conn.close();
            }
        }
    }

    public boolean updateStockAndPrice(int productId, int quantityDelta, double newPurchasePrice, String purchaseDate, Connection conn) throws SQLException {
        String sql = "UPDATE products SET current_quantity = current_quantity + ?, purchase_price = ?, last_purchase_date = ? WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityDelta);
            pstmt.setDouble(2, newPurchasePrice);
            pstmt.setString(3, purchaseDate);
            pstmt.setInt(4, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateStock(int productId, int quantityDelta, Connection conn) throws SQLException {
        String sql = "UPDATE products SET current_quantity = current_quantity + ? WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantityDelta);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean setStock(int productId, int newQuantity, Connection conn) throws SQLException {
        String sql = "UPDATE products SET current_quantity = ? WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteProduct(int id) throws SQLException {
        // Soft delete by setting is_active = 0
        String sql = "UPDATE products SET is_active = 0 WHERE item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public double getTotalStockValue() throws SQLException {
        String sql = "SELECT SUM(current_quantity * purchase_price) FROM products WHERE is_active = 1 AND current_quantity > 0";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = 1 AND current_quantity <= min_stock_level AND current_quantity > 0";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getOutOfStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = 1 AND current_quantity <= 0";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getTotalProductsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setItemId(rs.getInt("item_id"));
        p.setPartName(rs.getString("part_name"));
        p.setPartNumber(rs.getString("part_number"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setBrand(rs.getString("brand"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setSupplierName(rs.getString("supplier_name"));
        p.setPurchasePrice(rs.getDouble("purchase_price"));
        p.setSellingPrice(rs.getDouble("selling_price"));
        p.setWorkshopPrice(rs.getDouble("workshop_price"));
        p.setCurrentQuantity(rs.getInt("current_quantity"));
        p.setMinStockLevel(rs.getInt("min_stock_level"));
        p.setUnit(rs.getString("unit"));
        p.setDateAdded(rs.getString("date_added"));
        p.setLastPurchaseDate(rs.getString("last_purchase_date"));
        p.setActive(rs.getInt("is_active") == 1);
        return p;
    }
}
