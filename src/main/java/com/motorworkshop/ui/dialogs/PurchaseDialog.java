package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Product;
import com.motorworkshop.model.Purchase;
import com.motorworkshop.model.PurchaseItem;
import com.motorworkshop.model.Supplier;
import com.motorworkshop.service.InventoryService;
import com.motorworkshop.service.PurchaseService;
import com.motorworkshop.util.DateUtil;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to create a Purchase Invoice with multiple line items.
 */
public class PurchaseDialog extends JDialog {
    private final PurchaseService purchaseService;
    private final InventoryService inventoryService;
    private boolean saved = false;

    private JComboBox<Supplier> cbSupplier;
    private JTextField txtInvoiceNo;
    private JTextField txtDate;
    private JComboBox<String> cbPaymentStatus;
    private JTextField txtNotes;

    // Line item inputs
    private JComboBox<Product> cbProduct;
    private JTextField txtItemQty;
    private JTextField txtItemBuyPrice;
    private JButton btnAddItem;

    // Items table
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JLabel lblGrandTotal;

    private final List<PurchaseItem> itemList = new ArrayList<>();

    public PurchaseDialog(Frame owner, PurchaseService purchaseService, InventoryService inventoryService) {
        super(owner, "Create Supplier Purchase Order / Invoice", true);
        this.purchaseService = purchaseService;
        this.inventoryService = inventoryService;

        initComponents();
        loadDropdowns();
        setSize(850, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBorder(new EmptyBorder(15, 18, 15, 18));
        contentPane.setBackground(UIHelper.CARD_BG);

        // Title
        JLabel lblTitle = new JLabel("New Supplier Purchase Invoice");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        contentPane.add(lblTitle, BorderLayout.NORTH);

        // Main Center container
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(UIHelper.CARD_BG);

        // 1. Purchase Details Form
        JPanel headerForm = new JPanel(new GridLayout(2, 4, 8, 8));
        headerForm.setBackground(UIHelper.CARD_BG);
        headerForm.setBorder(new TitledBorder("Invoice Information"));

        cbSupplier = new JComboBox<>();
        txtInvoiceNo = new JTextField("PO-" + System.currentTimeMillis() % 100000, 12);
        txtDate = new JTextField(DateUtil.today(), 10);
        cbPaymentStatus = new JComboBox<>(new String[]{"PAID", "UNPAID", "PARTIAL"});
        txtNotes = new JTextField(15);

        headerForm.add(new JLabel("Supplier *:"));
        headerForm.add(cbSupplier);
        headerForm.add(new JLabel("Supplier Bill / Inv No *:"));
        headerForm.add(txtInvoiceNo);

        headerForm.add(new JLabel("Purchase Date:"));
        headerForm.add(txtDate);
        headerForm.add(new JLabel("Payment Status:"));
        headerForm.add(cbPaymentStatus);

        centerPanel.add(headerForm, BorderLayout.NORTH);

        // 2. Line Items Table & Add Form
        JPanel itemsContainer = new JPanel(new BorderLayout(8, 8));
        itemsContainer.setBackground(UIHelper.CARD_BG);
        itemsContainer.setBorder(new TitledBorder("Purchased Parts & Quantities"));

        // Add item row
        JPanel addItemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addItemRow.setBackground(UIHelper.CARD_BG);

        cbProduct = new JComboBox<>();
        cbProduct.setPreferredSize(new Dimension(300, 28));
        txtItemQty = new JTextField("1", 6);
        txtItemBuyPrice = new JTextField("0.00", 8);
        btnAddItem = UIHelper.createPrimaryButton("+ Add Item");

        cbProduct.addActionListener(e -> {
            Product p = (Product) cbProduct.getSelectedItem();
            if (p != null && p.getItemId() > 0) {
                txtItemBuyPrice.setText(String.valueOf(p.getPurchasePrice()));
            }
        });

        addItemRow.add(new JLabel("Select Part:"));
        addItemRow.add(cbProduct);
        addItemRow.add(new JLabel("Quantity:"));
        addItemRow.add(txtItemQty);
        addItemRow.add(new JLabel("Buy Rate (Rs.):"));
        addItemRow.add(txtItemBuyPrice);
        addItemRow.add(btnAddItem);

        itemsContainer.add(addItemRow, BorderLayout.NORTH);

        // Table
        String[] cols = {"SN", "Part Name", "Part Number", "Qty", "Buy Rate (Rs.)", "Total (Rs.)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(tableModel);
        UIHelper.styleTable(itemsTable);

        JScrollPane scrollPane = new JScrollPane(itemsTable);
        itemsContainer.add(scrollPane, BorderLayout.CENTER);

        // Table action buttons (Delete Row)
        JPanel tableBottom = new JPanel(new BorderLayout());
        tableBottom.setBackground(UIHelper.CARD_BG);

        JButton btnRemove = UIHelper.createDangerButton("Remove Selected Item");
        btnRemove.addActionListener(e -> onRemoveItem());
        tableBottom.add(btnRemove, BorderLayout.WEST);

        lblGrandTotal = new JLabel("Total Purchase: Rs. 0.00");
        lblGrandTotal.setFont(UIHelper.FONT_BIG_NUMBER);
        lblGrandTotal.setForeground(UIHelper.PRIMARY_COLOR);
        tableBottom.add(lblGrandTotal, BorderLayout.EAST);

        itemsContainer.add(tableBottom, BorderLayout.SOUTH);

        centerPanel.add(itemsContainer, BorderLayout.CENTER);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel footer = new JPanel(new BorderLayout(10, 10));
        footer.setBackground(UIHelper.CARD_BG);

        JPanel notesPanel = new JPanel(new BorderLayout(4, 4));
        notesPanel.setBackground(UIHelper.CARD_BG);
        notesPanel.add(new JLabel("Notes / Remarks:"), BorderLayout.WEST);
        notesPanel.add(txtNotes, BorderLayout.CENTER);
        footer.add(notesPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIHelper.CARD_BG);
        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JButton btnSave = UIHelper.createSuccessButton("Save Purchase & Update Stock");
        btnSave.addActionListener(e -> onSavePurchase());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        footer.add(btnPanel, BorderLayout.SOUTH);

        contentPane.add(footer, BorderLayout.SOUTH);

        btnAddItem.addActionListener(e -> onAddItem());
        setContentPane(contentPane);
    }

    private void loadDropdowns() {
        try {
            List<Supplier> suppliers = purchaseService.getAllSuppliers();
            cbSupplier.addItem(new Supplier(0, "-- Select Supplier --", "", "", "", ""));
            for (Supplier s : suppliers) cbSupplier.addItem(s);

            List<Product> products = inventoryService.getAllProducts();
            Product placeholder = new Product();
            placeholder.setItemId(0);
            placeholder.setPartName("-- Select Product --");
            cbProduct.addItem(placeholder);
            for (Product p : products) cbProduct.addItem(p);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onAddItem() {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p == null || p.getItemId() <= 0) {
            UIHelper.showWarning(this, "Please select a valid product!");
            return;
        }

        int qty = FormatUtil.parseInt(txtItemQty.getText(), 0);
        if (qty <= 0) {
            UIHelper.showWarning(this, "Quantity must be greater than 0!");
            txtItemQty.requestFocus();
            return;
        }

        double buyPrice = FormatUtil.parseDouble(txtItemBuyPrice.getText(), -1);
        if (buyPrice < 0) {
            UIHelper.showWarning(this, "Please enter a valid purchase price!");
            txtItemBuyPrice.requestFocus();
            return;
        }

        PurchaseItem item = new PurchaseItem(p.getItemId(), p.getPartName(), p.getPartNumber(), qty, buyPrice);
        itemList.add(item);

        refreshTable();
        txtItemQty.setText("1");
    }

    private void onRemoveItem() {
        int selected = itemsTable.getSelectedRow();
        if (selected >= 0 && selected < itemList.size()) {
            itemList.remove(selected);
            refreshTable();
        } else {
            UIHelper.showWarning(this, "Please select an item row to remove.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        double total = 0.0;
        int sn = 1;
        for (PurchaseItem item : itemList) {
            total += item.getTotalPrice();
            tableModel.addRow(new Object[]{
                    sn++,
                    item.getProductName(),
                    item.getPartNumber() != null ? item.getPartNumber() : "-",
                    item.getQuantity(),
                    FormatUtil.formatCurrencyPlain(item.getUnitPurchasePrice()),
                    FormatUtil.formatCurrencyPlain(item.getTotalPrice())
            });
        }
        lblGrandTotal.setText("Total Purchase: " + FormatUtil.formatCurrency(total));
    }

    private void onSavePurchase() {
        if (itemList.isEmpty()) {
            UIHelper.showWarning(this, "Please add at least one item to this purchase invoice!");
            return;
        }

        Supplier s = (Supplier) cbSupplier.getSelectedItem();
        int supId = s != null ? s.getSupplierId() : 0;
        String invNo = txtInvoiceNo.getText().trim();
        if (invNo.isEmpty()) {
            UIHelper.showWarning(this, "Invoice Number is required!");
            txtInvoiceNo.requestFocus();
            return;
        }

        Purchase p = new Purchase();
        p.setInvoiceNumber(invNo);
        p.setDate(txtDate.getText().trim());
        p.setSupplierId(supId);
        p.setPaymentStatus((String) cbPaymentStatus.getSelectedItem());
        p.setNotes(txtNotes.getText().trim());
        p.setItems(itemList);

        try {
            purchaseService.recordPurchase(p);
            UIHelper.showInfo(this, "Purchase order recorded successfully!\nInventory stock and buy prices have been updated.");
            saved = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving purchase: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
