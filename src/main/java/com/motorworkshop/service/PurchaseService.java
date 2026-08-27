package com.motorworkshop.service;

import com.motorworkshop.dao.PurchaseDAO;
import com.motorworkshop.dao.SupplierDAO;
import com.motorworkshop.model.Purchase;
import com.motorworkshop.model.PurchaseItem;
import com.motorworkshop.model.Supplier;
import com.motorworkshop.util.DateUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic service for managing Purchases and Supplier relationships.
 */
public class PurchaseService {
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    public List<Purchase> getAllPurchases() throws SQLException {
        return purchaseDAO.getAllPurchases();
    }

    public Purchase getPurchaseById(int id) throws SQLException {
        return purchaseDAO.getPurchaseById(id);
    }

    public List<Purchase> searchPurchases(String query, String startDate, String endDate, Integer supplierId) throws SQLException {
        return purchaseDAO.searchPurchases(query, startDate, endDate, supplierId);
    }

    public boolean recordPurchase(Purchase purchase) throws SQLException {
        if (purchase.getInvoiceNumber() == null || purchase.getInvoiceNumber().trim().isEmpty()) {
            purchase.setInvoiceNumber("PO-" + System.currentTimeMillis() % 1000000);
        }
        if (purchase.getDate() == null || purchase.getDate().trim().isEmpty()) {
            purchase.setDate(DateUtil.today());
        }
        if (purchase.getItems() == null || purchase.getItems().isEmpty()) {
            throw new IllegalArgumentException("Purchase must contain at least one item!");
        }
        for (PurchaseItem item : purchase.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be greater than 0!");
            }
            if (item.getUnitPurchasePrice() < 0) {
                throw new IllegalArgumentException("Purchase price cannot be negative!");
            }
        }
        purchase.recalculateTotal();
        return purchaseDAO.createPurchase(purchase);
    }

    public boolean deletePurchase(int id) throws SQLException {
        return purchaseDAO.deletePurchase(id);
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.getAllSuppliers();
    }

    public List<Supplier> searchSuppliers(String query) throws SQLException {
        return supplierDAO.searchSuppliers(query);
    }

    public boolean addSupplier(Supplier s) throws SQLException {
        if (s.getName() == null || s.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name is required!");
        }
        return supplierDAO.insertSupplier(s);
    }

    public boolean updateSupplier(Supplier s) throws SQLException {
        return supplierDAO.updateSupplier(s);
    }

    public boolean deleteSupplier(int id) throws SQLException {
        return supplierDAO.deleteSupplier(id);
    }
}
