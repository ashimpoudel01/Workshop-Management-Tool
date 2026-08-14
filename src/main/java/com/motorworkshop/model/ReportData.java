package com.motorworkshop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates aggregated reporting data for filtered ranges.
 */
public class ReportData {
    private String startDate;
    private String endDate;
    private String reportType;

    // Financial Totals
    private double totalSales;
    private double partsRevenue;
    private double serviceRevenue;
    private double totalDiscount;
    private double totalCogs;
    private double grossProfit;
    private double totalExpenses;
    private double netProfit;
    private double totalPurchases;
    private int totalInvoices;

    // Line items or breakdowns
    private List<Sale> salesList = new ArrayList<>();
    private List<Purchase> purchaseList = new ArrayList<>();
    private List<Expense> expenseList = new ArrayList<>();
    private List<Product> inventoryList = new ArrayList<>();
    private List<DashboardStats.TopItemMetric> topParts = new ArrayList<>();
    private List<DashboardStats.TopItemMetric> topServices = new ArrayList<>();

    public ReportData() {}

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getPartsRevenue() {
        return partsRevenue;
    }

    public void setPartsRevenue(double partsRevenue) {
        this.partsRevenue = partsRevenue;
    }

    public double getServiceRevenue() {
        return serviceRevenue;
    }

    public void setServiceRevenue(double serviceRevenue) {
        this.serviceRevenue = serviceRevenue;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getTotalCogs() {
        return totalCogs;
    }

    public void setTotalCogs(double totalCogs) {
        this.totalCogs = totalCogs;
    }

    public double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(double netProfit) {
        this.netProfit = netProfit;
    }

    public double getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public int getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(int totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public List<Sale> getSalesList() {
        return salesList;
    }

    public void setSalesList(List<Sale> salesList) {
        this.salesList = salesList;
    }

    public List<Purchase> getPurchaseList() {
        return purchaseList;
    }

    public void setPurchaseList(List<Purchase> purchaseList) {
        this.purchaseList = purchaseList;
    }

    public List<Expense> getExpenseList() {
        return expenseList;
    }

    public void setExpenseList(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    public List<Product> getInventoryList() {
        return inventoryList;
    }

    public void setInventoryList(List<Product> inventoryList) {
        this.inventoryList = inventoryList;
    }

    public List<DashboardStats.TopItemMetric> getTopParts() {
        return topParts;
    }

    public void setTopParts(List<DashboardStats.TopItemMetric> topParts) {
        this.topParts = topParts;
    }

    public List<DashboardStats.TopItemMetric> getTopServices() {
        return topServices;
    }

    public void setTopServices(List<DashboardStats.TopItemMetric> topServices) {
        this.topServices = topServices;
    }
}
