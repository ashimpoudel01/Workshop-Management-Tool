package com.motorworkshop.service;

import com.motorworkshop.dao.*;
import com.motorworkshop.model.*;
import com.motorworkshop.util.DateUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic service for Sales, Invoices, COGS, and Dashboard metric calculations.
 */
public class SaleService {
    private final SaleDAO saleDAO = new SaleDAO();
    private final SettingDAO settingDAO = new SettingDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public List<Sale> getAllSales() throws SQLException {
        return saleDAO.getAllSales();
    }

    public Sale getSaleById(int id) throws SQLException {
        return saleDAO.getSaleById(id);
    }

    public List<Sale> searchSales(String query, String startDate, String endDate, String paymentMethod) throws SQLException {
        return saleDAO.searchSales(query, startDate, endDate, paymentMethod);
    }

    public String generateNextInvoiceNumber() {
        WorkshopSetting settings = settingDAO.getSettings();
        return saleDAO.generateNextInvoiceNumber(settings.getInvoicePrefix());
    }

    public boolean createSale(Sale sale) throws SQLException {
        if (sale.getCustomerName() == null || sale.getCustomerName().trim().isEmpty()) {
            sale.setCustomerName("Walk-in Customer");
        }
        if (sale.getDate() == null || sale.getDate().trim().isEmpty()) {
            sale.setDate(DateUtil.today());
        }
        if (sale.getInvoiceNumber() == null || sale.getInvoiceNumber().trim().isEmpty()) {
            sale.setInvoiceNumber(generateNextInvoiceNumber());
        }
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new IllegalArgumentException("Invoice must have at least one part or service item!");
        }

        WorkshopSetting settings = settingDAO.getSettings();
        return saleDAO.createSale(sale, settings.isAllowNegativeStock());
    }

    public boolean deleteSale(int id) throws SQLException {
        return saleDAO.deleteSale(id);
    }

    /**
     * Aggregates all dashboard numbers ensuring exact distinction between
     * Sales Revenue, COGS, Gross Profit (Revenue - COGS), Operating Expenses, and Net Profit (Gross Profit - Expenses).
     */
    public DashboardStats getDashboardMetrics() throws SQLException {
        DashboardStats stats = new DashboardStats();
        String today = DateUtil.today();
        String startOfMonth = DateUtil.startOfMonth();
        String endOfMonth = DateUtil.endOfMonth();

        // 1. Today's figures
        double todaySales = saleDAO.getTotalSalesInPeriod(today, today);
        double todayCogs = saleDAO.getTotalCogsInPeriod(today, today);
        double todayGross = saleDAO.getTotalGrossProfitInPeriod(today, today);
        double todayExp = expenseDAO.getTotalExpensesInPeriod(today, today);
        double todayPurch = purchaseDAO.getTotalPurchasesInPeriod(today, today);
        int todayInvoices = saleDAO.getInvoiceCountInPeriod(today, today);

        stats.setTodaySales(todaySales);
        stats.setTodayCogs(todayCogs);
        stats.setTodayGrossProfit(todayGross);
        stats.setTodayExpenses(todayExp);
        stats.setTodayPurchases(todayPurch);
        stats.setTodayNetProfit(todayGross - todayExp);
        stats.setTodayInvoicesCount(todayInvoices);

        // 2. Inventory figures
        stats.setCurrentInventoryValue(productDAO.getTotalStockValue());
        stats.setTotalProductsCount(productDAO.getTotalProductsCount());
        stats.setLowStockCount(productDAO.getLowStockCount());
        stats.setOutOfStockCount(productDAO.getOutOfStockCount());

        // 3. Month to Date figures
        double monthSales = saleDAO.getTotalSalesInPeriod(startOfMonth, endOfMonth);
        double monthGross = saleDAO.getTotalGrossProfitInPeriod(startOfMonth, endOfMonth);
        double monthExp = expenseDAO.getTotalExpensesInPeriod(startOfMonth, endOfMonth);
        double monthPurch = purchaseDAO.getTotalPurchasesInPeriod(startOfMonth, endOfMonth);

        stats.setMonthlySales(monthSales);
        stats.setMonthlyGrossProfit(monthGross);
        stats.setMonthlyExpenses(monthExp);
        stats.setMonthlyPurchases(monthPurch);
        stats.setMonthlyNetProfit(monthGross - monthExp);

        // Average daily sales this month
        int dayOfMonth = java.time.LocalDate.now().getDayOfMonth();
        stats.setAverageDailySales(dayOfMonth > 0 ? (monthSales / dayOfMonth) : monthSales);

        // Top 5 parts & services for the current month
        stats.setTopSellingParts(saleDAO.getTopSellingParts(startOfMonth, endOfMonth, 5));
        stats.setTopPerformingServices(saleDAO.getTopPerformingServices(startOfMonth, endOfMonth, 5));

        return stats;
    }
}
