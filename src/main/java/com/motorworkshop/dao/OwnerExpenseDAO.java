package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OwnerExpenseDAO {
    public List<Expense> searchExpenses(String query, String category, String startDate, String endDate) throws SQLException {
        List<Expense> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM owner_expenses WHERE 1=1 ");
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
        sql.append("ORDER BY date DESC, owner_expense_id DESC");

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
        String sql = "INSERT INTO owner_expenses (date, category, description, amount, payment_method, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(pstmt, expense);
            if (pstmt.executeUpdate() > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) expense.setExpenseId(keys.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateExpense(Expense expense) throws SQLException {
        String sql = "UPDATE owner_expenses SET date = ?, category = ?, description = ?, amount = ?, payment_method = ?, notes = ? WHERE owner_expense_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            bind(pstmt, expense);
            pstmt.setInt(7, expense.getExpenseId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteExpense(int id) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM owner_expenses WHERE owner_expense_id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement pstmt, Expense expense) throws SQLException {
        pstmt.setString(1, expense.getDate());
        pstmt.setString(2, expense.getCategory());
        pstmt.setString(3, expense.getDescription());
        pstmt.setDouble(4, expense.getAmount());
        pstmt.setString(5, expense.getPaymentMethod());
        pstmt.setString(6, expense.getNotes());
    }

    private Expense mapExpense(ResultSet rs) throws SQLException {
        return new Expense(rs.getInt("owner_expense_id"), rs.getString("date"), rs.getString("category"),
                rs.getString("description"), rs.getDouble("amount"), rs.getString("payment_method"), rs.getString("notes"));
    }
}