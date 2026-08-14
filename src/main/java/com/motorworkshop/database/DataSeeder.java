package com.motorworkshop.database;

import com.motorworkshop.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Seeds default settings, motorcycle service menu, categories, sample suppliers, and parts on first run.
 */
public class DataSeeder {

    public static void seedDefaultDataIfNeeded() {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Check if settings table is empty
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM settings")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedSettings(conn);
                }
            }

            // Check if categories table is empty
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedCategories(conn);
                }
            }

            // Check if services table is empty
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM services")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedServices(conn);
                }
            }

            // Check if suppliers table is empty
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM suppliers")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedSuppliers(conn);
                }
            }

            // Check if products table is empty
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedProducts(conn);
                }
            }

            // Check if customers table is empty
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM customers")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    seedCustomers(conn);
                }
            }

        } catch (Exception e) {
            System.err.println("Error seeding default data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedSettings(Connection conn) throws Exception {
        String sql = "INSERT INTO settings (key, value) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            insertSetting(pstmt, "workshop_name", "Shree Krishna Motorcycle & Scooter Workshop");
            insertSetting(pstmt, "address", "Teku, Kathmandu, Nepal");
            insertSetting(pstmt, "phone", "+977-9841234567");
            insertSetting(pstmt, "pan_vat", "609876543");
            insertSetting(pstmt, "invoice_prefix", "INV-");
            insertSetting(pstmt, "default_currency", "Rs.");
            insertSetting(pstmt, "default_service_charge", "300");
            insertSetting(pstmt, "low_stock_threshold", "5");
            insertSetting(pstmt, "allow_negative_stock", "false");
        }
    }

    private static void insertSetting(PreparedStatement pstmt, String key, String val) throws Exception {
        pstmt.setString(1, key);
        pstmt.setString(2, val);
        pstmt.executeUpdate();
    }

    private static void seedCategories(Connection conn) throws Exception {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[][] cats = {
                    {"Lubricants & Fluids", "Engine oils, brake fluids, gear oils, fork oils"},
                    {"Filters", "Air filters, oil filters, fuel filters"},
                    {"Brake System", "Brake pads, brake shoes, brake cables, disc plates"},
                    {"Ignition & Electrical", "Spark plugs, batteries, bulbs, horns, CDI"},
                    {"Transmission & Clutch", "Drive belts, weight rollers, clutch shoes, chains, sprockets"},
                    {"Suspension & Wheels", "Tyres, tubes, bearings, fork seals, shock absorbers"},
                    {"Engine & Fuel Parts", "Carburetor kits, gaskets, valves, pistons, FI parts"},
                    {"Cables & Controls", "Clutch cables, throttle cables, speedometer cables"}
            };
            for (String[] cat : cats) {
                pstmt.setString(1, cat[0]);
                pstmt.setString(2, cat[1]);
                pstmt.executeUpdate();
            }
        }
    }

    private static void seedServices(Connection conn) throws Exception {
        String sql = "INSERT INTO services (service_name, default_price, estimated_duration_minutes, description, is_active) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] services = {
                    {"General Servicing", 350.0, 45, "Complete vehicle checkup, washing, chain lube, brake adjustment, tuning"},
                    {"Engine Oil Change", 100.0, 15, "Oil drain, oil filter clean/change, fresh oil refill"},
                    {"Brake Service (Front/Rear)", 200.0, 30, "Brake shoe/pad cleaning, caliper greasing, lever adjustment"},
                    {"Chain Cleaning & Lubrication", 150.0, 20, "Drive chain degreasing, tightening, and lube"},
                    {"Carburetor Cleaning & Tuning", 300.0, 40, "Carburetor disassembly, jet cleaning, tuning for mileage"},
                    {"FI (Fuel Injection) Cleaning", 600.0, 60, "Ultrasonic injector clean and throttle body service"},
                    {"Pickup Improvement Tuning", 400.0, 45, "Roller, clutch, variator tuning for scooters & bikes"},
                    {"Full Engine Overhaul", 4500.0, 360, "Complete engine dismantling, bore, piston, valve job"},
                    {"Half Engine Repair", 2200.0, 180, "Cylinder head, timing chain, valve servicing"},
                    {"Electrical Wiring Repair", 350.0, 45, "Wiring troubleshooting, harness repair, fuse check"},
                    {"Tyre Replacement / Fitting", 150.0, 25, "Tyre unmount, new tyre fitting, air pressure check"},
                    {"Battery Charging & Maintenance", 100.0, 30, "Terminal cleaning, battery acid check, boost charge"}
            };
            for (Object[] s : services) {
                pstmt.setString(1, (String) s[0]);
                pstmt.setDouble(2, (Double) s[1]);
                pstmt.setInt(3, (Integer) s[2]);
                pstmt.setString(4, (String) s[3]);
                pstmt.executeUpdate();
            }
        }
    }

    private static void seedSuppliers(Connection conn) throws Exception {
        String sql = "INSERT INTO suppliers (name, phone, email, address, notes) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[][] suppliers = {
                    {"Nepal Auto Parts Suppliers", "9851022334", "nepalautoparts@gmail.com", "Teku, Kathmandu", "Authorized distributor of genuine Bajaj & Honda parts"},
                    {"Himalayan Lubricants Dist.", "9841987654", "himalayanlubes@gmail.com", "Tripureshwor, Kathmandu", "Supplier of Motul, Castrol, and Servo engine oils"},
                    {"Valley Tyres & Accessories", "9812345678", "valleytyres@gmail.com", "Balkumari, Lalitpur", "MRF, Ceat, Michelin bike tyres supplier"}
            };
            for (String[] sup : suppliers) {
                pstmt.setString(1, sup[0]);
                pstmt.setString(2, sup[1]);
                pstmt.setString(3, sup[2]);
                pstmt.setString(4, sup[3]);
                pstmt.setString(5, sup[4]);
                pstmt.executeUpdate();
            }
        }
    }

    private static void seedProducts(Connection conn) throws Exception {
        String sql = "INSERT INTO products (part_name, part_number, category_id, brand, supplier_id, " +
                "purchase_price, selling_price, workshop_price, current_quantity, min_stock_level, unit, date_added, last_purchase_date, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

        String date = DateUtil.today();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Object[][] items = {
                    // name, partNo, catId, brand, supId, buyPrice, sellPrice, wsPrice, qty, minQty, unit
                    {"Motul 4T 20W40 1L Engine Oil", "MOT-4T-20W40", 1, "Motul", 2, 550.0, 750.0, 700.0, 24, 6, "Litre"},
                    {"Castrol Activ 20W50 1L", "CAS-ACT-1L", 1, "Castrol", 2, 520.0, 700.0, 680.0, 18, 5, "Litre"},
                    {"NGK Spark Plug CPR8EA-9", "NGK-CPR8EA9", 4, "NGK", 1, 140.0, 220.0, 200.0, 30, 8, "Pcs"},
                    {"Brake Shoe (Pulsar / FZ / Dio)", "BS-UNIV-01", 3, "KBX", 1, 280.0, 420.0, 380.0, 15, 4, "Pair"},
                    {"Front Disc Brake Pad (Pulsar 150/220)", "BP-PUL-FR", 3, "Endurance", 1, 240.0, 380.0, 350.0, 12, 4, "Pair"},
                    {"Air Filter Element (Honda Dio / Activa)", "AF-DIO-110", 2, "Honda OEM", 1, 220.0, 350.0, 320.0, 8, 3, "Pcs"},
                    {"Drive Belt (Dio / Activa / Ntorq)", "DB-SCOOTER-01", 5, "Bando", 1, 650.0, 950.0, 900.0, 6, 2, "Pcs"},
                    {"Weight Roller Set (6 Pcs)", "WR-SCOOT-SET", 5, "OEM", 1, 200.0, 350.0, 300.0, 10, 3, "Set"},
                    {"Clutch Shoe Assembly (Scooter)", "CS-DIO-NTORQ", 5, "Endurance", 1, 850.0, 1250.0, 1150.0, 4, 2, "Set"},
                    {"Halogen Headlight Bulb 12V 35/35W", "BLB-H4-35W", 4, "Osram", 1, 110.0, 180.0, 160.0, 20, 5, "Pcs"},
                    {"Wheel Bearing 6202 (Front/Rear)", "BRG-6202-RS", 6, "SKF", 1, 130.0, 220.0, 200.0, 16, 5, "Pcs"},
                    {"MRF Zapper 90/90-12 Tubeless Tyre", "TYR-MRF-909012", 6, "MRF", 3, 2100.0, 2650.0, 2550.0, 4, 2, "Pcs"},
                    {"Exide 12V 5Ah Maintenance Free Battery", "BAT-EXIDE-12V5A", 4, "Exide", 1, 1450.0, 1950.0, 1850.0, 5, 2, "Pcs"}
            };

            for (Object[] item : items) {
                pstmt.setString(1, (String) item[0]);
                pstmt.setString(2, (String) item[1]);
                pstmt.setInt(3, (Integer) item[2]);
                pstmt.setString(4, (String) item[3]);
                pstmt.setInt(5, (Integer) item[4]);
                pstmt.setDouble(6, (Double) item[5]);
                pstmt.setDouble(7, (Double) item[6]);
                pstmt.setDouble(8, (Double) item[7]);
                pstmt.setInt(9, (Integer) item[8]);
                pstmt.setInt(10, (Integer) item[9]);
                pstmt.setString(11, (String) item[10]);
                pstmt.setString(12, date);
                pstmt.setString(13, date);
                pstmt.executeUpdate();
            }
        }
    }

    private static void seedCustomers(Connection conn) throws Exception {
        String sql = "INSERT INTO customers (name, phone, address, vehicle_number, vehicle_brand, vehicle_model, notes, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String date = DateUtil.today();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[][] custs = {
                    {"Ramesh Shrestha", "9841112233", "Baneshwor, Kathmandu", "Ba 85 Pa 4321", "Bajaj", "Pulsar 150", "Regular servicing customer", date},
                    {"Sita Gurung", "9818445566", "Kupandole, Lalitpur", "Pra 3-02-001 Pa 7890", "Honda", "Dio BS4", "Prefers Motul oil", date},
                    {"Bikash Thapa", "9860778899", "Kalanki, Kathmandu", "Ba 92 Pa 6543", "TVS", "Ntorq 125", "Daily commuter", date}
            };
            for (String[] c : custs) {
                pstmt.setString(1, c[0]);
                pstmt.setString(2, c[1]);
                pstmt.setString(3, c[2]);
                pstmt.setString(4, c[3]);
                pstmt.setString(5, c[4]);
                pstmt.setString(6, c[5]);
                pstmt.setString(7, c[6]);
                pstmt.setString(8, c[7]);
                pstmt.executeUpdate();
            }
        }
    }
}
