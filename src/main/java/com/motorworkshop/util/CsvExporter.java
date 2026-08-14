package com.motorworkshop.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Utility to export JTable models or custom data to CSV files.
 */
public class CsvExporter {

    public static void exportTableToCsv(Component parent, JTable table, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to CSV");
        chooser.setSelectedFile(new File(defaultFileName.endsWith(".csv") ? defaultFileName : defaultFileName + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = chooser.showSaveDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                TableModel model = table.getModel();
                int colCount = model.getColumnCount();

                // Write Header
                for (int i = 0; i < colCount; i++) {
                    writer.print(escapeCsv(model.getColumnName(i)));
                    if (i < colCount - 1) writer.print(",");
                }
                writer.println();

                // Write Data
                int rowCount = model.getRowCount();
                for (int row = 0; row < rowCount; row++) {
                    for (int col = 0; col < colCount; col++) {
                        Object val = model.getValueAt(row, col);
                        writer.print(escapeCsv(val != null ? val.toString() : ""));
                        if (col < colCount - 1) writer.print(",");
                    }
                    writer.println();
                }

                UIHelper.showInfo(parent, "Data successfully exported to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception e) {
                UIHelper.showError(parent, "Failed to export CSV: " + e.getMessage());
            }
        }
    }

    public static void exportRowsToCsv(Component parent, String[] headers, List<String[]> rows, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export to CSV");
        chooser.setSelectedFile(new File(defaultFileName.endsWith(".csv") ? defaultFileName : defaultFileName + ".csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = chooser.showSaveDialog(parent);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                // Header
                for (int i = 0; i < headers.length; i++) {
                    writer.print(escapeCsv(headers[i]));
                    if (i < headers.length - 1) writer.print(",");
                }
                writer.println();

                // Rows
                for (String[] row : rows) {
                    for (int i = 0; i < row.length; i++) {
                        writer.print(escapeCsv(row[i] != null ? row[i] : ""));
                        if (i < row.length - 1) writer.print(",");
                    }
                    writer.println();
                }

                UIHelper.showInfo(parent, "Report successfully exported to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception e) {
                UIHelper.showError(parent, "Failed to export CSV: " + e.getMessage());
            }
        }
    }

    private static String escapeCsv(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
