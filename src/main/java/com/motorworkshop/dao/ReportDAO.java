package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for comprehensive Financial, Inventory, Sales, and Purchase Reports.
 */
public class ReportDAO {

    public ReportData generateReport(String startDate, String endDate, String reportType) throws SQLException {
        ReportData data = new ReportData();
        data.setStartDate(startDate != null ? startDate.trim() : "");
        data.setEndDate(endDate != null ? endDate.trim() : "");
        data.setReportType(reportType);

        String start = data.getStartDate();
        String end = data.getEndDate();

        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Sales Totals
            StringBuilder sqlSales = new StringBuilder(
                    "SELECT " +
                    "COALESCE(SUM(total_amount), 0.0) as total_sales, " +
                    "COALESCE(SUM(parts_total), 0.0) as parts_rev, " +
                    "COALESCE(SUM(service_charge), 0.0) as service_rev, " +
                    "COALESCE(SUM(discount), 0.0) as total_disc, " +
                    "COALESCE(SUM(total_cogs), 0.0) as total_cogs, " +
                    "COALESCE(SUM(gross_profit), 0.0) as gross_profit, " +
                    "COUNT(*) as total_invoices " +
                    "FROM sales WHERE 1=1 "
            );
            List<Object> salesParams = new ArrayList<>();
            if (!start.isEmpty()) {
                sqlSales.append("AND substr(date, 1, 10) >= ? ");
                salesParams.add(start);
            }
            if (!end.isEmpty()) {
                sqlSales.append("AND substr(date, 1, 10) <= ? ");
                salesParams.add(end);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlSales.toString())) {
                for (int i = 0; i < salesParams.size(); i++) {
                    pstmt.setObject(i + 1, salesParams.get(i));
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        data.setTotalSales(rs.getDouble("total_sales"));
                        data.setPartsRevenue(rs.getDouble("parts_rev"));
                        data.setServiceRevenue(rs.getDouble("service_rev"));
                        data.setTotalDiscount(rs.getDouble("total_disc"));
                        data.setTotalCogs(rs.getDouble("total_cogs"));
                        data.setGrossProfit(rs.getDouble("gross_profit"));
                        data.setTotalInvoices(rs.getInt("total_invoices"));
                    }
                }
            }

            // 2. Expenses Total
            StringBuilder sqlExp = new StringBuilder("SELECT COALESCE(SUM(amount), 0.0) as total_exp FROM expenses WHERE 1=1 ");
            List<Object> expParams = new ArrayList<>();
            if (!start.isEmpty()) {
                sqlExp.append("AND substr(date, 1, 10) >= ? ");
                expParams.add(start);
            }
            if (!end.isEmpty()) {
                sqlExp.append("AND substr(date, 1, 10) <= ? ");
                expParams.add(end);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlExp.toString())) {
                for (int i = 0; i < expParams.size(); i++) {
                    pstmt.setObject(i + 1, expParams.get(i));
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        data.setTotalExpenses(rs.getDouble("total_exp"));
                    }
                }
            }

            // 3. Purchases Total
            StringBuilder sqlPurch = new StringBuilder("SELECT COALESCE(SUM(total_amount), 0.0) as total_purch FROM purchases WHERE 1=1 ");
            List<Object> purchParams = new ArrayList<>();
            if (!start.isEmpty()) {
                sqlPurch.append("AND substr(date, 1, 10) >= ? ");
                purchParams.add(start);
            }
            if (!end.isEmpty()) {
                sqlPurch.append("AND substr(date, 1, 10) <= ? ");
                purchParams.add(end);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlPurch.toString())) {
                for (int i = 0; i < purchParams.size(); i++) {
                    pstmt.setObject(i + 1, purchParams.get(i));
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        data.setTotalPurchases(rs.getDouble("total_purch"));
                    }
                }
            }

            // Calculate Net Profit: Gross Profit - Expenses
            data.setNetProfit(data.getGrossProfit() - data.getTotalExpenses());

            // 4. Sales items list
            SaleDAO saleDAO = new SaleDAO();
            data.setSalesList(saleDAO.searchSales(null, start, end, null));

            // 5. Purchases list
            PurchaseDAO purchaseDAO = new PurchaseDAO();
            data.setPurchaseList(purchaseDAO.searchPurchases(null, start, end, null));

            // 6. Expenses list
            ExpenseDAO expenseDAO = new ExpenseDAO();
            data.setExpenseList(expenseDAO.searchExpenses(null, null, start, end));

            // 7. Top Selling Parts
            data.setTopParts(saleDAO.getTopSellingParts(start, end, 10));

            // 8. Top Performing Services
            data.setTopServices(saleDAO.getTopPerformingServices(start, end, 10));

            // 9. Inventory List
            ProductDAO productDAO = new ProductDAO();
            data.setInventoryList(productDAO.getAllProducts(true));
        }

        return data;
    }
}
