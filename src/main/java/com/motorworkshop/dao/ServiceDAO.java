package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.ServiceItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Predefined Workshop Services.
 */
public class ServiceDAO {

    public List<ServiceItem> getAllServices(boolean activeOnly) throws SQLException {
        List<ServiceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM services " +
                     (activeOnly ? "WHERE is_active = 1 " : "") +
                     "ORDER BY service_name ASC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ServiceItem(
                        rs.getInt("service_id"),
                        rs.getString("service_name"),
                        rs.getDouble("default_price"),
                        rs.getInt("estimated_duration_minutes"),
                        rs.getString("description"),
                        rs.getInt("is_active") == 1
                ));
            }
        }
        return list;
    }

    public ServiceItem getServiceById(int id) throws SQLException {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ServiceItem(
                            rs.getInt("service_id"),
                            rs.getString("service_name"),
                            rs.getDouble("default_price"),
                            rs.getInt("estimated_duration_minutes"),
                            rs.getString("description"),
                            rs.getInt("is_active") == 1
                    );
                }
            }
        }
        return null;
    }

    public boolean insertService(ServiceItem s) throws SQLException {
        String sql = "INSERT INTO services (service_name, default_price, estimated_duration_minutes, description, is_active) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, s.getServiceName());
            pstmt.setDouble(2, s.getDefaultPrice());
            pstmt.setInt(3, s.getEstimatedDurationMinutes());
            pstmt.setString(4, s.getDescription());
            pstmt.setInt(5, s.isActive() ? 1 : 0);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        s.setServiceId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateService(ServiceItem s) throws SQLException {
        String sql = "UPDATE services SET service_name = ?, default_price = ?, estimated_duration_minutes = ?, " +
                     "description = ?, is_active = ? WHERE service_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getServiceName());
            pstmt.setDouble(2, s.getDefaultPrice());
            pstmt.setInt(3, s.getEstimatedDurationMinutes());
            pstmt.setString(4, s.getDescription());
            pstmt.setInt(5, s.isActive() ? 1 : 0);
            pstmt.setInt(6, s.getServiceId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteService(int id) throws SQLException {
        String sql = "UPDATE services SET is_active = 0 WHERE service_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }
}
