package com.motorworkshop.service;

import com.motorworkshop.dao.OwnerExpenseDAO;
import com.motorworkshop.model.Expense;
import com.motorworkshop.util.DateUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class OwnerExpenseService {
    private final OwnerExpenseDAO expenseDAO = new OwnerExpenseDAO();
    public static final List<String> CATEGORIES = Arrays.asList(
            "Personal", "Family", "Education", "Health", "Travel", "Loan", "Investment", "Other"
    );

    public List<Expense> searchExpenses(String query, String category, String startDate, String endDate) throws SQLException {
        return expenseDAO.searchExpenses(query, category, startDate, endDate);
    }

    public boolean addExpense(Expense expense) throws SQLException {
        validate(expense);
        if (expense.getDate() == null || expense.getDate().trim().isEmpty()) expense.setDate(DateUtil.today());
        return expenseDAO.insertExpense(expense);
    }

    public boolean updateExpense(Expense expense) throws SQLException {
        validate(expense);
        return expenseDAO.updateExpense(expense);
    }

    public boolean deleteExpense(int id) throws SQLException {
        return expenseDAO.deleteExpense(id);
    }

    private void validate(Expense expense) {
        if (expense.getAmount() <= 0) throw new IllegalArgumentException("Owner expense amount must be greater than 0!");
        if (expense.getDescription() == null || expense.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required!");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) expense.setCategory("Other");
    }
}