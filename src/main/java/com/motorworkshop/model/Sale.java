package com.motorworkshop.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a completed or ongoing sales invoice for parts and services.
 */
public class Sale {
    private int saleId;
    private String invoiceNumber;
    private String date;
    private int customerId;
    private String customerName;
    private String customerPhone;
    private String vehicleType;       // Motorcycle, Scooter
    private String vehicleBrand;      // Honda, Yamaha, Bajaj, etc.
    private String vehicleModel;      // Pulsar 150, Dio, etc.
    private String vehicleRegNo;      // License plate
    private double serviceCharge;
    private double partsTotal;
    private double discount;
    private double subtotal;          // serviceCharge + partsTotal
    private double totalAmount;       // subtotal - discount
    private double totalCogs;         // Cost of goods sold for all parts
    private double grossProfit;       // totalAmount - totalCogs
    private String paymentMethod;     // Cash, Bank, eSewa, Khalti, Other
    private String notes;
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
        this.paymentMethod = "Cash";
    }

    public Sale(int saleId, String invoiceNumber, String date, int customerId, String customerName,
                String customerPhone, String vehicleType, String vehicleBrand, String vehicleModel,
                String vehicleRegNo, double serviceCharge, double partsTotal, double discount,
                double subtotal, double totalAmount, double totalCogs, double grossProfit,
                String paymentMethod, String notes) {
        this.saleId = saleId;
        this.invoiceNumber = invoiceNumber;
        this.date = date;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.vehicleType = vehicleType;
        this.vehicleBrand = vehicleBrand;
        this.vehicleModel = vehicleModel;
        this.vehicleRegNo = vehicleRegNo;
        this.serviceCharge = serviceCharge;
        this.partsTotal = partsTotal;
        this.discount = discount;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
        this.totalCogs = totalCogs;
        this.grossProfit = grossProfit;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
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

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleRegNo() {
        return vehicleRegNo;
    }

    public void setVehicleRegNo(String vehicleRegNo) {
        this.vehicleRegNo = vehicleRegNo;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public double getPartsTotal() {
        return partsTotal;
    }

    public void setPartsTotal(double partsTotal) {
        this.partsTotal = partsTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        recalculateTotals();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
        recalculateTotals();
    }

    public void addItem(SaleItem item) {
        this.items.add(item);
        recalculateTotals();
    }

    public void recalculateTotals() {
        double partsSum = 0.0;
        double serviceSum = 0.0;
        double cogsSum = 0.0;

        if (items != null) {
            for (SaleItem item : items) {
                if (item.getItemType() == SaleItem.ItemType.PART) {
                    partsSum += item.getTotalPrice();
                    cogsSum += item.getTotalCost();
                } else if (item.getItemType() == SaleItem.ItemType.SERVICE) {
                    serviceSum += item.getTotalPrice();
                }
            }
        }

        this.partsTotal = partsSum;
        this.serviceCharge = serviceSum;
        this.subtotal = partsSum + serviceSum;
        this.totalAmount = Math.max(0.0, this.subtotal - this.discount);
        this.totalCogs = cogsSum;
        this.grossProfit = this.totalAmount - this.totalCogs;
    }
}
