package com.motorworkshop.service;

import com.motorworkshop.dao.PriceHistoryDAO;
import com.motorworkshop.dao.ProductDAO;
import com.motorworkshop.dao.ServiceDAO;
import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.PriceHistory;
import com.motorworkshop.model.Product;
import com.motorworkshop.model.ServiceItem;
import com.motorworkshop.util.DateUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic service for Pricing Strategies, Profit Margin calculation, Price History, and Service rates.
 */
public class PricingService {
    private final ProductDAO productDAO = new ProductDAO();
    private final PriceHistoryDAO priceHistoryDAO = new PriceHistoryDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    public List<Product> getAllPricingProducts() throws SQLException {
        return productDAO.getAllProducts(true);
    }

    public boolean updateProductPricing(int productId, double newSellingPrice, double newWorkshopPrice, String reason) throws SQLException {
        Product p = productDAO.getProductById(productId);
        if (p == null) throw new IllegalArgumentException("Product not found!");

        if (newSellingPrice < 0 || newWorkshopPrice < 0) {
            throw new IllegalArgumentException("Prices cannot be negative!");
        }

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            double oldSell = p.getSellingPrice();
            double oldBuy = p.getPurchasePrice();

            p.setSellingPrice(newSellingPrice);
            p.setWorkshopPrice(newWorkshopPrice);
            productDAO.updateProduct(p, conn);

            // Record price history
            PriceHistory hist = new PriceHistory(
                    0,
                    productId,
                    p.getPartName(),
                    oldBuy,
                    oldBuy,
                    oldSell,
                    newSellingPrice,
                    DateUtil.today(),
                    reason != null && !reason.trim().isEmpty() ? reason : "Manual price update"
            );
            priceHistoryDAO.insertPriceHistory(hist, conn);

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

    public List<PriceHistory> getPriceHistory(int productId) throws SQLException {
        return priceHistoryDAO.getHistoryByProduct(productId);
    }

    public List<PriceHistory> getRecentPriceHistories(int limit) throws SQLException {
        return priceHistoryDAO.getAllRecentHistory(limit);
    }

    // Workshop Services Management
    public List<ServiceItem> getAllServices(boolean activeOnly) throws SQLException {
        return serviceDAO.getAllServices(activeOnly);
    }

    public boolean addService(ServiceItem item) throws SQLException {
        if (item.getServiceName() == null || item.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service Name is required!");
        }
        if (item.getDefaultPrice() < 0) {
            throw new IllegalArgumentException("Service Price cannot be negative!");
        }
        return serviceDAO.insertService(item);
    }

    public boolean updateService(ServiceItem item) throws SQLException {
        if (item.getServiceName() == null || item.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service Name is required!");
        }
        return serviceDAO.updateService(item);
    }

    public boolean deleteService(int id) throws SQLException {
        return serviceDAO.deleteService(id);
    }
}
