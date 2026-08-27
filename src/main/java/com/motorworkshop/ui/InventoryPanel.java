package com.motorworkshop.ui;

import com.motorworkshop.model.Category;
import com.motorworkshop.model.Product;
import com.motorworkshop.service.InventoryService;
import com.motorworkshop.ui.dialogs.ProductDialog;
import com.motorworkshop.ui.dialogs.StockAdjustDialog;
import com.motorworkshop.ui.dialogs.StockHistoryDialog;
import com.motorworkshop.util.CsvExporter;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory Management Panel with search, category/brand filters, stock indicators, adjustment, and audit log.
 */
public class InventoryPanel extends JPanel {
    private final InventoryService inventoryService;
    private final Frame parentFrame;

    private JTextField txtSearch;
    private JComboBox<Category> cbCategoryFilter;
    private JComboBox<String> cbBrandFilter;
    private JCheckBox chkLowStockOnly;
    private JCheckBox chkOutOfStockOnly;

    private JTable productTable;
    private DefaultTableModel tableModel;
    private List<Product> currentProductList = new ArrayList<>();

    private JLabel lblTotalItems;
    private JLabel lblTotalValuation;

    public InventoryPanel(InventoryService inventoryService, Frame parentFrame) {
        this.inventoryService = inventoryService;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(15, 18, 15, 18));
        setBackground(UIHelper.BG_LIGHT);

