package com.motorworkshop.model;

/**
 * Represents an audit record of inventory movements.
 */
public class InventoryTransaction {
    public enum Type {
        PURCHASE,
        SALE,
        SERVICE_USAGE,
        STOCK_ADJUSTMENT,
        RETURN,
        DAMAGE
    }

    private int transactionId;
    private int productId;
    private String productName;
    private String date;
    private Type transactionType;
    private int quantity;          // Positive for stock increase, negative or positive depending on context (here positive quantity affected)
    private int previousStock;
    private int newStock;
    private String referenceId;    // Invoice / PO / Adjustment Ref
    private String notes;

    public InventoryTransaction() {}

    public InventoryTransaction(int transactionId, int productId, String productName, String date,
                                Type transactionType, int quantity, int previousStock, int newStock,
                                String referenceId, String notes) {
        this.transactionId = transactionId;
        this.productId = productId;
        this.productName = productName;
        this.date = date;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.referenceId = referenceId;
        this.notes = notes;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Type getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Type transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(int previousStock) {
        this.previousStock = previousStock;
    }

    public int getNewStock() {
        return newStock;
    }

    public void setNewStock(int newStock) {
        this.newStock = newStock;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
