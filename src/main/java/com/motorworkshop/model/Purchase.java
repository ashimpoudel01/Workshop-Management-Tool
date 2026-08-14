package com.motorworkshop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a purchase invoice from a supplier with multiple items.
 */
public class Purchase {
    private int purchaseId;
    private String invoiceNumber;
    private String date;
    private int supplierId;
    private String supplierName;
    private double totalAmount;
    private String paymentStatus; // PAID, UNPAID, PARTIAL
    private String notes;
    private List<PurchaseItem> items = new ArrayList<>();

    public Purchase() {}

    public Purchase(int purchaseId, String invoiceNumber, String date, int supplierId,
                    String supplierName, double totalAmount, String paymentStatus, String notes) {
        this.purchaseId = purchaseId;
        this.invoiceNumber = invoiceNumber;
        this.date = date;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.notes = notes;
    }

    public int getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
        recalculateTotal();
    }

    public void addItem(PurchaseItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        double sum = 0.0;
        if (items != null) {
            for (PurchaseItem item : items) {
                sum += item.getTotalPrice();
            }
        }
        this.totalAmount = sum;
    }
}
