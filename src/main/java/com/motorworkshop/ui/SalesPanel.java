package com.motorworkshop.ui;

import com.motorworkshop.model.Sale;
import com.motorworkshop.model.WorkshopSetting;
import com.motorworkshop.service.*;
import com.motorworkshop.ui.dialogs.InvoicePreviewDialog;
import com.motorworkshop.ui.dialogs.NewSaleDialog;
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
 * Sales Invoices, Workshop Job Cards, and Billing management panel.
 */
public class SalesPanel extends JPanel {
    private final SaleService saleService;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final CustomerService customerService;
    private final SettingService settingService;
    private final Frame parentFrame;

    private JTextField txtSearch;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JComboBox<String> cbPaymentFilter;

    private JTable salesTable;
    private DefaultTableModel tableModel;
    private List<Sale> currentSalesList = new ArrayList<>();

    private JLabel lblTotalRevenue;
    private JLabel lblTotalGrossProfit;
    private JLabel lblTotalParts;
    private JLabel lblTotalService;
    private JLabel lblTotalBills;

    public SalesPanel(SaleService saleService, InventoryService inventoryService,
                      PricingService pricingService, CustomerService customerService,
                      SettingService settingService, Frame parentFrame) {
        this.saleService = saleService;
        this.inventoryService = inventoryService;
        this.pricingService = pricingService;
        this.customerService = customerService;
        this.settingService = settingService;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(15, 18, 15, 18));
        setBackground(UIHelper.BG_LIGHT);

        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(UIHelper.BG_LIGHT);

