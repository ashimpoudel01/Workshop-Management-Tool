package com.motorworkshop.model;

/**
 * Represents a predefined workshop service (e.g. General Servicing, Oil Change, Brake Service).
 */
public class ServiceItem {
    private int serviceId;
    private String serviceName;
    private double defaultPrice;
    private int estimatedDurationMinutes;
    private String description;
    private boolean active;

    public ServiceItem() {
        this.active = true;
    }

    public ServiceItem(int serviceId, String serviceName, double defaultPrice,
                       int estimatedDurationMinutes, String description, boolean active) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.defaultPrice = defaultPrice;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.description = description;
        this.active = active;
    }

    public ServiceItem(String serviceName, double defaultPrice, int estimatedDurationMinutes, String description) {
        this.serviceName = serviceName;
        this.defaultPrice = defaultPrice;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.description = description;
        this.active = true;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(double defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public int getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return serviceName + " - Rs. " + defaultPrice;
    }
}
