package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Supplier;
import com.motorworkshop.service.PurchaseService;
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
 * Dialog to manage, search, add, and change Vendor / Supplier profiles.
 */
public class VendorManagementDialog extends JDialog {
    private final PurchaseService purchaseService;
    private boolean changed = false;

    private JTextField txtSearch;
    private JTable vendorTable;
    private DefaultTableModel tableModel;
    private List<Supplier> currentVendors = new ArrayList<>();
    private JLabel lblTotalVendors;

    public VendorManagementDialog(Window owner, PurchaseService purchaseService) {
        super(owner, "Vendor & Supplier Management", ModalityType.APPLICATION_MODAL);
        this.purchaseService = purchaseService;

        initComponents();
        loadData();
        setSize(850, 520);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBorder(new EmptyBorder(15, 18, 15, 18));
        contentPane.setBackground(UIHelper.BG_LIGHT);

        // Top Section: Title & Actions
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(UIHelper.BG_LIGHT);

        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);

        JLabel lblTitle = new JLabel("Vendors & Suppliers Directory");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.BG_LIGHT);

        JButton btnAdd = UIHelper.createSuccessButton("+ Add Vendor");
        btnAdd.addActionListener(e -> onAddVendor());

        JButton btnEdit = UIHelper.createPrimaryButton("Edit / Change Vendor");
        btnEdit.addActionListener(e -> onEditVendor());

        JButton btnDelete = UIHelper.createDangerButton("Delete Vendor");
        btnDelete.addActionListener(e -> onDeleteVendor());

        actionBtns.add(btnAdd);
        actionBtns.add(btnEdit);
        actionBtns.add(btnDelete);

        headerBar.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // Search Bar
        JPanel searchBar = UIHelper.createCard();
        searchBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search vendor name, phone, address...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        searchBar.add(new JLabel("Search Vendors:"));
        searchBar.add(txtSearch);
        topContainer.add(searchBar, BorderLayout.SOUTH);

        contentPane.add(topContainer, BorderLayout.NORTH);

        // Center Table
        String[] cols = {"S.N.", "Vendor / Supplier Name", "Phone", "Email", "Address", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        vendorTable = new JTable(tableModel);
        UIHelper.styleTable(vendorTable);

        contentPane.add(new JScrollPane(vendorTable), BorderLayout.CENTER);

        // Footer Bar
        JPanel footerBar = UIHelper.createCard();
        footerBar.setLayout(new BorderLayout());

        lblTotalVendors = new JLabel("Total Vendors: 0");
        lblTotalVendors.setFont(UIHelper.FONT_BOLD);
        footerBar.add(lblTotalVendors, BorderLayout.WEST);

        JButton btnClose = UIHelper.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dispose());
        footerBar.add(btnClose, BorderLayout.EAST);

        contentPane.add(footerBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    public void loadData() {
        try {
            String query = txtSearch.getText().trim();
            currentVendors = purchaseService.searchSuppliers(query);
            tableModel.setRowCount(0);

            int sn = 1;
            for (Supplier s : currentVendors) {
                tableModel.addRow(new Object[]{
                        sn++,
                        s.getName(),
                        s.getPhone() != null ? s.getPhone() : "-",
                        s.getEmail() != null ? s.getEmail() : "-",
                        s.getAddress() != null ? s.getAddress() : "-",
                        s.getNotes() != null ? s.getNotes() : "-"
                });
            }
            lblTotalVendors.setText("Total Vendors: " + currentVendors.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Supplier getSelectedSupplier() {
        int row = vendorTable.getSelectedRow();
        if (row >= 0 && row < currentVendors.size()) {
            return currentVendors.get(row);
        }
        return null;
    }

    private void onAddVendor() {
        SupplierDialog dlg = new SupplierDialog(this, purchaseService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            changed = true;
            loadData();
        }
    }

    private void onEditVendor() {
        Supplier s = getSelectedSupplier();
        if (s == null) {
            UIHelper.showWarning(this, "Please select a vendor from the table to edit.");
            return;
        }
        SupplierDialog dlg = new SupplierDialog(this, purchaseService, s);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            changed = true;
            loadData();
        }
    }

    private void onDeleteVendor() {
        Supplier s = getSelectedSupplier();
        if (s == null) {
            UIHelper.showWarning(this, "Please select a vendor to delete.");
            return;
        }
        if (UIHelper.showConfirm(this, "Are you sure you want to delete vendor '" + s.getName() + "'?", "Confirm Vendor Deletion")) {
            try {
                purchaseService.deleteSupplier(s.getSupplierId());
                changed = true;
                UIHelper.showInfo(this, "Vendor deleted successfully.");
                loadData();
            } catch (Exception e) {
                UIHelper.showError(this, "Error deleting vendor: " + e.getMessage());
            }
        }
    }

    public boolean isChanged() {
        return changed;
    }
}
