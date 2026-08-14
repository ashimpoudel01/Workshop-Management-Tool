package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.*;
import com.motorworkshop.service.CustomerService;
import com.motorworkshop.service.InventoryService;
import com.motorworkshop.service.PricingService;
import com.motorworkshop.service.SaleService;
import com.motorworkshop.service.SettingService;
import com.motorworkshop.util.DateUtil;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Full-featured Sales & Workshop Job Card Billing Dialog.
 */
public class NewSaleDialog extends JDialog {
    private final SaleService saleService;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final CustomerService customerService;
    private final SettingService settingService;

    private boolean saved = false;
    private Sale createdSale = null;

    // Customer & Vehicle fields
    private JComboBox<Customer> cbCustomer;
    private JTextField txtCustomerName;
    private JTextField txtCustomerPhone;
    private JComboBox<String> cbVehicleType;
    private JTextField txtVehicleBrand;
    private JTextField txtVehicleModel;
    private JTextField txtVehicleRegNo;

    // Invoice Header
    private JTextField txtInvoiceNo;
    private JTextField txtDate;

    // Service selection
    private JComboBox<ServiceItem> cbService;
    private JTextField txtServicePrice;
    private JButton btnAddService;

    // Part selection
    private JComboBox<Product> cbProduct;
    private JTextField txtPartQty;
    private JTextField txtPartPrice;
    private JLabel lblAvailableStock;
    private JButton btnAddPart;

    // Line Items Table
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private final List<SaleItem> itemList = new ArrayList<>();

    // Summary & Totals
    private JLabel lblPartsTotal;
    private JLabel lblServiceTotal;
    private JLabel lblSubtotal;
    private JTextField txtDiscount;
    private JLabel lblGrandTotal;
    private JLabel lblProfitInfo;
    private JComboBox<String> cbPaymentMethod;
    private JTextField txtNotes;

    public NewSaleDialog(Frame owner, SaleService saleService, InventoryService inventoryService,
                         PricingService pricingService, CustomerService customerService,
                         SettingService settingService) {
        super(owner, "Create New Sales / Workshop Invoice", true);
        this.saleService = saleService;
        this.inventoryService = inventoryService;
        this.pricingService = pricingService;
        this.customerService = customerService;
        this.settingService = settingService;

        initComponents();
        loadDropdownData();
        setSize(1000, 720);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(12, 16, 12, 16));
        contentPane.setBackground(UIHelper.CARD_BG);

