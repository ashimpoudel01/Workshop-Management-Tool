package com.motorworkshop;

import com.formdev.flatlaf.FlatLightLaf;
import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.ui.LoginDialog;
import com.motorworkshop.ui.MainFrame;
import com.motorworkshop.ui.UserRole;

import javax.swing.*;

/**
 * Application Entry Point.
 * Initializes SQLite database, applies modern Look &amp; Feel, and launches the Main Window.
 */
public class Main {
    public static void main(String[] args) {
        // Set Look and Feel (FlatLaf or System default fallback)
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.thumbArc", 6);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TableHeader.height", 36);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", java.awt.Color.WHITE);
            UIManager.put("TabbedPane.tabAreaInsets", new java.awt.Insets(4, 6, 0, 6));
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize SQLite Database schema & initial seed data
        try {
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize SQLite Database: " + e.getMessage(),
                    "Database Startup Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
                    return;
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);
            UserRole role = loginDialog.getAuthenticatedRole();
            if (role == null) {
                System.exit(0);
            }
            MainFrame mainFrame = new MainFrame(role);
            mainFrame.setVisible(true);
        });
    }
}
