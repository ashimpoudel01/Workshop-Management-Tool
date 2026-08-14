package com.motorworkshop.model;

/**
 * Represents an individual line item in a supplier purchase order.
 */
public class PurchaseItem {
    private int id;
    private int purchaseId;
    private int productId;
    private String productName;
    private String partNumber;
    private int quantity;
    private double unitPurchasePrice;
    private double totalPrice;

    public PurchaseItem() {}

    public PurchaseItem(int id, int purchaseId, int productId, String productName,
                        String partNumber, int quantity, double unitPurchasePrice, double totalPrice) {
        this.id = id;
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.productName = productName;
        this.partNumber = partNumber;
        this.quantity = quantity;
        this.unitPurchasePrice = unitPurchasePrice;
        this.totalPrice = totalPrice;
    }

    public PurchaseItem(int productId, String productName, String partNumber,
                        int quantity, double unitPurchasePrice) {
        this.productId = productId;
        this.productName = productName;
        this.partNumber = partNumber;
        this.quantity = quantity;
        this.unitPurchasePrice = unitPurchasePrice;
        this.totalPrice = quantity * unitPurchasePrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
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

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.unitPurchasePrice;
    }

    public double getUnitPurchasePrice() {
        return unitPurchasePrice;
    }

    public void setUnitPurchasePrice(double unitPurchasePrice) {
        this.unitPurchasePrice = unitPurchasePrice;
        this.totalPrice = this.quantity * this.unitPurchasePrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
