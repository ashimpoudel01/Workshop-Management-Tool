package com.motorworkshop.model;

/**
 * Represents a daily operating expense.
 */
public class Expense {
    private int expenseId;
    private String date;
    private String category;
    private String description;
    private double amount;
    private String paymentMethod;
    private String notes;

    public Expense() {
        this.paymentMethod = "Cash";
        this.category = "Miscellaneous";
    }

    public Expense(int expenseId, String date, String category, String description,
                   double amount, String paymentMethod, String notes) {
        this.expenseId = expenseId;
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public Expense(String date, String category, String description, double amount,
                   String paymentMethod, String notes) {
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
}
