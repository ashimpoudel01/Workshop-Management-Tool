package com.motorworkshop.service;

import com.motorworkshop.database.DatabaseManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Handles SQLite database backup, export, and restoration with data safety checks.
 */
public class BackupRestoreService {

    public static boolean backupDatabase(File targetFile) throws IOException {
        File currentDb = DatabaseManager.getDatabaseFile();
        if (!currentDb.exists()) {
            throw new IOException("Database file does not exist to backup!");
        }

        // Flush WAL checkpoint first to ensure all transactions are on main db file
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA wal_checkpoint(FULL);");
        } catch (Exception ignored) {}

        copyFile(currentDb, targetFile);
        return true;
    }

    public static boolean restoreDatabase(File backupFile) throws IOException {
        if (!backupFile.exists() || backupFile.length() == 0) {
            throw new IOException("Selected backup file is invalid or empty!");
        }

        File currentDb = DatabaseManager.getDatabaseFile();
        
        // Create an automatic safety backup of current DB before overwriting
        File autoBackup = new File(currentDb.getParentFile(), "workshop_auto_pre_restore_" + System.currentTimeMillis() + ".bak");
        if (currentDb.exists()) {
            copyFile(currentDb, autoBackup);
        }

        copyFile(backupFile, currentDb);
        DatabaseManager.initializeDatabase();
        return true;
    }

    private static void copyFile(File source, File dest) throws IOException {
        if (dest.getParentFile() != null && !dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
             FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }
}
