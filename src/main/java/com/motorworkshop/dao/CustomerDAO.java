package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer records and vehicle profiles.
 */
public class CustomerDAO {

    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM sales s WHERE s.customer_id = c.customer_id) as total_visits, " +
                     "(SELECT COALESCE(SUM(s.total_amount), 0.0) FROM sales s WHERE s.customer_id = c.customer_id) as total_spent, " +
                     "(SELECT MAX(s.date) FROM sales s WHERE s.customer_id = c.customer_id) as last_visit " +
                     "FROM customers c " +
                     "ORDER BY c.name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        }
        return list;
    }

    public Customer getCustomerById(int id) throws SQLException {
        String sql = "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM sales s WHERE s.customer_id = c.customer_id) as total_visits, " +
                     "(SELECT COALESCE(SUM(s.total_amount), 0.0) FROM sales s WHERE s.customer_id = c.customer_id) as total_spent, " +
                     "(SELECT MAX(s.date) FROM sales s WHERE s.customer_id = c.customer_id) as last_visit " +
                     "FROM customers c " +
                     "WHERE c.customer_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapCustomer(rs);
                }
            }
        }
        return null;
    }

    public List<Customer> searchCustomers(String query) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM sales s WHERE s.customer_id = c.customer_id) as total_visits, " +
                     "(SELECT COALESCE(SUM(s.total_amount), 0.0) FROM sales s WHERE s.customer_id = c.customer_id) as total_spent, " +
                     "(SELECT MAX(s.date) FROM sales s WHERE s.customer_id = c.customer_id) as last_visit " +
                     "FROM customers c " +
                     "WHERE (c.name LIKE ? OR c.phone LIKE ? OR c.vehicle_number LIKE ? OR c.vehicle_model LIKE ?) " +
                     "ORDER BY c.name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + (query != null ? query.trim() : "") + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, pattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCustomer(rs));
                }
            }
        }
        return list;
    }

    public boolean insertCustomer(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (name, phone, address, vehicle_number, vehicle_brand, vehicle_model, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getPhone());
            pstmt.setString(3, c.getAddress());
            pstmt.setString(4, c.getVehicleNumber());
            pstmt.setString(5, c.getVehicleBrand());
            pstmt.setString(6, c.getVehicleModel());
            pstmt.setString(7, c.getNotes());
            pstmt.setString(8, c.getCreatedAt());

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        c.setCustomerId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateCustomer(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name = ?, phone = ?, address = ?, vehicle_number = ?, " +
                     "vehicle_brand = ?, vehicle_model = ?, notes = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getPhone());
            pstmt.setString(3, c.getAddress());
            pstmt.setString(4, c.getVehicleNumber());
            pstmt.setString(5, c.getVehicleBrand());
            pstmt.setString(6, c.getVehicleModel());
            pstmt.setString(7, c.getNotes());
            pstmt.setInt(8, c.getCustomerId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setVehicleNumber(rs.getString("vehicle_number"));
        c.setVehicleBrand(rs.getString("vehicle_brand"));
        c.setVehicleModel(rs.getString("vehicle_model"));
        c.setNotes(rs.getString("notes"));
        c.setCreatedAt(rs.getString("created_at"));
        c.setTotalVisits(rs.getInt("total_visits"));
        c.setTotalSpent(rs.getDouble("total_spent"));
        c.setLastVisitDate(rs.getString("last_visit"));
        return c;
    }
}
