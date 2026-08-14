package com.motorworkshop.model;

/**
 * Represents general workshop configuration parameters.
 */
public class WorkshopSetting {
    private String workshopName;
    private String address;
    private String phoneNumber;
    private String panVatNumber;
    private String invoicePrefix;
    private String defaultCurrency;
    private double defaultServiceCharge;
    private int lowStockThreshold;
    private boolean allowNegativeStock;

    public WorkshopSetting() {
        this.workshopName = "Shree Krishna Motorcycle Workshop";
        this.address = "Kathmandu, Nepal";
        this.phoneNumber = "+977-9800000000";
        this.panVatNumber = "600123456";
        this.invoicePrefix = "INV-";
        this.defaultCurrency = "Rs.";
        this.defaultServiceCharge = 250.0;
        this.lowStockThreshold = 5;
        this.allowNegativeStock = false;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPanVatNumber() {
        return panVatNumber;
    }

    public void setPanVatNumber(String panVatNumber) {
        this.panVatNumber = panVatNumber;
    }

    public String getInvoicePrefix() {
        return invoicePrefix;
    }

    public void setInvoicePrefix(String invoicePrefix) {
        this.invoicePrefix = invoicePrefix;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public double getDefaultServiceCharge() {
        return defaultServiceCharge;
    }

    public void setDefaultServiceCharge(double defaultServiceCharge) {
        this.defaultServiceCharge = defaultServiceCharge;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isAllowNegativeStock() {
        return allowNegativeStock;
    }

    public void setAllowNegativeStock(boolean allowNegativeStock) {
        this.allowNegativeStock = allowNegativeStock;
    }
}
