package com.motorworkshop.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the SQLite database schema, creates all necessary tables and indexes.
 */
public class SchemaInitializer {

    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Check if settings table has old schema
            try (var rs = stmt.executeQuery("PRAGMA table_info(settings)")) {
                boolean hasKeyCol = false;
                while (rs.next()) {
                    if ("key".equalsIgnoreCase(rs.getString("name"))) {
                        hasKeyCol = true;
                        break;
                    }
                }
                // If table existed with different columns, drop and recreate cleanly
                if (!hasKeyCol) {
                    stmt.execute("DROP TABLE IF EXISTS settings;");
                }
            } catch (Exception ignored) {}

            // Check sales table for customer_id column
            try (var rs = stmt.executeQuery("PRAGMA table_info(sales)")) {
                boolean hasCustId = false;
                while (rs.next()) {
                    if ("customer_id".equalsIgnoreCase(rs.getString("name"))) {
                        hasCustId = true;
                        break;
                    }
                }
                if (!hasCustId) {
                    stmt.execute("DROP TABLE IF EXISTS sale_items;");
                    stmt.execute("DROP TABLE IF EXISTS sales;");
                }
            } catch (Exception ignored) {}

            // Check products table for workshop_price column
            try (var rs = stmt.executeQuery("PRAGMA table_info(products)")) {
                boolean hasWsPrice = false;
                while (rs.next()) {
                    if ("workshop_price".equalsIgnoreCase(rs.getString("name"))) {
                        hasWsPrice = true;
                        break;
                    }
                }
                if (!hasWsPrice) {
                    stmt.execute("DROP TABLE IF EXISTS inventory_transactions;");
                    stmt.execute("DROP TABLE IF EXISTS price_history;");
                    stmt.execute("DROP TABLE IF EXISTS purchase_items;");
                    stmt.execute("DROP TABLE IF EXISTS purchases;");
                    stmt.execute("DROP TABLE IF EXISTS products;");
                }
            } catch (Exception ignored) {}

            // Check services table
            try (var rs = stmt.executeQuery("PRAGMA table_info(services)")) {
                boolean hasDuration = false;
                while (rs.next()) {
                    if ("estimated_duration_minutes".equalsIgnoreCase(rs.getString("name"))) {
                        hasDuration = true;
                        break;
                    }
                }
                if (!hasDuration) {
                    stmt.execute("DROP TABLE IF EXISTS services;");
                }
            } catch (Exception ignored) {}

            // Check customers table
            try (var rs = stmt.executeQuery("PRAGMA table_info(customers)")) {
                boolean hasCreated = false;
                while (rs.next()) {
                    if ("created_at".equalsIgnoreCase(rs.getString("name"))) {
                        hasCreated = true;
                        break;
                    }
                }
                if (!hasCreated) {
                    stmt.execute("DROP TABLE IF EXISTS customers;");
                }
            } catch (Exception ignored) {}

            // Check expenses table for expense_id column
            try (var rs = stmt.executeQuery("PRAGMA table_info(expenses)")) {
                boolean hasExpenseId = false;
                while (rs.next()) {
                    if ("expense_id".equalsIgnoreCase(rs.getString("name"))) {
                        hasExpenseId = true;
                        break;
                    }
                }
                if (!hasExpenseId) {
                    stmt.execute("DROP TABLE IF EXISTS expenses;");
                }
            } catch (Exception ignored) {}

            // Settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "key TEXT PRIMARY KEY, " +
                    "value TEXT" +
                    ");");

            // Categories table
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT" +
                    ");");

            // Suppliers table
            stmt.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                    "supplier_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT, " +
                    "email TEXT, " +
                    "address TEXT, " +
                    "notes TEXT" +
                    ");");

            // Products / Inventory table
            stmt.execute("CREATE TABLE IF NOT EXISTS products (" +
                    "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "part_name TEXT NOT NULL, " +
                    "part_number TEXT, " +
                    "category_id INTEGER, " +
                    "brand TEXT, " +
                    "supplier_id INTEGER, " +
                    "purchase_price REAL NOT NULL DEFAULT 0.0, " +
                    "selling_price REAL NOT NULL DEFAULT 0.0, " +
                    "workshop_price REAL NOT NULL DEFAULT 0.0, " +
                    "current_quantity INTEGER NOT NULL DEFAULT 0, " +
                    "min_stock_level INTEGER NOT NULL DEFAULT 5, " +
                    "unit TEXT DEFAULT 'Pcs', " +
                    "date_added TEXT NOT NULL, " +
                    "last_purchase_date TEXT, " +
                    "is_active INTEGER NOT NULL DEFAULT 1, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL" +
                    ");");

            // Price History table
            stmt.execute("CREATE TABLE IF NOT EXISTS price_history (" +
                    "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER NOT NULL, " +
                    "old_purchase_price REAL, " +
                    "new_purchase_price REAL, " +
                    "old_selling_price REAL, " +
                    "new_selling_price REAL, " +
                    "change_date TEXT NOT NULL, " +
                    "reason TEXT, " +
                    "FOREIGN KEY (product_id) REFERENCES products(item_id) ON DELETE CASCADE" +
                    ");");

            // Inventory Transactions table (Audit trail)
            stmt.execute("CREATE TABLE IF NOT EXISTS inventory_transactions (" +
                    "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "transaction_type TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "previous_stock INTEGER NOT NULL, " +
                    "new_stock INTEGER NOT NULL, " +
                    "reference_id TEXT, " +
                    "notes TEXT, " +
                    "FOREIGN KEY (product_id) REFERENCES products(item_id) ON DELETE CASCADE" +
                    ");");

            // Services table
            stmt.execute("CREATE TABLE IF NOT EXISTS services (" +
                    "service_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "service_name TEXT NOT NULL UNIQUE, " +
                    "default_price REAL NOT NULL DEFAULT 0.0, " +
                    "estimated_duration_minutes INTEGER DEFAULT 30, " +
                    "description TEXT, " +
                    "is_active INTEGER NOT NULL DEFAULT 1" +
                    ");");

            // Customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "phone TEXT NOT NULL, " +
                    "address TEXT, " +
                    "vehicle_number TEXT, " +
                    "vehicle_brand TEXT, " +
                    "vehicle_model TEXT, " +
                    "notes TEXT, " +
                    "created_at TEXT NOT NULL" +
                    ");");

            // Purchases table
            stmt.execute("CREATE TABLE IF NOT EXISTS purchases (" +
                    "purchase_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "invoice_number TEXT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "supplier_id INTEGER, " +
                    "total_amount REAL NOT NULL DEFAULT 0.0, " +
                    "payment_status TEXT NOT NULL DEFAULT 'PAID', " +
                    "notes TEXT, " +
                    "FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL" +
                    ");");

            // Purchase Items table
            stmt.execute("CREATE TABLE IF NOT EXISTS purchase_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "purchase_id INTEGER NOT NULL, " +
                    "product_id INTEGER NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "unit_purchase_price REAL NOT NULL, " +
                    "total_price REAL NOT NULL, " +
                    "FOREIGN KEY (purchase_id) REFERENCES purchases(purchase_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (product_id) REFERENCES products(item_id) ON DELETE RESTRICT" +
                    ");");

            // Sales table
            stmt.execute("CREATE TABLE IF NOT EXISTS sales (" +
                    "sale_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "invoice_number TEXT NOT NULL UNIQUE, " +
                    "date TEXT NOT NULL, " +
                    "customer_id INTEGER, " +
                    "customer_name TEXT NOT NULL, " +
                    "customer_phone TEXT, " +
                    "vehicle_type TEXT, " +
                    "vehicle_brand TEXT, " +
                    "vehicle_model TEXT, " +
                    "vehicle_reg_no TEXT, " +
                    "service_charge REAL NOT NULL DEFAULT 0.0, " +
                    "parts_total REAL NOT NULL DEFAULT 0.0, " +
                    "discount REAL NOT NULL DEFAULT 0.0, " +
                    "subtotal REAL NOT NULL DEFAULT 0.0, " +
                    "total_amount REAL NOT NULL DEFAULT 0.0, " +
                    "total_cogs REAL NOT NULL DEFAULT 0.0, " +
                    "gross_profit REAL NOT NULL DEFAULT 0.0, " +
                    "payment_method TEXT NOT NULL DEFAULT 'Cash', " +
                    "notes TEXT, " +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL" +
                    ");");

            // Sale Items table
            stmt.execute("CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sale_id INTEGER NOT NULL, " +
                    "item_type TEXT NOT NULL, " + // 'PART' or 'SERVICE'
                    "item_id INTEGER, " +
                    "item_name TEXT NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "unit_price REAL NOT NULL, " +
                    "unit_cost REAL NOT NULL DEFAULT 0.0, " +
                    "total_price REAL NOT NULL, " +
                    "total_cost REAL NOT NULL DEFAULT 0.0, " +
                    "FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE" +
                    ");");

            // Expenses table
            stmt.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                    "expense_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "description TEXT, " +
                    "amount REAL NOT NULL, " +
                    "payment_method TEXT NOT NULL DEFAULT 'Cash', " +
                    "notes TEXT" +
                    ");");

            // Create helpful search & performance indexes
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_name ON products(part_name);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_products_part_no ON products(part_number);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sales_invoice ON sales(invoice_number);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sales_customer ON sales(customer_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_purchases_date ON purchases(date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_inv_tx_date ON inventory_transactions(date);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_customers_veh ON customers(vehicle_number);");

            // Backfill and repair parts_total, service_charge, and COGS for any sales missing them
            stmt.executeUpdate(
                    "UPDATE sales SET " +
                    "parts_total = COALESCE((SELECT SUM(total_price) FROM sale_items WHERE sale_items.sale_id = sales.sale_id AND (UPPER(sale_items.item_type) = 'PART' OR UPPER(sale_items.item_type) = 'PRODUCT')), 0.0), " +
                    "service_charge = COALESCE((SELECT SUM(total_price) FROM sale_items WHERE sale_items.sale_id = sales.sale_id AND (UPPER(sale_items.item_type) = 'SERVICE' OR UPPER(sale_items.item_type) = 'LABOR')), 0.0) " +
                    "WHERE (parts_total = 0.0 AND service_charge = 0.0) OR subtotal = 0.0;"
            );

            stmt.executeUpdate(
                    "UPDATE sales SET " +
                    "subtotal = parts_total + service_charge, " +
                    "total_amount = MAX(0.0, (parts_total + service_charge) - discount) " +
                    "WHERE subtotal = 0.0 AND (parts_total > 0.0 OR service_charge > 0.0);"
            );

            stmt.executeUpdate(
                    "UPDATE sales SET " +
                    "total_cogs = COALESCE((SELECT SUM(total_cost) FROM sale_items WHERE sale_items.sale_id = sales.sale_id AND (UPPER(sale_items.item_type) = 'PART' OR UPPER(sale_items.item_type) = 'PRODUCT')), 0.0), " +
                    "gross_profit = total_amount - COALESCE((SELECT SUM(total_cost) FROM sale_items WHERE sale_items.sale_id = sales.sale_id AND (UPPER(sale_items.item_type) = 'PART' OR UPPER(sale_items.item_type) = 'PRODUCT')), 0.0) " +
                    "WHERE total_cogs = 0.0 AND gross_profit = 0.0 AND total_amount > 0.0;"
            );

        } catch (SQLException e) {
            System.err.println("Error initializing SQLite schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
