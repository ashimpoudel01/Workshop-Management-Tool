package com.motorworkshop.dao;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.WorkshopSetting;
import com.motorworkshop.util.FormatUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for workshop configuration settings.
 */
public class SettingDAO {

    public WorkshopSetting getSettings() {
        WorkshopSetting settings = new WorkshopSetting();
        Map<String, String> map = getAllSettingsMap();

        if (map.containsKey("workshop_name")) settings.setWorkshopName(map.get("workshop_name"));
        if (map.containsKey("address")) settings.setAddress(map.get("address"));
        if (map.containsKey("phone")) settings.setPhoneNumber(map.get("phone"));
        if (map.containsKey("pan_vat")) settings.setPanVatNumber(map.get("pan_vat"));
        if (map.containsKey("invoice_prefix")) settings.setInvoicePrefix(map.get("invoice_prefix"));
        if (map.containsKey("default_currency")) settings.setDefaultCurrency(map.get("default_currency"));
        if (map.containsKey("default_service_charge")) {
            settings.setDefaultServiceCharge(FormatUtil.parseDouble(map.get("default_service_charge"), 250.0));
        }
        if (map.containsKey("low_stock_threshold")) {
            settings.setLowStockThreshold(FormatUtil.parseInt(map.get("low_stock_threshold"), 5));
        }
        if (map.containsKey("allow_negative_stock")) {
            settings.setAllowNegativeStock(Boolean.parseBoolean(map.get("allow_negative_stock")));
        }
        return settings;
    }

    public Map<String, String> getAllSettingsMap() {
        Map<String, String> map = new HashMap<>();
        String sql = "SELECT key, value FROM settings";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean saveSettings(WorkshopSetting settings) {
        String sql = "INSERT INTO settings (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            saveKey(pstmt, "workshop_name", settings.getWorkshopName());
            saveKey(pstmt, "address", settings.getAddress());
            saveKey(pstmt, "phone", settings.getPhoneNumber());
            saveKey(pstmt, "pan_vat", settings.getPanVatNumber());
            saveKey(pstmt, "invoice_prefix", settings.getInvoicePrefix());
            saveKey(pstmt, "default_currency", settings.getDefaultCurrency());
            saveKey(pstmt, "default_service_charge", String.valueOf(settings.getDefaultServiceCharge()));
            saveKey(pstmt, "low_stock_threshold", String.valueOf(settings.getLowStockThreshold()));
            saveKey(pstmt, "allow_negative_stock", String.valueOf(settings.isAllowNegativeStock()));

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void saveKey(PreparedStatement pstmt, String key, String val) throws SQLException {
        pstmt.setString(1, key);
        pstmt.setString(2, val != null ? val : "");
        pstmt.executeUpdate();
    }
}