        // Header Title & Action Buttons
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);

        JLabel lblTitle = new JLabel("Sales & Workshop Job Card Billing");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.BG_LIGHT);

        JButton btnNewSale = UIHelper.createSuccessButton("+ New Sale / Job Card");
        btnNewSale.addActionListener(e -> openNewSale());

        JButton btnPrint = UIHelper.createPrimaryButton("Print / View Invoice");
        btnPrint.addActionListener(e -> onPrintInvoice());

        JButton btnDelete = UIHelper.createDangerButton("Delete Invoice");
        btnDelete.addActionListener(e -> onDeleteInvoice());

        JButton btnExport = UIHelper.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, salesTable, "sales_report"));

        actionBtns.add(btnNewSale);
        actionBtns.add(btnPrint);
        actionBtns.add(btnDelete);
        actionBtns.add(btnExport);

        headerBar.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterBar = UIHelper.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search invoice, customer, vehicle plate...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        txtStartDate = new JTextField(DateUtil.startOfMonth(), 9);
        txtEndDate = new JTextField(DateUtil.today(), 9);
        cbPaymentFilter = new JComboBox<>(new String[]{"All Methods", "Cash", "Bank", "eSewa", "Khalti", "Other"});
        cbPaymentFilter.addActionListener(e -> loadData());

        JButton btnFilter = UIHelper.createSecondaryButton("Filter Dates");
        btnFilter.addActionListener(e -> loadData());

        JButton btnToday = UIHelper.createSecondaryButton("Today");
        btnToday.addActionListener(e -> {
            txtStartDate.setText(DateUtil.today());
            txtEndDate.setText(DateUtil.today());
            loadData();
        });

        JButton btnAllTime = UIHelper.createSecondaryButton("All Time");
        btnAllTime.addActionListener(e -> {
            txtStartDate.setText("");
            txtEndDate.setText("");
            loadData();
        });

        filterBar.add(new JLabel("Search:"));
        filterBar.add(txtSearch);
        filterBar.add(new JLabel("Payment:"));
        filterBar.add(cbPaymentFilter);
        filterBar.add(new JLabel("From:"));
        filterBar.add(txtStartDate);
        filterBar.add(new JLabel("To:"));
        filterBar.add(txtEndDate);
        filterBar.add(btnFilter);
        filterBar.add(btnToday);
        filterBar.add(btnAllTime);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Sales Table
        String[] cols = {"S.N.", "Inv #", "Date", "Customer Name", "Phone", "Vehicle", "Plate No", "Parts Total", "Service Fee", "Discount", "Total Paid", "COGS", "Gross Profit", "Payment"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        salesTable = new JTable(tableModel);
        UIHelper.styleTable(salesTable);

        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Summary
        JPanel footerBar = UIHelper.createCard();
        footerBar.setLayout(new FlowLayout(FlowLayout.RIGHT, 16, 4));

        lblTotalBills = new JLabel("Total Bills: 0");
        lblTotalBills.setFont(UIHelper.FONT_BOLD);

        lblTotalParts = new JLabel("Parts Total: Rs. 0.00");
        lblTotalParts.setFont(UIHelper.FONT_SUBHEADER);
        lblTotalParts.setForeground(UIHelper.TEXT_DARK);

        lblTotalService = new JLabel("Service Fees: Rs. 0.00");
        lblTotalService.setFont(UIHelper.FONT_SUBHEADER);
        lblTotalService.setForeground(UIHelper.TEXT_DARK);

        lblTotalRevenue = new JLabel("Total Revenue: Rs. 0.00");
        lblTotalRevenue.setFont(UIHelper.FONT_BIG_NUMBER);
        lblTotalRevenue.setForeground(UIHelper.PRIMARY_COLOR);

        lblTotalGrossProfit = new JLabel("Total Gross Profit: Rs. 0.00");
        lblTotalGrossProfit.setFont(UIHelper.FONT_BIG_NUMBER);
        lblTotalGrossProfit.setForeground(UIHelper.SUCCESS_COLOR);

        footerBar.add(lblTotalBills);
        footerBar.add(new JSeparator(JSeparator.VERTICAL));
        footerBar.add(lblTotalParts);
        footerBar.add(lblTotalService);
        footerBar.add(new JSeparator(JSeparator.VERTICAL));
        footerBar.add(lblTotalRevenue);
        footerBar.add(lblTotalGrossProfit);
        add(footerBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                String search = txtSearch.getText().trim();
                String start = txtStartDate.getText().trim();
                String end = txtEndDate.getText().trim();
                String payMethod = (String) cbPaymentFilter.getSelectedItem();

                currentSalesList = saleService.searchSales(search, start, end, payMethod);
                tableModel.setRowCount(0);

                double totalRev = 0.0;
                double totalGross = 0.0;
                double totalParts = 0.0;
                double totalService = 0.0;

                int sn = 1;
                for (Sale s : currentSalesList) {
                    totalRev += s.getTotalAmount();
                    totalGross += s.getGrossProfit();
                    totalParts += s.getPartsTotal();
                    totalService += s.getServiceCharge();

                    tableModel.addRow(new Object[]{
                            sn++,
                            s.getInvoiceNumber(),
                            s.getDate(),
                            s.getCustomerName(),
                            s.getCustomerPhone() != null ? s.getCustomerPhone() : "-",
                            (s.getVehicleBrand() != null ? s.getVehicleBrand() : "") + " " + (s.getVehicleModel() != null ? s.getVehicleModel() : ""),
                            s.getVehicleRegNo() != null ? s.getVehicleRegNo() : "-",
                            FormatUtil.formatCurrencyPlain(s.getPartsTotal()),
                            FormatUtil.formatCurrencyPlain(s.getServiceCharge()),
                            FormatUtil.formatCurrencyPlain(s.getDiscount()),
                            FormatUtil.formatCurrencyPlain(s.getTotalAmount()),
                            FormatUtil.formatCurrencyPlain(s.getTotalCogs()),
                            FormatUtil.formatCurrencyPlain(s.getGrossProfit()),
                            s.getPaymentMethod()
                    });
                }

                lblTotalBills.setText("Total Bills: " + currentSalesList.size());
                lblTotalParts.setText("Parts Total: " + FormatUtil.formatCurrency(totalParts));
                lblTotalService.setText("Service Fees: " + FormatUtil.formatCurrency(totalService));
                lblTotalRevenue.setText("Total Revenue: " + FormatUtil.formatCurrency(totalRev));
                lblTotalGrossProfit.setText("Total Gross Profit: " + FormatUtil.formatCurrency(totalGross));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void openNewSale() {
        NewSaleDialog dlg = new NewSaleDialog(parentFrame, saleService, inventoryService, pricingService, customerService, settingService);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void onPrintInvoice() {
        int row = salesTable.getSelectedRow();
        if (row < 0 || row >= currentSalesList.size()) {
            UIHelper.showWarning(this, "Please select an invoice from the table to view/print.");
            return;
        }

        Sale s = currentSalesList.get(row);
        try {
            Sale full = saleService.getSaleById(s.getSaleId());
            WorkshopSetting settings = settingService.getSettings();
            InvoicePreviewDialog preview = new InvoicePreviewDialog(parentFrame, full, settings);
            preview.setVisible(true);
        } catch (Exception e) {
            UIHelper.showError(this, "Error loading invoice details: " + e.getMessage());
        }
    }

    private void onDeleteInvoice() {
        int row = salesTable.getSelectedRow();
        if (row < 0 || row >= currentSalesList.size()) {
            UIHelper.showWarning(this, "Please select an invoice to delete.");
            return;
        }

        Sale s = currentSalesList.get(row);
        if (UIHelper.showConfirm(this,
                "Are you sure you want to delete invoice " + s.getInvoiceNumber() + "?\n" +
                "Any spare parts included in this bill will be RESTOCKED into inventory.",
                "Confirm Invoice Deletion")) {
            try {
                saleService.deleteSale(s.getSaleId());
                UIHelper.showInfo(this, "Invoice deleted and inventory stock restored.");
                loadData();
            } catch (Exception e) {
                UIHelper.showError(this, "Error deleting invoice: " + e.getMessage());
            }
        }
    }
}
