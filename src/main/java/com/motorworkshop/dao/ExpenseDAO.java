package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Operating Expenses.
 */
public class ExpenseDAO {

    public List<Expense> getAllExpenses() throws SQLException {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY date DESC, expense_id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapExpense(rs));
            }
        }
        return list;
    }

    public Expense getExpenseById(int id) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE expense_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapExpense(rs);
                }
            }
        }
        return null;
    }

    public List<Expense> searchExpenses(String query, String category, String startDate, String endDate) throws SQLException {
        List<Expense> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM expenses WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String pattern = "%" + query.trim() + "%";
            sql.append("AND (description LIKE ? OR notes LIKE ?) ");
            params.add(pattern);
            params.add(pattern);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All Categories")) {
            sql.append("AND category = ? ");
            params.add(category.trim());
        }

        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append("AND substr(date, 1, 10) >= ? ");
            params.add(startDate.trim());
        }

        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append("AND substr(date, 1, 10) <= ? ");
            params.add(endDate.trim());
        }

        sql.append("ORDER BY date DESC, expense_id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapExpense(rs));
                }
            }
        }
        return list;
    }

    public boolean insertExpense(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (date, category, description, amount, payment_method, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, expense.getDate());
            pstmt.setString(2, expense.getCategory());
            pstmt.setString(3, expense.getDescription());
            pstmt.setDouble(4, expense.getAmount());
            pstmt.setString(5, expense.getPaymentMethod());
            pstmt.setString(6, expense.getNotes());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        expense.setExpenseId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateExpense(Expense expense) throws SQLException {
        String sql = "UPDATE expenses SET date = ?, category = ?, description = ?, amount = ?, " +
                     "payment_method = ?, notes = ? WHERE expense_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, expense.getDate());
            pstmt.setString(2, expense.getCategory());
            pstmt.setString(3, expense.getDescription());
            pstmt.setDouble(4, expense.getAmount());
            pstmt.setString(5, expense.getPaymentMethod());
            pstmt.setString(6, expense.getNotes());
            pstmt.setInt(7, expense.getExpenseId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteExpense(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE expense_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public double getTotalExpensesInPeriod(String startDate, String endDate) throws SQLException {
        String sql = "SELECT SUM(amount) FROM expenses WHERE substr(date, 1, 10) >= ? AND substr(date, 1, 10) <= ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    private Expense mapExpense(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt("expense_id"),
                rs.getString("date"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("amount"),
                rs.getString("payment_method"),
                rs.getString("notes")
        );
    }
}
