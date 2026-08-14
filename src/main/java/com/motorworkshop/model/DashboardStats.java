package com.motorworkshop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates aggregated metrics for the main workshop dashboard.
 */
public class DashboardStats {
    private double todaySales;
    private double todayExpenses;
    private double todayPurchases;
    private double todayCogs;
    private double todayGrossProfit;
    private double todayNetProfit;
    private int todayInvoicesCount;
    
    private double currentInventoryValue;
    private int totalProductsCount;
    private int lowStockCount;
    private int outOfStockCount;

    private double monthlySales;
    private double monthlyExpenses;
    private double monthlyPurchases;
    private double monthlyGrossProfit;
    private double monthlyNetProfit;
    private double averageDailySales;

    private List<TopItemMetric> topSellingParts = new ArrayList<>();
    private List<TopItemMetric> topPerformingServices = new ArrayList<>();

    public DashboardStats() {}

    public double getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(double todaySales) {
        this.todaySales = todaySales;
    }

    public double getTodayExpenses() {
        return todayExpenses;
    }

    public void setTodayExpenses(double todayExpenses) {
        this.todayExpenses = todayExpenses;
    }

    public double getTodayPurchases() {
        return todayPurchases;
    }

    public void setTodayPurchases(double todayPurchases) {
        this.todayPurchases = todayPurchases;
    }

    public double getTodayCogs() {
        return todayCogs;
    }

    public void setTodayCogs(double todayCogs) {
        this.todayCogs = todayCogs;
    }

    public double getTodayGrossProfit() {
        return todayGrossProfit;
    }

    public void setTodayGrossProfit(double todayGrossProfit) {
        this.todayGrossProfit = todayGrossProfit;
    }

    public double getTodayNetProfit() {
        return todayNetProfit;
    }

    public void setTodayNetProfit(double todayNetProfit) {
        this.todayNetProfit = todayNetProfit;
    }

    public int getTodayInvoicesCount() {
        return todayInvoicesCount;
    }

    public void setTodayInvoicesCount(int todayInvoicesCount) {
        this.todayInvoicesCount = todayInvoicesCount;
    }

    public double getCurrentInventoryValue() {
        return currentInventoryValue;
    }

    public void setCurrentInventoryValue(double currentInventoryValue) {
        this.currentInventoryValue = currentInventoryValue;
    }

    public int getTotalProductsCount() {
        return totalProductsCount;
    }

    public void setTotalProductsCount(int totalProductsCount) {
        this.totalProductsCount = totalProductsCount;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(int outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }

    public double getMonthlySales() {
        return monthlySales;
    }

    public void setMonthlySales(double monthlySales) {
        this.monthlySales = monthlySales;
    }

    public double getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public void setMonthlyExpenses(double monthlyExpenses) {
        this.monthlyExpenses = monthlyExpenses;
    }

    public double getMonthlyPurchases() {
        return monthlyPurchases;
    }

    public void setMonthlyPurchases(double monthlyPurchases) {
        this.monthlyPurchases = monthlyPurchases;
    }

    public double getMonthlyGrossProfit() {
        return monthlyGrossProfit;
    }

    public void setMonthlyGrossProfit(double monthlyGrossProfit) {
        this.monthlyGrossProfit = monthlyGrossProfit;
    }

    public double getMonthlyNetProfit() {
        return monthlyNetProfit;
    }

    public void setMonthlyNetProfit(double monthlyNetProfit) {
        this.monthlyNetProfit = monthlyNetProfit;
    }

    public double getAverageDailySales() {
        return averageDailySales;
    }

    public void setAverageDailySales(double averageDailySales) {
        this.averageDailySales = averageDailySales;
    }

    public List<TopItemMetric> getTopSellingParts() {
        return topSellingParts;
    }

    public void setTopSellingParts(List<TopItemMetric> topSellingParts) {
        this.topSellingParts = topSellingParts;
    }

    public List<TopItemMetric> getTopPerformingServices() {
        return topPerformingServices;
    }

    public void setTopPerformingServices(List<TopItemMetric> topPerformingServices) {
        this.topPerformingServices = topPerformingServices;
    }

    public static class TopItemMetric {
        private String name;
        private int quantity;
        private double totalRevenue;

        public TopItemMetric(String name, int quantity, double totalRevenue) {
            this.name = name;
            this.quantity = quantity;
            this.totalRevenue = totalRevenue;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getTotalRevenue() {
            return totalRevenue;
        }
    }
}
