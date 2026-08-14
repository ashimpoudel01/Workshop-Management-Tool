package com.motorworkshop.model;

/**
 * Represents a customer profile and motorcycle details.
 */
public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private String address;
    private String vehicleNumber;
    private String vehicleBrand;
    private String vehicleModel;
    private String notes;
    private String createdAt;
    
    // Aggregated statistics for customer view
    private int totalVisits;
    private double totalSpent;
    private String lastVisitDate;

    public Customer() {}

    public Customer(int customerId, String name, String phone, String address,
                    String vehicleNumber, String vehicleBrand, String vehicleModel,
                    String notes, String createdAt) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.vehicleNumber = vehicleNumber;
        this.vehicleBrand = vehicleBrand;
        this.vehicleModel = vehicleModel;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Customer(String name, String phone, String address, String vehicleNumber, String vehicleBrand, String vehicleModel) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.vehicleNumber = vehicleNumber;
        this.vehicleBrand = vehicleBrand;
        this.vehicleModel = vehicleModel;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(int totalVisits) {
        this.totalVisits = totalVisits;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public String getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(String lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    @Override
    public String toString() {
        return name + " (" + (vehicleNumber != null && !vehicleNumber.isEmpty() ? vehicleNumber : phone) + ")";
    }
}