        initComponents();
        loadFilterDropdowns();
        loadData();
    }

    private void initComponents() {
        // Top Section: Title + Search & Filters
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(UIHelper.BG_LIGHT);

        // Header Title & Action Buttons
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);

        JLabel lblTitle = new JLabel("Parts & Inventory Stock Management");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.BG_LIGHT);

        JButton btnAdd = UIHelper.createSuccessButton("+ Add Item");
        btnAdd.addActionListener(e -> onAddItem());

        JButton btnEdit = UIHelper.createPrimaryButton("Edit Item");
        btnEdit.addActionListener(e -> onEditItem());

        JButton btnAdjust = UIHelper.createSecondaryButton("Adjust Stock");
        btnAdjust.addActionListener(e -> onAdjustStock());

        JButton btnHistory = UIHelper.createSecondaryButton("Stock Audit Trail");
        btnHistory.addActionListener(e -> onStockHistory());

        JButton btnDelete = UIHelper.createDangerButton("Delete");
        btnDelete.addActionListener(e -> onDeleteItem());

        JButton btnExport = UIHelper.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, productTable, "inventory_stock"));

        actionBtns.add(btnAdd);
        actionBtns.add(btnEdit);
        actionBtns.add(btnAdjust);
        actionBtns.add(btnHistory);
        actionBtns.add(btnDelete);
        actionBtns.add(btnExport);

        headerBar.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // Filter Controls Panel
        JPanel filterBar = UIHelper.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search name, part no, brand...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        cbCategoryFilter = new JComboBox<>();
        cbCategoryFilter.addActionListener(e -> loadData());

        cbBrandFilter = new JComboBox<>();
        cbBrandFilter.addActionListener(e -> loadData());

        chkLowStockOnly = new JCheckBox("Low Stock (< Min)");
        chkLowStockOnly.setBackground(UIHelper.CARD_BG);
        chkLowStockOnly.addActionListener(e -> loadData());

        chkOutOfStockOnly = new JCheckBox("Out of Stock (0)");
        chkOutOfStockOnly.setBackground(UIHelper.CARD_BG);
        chkOutOfStockOnly.addActionListener(e -> loadData());

        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtSearch);
        filterBar.add(new JLabel("Category:"));
        filterBar.add(cbCategoryFilter);
        filterBar.add(new JLabel("Brand:"));
        filterBar.add(cbBrandFilter);
        filterBar.add(chkLowStockOnly);
        filterBar.add(chkOutOfStockOnly);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"S.N.", "Part / Item Name", "Part No", "Category", "Brand", "Buy Rate", "Retail Rate", "Workshop Rate", "Current Stock", "Min Stock", "Unit", "Total Value", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        productTable = new JTable(tableModel);
        UIHelper.styleTable(productTable);

        // Custom renderer for Status column
        productTable.getColumnModel().getColumn(12).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(JLabel.CENTER);
                c.setFont(UIHelper.FONT_BOLD);
                String val = value != null ? value.toString() : "";
                if ("Out of Stock".equalsIgnoreCase(val)) {
                    c.setForeground(UIHelper.DANGER_COLOR);
                } else if ("Low Stock".equalsIgnoreCase(val)) {
                    c.setForeground(UIHelper.WARNING_COLOR);
                } else {
                    c.setForeground(UIHelper.SUCCESS_COLOR);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Summary Bar
        JPanel footerBar = UIHelper.createCard();
        footerBar.setLayout(new BorderLayout());

        lblTotalItems = new JLabel("Total Items: 0");
        lblTotalItems.setFont(UIHelper.FONT_BOLD);

        lblTotalValuation = new JLabel("Inventory Valuation: Rs. 0.00");
        lblTotalValuation.setFont(UIHelper.FONT_BIG_NUMBER);
        lblTotalValuation.setForeground(UIHelper.PRIMARY_COLOR);

        footerBar.add(lblTotalItems, BorderLayout.WEST);
        footerBar.add(lblTotalValuation, BorderLayout.EAST);
        add(footerBar, BorderLayout.SOUTH);
    }

    private void loadFilterDropdowns() {
        try {
            cbCategoryFilter.removeAllItems();
            cbCategoryFilter.addItem(new Category(0, "All Categories", ""));
            for (Category c : inventoryService.getAllCategories()) {
                cbCategoryFilter.addItem(c);
            }

            cbBrandFilter.removeAllItems();
            cbBrandFilter.addItem("All Brands");
            for (String b : inventoryService.getAllBrands()) {
                cbBrandFilter.addItem(b);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                String search = txtSearch.getText().trim();
                Category cat = (Category) cbCategoryFilter.getSelectedItem();
                Integer catId = (cat != null && cat.getCategoryId() > 0) ? cat.getCategoryId() : null;
                String brand = (String) cbBrandFilter.getSelectedItem();
                boolean lowOnly = chkLowStockOnly.isSelected();
                boolean outOnly = chkOutOfStockOnly.isSelected();

                currentProductList = inventoryService.searchProducts(search, catId, brand, lowOnly, outOnly);
                tableModel.setRowCount(0);

                double totalValuation = 0.0;
                int sn = 1;
                for (Product p : currentProductList) {
                    double val = p.getTotalStockValue();
                    totalValuation += val;

                    String status = "In Stock";
                    if (p.isOutOfStock()) status = "Out of Stock";
                    else if (p.isLowStock()) status = "Low Stock";

                    tableModel.addRow(new Object[]{
                            sn++,
                            p.getPartName(),
                            p.getPartNumber() != null ? p.getPartNumber() : "-",
                            p.getCategoryName() != null ? p.getCategoryName() : "-",
                            p.getBrand() != null ? p.getBrand() : "-",
                            FormatUtil.formatCurrencyPlain(p.getPurchasePrice()),
                            FormatUtil.formatCurrencyPlain(p.getSellingPrice()),
                            FormatUtil.formatCurrencyPlain(p.getWorkshopPrice()),
                            p.getCurrentQuantity(),
                            p.getMinStockLevel(),
                            p.getUnit(),
                            FormatUtil.formatCurrencyPlain(val),
                            status
                    });
                }

                lblTotalItems.setText("Total Parts Listed: " + currentProductList.size());
                lblTotalValuation.setText("Total Inventory Valuation: " + FormatUtil.formatCurrency(totalValuation));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Product getSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row >= 0 && row < currentProductList.size()) {
            return currentProductList.get(row);
        }
        return null;
    }

    private void onAddItem() {
        ProductDialog dlg = new ProductDialog(parentFrame, inventoryService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadFilterDropdowns();
            loadData();
        }
    }

    private void onEditItem() {
        Product p = getSelectedProduct();
        if (p == null) {
            UIHelper.showWarning(this, "Please select an item from the table to edit.");
            return;
        }
        ProductDialog dlg = new ProductDialog(parentFrame, inventoryService, p);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadFilterDropdowns();
            loadData();
        }
    }

    private void onAdjustStock() {
        Product p = getSelectedProduct();
        if (p == null) {
            UIHelper.showWarning(this, "Please select an item from the table to adjust stock.");
            return;
        }
        StockAdjustDialog dlg = new StockAdjustDialog(parentFrame, inventoryService, p);
        dlg.setVisible(true);
        if (dlg.isAdjusted()) {
            loadData();
        }
    }

    private void onStockHistory() {
        Product p = getSelectedProduct();
        StockHistoryDialog dlg = new StockHistoryDialog(parentFrame, inventoryService, p);
        dlg.setVisible(true);
    }

    private void onDeleteItem() {
        Product p = getSelectedProduct();
        if (p == null) {
            UIHelper.showWarning(this, "Please select an item from the table to delete.");
            return;
        }
        if (UIHelper.showConfirm(this, "Are you sure you want to deactivate item '" + p.getPartName() + "'?", "Confirm Deactivation")) {
            try {
                inventoryService.deleteProduct(p.getItemId());
                UIHelper.showInfo(this, "Item deactivated successfully.");
                loadData();
            } catch (Exception e) {
                UIHelper.showError(this, "Error deleting item: " + e.getMessage());
            }
        }
    }
}
