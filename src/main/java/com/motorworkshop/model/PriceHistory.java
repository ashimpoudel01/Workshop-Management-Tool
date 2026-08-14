package com.motorworkshop.model;

/**
 * Tracks historical price changes for products.
 */
public class PriceHistory {
    private int historyId;
    private int productId;
    private String productName;
    private double oldPurchasePrice;
    private double newPurchasePrice;
    private double oldSellingPrice;
    private double newSellingPrice;
    private String changeDate;
    private String reason;

    public PriceHistory() {}

    public PriceHistory(int historyId, int productId, String productName, double oldPurchasePrice,
                        double newPurchasePrice, double oldSellingPrice, double newSellingPrice,
                        String changeDate, String reason) {
        this.historyId = historyId;
        this.productId = productId;
        this.productName = productName;
        this.oldPurchasePrice = oldPurchasePrice;
        this.newPurchasePrice = newPurchasePrice;
        this.oldSellingPrice = oldSellingPrice;
        this.newSellingPrice = newSellingPrice;
        this.changeDate = changeDate;
        this.reason = reason;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getOldPurchasePrice() {
        return oldPurchasePrice;
    }

    public void setOldPurchasePrice(double oldPurchasePrice) {
        this.oldPurchasePrice = oldPurchasePrice;
    }

    public double getNewPurchasePrice() {
        return newPurchasePrice;
    }

    public void setNewPurchasePrice(double newPurchasePrice) {
        this.newPurchasePrice = newPurchasePrice;
    }

    public double getOldSellingPrice() {
        return oldSellingPrice;
    }

    public void setOldSellingPrice(double oldSellingPrice) {
        this.oldSellingPrice = oldSellingPrice;
    }

    public double getNewSellingPrice() {
        return newSellingPrice;
    }

    public void setNewSellingPrice(double newSellingPrice) {
        this.newSellingPrice = newSellingPrice;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
