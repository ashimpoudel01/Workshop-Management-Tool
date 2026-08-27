package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Category;
import com.motorworkshop.model.Product;
import com.motorworkshop.model.Supplier;
import com.motorworkshop.service.InventoryService;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog to Add or Edit Inventory Items / Spare Parts.
 */
public class ProductDialog extends JDialog {
    private final InventoryService inventoryService;
    private final Product productToEdit;
    private boolean saved = false;

    private JTextField txtPartName;
    private JTextField txtPartNumber;
    private JComboBox<Category> cbCategory;
    private JTextField txtBrand;
    private JComboBox<Supplier> cbSupplier;
    private JTextField txtPurchasePrice;
    private JTextField txtSellingPrice;
    private JTextField txtWorkshopPrice;
    private JTextField txtQuantity;
    private JTextField txtMinStock;
    private JComboBox<String> cbUnit;

    public ProductDialog(Frame owner, InventoryService inventoryService, Product productToEdit) {
        super(owner, productToEdit == null ? "Add New Spare Part / Item" : "Edit Part Details", true);
        this.inventoryService = inventoryService;
        this.productToEdit = productToEdit;

        initComponents();
        populateData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(18, 20, 18, 20));
        contentPane.setBackground(UIHelper.CARD_BG);

        // Header Title
        JLabel lblHeader = new JLabel(productToEdit == null ? "Add New Inventory Item" : "Edit Item Details");
        lblHeader.setFont(UIHelper.FONT_HEADER);
        lblHeader.setForeground(UIHelper.PRIMARY_COLOR);
        contentPane.add(lblHeader, BorderLayout.NORTH);

        // Form fields in 2 columns
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIHelper.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtPartName = new JTextField(20);
        txtPartNumber = new JTextField(15);
        cbCategory = new JComboBox<>();
        txtBrand = new JTextField(15);
        cbSupplier = new JComboBox<>();
        txtPurchasePrice = new JTextField("0.00", 10);
        txtSellingPrice = new JTextField("0.00", 10);
        txtWorkshopPrice = new JTextField("0.00", 10);
        txtQuantity = new JTextField("0", 8);
        txtMinStock = new JTextField("5", 8);
        cbUnit = new JComboBox<>(new String[]{"Pcs", "Litre", "Set", "Pair", "Box", "Meter", "Kg"});

        // Load Categories & Suppliers
        loadCategories();
        loadSuppliers(0);

        JPanel supplierBox = new JPanel(new BorderLayout(4, 0));
        supplierBox.setBackground(UIHelper.CARD_BG);
        supplierBox.add(cbSupplier, BorderLayout.CENTER);
        JButton btnAddSup = UIHelper.createSuccessButton("+");
        btnAddSup.setToolTipText("Add New Vendor / Supplier");
        btnAddSup.setPreferredSize(new Dimension(36, 26));
        btnAddSup.addActionListener(e -> onAddNewSupplier());
        supplierBox.add(btnAddSup, BorderLayout.EAST);

        // Row 0
        int row = 0;
        addFormField(formPanel, gbc, "Part / Item Name *:", txtPartName, 0, row, 2);
        
        row++;
        addFormField(formPanel, gbc, "Part / OEM Number:", txtPartNumber, 0, row, 1);
        addFormField(formPanel, gbc, "Brand / Maker:", txtBrand, 1, row, 1);

        row++;
        addFormField(formPanel, gbc, "Category:", cbCategory, 0, row, 1);
        addFormField(formPanel, gbc, "Primary Supplier:", supplierBox, 1, row, 1);

        row++;
        addFormField(formPanel, gbc, "Purchase Price (Rs.) *:", txtPurchasePrice, 0, row, 1);
        addFormField(formPanel, gbc, "Selling Price (Retail Rs.) *:", txtSellingPrice, 1, row, 1);

        row++;
        addFormField(formPanel, gbc, "Workshop Price (Rs.):", txtWorkshopPrice, 0, row, 1);
        addFormField(formPanel, gbc, "Unit of Measure:", cbUnit, 1, row, 1);

        row++;
        addFormField(formPanel, gbc, "Initial Stock Quantity:", txtQuantity, 0, row, 1);
        addFormField(formPanel, gbc, "Min Stock Level (Alert):", txtMinStock, 1, row, 1);

        if (productToEdit != null) {
            txtQuantity.setEditable(false);
            txtQuantity.setToolTipText("Use 'Stock Adjustment' on the inventory table to modify stock");
        }

