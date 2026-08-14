package com.motorworkshop;

import com.formdev.flatlaf.FlatLightLaf;
import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.ui.MainFrame;

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
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
