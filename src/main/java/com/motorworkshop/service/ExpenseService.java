package com.motorworkshop.service;

import com.motorworkshop.dao.ExpenseDAO;
import com.motorworkshop.model.Expense;
import com.motorworkshop.util.DateUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Business logic service for managing Workshop Operating Expenses.
 */
public class ExpenseService {
    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    public static final List<String> EXPENSE_CATEGORIES = Arrays.asList(
            "Rent",
            "Salary",
            "Electricity",
            "Water",
            "Internet",
            "Transportation",
            "Tools",
            "Maintenance",
            "Marketing",
            "Food/Tea",
            "Miscellaneous"
    );

    public List<Expense> getAllExpenses() throws SQLException {
        return expenseDAO.getAllExpenses();
    }

    public Expense getExpenseById(int id) throws SQLException {
        return expenseDAO.getExpenseById(id);
    }

    public List<Expense> searchExpenses(String query, String category, String startDate, String endDate) throws SQLException {
        return expenseDAO.searchExpenses(query, category, startDate, endDate);
    }

    public boolean addExpense(Expense expense) throws SQLException {
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than 0!");
        }
        if (expense.getDate() == null || expense.getDate().trim().isEmpty()) {
            expense.setDate(DateUtil.today());
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            expense.setCategory("Miscellaneous");
        }
        return expenseDAO.insertExpense(expense);
    }

    public boolean updateExpense(Expense expense) throws SQLException {
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than 0!");
        }
        return expenseDAO.updateExpense(expense);
    }

    public boolean deleteExpense(int id) throws SQLException {
        return expenseDAO.deleteExpense(id);
    }
}