        contentPane.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIHelper.CARD_BG);

        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UIHelper.createPrimaryButton(productToEdit == null ? "Save Part" : "Update Part");
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        contentPane.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int gridX, int gridY, int gridWidth) {
        gbc.gridx = gridX;
        gbc.gridy = gridY;
        gbc.gridwidth = gridWidth;

        JPanel wrapper = new JPanel(new BorderLayout(4, 4));
        wrapper.setBackground(UIHelper.CARD_BG);
        JLabel label = new JLabel(labelText);
        label.setFont(UIHelper.FONT_BOLD);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);

        panel.add(wrapper, gbc);
    }

    private void loadCategories() {
        try {
            cbCategory.removeAllItems();
            List<Category> categories = inventoryService.getAllCategories();
            cbCategory.addItem(new Category(0, "-- Select Category --", ""));
            for (Category c : categories) cbCategory.addItem(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSuppliers(int selectSupplierId) {
        try {
            cbSupplier.removeAllItems();
            List<Supplier> suppliers = inventoryService.getAllSuppliers();
            cbSupplier.addItem(new Supplier(0, "-- Select Supplier --", "", "", "", ""));
            Supplier toSelect = null;
            for (Supplier s : suppliers) {
                cbSupplier.addItem(s);
                if (selectSupplierId > 0 && s.getSupplierId() == selectSupplierId) {
                    toSelect = s;
                }
            }
            if (toSelect != null) {
                cbSupplier.setSelectedItem(toSelect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onAddNewSupplier() {
        com.motorworkshop.service.PurchaseService ps = new com.motorworkshop.service.PurchaseService();
        SupplierDialog dlg = new SupplierDialog(this, ps, null);
        dlg.setVisible(true);
        if (dlg.isSaved() && dlg.getSavedSupplier() != null) {
            loadSuppliers(dlg.getSavedSupplier().getSupplierId());
        }
    }

    private void populateData() {
        if (productToEdit != null) {
            txtPartName.setText(productToEdit.getPartName());
            txtPartNumber.setText(productToEdit.getPartNumber());
            txtBrand.setText(productToEdit.getBrand());
            txtPurchasePrice.setText(String.valueOf(productToEdit.getPurchasePrice()));
            txtSellingPrice.setText(String.valueOf(productToEdit.getSellingPrice()));
            txtWorkshopPrice.setText(String.valueOf(productToEdit.getWorkshopPrice()));
            txtQuantity.setText(String.valueOf(productToEdit.getCurrentQuantity()));
            txtMinStock.setText(String.valueOf(productToEdit.getMinStockLevel()));
            cbUnit.setSelectedItem(productToEdit.getUnit());

            // Select Category
            for (int i = 0; i < cbCategory.getItemCount(); i++) {
                Category c = cbCategory.getItemAt(i);
                if (c != null && c.getCategoryId() == productToEdit.getCategoryId()) {
                    cbCategory.setSelectedIndex(i);
                    break;
                }
            }

            // Select Supplier
            for (int i = 0; i < cbSupplier.getItemCount(); i++) {
                Supplier s = cbSupplier.getItemAt(i);
                if (s != null && s.getSupplierId() == productToEdit.getSupplierId()) {
                    cbSupplier.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void onSave() {
        String name = txtPartName.getText().trim();
        if (name.isEmpty()) {
            UIHelper.showWarning(this, "Part Name cannot be empty!");
            txtPartName.requestFocus();
            return;
        }

        double buyPrice = FormatUtil.parseDouble(txtPurchasePrice.getText(), -1);
        double sellPrice = FormatUtil.parseDouble(txtSellingPrice.getText(), -1);
        double wsPrice = FormatUtil.parseDouble(txtWorkshopPrice.getText(), sellPrice);
        int qty = FormatUtil.parseInt(txtQuantity.getText(), 0);
        int minStock = FormatUtil.parseInt(txtMinStock.getText(), 5);

        if (buyPrice < 0) {
            UIHelper.showWarning(this, "Please enter a valid Purchase Price!");
            txtPurchasePrice.requestFocus();
            return;
        }
        if (sellPrice < 0) {
            UIHelper.showWarning(this, "Please enter a valid Selling Price!");
            txtSellingPrice.requestFocus();
            return;
        }

        Category selectedCat = (Category) cbCategory.getSelectedItem();
        int catId = selectedCat != null ? selectedCat.getCategoryId() : 0;

        Supplier selectedSup = (Supplier) cbSupplier.getSelectedItem();
        int supId = selectedSup != null ? selectedSup.getSupplierId() : 0;

        try {
            if (productToEdit == null) {
                Product p = new Product();
                p.setPartName(name);
                p.setPartNumber(txtPartNumber.getText().trim());
                p.setCategoryId(catId);
                p.setBrand(txtBrand.getText().trim());
                p.setSupplierId(supId);
                p.setPurchasePrice(buyPrice);
                p.setSellingPrice(sellPrice);
                p.setWorkshopPrice(wsPrice > 0 ? wsPrice : sellPrice);
                p.setCurrentQuantity(qty);
                p.setMinStockLevel(minStock);
                p.setUnit((String) cbUnit.getSelectedItem());
                p.setActive(true);

                inventoryService.addProduct(p);
                UIHelper.showInfo(this, "Item successfully added to inventory!");
            } else {
                productToEdit.setPartName(name);
                productToEdit.setPartNumber(txtPartNumber.getText().trim());
                productToEdit.setCategoryId(catId);
                productToEdit.setBrand(txtBrand.getText().trim());
                productToEdit.setSupplierId(supId);
                productToEdit.setPurchasePrice(buyPrice);
                productToEdit.setSellingPrice(sellPrice);
                productToEdit.setWorkshopPrice(wsPrice > 0 ? wsPrice : sellPrice);
                productToEdit.setMinStockLevel(minStock);
                productToEdit.setUnit((String) cbUnit.getSelectedItem());

                inventoryService.updateProduct(productToEdit);
                UIHelper.showInfo(this, "Item successfully updated!");
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving product: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
