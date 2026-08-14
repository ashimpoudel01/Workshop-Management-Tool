package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.InventoryTransaction;
import com.motorworkshop.model.Product;
import com.motorworkshop.service.InventoryService;
import com.motorworkshop.util.CsvExporter;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dialog to view complete movement history of an item or the workshop inventory.
 */
public class StockHistoryDialog extends JDialog {
    private final InventoryService inventoryService;
    private final Product product;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    public StockHistoryDialog(Frame owner, InventoryService inventoryService, Product product) {
        super(owner, product != null ? "Stock Movement History - " + product.getPartName() : "Overall Inventory Audit Trail", true);
        this.inventoryService = inventoryService;
        this.product = product;

        initComponents();
        loadData();
        setSize(800, 450);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(UIHelper.CARD_BG);

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UIHelper.CARD_BG);
        JLabel lblTitle = new JLabel(product != null ? "Audit Trail for: " + product.getPartName() : "Recent Stock Movements");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        topPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnExport = UIHelper.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, historyTable, "stock_history"));
        topPanel.add(btnExport, BorderLayout.EAST);

        contentPane.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Date", "Item Name", "Movement Type", "Qty Changed", "Previous Stock", "New Stock", "Reference", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        UIHelper.styleTable(historyTable);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(UIHelper.CARD_BG);
        JButton btnClose = UIHelper.createPrimaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<InventoryTransaction> list;
            if (product != null) {
                list = inventoryService.getTransactionsByProduct(product.getItemId());
            } else {
                list = inventoryService.getRecentTransactions(150);
            }

            for (InventoryTransaction tx : list) {
                tableModel.addRow(new Object[]{
                        tx.getDate(),
                        tx.getProductName(),
                        tx.getTransactionType(),
                        (tx.getQuantity() > 0 ? "+" : "") + tx.getQuantity(),
                        tx.getPreviousStock(),
                        tx.getNewStock(),
                        tx.getReferenceId(),
                        tx.getNotes()
                });
            }
        } catch (Exception e) {
            UIHelper.showError(this, "Failed to load audit history: " + e.getMessage());
        }
    }
}