        // Header Title
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UIHelper.CARD_BG);
        JLabel lblTitle = new JLabel("New Sales Invoice & Workshop Job Card");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        topHeader.add(lblTitle, BorderLayout.WEST);

        txtInvoiceNo = new JTextField(saleService.generateNextInvoiceNumber(), 12);
        txtInvoiceNo.setEditable(false);
        txtDate = new JTextField(DateUtil.today(), 10);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        headerRight.setBackground(UIHelper.CARD_BG);
        headerRight.add(new JLabel("Invoice No:"));
        headerRight.add(txtInvoiceNo);
        headerRight.add(new JLabel("Date:"));
        headerRight.add(txtDate);
        topHeader.add(headerRight, BorderLayout.EAST);

        contentPane.add(topHeader, BorderLayout.NORTH);

        // Main Center Panel (Split into Left: Customer & Items, Right: Summary & Calculation)
        JPanel centerGrid = new JPanel(new BorderLayout(10, 10));
        centerGrid.setBackground(UIHelper.CARD_BG);

        // Left Container
        JPanel leftContainer = new JPanel(new BorderLayout(8, 8));
        leftContainer.setBackground(UIHelper.CARD_BG);

        // 1. Customer & Vehicle Box
        JPanel custBox = new JPanel(new GridLayout(3, 4, 6, 6));
        custBox.setBackground(UIHelper.CARD_BG);
        custBox.setBorder(new TitledBorder("Customer & Vehicle Details"));

        cbCustomer = new JComboBox<>();
        txtCustomerName = new JTextField("Walk-in Customer", 12);
        txtCustomerPhone = new JTextField(10);
        cbVehicleType = new JComboBox<>(new String[]{"Motorcycle", "Scooter", "Moped", "Other"});
        txtVehicleBrand = new JTextField("Honda", 10);
        txtVehicleModel = new JTextField("Dio", 10);
        txtVehicleRegNo = new JTextField("Ba 00 Pa 0000", 12);

        cbCustomer.addActionListener(e -> onCustomerSelected());

        custBox.add(new JLabel("Existing Customer:"));
        custBox.add(cbCustomer);
        custBox.add(new JLabel("Customer Name *:"));
        custBox.add(txtCustomerName);

        custBox.add(new JLabel("Contact Phone:"));
        custBox.add(txtCustomerPhone);
        custBox.add(new JLabel("Vehicle Type:"));
        custBox.add(cbVehicleType);

        custBox.add(new JLabel("Brand & Model:"));
        JPanel brandModel = new JPanel(new GridLayout(1, 2, 4, 4));
        brandModel.setBackground(UIHelper.CARD_BG);
        brandModel.add(txtVehicleBrand);
        brandModel.add(txtModelField());
        custBox.add(brandModel);

        custBox.add(new JLabel("Vehicle Plate No:"));
        custBox.add(txtVehicleRegNo);

        leftContainer.add(custBox, BorderLayout.NORTH);

        // 2. Add Service & Parts Bar
        JPanel addItemsPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        addItemsPanel.setBackground(UIHelper.CARD_BG);

        // Service row
        JPanel serviceRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        serviceRow.setBackground(UIHelper.CARD_BG);
        serviceRow.setBorder(new TitledBorder("Add Workshop Service"));
        cbService = new JComboBox<>();
        cbService.setPreferredSize(new Dimension(280, 26));
        txtServicePrice = new JTextField("0.00", 8);
        btnAddService = UIHelper.createPrimaryButton("+ Add Service");
        btnAddService.addActionListener(e -> onAddService());

        cbService.addActionListener(e -> {
            ServiceItem s = (ServiceItem) cbService.getSelectedItem();
            if (s != null && s.getServiceId() > 0) {
                txtServicePrice.setText(String.valueOf(s.getDefaultPrice()));
            }
        });

        serviceRow.add(new JLabel("Service:"));
        serviceRow.add(cbService);
        serviceRow.add(new JLabel("Charge (Rs.):"));
        serviceRow.add(txtServicePrice);
        serviceRow.add(btnAddService);
        addItemsPanel.add(serviceRow);

        // Part row
        JPanel partRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        partRow.setBackground(UIHelper.CARD_BG);
        partRow.setBorder(new TitledBorder("Add Spare Part / Consumable"));
        cbProduct = new JComboBox<>();
        cbProduct.setPreferredSize(new Dimension(260, 26));
        txtPartQty = new JTextField("1", 4);
        txtPartPrice = new JTextField("0.00", 7);
        lblAvailableStock = new JLabel("Stock: 0");
        lblAvailableStock.setFont(UIHelper.FONT_BOLD);
        lblAvailableStock.setForeground(UIHelper.INFO_COLOR);
        btnAddPart = UIHelper.createPrimaryButton("+ Add Part");
        btnAddPart.addActionListener(e -> onAddPart());

        cbProduct.addActionListener(e -> {
            Product p = (Product) cbProduct.getSelectedItem();
            if (p != null && p.getItemId() > 0) {
                txtPartPrice.setText(String.valueOf(p.getSellingPrice()));
                lblAvailableStock.setText("Stock: " + p.getCurrentQuantity() + " " + p.getUnit());
                if (p.getCurrentQuantity() <= 0) {
                    lblAvailableStock.setForeground(UIHelper.DANGER_COLOR);
                } else if (p.isLowStock()) {
                    lblAvailableStock.setForeground(UIHelper.WARNING_COLOR);
                } else {
                    lblAvailableStock.setForeground(UIHelper.SUCCESS_COLOR);
                }
            }
        });

        partRow.add(new JLabel("Part:"));
        partRow.add(cbProduct);
        partRow.add(new JLabel("Qty:"));
        partRow.add(txtPartQty);
        partRow.add(new JLabel("Rate:"));
        partRow.add(txtPartPrice);
        partRow.add(lblAvailableStock);
        partRow.add(btnAddPart);
        addItemsPanel.add(partRow);

        leftContainer.add(addItemsPanel, BorderLayout.CENTER);

        // 3. Invoice Table
        String[] cols = {"SN", "Type", "Description", "Qty", "Rate (Rs.)", "Total (Rs.)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(tableModel);
        UIHelper.styleTable(itemsTable);
        JScrollPane scrollTable = new JScrollPane(itemsTable);
        scrollTable.setPreferredSize(new Dimension(550, 180));

        JPanel tableWrapper = new JPanel(new BorderLayout(4, 4));
        tableWrapper.setBackground(UIHelper.CARD_BG);
        tableWrapper.add(scrollTable, BorderLayout.CENTER);

        JButton btnRemoveLine = UIHelper.createDangerButton("Remove Item");
        btnRemoveLine.addActionListener(e -> onRemoveLineItem());
        tableWrapper.add(btnRemoveLine, BorderLayout.WEST);

        // Add tableWrapper to bottom of left container
        JPanel leftCombined = new JPanel(new BorderLayout(6, 6));
        leftCombined.setBackground(UIHelper.CARD_BG);
        leftCombined.add(leftContainer, BorderLayout.NORTH);
        leftCombined.add(tableWrapper, BorderLayout.CENTER);

        centerGrid.add(leftCombined, BorderLayout.CENTER);

        // Right Summary Panel
        JPanel rightSummary = new JPanel(new BorderLayout(10, 10));
        rightSummary.setPreferredSize(new Dimension(280, 400));
        rightSummary.setBackground(UIHelper.CARD_BG);
        rightSummary.setBorder(new TitledBorder("Payment & Billing Summary"));

        JPanel summaryForm = new JPanel(new GridLayout(8, 2, 6, 8));
        summaryForm.setBackground(UIHelper.CARD_BG);

        lblPartsTotal = new JLabel("Rs. 0.00");
        lblServiceTotal = new JLabel("Rs. 0.00");
        lblSubtotal = new JLabel("Rs. 0.00");
        txtDiscount = new JTextField("0.00", 8);
        lblGrandTotal = new JLabel("Rs. 0.00");
        lblGrandTotal.setFont(UIHelper.FONT_BIG_NUMBER);
        lblGrandTotal.setForeground(UIHelper.PRIMARY_COLOR);

        lblProfitInfo = new JLabel("Gross Profit: Rs. 0.00");
        lblProfitInfo.setFont(UIHelper.FONT_BOLD);
        lblProfitInfo.setForeground(UIHelper.SUCCESS_COLOR);

        cbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Bank", "eSewa", "Khalti", "Other"});
        txtNotes = new JTextField(12);

        txtDiscount.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalculate(); }
            public void removeUpdate(DocumentEvent e) { recalculate(); }
            public void changedUpdate(DocumentEvent e) { recalculate(); }
        });

        summaryForm.add(new JLabel("Parts Total:"));
        summaryForm.add(lblPartsTotal);
        summaryForm.add(new JLabel("Service Total:"));
        summaryForm.add(lblServiceTotal);
        summaryForm.add(new JLabel("Subtotal:"));
        summaryForm.add(lblSubtotal);
        summaryForm.add(new JLabel("Discount (Rs.):"));
        summaryForm.add(txtDiscount);
        summaryForm.add(new JLabel("Net Payable:"));
        summaryForm.add(lblGrandTotal);
        summaryForm.add(new JLabel("Payment Mode:"));
        summaryForm.add(cbPaymentMethod);
        summaryForm.add(new JLabel("Est. Profit:"));
        summaryForm.add(lblProfitInfo);
        summaryForm.add(new JLabel("Remarks:"));
        summaryForm.add(txtNotes);

        rightSummary.add(summaryForm, BorderLayout.NORTH);

        // Action Buttons on Right
        JPanel actionButtons = new JPanel(new GridLayout(3, 1, 6, 6));
        actionButtons.setBackground(UIHelper.CARD_BG);

        JButton btnSaveAndPrint = UIHelper.createSuccessButton("Save & Print Invoice");
        btnSaveAndPrint.addActionListener(e -> onSave(true));

        JButton btnSaveOnly = UIHelper.createPrimaryButton("Save Invoice Only");
        btnSaveOnly.addActionListener(e -> onSave(false));

        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        actionButtons.add(btnSaveAndPrint);
        actionButtons.add(btnSaveOnly);
        actionButtons.add(btnCancel);

        rightSummary.add(actionButtons, BorderLayout.SOUTH);

        centerGrid.add(rightSummary, BorderLayout.EAST);
        contentPane.add(centerGrid, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    private JTextField txtModelField() {
        return txtVehicleModel;
    }

    private void loadDropdownData() {
        try {
            // Load Customers
            List<Customer> customers = customerService.getAllCustomers();
            Customer walkIn = new Customer();
            walkIn.setCustomerId(0);
            walkIn.setName("-- Select Existing Customer --");
            cbCustomer.addItem(walkIn);
            for (Customer c : customers) cbCustomer.addItem(c);

            // Load Services
            List<ServiceItem> services = pricingService.getAllServices(true);
            ServiceItem sPlace = new ServiceItem();
            sPlace.setServiceId(0);
            sPlace.setServiceName("-- Select Workshop Service --");
            cbService.addItem(sPlace);
            for (ServiceItem s : services) cbService.addItem(s);

            // Load Products
            List<Product> products = inventoryService.getAllProducts();
            Product pPlace = new Product();
            pPlace.setItemId(0);
            pPlace.setPartName("-- Select Spare Part --");
            cbProduct.addItem(pPlace);
            for (Product p : products) cbProduct.addItem(p);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onCustomerSelected() {
        Customer c = (Customer) cbCustomer.getSelectedItem();
        if (c != null && c.getCustomerId() > 0) {
            txtCustomerName.setText(c.getName());
            txtCustomerPhone.setText(c.getPhone() != null ? c.getPhone() : "");
            if (c.getVehicleBrand() != null && !c.getVehicleBrand().isEmpty()) txtVehicleBrand.setText(c.getVehicleBrand());
            if (c.getVehicleModel() != null && !c.getVehicleModel().isEmpty()) txtVehicleModel.setText(c.getVehicleModel());
            if (c.getVehicleNumber() != null && !c.getVehicleNumber().isEmpty()) txtVehicleRegNo.setText(c.getVehicleNumber());
        }
    }

    private void onAddService() {
        ServiceItem s = (ServiceItem) cbService.getSelectedItem();
        if (s == null || s.getServiceId() <= 0) {
            UIHelper.showWarning(this, "Please select a valid service!");
            return;
        }

        double price = FormatUtil.parseDouble(txtServicePrice.getText(), -1);
        if (price < 0) {
            UIHelper.showWarning(this, "Please enter a valid service rate!");
            txtServicePrice.requestFocus();
            return;
        }

        SaleItem item = new SaleItem(SaleItem.ItemType.SERVICE, s.getServiceId(), s.getServiceName(), 1, price, 0.0);
        itemList.add(item);
        refreshTable();
    }

    private void onAddPart() {
        Product p = (Product) cbProduct.getSelectedItem();
        if (p == null || p.getItemId() <= 0) {
            UIHelper.showWarning(this, "Please select a valid spare part!");
            return;
        }

        int qty = FormatUtil.parseInt(txtPartQty.getText(), 0);
        if (qty <= 0) {
            UIHelper.showWarning(this, "Quantity must be greater than 0!");
            txtPartQty.requestFocus();
            return;
        }

        WorkshopSetting settings = settingService.getSettings();
        if (!settings.isAllowNegativeStock() && p.getCurrentQuantity() < qty) {
            UIHelper.showWarning(this, "Insufficient stock! Available: " + p.getCurrentQuantity() + ", Requested: " + qty);
            return;
        }

        double price = FormatUtil.parseDouble(txtPartPrice.getText(), -1);
        if (price < 0) {
            UIHelper.showWarning(this, "Please enter a valid selling price!");
            txtPartPrice.requestFocus();
            return;
        }

        SaleItem item = new SaleItem(SaleItem.ItemType.PART, p.getItemId(), p.getPartName(), qty, price, p.getPurchasePrice());
        itemList.add(item);
        refreshTable();
        txtPartQty.setText("1");
    }

    private void onRemoveLineItem() {
        int selected = itemsTable.getSelectedRow();
        if (selected >= 0 && selected < itemList.size()) {
            itemList.remove(selected);
            refreshTable();
        } else {
            UIHelper.showWarning(this, "Please select an item to remove.");
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        int sn = 1;
        for (SaleItem item : itemList) {
            tableModel.addRow(new Object[]{
                    sn++,
                    item.getItemType() == SaleItem.ItemType.PART ? "Part" : "Service",
                    item.getItemName(),
                    item.getQuantity(),
                    FormatUtil.formatCurrencyPlain(item.getUnitPrice()),
                    FormatUtil.formatCurrencyPlain(item.getTotalPrice())
            });
        }
        recalculate();
    }

    private void recalculate() {
        double partsTotal = 0.0;
        double serviceTotal = 0.0;
        double cogsTotal = 0.0;

        for (SaleItem item : itemList) {
            if (item.getItemType() == SaleItem.ItemType.PART) {
                partsTotal += item.getTotalPrice();
                cogsTotal += item.getTotalCost();
            } else {
                serviceTotal += item.getTotalPrice();
            }
        }

        double subtotal = partsTotal + serviceTotal;
        double discount = FormatUtil.parseDouble(txtDiscount.getText(), 0.0);
        double total = Math.max(0.0, subtotal - discount);
        double grossProfit = total - cogsTotal;

        lblPartsTotal.setText(FormatUtil.formatCurrency(partsTotal));
        lblServiceTotal.setText(FormatUtil.formatCurrency(serviceTotal));
        lblSubtotal.setText(FormatUtil.formatCurrency(subtotal));
        lblGrandTotal.setText(FormatUtil.formatCurrency(total));
        lblProfitInfo.setText("Gross Profit: " + FormatUtil.formatCurrency(grossProfit));
    }

    private void onSave(boolean openPrintPreview) {
        if (itemList.isEmpty()) {
            UIHelper.showWarning(this, "Please add at least one part or service to create an invoice!");
            return;
        }

        String custName = txtCustomerName.getText().trim();
        if (custName.isEmpty()) custName = "Walk-in Customer";

        Customer selectedCust = (Customer) cbCustomer.getSelectedItem();
        int custId = selectedCust != null ? selectedCust.getCustomerId() : 0;

        Sale sale = new Sale();
        sale.setInvoiceNumber(txtInvoiceNo.getText().trim());
        sale.setDate(txtDate.getText().trim());
        sale.setCustomerId(custId);
        sale.setCustomerName(custName);
        sale.setCustomerPhone(txtCustomerPhone.getText().trim());
        sale.setVehicleType((String) cbVehicleType.getSelectedItem());
        sale.setVehicleBrand(txtVehicleBrand.getText().trim());
        sale.setVehicleModel(txtVehicleModel.getText().trim());
        sale.setVehicleRegNo(txtVehicleRegNo.getText().trim());
        sale.setPaymentMethod((String) cbPaymentMethod.getSelectedItem());
        sale.setNotes(txtNotes.getText().trim());
        sale.setDiscount(FormatUtil.parseDouble(txtDiscount.getText(), 0.0));
        sale.setItems(itemList);

        try {
            saleService.createSale(sale);
            saved = true;
            createdSale = sale;

            if (openPrintPreview) {
                WorkshopSetting settings = settingService.getSettings();
                InvoicePreviewDialog preview = new InvoicePreviewDialog((Frame) getOwner(), sale, settings);
                preview.setVisible(true);
            } else {
                UIHelper.showInfo(this, "Invoice " + sale.getInvoiceNumber() + " successfully generated and saved!");
            }

            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Failed to create invoice: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Sale getCreatedSale() {
        return createdSale;
    }
}
