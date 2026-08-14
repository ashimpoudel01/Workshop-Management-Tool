package com.motorworkshop.model;

/**
 * Represents an individual line item in a sales invoice (part sold or service performed).
 */
public class SaleItem {
    public enum ItemType {
        PART,
        SERVICE
    }

    private int id;
    private int saleId;
    private ItemType itemType;
    private int itemId;          // product_id if PART, service_id if SERVICE
    private String itemName;
    private int quantity;
    private double unitPrice;    // Selling price
    private double unitCost;     // Purchase cost for parts (0 for service)
    private double totalPrice;   // quantity * unitPrice
    private double totalCost;    // quantity * unitCost (COGS)

    public SaleItem() {}

    public SaleItem(int id, int saleId, ItemType itemType, int itemId, String itemName,
                    int quantity, double unitPrice, double unitCost, double totalPrice, double totalCost) {
        this.id = id;
        this.saleId = saleId;
        this.itemType = itemType;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
        this.totalPrice = totalPrice;
        this.totalCost = totalCost;
    }

    public SaleItem(ItemType itemType, int itemId, String itemName, int quantity, double unitPrice, double unitCost) {
        this.itemType = itemType;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.unitCost = unitCost;
        this.totalPrice = quantity * unitPrice;
        this.totalCost = quantity * unitCost;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.unitPrice;
        this.totalCost = this.quantity * this.unitCost;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
        this.totalCost = this.quantity * this.unitCost;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}
