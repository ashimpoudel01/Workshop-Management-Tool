package com.motorworkshop.service;

import com.motorworkshop.dao.CategoryDAO;
import com.motorworkshop.dao.InventoryTransactionDAO;
import com.motorworkshop.dao.ProductDAO;
import com.motorworkshop.dao.SupplierDAO;
import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.Category;
import com.motorworkshop.model.InventoryTransaction;
import com.motorworkshop.model.Product;
import com.motorworkshop.model.Supplier;
import com.motorworkshop.util.DateUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic service for Product and Inventory lifecycle management.
 */
public class InventoryService {
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final InventoryTransactionDAO transactionDAO = new InventoryTransactionDAO();

    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts(true);
    }

    public Product getProductById(int id) throws SQLException {
        return productDAO.getProductById(id);
    }

    public List<Product> searchProducts(String query, Integer categoryId, String brand, Boolean lowStockOnly, Boolean outOfStockOnly) throws SQLException {
        return productDAO.searchProducts(query, categoryId, brand, lowStockOnly, outOfStockOnly);
    }

    public List<String> getAllBrands() throws SQLException {
        return productDAO.getAllBrands();
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.getAllSuppliers();
    }

    public boolean addProduct(Product p) throws SQLException {
        if (p.getPartName() == null || p.getPartName().trim().isEmpty()) {
            throw new IllegalArgumentException("Part Name is required!");
        }
        if (p.getSellingPrice() < 0 || p.getPurchasePrice() < 0) {
            throw new IllegalArgumentException("Prices cannot be negative!");
        }
        p.setDateAdded(DateUtil.today());
        p.setLastPurchaseDate(DateUtil.today());

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            boolean ok = productDAO.insertProduct(p, conn);
            if (ok && p.getCurrentQuantity() > 0) {
                // Log initial stock movement
                InventoryTransaction tx = new InventoryTransaction(
                        0,
                        p.getItemId(),
                        p.getPartName(),
                        DateUtil.today(),
                        InventoryTransaction.Type.STOCK_ADJUSTMENT,
                        p.getCurrentQuantity(),
                        0,
                        p.getCurrentQuantity(),
                        "INIT-" + p.getItemId(),
                        "Initial stock entry"
                );
                transactionDAO.insertTransaction(tx, conn);
            }
            conn.commit();
            return ok;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateProduct(Product p) throws SQLException {
        if (p.getPartName() == null || p.getPartName().trim().isEmpty()) {
            throw new IllegalArgumentException("Part Name is required!");
        }
        return productDAO.updateProduct(p, null);
    }

    public boolean deleteProduct(int id) throws SQLException {
        return productDAO.deleteProduct(id);
    }

    public boolean adjustStock(int productId, int quantityDelta, InventoryTransaction.Type txType, String notes) throws SQLException {
        Product p = productDAO.getProductById(productId);
        if (p == null) {
            throw new IllegalArgumentException("Product not found!");
        }

        int prevStock = p.getCurrentQuantity();
        int newStock = prevStock + quantityDelta;

        if (newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative! Available: " + prevStock);
        }

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            productDAO.setStock(productId, newStock, conn);

            InventoryTransaction tx = new InventoryTransaction(
                    0,
                    productId,
                    p.getPartName(),
                    DateUtil.today(),
                    txType != null ? txType : InventoryTransaction.Type.STOCK_ADJUSTMENT,
                    quantityDelta,
                    prevStock,
                    newStock,
                    "ADJ-" + System.currentTimeMillis() % 10000,
                    notes != null ? notes : "Manual stock adjustment"
            );
            transactionDAO.insertTransaction(tx, conn);

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<InventoryTransaction> getTransactionsByProduct(int productId) throws SQLException {
        return transactionDAO.getTransactionsByProduct(productId);
    }

    public List<InventoryTransaction> getRecentTransactions(int limit) throws SQLException {
        return transactionDAO.getRecentTransactions(limit);
    }

    public double getTotalStockValue() throws SQLException {
        return productDAO.getTotalStockValue();
    }

    public int getLowStockCount() throws SQLException {
        return productDAO.getLowStockCount();
    }

    public int getOutOfStockCount() throws SQLException {
        return productDAO.getOutOfStockCount();
    }
}
