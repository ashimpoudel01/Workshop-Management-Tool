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
 * Full-featured Sales & Workshop Job Card Billing Dialog with clean GridBag alignment.
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
        super(owner, "Create New Sales / Workshop Job Card Invoice", true);
        this.saleService = saleService;
        this.inventoryService = inventoryService;
        this.pricingService = pricingService;
        this.customerService = customerService;
        this.settingService = settingService;

        initComponents();
        loadDropdownData();
        setSize(1180, 780);
        setMinimumSize(new Dimension(1020, 660));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBorder(new EmptyBorder(14, 16, 14, 16));
        contentPane.setBackground(UIHelper.BG_LIGHT);

        // 1. Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(UIHelper.BG_LIGHT);
        
        JLabel lblTitle = new JLabel("New Sales Invoice & Job Card Billing");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        topHeader.add(lblTitle, BorderLayout.WEST);

        txtInvoiceNo = new JTextField(saleService.generateNextInvoiceNumber(), 12);
        txtInvoiceNo.setEditable(false);
        txtInvoiceNo.setFont(UIHelper.FONT_BOLD);
        txtDate = new JTextField(DateUtil.today(), 10);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setBackground(UIHelper.BG_LIGHT);
        headerRight.add(new JLabel("Invoice No:"));
        headerRight.add(txtInvoiceNo);
        headerRight.add(new JLabel("Date:"));
        headerRight.add(txtDate);
        topHeader.add(headerRight, BorderLayout.EAST);

        contentPane.add(topHeader, BorderLayout.NORTH);

        // 2. Main Content Center Split (Left: Details & Items, Right: Billing Summary)
        JPanel mainCenter = new JPanel(new BorderLayout(12, 12));
        mainCenter.setBackground(UIHelper.BG_LIGHT);

        // Left Container (Vertical Flow: Customer details -> Add Items panel -> Table)
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.setBackground(UIHelper.BG_LIGHT);

        // Upper Section: Customer Details + Add Service / Parts Rows
        JPanel upperLeft = new JPanel();
        upperLeft.setLayout(new BoxLayout(upperLeft, BoxLayout.Y_AXIS));
        upperLeft.setBackground(UIHelper.BG_LIGHT);

        // A. Customer & Vehicle Box
        JPanel custBox = UIHelper.createCard();
        custBox.setLayout(new GridLayout(3, 4, 8, 8));
        custBox.setBorder(new TitledBorder("Customer & Vehicle Details"));

        cbCustomer = new JComboBox<>();
        JPanel customerSelectPanel = new JPanel(new BorderLayout(4, 0));
        customerSelectPanel.setBackground(UIHelper.CARD_BG);
        customerSelectPanel.add(cbCustomer, BorderLayout.CENTER);
        JButton btnAddCust = UIHelper.createSuccessButton("+");
        btnAddCust.setToolTipText("Add New Customer Profile");
        btnAddCust.setPreferredSize(new Dimension(36, 26));
        btnAddCust.addActionListener(e -> onAddNewCustomer());
        customerSelectPanel.add(btnAddCust, BorderLayout.EAST);

        txtCustomerName = new JTextField("Walk-in Customer", 12);
        txtCustomerPhone = new JTextField(10);
        cbVehicleType = new JComboBox<>(new String[]{"Motorcycle", "Scooter", "Moped", "Other"});
        txtVehicleBrand = new JTextField("Honda", 10);
        txtVehicleModel = new JTextField("Dio", 10);
        txtVehicleRegNo = new JTextField("Ba 00 Pa 0000", 12);

        cbCustomer.addActionListener(e -> onCustomerSelected());

        custBox.add(new JLabel("Existing Customer:"));
        custBox.add(customerSelectPanel);
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
        brandModel.add(txtVehicleModel);
        custBox.add(brandModel);

        custBox.add(new JLabel("Plate / Reg No:"));
        custBox.add(txtVehicleRegNo);

        upperLeft.add(custBox);
        upperLeft.add(Box.createRigidArea(new Dimension(0, 6)));

        // B. Add Service Row (GridBagLayout for reliable alignment)
        JPanel serviceCard = UIHelper.createCard();
        serviceCard.setLayout(new GridBagLayout());
        serviceCard.setBorder(new TitledBorder("Add Workshop Service / Labor"));

        GridBagConstraints gbcS = new GridBagConstraints();
        gbcS.insets = new Insets(4, 4, 4, 4);
        gbcS.fill = GridBagConstraints.HORIZONTAL;

        cbService = new JComboBox<>();
        JButton btnCreateService = UIHelper.createSuccessButton("+ New");
        btnCreateService.setToolTipText("Create new workshop service directly");
        btnCreateService.setPreferredSize(new Dimension(65, 30));
        btnCreateService.addActionListener(e -> onAddNewService());

        JPanel serviceSelectPanel = new JPanel(new BorderLayout(4, 0));
        serviceSelectPanel.setBackground(UIHelper.CARD_BG);
        serviceSelectPanel.add(cbService, BorderLayout.CENTER);
        serviceSelectPanel.add(btnCreateService, BorderLayout.EAST);

        txtServicePrice = new JTextField("0.00", 8);
        btnAddService = UIHelper.createPrimaryButton("+ Add Service");
        btnAddService.setPreferredSize(new Dimension(130, 32));
        btnAddService.addActionListener(e -> onAddService());

        cbService.addActionListener(e -> {
            ServiceItem s = (ServiceItem) cbService.getSelectedItem();
            if (s != null && s.getServiceId() > 0) {
                txtServicePrice.setText(String.valueOf(s.getDefaultPrice()));
            }
        });

        gbcS.gridx = 0; gbcS.gridy = 0; gbcS.weightx = 0;
        serviceCard.add(new JLabel("Service:"), gbcS);

        gbcS.gridx = 1; gbcS.gridy = 0; gbcS.weightx = 1.0;
        serviceCard.add(serviceSelectPanel, gbcS);

        gbcS.gridx = 2; gbcS.gridy = 0; gbcS.weightx = 0;
        serviceCard.add(new JLabel("Fee (Rs.):"), gbcS);

        gbcS.gridx = 3; gbcS.gridy = 0; gbcS.weightx = 0;
        serviceCard.add(txtServicePrice, gbcS);

        gbcS.gridx = 4; gbcS.gridy = 0; gbcS.weightx = 0;
        serviceCard.add(btnAddService, gbcS);

        upperLeft.add(serviceCard);
        upperLeft.add(Box.createRigidArea(new Dimension(0, 6)));

        // C. Add Part Row (GridBagLayout for reliable alignment)
        JPanel partCard = UIHelper.createCard();
        partCard.setLayout(new GridBagLayout());
        partCard.setBorder(new TitledBorder("Add Spare Part / Consumable"));

        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(4, 4, 4, 4);
        gbcP.fill = GridBagConstraints.HORIZONTAL;

        cbProduct = new JComboBox<>();
        JButton btnCreatePart = UIHelper.createSuccessButton("+ New Part");
        btnCreatePart.setToolTipText("Add new spare part directly to inventory from Job Card");
        btnCreatePart.setPreferredSize(new Dimension(95, 30));
        btnCreatePart.addActionListener(e -> onAddNewPart());

        JPanel productSelectPanel = new JPanel(new BorderLayout(4, 0));
        productSelectPanel.setBackground(UIHelper.CARD_BG);
        productSelectPanel.add(cbProduct, BorderLayout.CENTER);
        productSelectPanel.add(btnCreatePart, BorderLayout.EAST);

        txtPartQty = new JTextField("1", 4);
        txtPartPrice = new JTextField("0.00", 7);
        lblAvailableStock = new JLabel("Stock: 0 Pcs");
        lblAvailableStock.setFont(UIHelper.FONT_BOLD);
        lblAvailableStock.setForeground(UIHelper.INFO_COLOR);
        lblAvailableStock.setPreferredSize(new Dimension(110, 24));

        btnAddPart = UIHelper.createPrimaryButton("+ Add Part");
        btnAddPart.setPreferredSize(new Dimension(130, 32));
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

        gbcP.gridx = 0; gbcP.gridy = 0; gbcP.weightx = 0;
        partCard.add(new JLabel("Part:"), gbcP);

        gbcP.gridx = 1; gbcP.gridy = 0; gbcP.weightx = 1.0;
        partCard.add(productSelectPanel, gbcP);

        gbcP.gridx = 2; gbcP.gridy = 0; gbcP.weightx = 0;
        partCard.add(new JLabel("Qty:"), gbcP);

        gbcP.gridx = 3; gbcP.gridy = 0; gbcP.weightx = 0;
        partCard.add(txtPartQty, gbcP);

        gbcP.gridx = 4; gbcP.gridy = 0; gbcP.weightx = 0;
        partCard.add(new JLabel("Rate:"), gbcP);

        gbcP.gridx = 5; gbcP.gridy = 0; gbcP.weightx = 0;
        partCard.add(txtPartPrice, gbcP);

        gbcP.gridx = 6; gbcP.gridy = 0; gbcP.weightx = 0;
        partCard.add(lblAvailableStock, gbcP);

        gbcP.gridx = 7; gbcP.gridy = 0; gbcP.weightx = 0;
        partCard.add(btnAddPart, gbcP);

        upperLeft.add(partCard);

        leftPanel.add(upperLeft, BorderLayout.NORTH);

        // D. Invoice Items Table
        String[] cols = {"SN", "Type", "Description / Item Name", "Qty", "Unit Rate (Rs.)", "Total Price (Rs.)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemsTable = new JTable(tableModel);
        UIHelper.styleTable(itemsTable);
        JScrollPane scrollTable = new JScrollPane(itemsTable);

        JPanel tableCard = UIHelper.createCard();
        tableCard.setLayout(new BorderLayout(6, 6));
        tableCard.setBorder(new TitledBorder("Invoice Items"));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tableActions.setBackground(UIHelper.CARD_BG);
        JButton btnRemoveLine = UIHelper.createDangerButton("Remove Selected Item");
        btnRemoveLine.addActionListener(e -> onRemoveLineItem());
        tableActions.add(btnRemoveLine);
        tableCard.add(tableActions, BorderLayout.SOUTH);

        leftPanel.add(tableCard, BorderLayout.CENTER);
        mainCenter.add(leftPanel, BorderLayout.CENTER);

        // Right Summary Panel
        JPanel rightSummary = UIHelper.createCard();
        rightSummary.setLayout(new BorderLayout(10, 10));
        rightSummary.setPreferredSize(new Dimension(300, 500));
        rightSummary.setBorder(new TitledBorder("Payment & Billing Summary"));

        JPanel summaryForm = new JPanel(new GridLayout(8, 2, 6, 12));
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
        summaryForm.add(new JLabel("Remarks / Notes:"));
        summaryForm.add(txtNotes);

        rightSummary.add(summaryForm, BorderLayout.NORTH);

        // Action Buttons on Right
        JPanel actionButtons = new JPanel(new GridLayout(3, 1, 8, 8));
        actionButtons.setBackground(UIHelper.CARD_BG);

        JButton btnSaveAndPrint = UIHelper.createSuccessButton("Save & Print Invoice");
        btnSaveAndPrint.setPreferredSize(new Dimension(260, 38));
        btnSaveAndPrint.addActionListener(e -> onSave(true));

        JButton btnSaveOnly = UIHelper.createPrimaryButton("Save Invoice Only");
        btnSaveOnly.setPreferredSize(new Dimension(260, 36));
        btnSaveOnly.addActionListener(e -> onSave(false));

        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        actionButtons.add(btnSaveAndPrint);
        actionButtons.add(btnSaveOnly);
        actionButtons.add(btnCancel);

        rightSummary.add(actionButtons, BorderLayout.SOUTH);
        mainCenter.add(rightSummary, BorderLayout.EAST);

        contentPane.add(mainCenter, BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    private void loadDropdownData() {
        loadCustomersDropdown(0);
        loadServicesDropdown(0);
        loadProductsDropdown(0);
    }

    private void loadCustomersDropdown(int selectCustomerId) {
        try {
            cbCustomer.removeAllItems();
            List<Customer> customers = customerService.getAllCustomers();
            Customer walkIn = new Customer();
            walkIn.setCustomerId(0);
            walkIn.setName("-- Select Existing Customer --");
            cbCustomer.addItem(walkIn);
            Customer toSelect = null;
            for (Customer c : customers) {
                cbCustomer.addItem(c);
                if (selectCustomerId > 0 && c.getCustomerId() == selectCustomerId) {
                    toSelect = c;
                }
            }
            if (toSelect != null) {
                cbCustomer.setSelectedItem(toSelect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadServicesDropdown(int selectServiceId) {
        try {
            cbService.removeAllItems();
            List<ServiceItem> services = pricingService.getAllServices(true);
            ServiceItem sPlace = new ServiceItem();
            sPlace.setServiceId(0);
            sPlace.setServiceName("-- Select Workshop Service --");
            cbService.addItem(sPlace);
            ServiceItem toSelect = null;
            for (ServiceItem s : services) {
                cbService.addItem(s);
                if (selectServiceId > 0 && s.getServiceId() == selectServiceId) {
                    toSelect = s;
                }
            }
            if (toSelect != null) {
                cbService.setSelectedItem(toSelect);
                txtServicePrice.setText(String.valueOf(toSelect.getDefaultPrice()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsDropdown(int selectProductId) {
        try {
            cbProduct.removeAllItems();
            List<Product> products = inventoryService.getAllProducts();
            Product pPlace = new Product();
            pPlace.setItemId(0);
            pPlace.setPartName("-- Select Spare Part --");
            cbProduct.addItem(pPlace);
            Product toSelect = null;
            for (Product p : products) {
                cbProduct.addItem(p);
                if (selectProductId > 0 && p.getItemId() == selectProductId) {
                    toSelect = p;
                }
            }
            if (toSelect != null) {
                cbProduct.setSelectedItem(toSelect);
                txtPartPrice.setText(String.valueOf(toSelect.getSellingPrice()));
                lblAvailableStock.setText("Stock: " + toSelect.getCurrentQuantity() + " " + toSelect.getUnit());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onAddNewCustomer() {
        CustomerDialog dlg = new CustomerDialog((Frame) getOwner(), customerService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                List<Customer> list = customerService.getAllCustomers();
                if (!list.isEmpty()) {
                    loadCustomersDropdown(list.get(list.size() - 1).getCustomerId());
                } else {
                    loadCustomersDropdown(0);
                }
            } catch (Exception e) {
                loadCustomersDropdown(0);
            }
        }
    }

    private void onAddNewService() {
        ServiceDialog dlg = new ServiceDialog((Frame) getOwner(), pricingService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                List<ServiceItem> list = pricingService.getAllServices(true);
                if (!list.isEmpty()) {
                    loadServicesDropdown(list.get(list.size() - 1).getServiceId());
                } else {
                    loadServicesDropdown(0);
                }
            } catch (Exception e) {
                loadServicesDropdown(0);
            }
        }
    }

    private void onAddNewPart() {
        ProductDialog dlg = new ProductDialog((Frame) getOwner(), inventoryService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            try {
                List<Product> list = inventoryService.getAllProducts();
                if (!list.isEmpty()) {
                    Product latest = list.get(list.size() - 1);
                    loadProductsDropdown(latest.getItemId());
                } else {
                    loadProductsDropdown(0);
                }
            } catch (Exception e) {
                loadProductsDropdown(0);
            }
        }
    }

    private void onCustomerSelected() {
        Customer c = (Customer) cbCustomer.getSelectedItem();
        if (c != null && c.getCustomerId() > 0) {
            txtCustomerName.setText(c.getName());
            txtCustomerPhone.setText(c.getPhone() != null && !c.getPhone().equals("-") ? c.getPhone() : "");
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
        String phone = txtCustomerPhone.getText().trim();
        String brand = txtVehicleBrand.getText().trim();
        String model = txtVehicleModel.getText().trim();
        String regNo = txtVehicleRegNo.getText().trim();

        // Always record walk-in / newly entered customer into Customer Details table
        if (custId <= 0) {
            try {
                Customer recordedCust = customerService.findOrCreateCustomer(custName, phone, brand, model, regNo);
                if (recordedCust != null) {
                    custId = recordedCust.getCustomerId();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber(txtInvoiceNo.getText().trim());
        sale.setDate(txtDate.getText().trim());
        sale.setCustomerId(custId);
        sale.setCustomerName(custName);
        sale.setCustomerPhone(phone.isEmpty() ? "-" : phone);
        sale.setVehicleType((String) cbVehicleType.getSelectedItem());
        sale.setVehicleBrand(brand);
        sale.setVehicleModel(model);
        sale.setVehicleRegNo(regNo);
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
