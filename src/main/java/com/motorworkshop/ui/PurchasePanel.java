package com.motorworkshop.ui;

import com.motorworkshop.model.Purchase;
import com.motorworkshop.model.PurchaseItem;
import com.motorworkshop.model.Supplier;
import com.motorworkshop.service.InventoryService;
import com.motorworkshop.service.PurchaseService;
import com.motorworkshop.ui.dialogs.PurchaseDialog;
import com.motorworkshop.util.CsvExporter;
import com.motorworkshop.util.DateUtil;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Orders & Stock Inward management panel.
 */
public class PurchasePanel extends JPanel {
    private final PurchaseService purchaseService;
    private final InventoryService inventoryService;
    private final Frame parentFrame;

    private JTextField txtSearch;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JComboBox<Supplier> cbSupplierFilter;

    private JTable purchaseTable;
    private DefaultTableModel tableModel;
    private List<Purchase> currentPurchaseList = new ArrayList<>();
    private JLabel lblTotalPurchases;

    public PurchasePanel(PurchaseService purchaseService, InventoryService inventoryService, Frame parentFrame) {
        this.purchaseService = purchaseService;
        this.inventoryService = inventoryService;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(15, 18, 15, 18));
        setBackground(UIHelper.BG_LIGHT);

        initComponents();
        loadSuppliers();
        loadData();
    }

    private void initComponents() {
        // Top Container
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(UIHelper.BG_LIGHT);

        // Header Title & Action Buttons
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);

        JLabel lblTitle = new JLabel("Supplier Purchases & Stock Inward");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.BG_LIGHT);

        JButton btnNew = UIHelper.createSuccessButton("+ New Purchase");
        btnNew.addActionListener(e -> openNewPurchase());

        JButton btnView = UIHelper.createPrimaryButton("View Items");
        btnView.addActionListener(e -> onViewItems());

        JButton btnDelete = UIHelper.createDangerButton("Delete Purchase");
        btnDelete.addActionListener(e -> onDeletePurchase());

        JButton btnExport = UIHelper.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, purchaseTable, "purchases_history"));

        actionBtns.add(btnNew);
        actionBtns.add(btnView);
        actionBtns.add(btnDelete);
        actionBtns.add(btnExport);

        headerBar.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // Filters Bar
        JPanel filterBar = UIHelper.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtSearch = new JTextField(12);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search invoice, supplier...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        txtStartDate = new JTextField(DateUtil.startOfMonth(), 9);
        txtEndDate = new JTextField(DateUtil.today(), 9);
        cbSupplierFilter = new JComboBox<>();
        cbSupplierFilter.addActionListener(e -> loadData());

        JButton btnFilterDate = UIHelper.createSecondaryButton("Filter Dates");
        btnFilterDate.addActionListener(e -> loadData());

        JButton btnAllDates = UIHelper.createSecondaryButton("All Time");
        btnAllDates.addActionListener(e -> {
            txtStartDate.setText("");
            txtEndDate.setText("");
            loadData();
        });

        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtSearch);
        filterBar.add(new JLabel("Supplier:"));
        filterBar.add(cbSupplierFilter);
        filterBar.add(new JLabel("From:"));
        filterBar.add(txtStartDate);
        filterBar.add(new JLabel("To:"));
        filterBar.add(txtEndDate);
        filterBar.add(btnFilterDate);
        filterBar.add(btnAllDates);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Date", "Invoice / Bill No", "Supplier Name", "Total Amount (Rs.)", "Payment Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        purchaseTable = new JTable(tableModel);
        UIHelper.styleTable(purchaseTable);

        JScrollPane scrollPane = new JScrollPane(purchaseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Total
        JPanel footerBar = UIHelper.createCard();
        footerBar.setLayout(new BorderLayout());
        lblTotalPurchases = new JLabel("Total Purchases: Rs. 0.00");
        lblTotalPurchases.setFont(UIHelper.FONT_BIG_NUMBER);
        lblTotalPurchases.setForeground(UIHelper.PRIMARY_COLOR);
        footerBar.add(lblTotalPurchases, BorderLayout.EAST);
        add(footerBar, BorderLayout.SOUTH);
    }

    private void loadSuppliers() {
        try {
            cbSupplierFilter.removeAllItems();
            cbSupplierFilter.addItem(new Supplier(0, "All Suppliers", "", "", "", ""));
            for (Supplier s : purchaseService.getAllSuppliers()) {
                cbSupplierFilter.addItem(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                String search = txtSearch.getText().trim();
                String start = txtStartDate.getText().trim();
                String end = txtEndDate.getText().trim();
                Supplier s = (Supplier) cbSupplierFilter.getSelectedItem();
                Integer supId = s != null && s.getSupplierId() > 0 ? s.getSupplierId() : null;

                currentPurchaseList = purchaseService.searchPurchases(search, start, end, supId);
                tableModel.setRowCount(0);

                double total = 0.0;
                for (Purchase p : currentPurchaseList) {
                    total += p.getTotalAmount();
                    tableModel.addRow(new Object[]{
                            p.getPurchaseId(),
                            p.getDate(),
                            p.getInvoiceNumber(),
                            p.getSupplierName() != null ? p.getSupplierName() : "General Supplier",
                            FormatUtil.formatCurrencyPlain(p.getTotalAmount()),
                            p.getPaymentStatus(),
                            p.getNotes() != null ? p.getNotes() : ""
                    });
                }
                lblTotalPurchases.setText("Total Purchases: " + FormatUtil.formatCurrency(total));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void openNewPurchase() {
        PurchaseDialog dlg = new PurchaseDialog(parentFrame, purchaseService, inventoryService);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void onViewItems() {
        int row = purchaseTable.getSelectedRow();
        if (row < 0 || row >= currentPurchaseList.size()) {
            UIHelper.showWarning(this, "Please select a purchase invoice to view items.");
            return;
        }

        Purchase p = currentPurchaseList.get(row);
        try {
            Purchase full = purchaseService.getPurchaseById(p.getPurchaseId());
            if (full != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Purchase Invoice: ").append(full.getInvoiceNumber()).append("\n");
                sb.append("Date: ").append(full.getDate()).append(" | Supplier: ").append(full.getSupplierName()).append("\n\n");
                sb.append(String.format("%-30s %-8s %-12s %-12s\n", "Item Name", "Qty", "Buy Rate", "Total"));
                sb.append("-------------------------------------------------------------------\n");
                for (PurchaseItem item : full.getItems()) {
                    sb.append(String.format("%-30s %-8d %-12s %-12s\n",
                            item.getProductName(),
                            item.getQuantity(),
                            FormatUtil.formatCurrency(item.getUnitPurchasePrice()),
                            FormatUtil.formatCurrency(item.getTotalPrice())));
                }
                sb.append("-------------------------------------------------------------------\n");
                sb.append("Total Amount: ").append(FormatUtil.formatCurrency(full.getTotalAmount()));

                JTextArea area = new JTextArea(sb.toString(), 15, 55);
                area.setFont(new Font("Monospaced", Font.PLAIN, 12));
                area.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(area), "Purchase Details", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            UIHelper.showError(this, "Failed to load purchase items: " + e.getMessage());
        }
    }

    private void onDeletePurchase() {
        int row = purchaseTable.getSelectedRow();
        if (row < 0 || row >= currentPurchaseList.size()) {
            UIHelper.showWarning(this, "Please select a purchase order to delete.");
            return;
        }

        Purchase p = currentPurchaseList.get(row);
        if (UIHelper.showConfirm(this,
                "Deleting purchase " + p.getInvoiceNumber() + " will REVERSE and DEDUCT the purchased stock quantity from inventory.\nProceed?",
                "Confirm Purchase Reversal")) {
            try {
                purchaseService.deletePurchase(p.getPurchaseId());
                UIHelper.showInfo(this, "Purchase deleted and inventory stock reversed.");
                loadData();
            } catch (Exception e) {
                UIHelper.showError(this, "Error deleting purchase: " + e.getMessage());
            }
        }
    }
}
