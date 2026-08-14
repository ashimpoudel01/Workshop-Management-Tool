package com.motorworkshop.model;

/**
 * Represents a spare part or consumable inventory item.
 */
public class Product {
    private int itemId;
    private String partName;
    private String partNumber;
    private int categoryId;
    private String categoryName;
    private String brand;
    private int supplierId;
    private String supplierName;
    private double purchasePrice;
    private double sellingPrice;      // Retail selling price
    private double workshopPrice;     // Workshop / service discounted price
    private int currentQuantity;
    private int minStockLevel;
    private String unit;              // Pcs, Litre, Set, Box, Pair, etc.
    private String dateAdded;
    private String lastPurchaseDate;
    private boolean active;

    public Product() {
        this.unit = "Pcs";
        this.minStockLevel = 5;
        this.active = true;
    }

    public Product(int itemId, String partName, String partNumber, int categoryId, String brand,
                   int supplierId, double purchasePrice, double sellingPrice, double workshopPrice,
                   int currentQuantity, int minStockLevel, String unit, String dateAdded,
                   String lastPurchaseDate, boolean active) {
        this.itemId = itemId;
        this.partName = partName;
        this.partNumber = partNumber;
        this.categoryId = categoryId;
        this.brand = brand;
        this.supplierId = supplierId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.workshopPrice = workshopPrice > 0 ? workshopPrice : sellingPrice;
        this.currentQuantity = currentQuantity;
        this.minStockLevel = minStockLevel;
        this.unit = unit != null ? unit : "Pcs";
        this.dateAdded = dateAdded;
        this.lastPurchaseDate = lastPurchaseDate;
        this.active = active;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getWorkshopPrice() {
        return workshopPrice > 0 ? workshopPrice : sellingPrice;
    }

    public void setWorkshopPrice(double workshopPrice) {
        this.workshopPrice = workshopPrice;
    }

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(String lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getProfitPerUnit() {
        return sellingPrice - purchasePrice;
    }

    public double getProfitMarginPercent() {
        if (purchasePrice <= 0) return 0.0;
        return ((sellingPrice - purchasePrice) / purchasePrice) * 100.0;
    }

    public double getTotalStockValue() {
        return currentQuantity * purchasePrice;
    }

    public boolean isLowStock() {
        return currentQuantity <= minStockLevel && currentQuantity > 0;
    }

    public boolean isOutOfStock() {
        return currentQuantity <= 0;
    }

    @Override
    public String toString() {
        return partName + (brand != null && !brand.isEmpty() ? " (" + brand + ")" : "") + " - Rs. " + sellingPrice;
    }
}
