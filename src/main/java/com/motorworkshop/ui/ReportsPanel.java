package com.motorworkshop.ui;

import com.motorworkshop.model.DashboardStats;
import com.motorworkshop.model.Expense;
import com.motorworkshop.model.Purchase;
import com.motorworkshop.model.ReportData;
import com.motorworkshop.model.Sale;
import com.motorworkshop.service.ReportService;
import com.motorworkshop.util.CsvExporter;
import com.motorworkshop.util.DateUtil;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Comprehensive Financial & Operational Reporting panel with accurate P&L and Profit distinction.
 */
public class ReportsPanel extends JPanel {
    private final ReportService reportService;

    private JTextField txtStartDate;
    private JTextField txtEndDate;

    // KPI Summary Labels
    private JLabel lblTotalSales;
    private JLabel lblPartsRev;
    private JLabel lblServiceRev;
    private JLabel lblDiscounts;
    private JLabel lblCogs;
    private JLabel lblGrossProfit;
    private JLabel lblExpenses;
    private JLabel lblNetProfit;
    private JLabel lblPurchases;
    private JLabel lblInvoicesCount;

    // Tables
    private JTabbedPane tabbedPane;
    private JTable pnlTable;
    private DefaultTableModel pnlModel;

    private JTable salesTable;
    private DefaultTableModel salesModel;

    private JTable purchasesTable;
    private DefaultTableModel purchasesModel;

    private JTable expensesTable;
    private DefaultTableModel expensesModel;

    private JTable topPartsTable;
    private DefaultTableModel topPartsModel;

    private JTable topServicesTable;
    private DefaultTableModel topServicesModel;

    private ReportData currentReportData;
    private ReportService.FilterPreset currentPreset = ReportService.FilterPreset.THIS_MONTH;

    public ReportsPanel(ReportService reportService) {
        this.reportService = reportService;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 18, 15, 18));
        setBackground(UIHelper.BG_LIGHT);

        initComponents();
        loadReport(ReportService.FilterPreset.THIS_MONTH);
    }

    private void initComponents() {
        // Top Filter Bar
        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setBackground(UIHelper.BG_LIGHT);

        // Header Title
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);
        JLabel lblTitle = new JLabel("Workshop Financial & Performance Reports");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        headerRight.setBackground(UIHelper.BG_LIGHT);

        JButton btnRefresh = UIHelper.createSecondaryButton("Refresh Report");
        btnRefresh.addActionListener(e -> refreshData());

        JButton btnExport = UIHelper.createSecondaryButton("Export Active Tab CSV");
        btnExport.addActionListener(e -> onExportCurrentTab());

        headerRight.add(btnRefresh);
        headerRight.add(btnExport);
        headerBar.add(headerRight, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // Filter Bar with presets
        JPanel filterBar = UIHelper.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

        JButton btnToday = UIHelper.createSecondaryButton("Today");
        btnToday.addActionListener(e -> loadReport(ReportService.FilterPreset.TODAY));

        JButton btnYesterday = UIHelper.createSecondaryButton("Yesterday");
        btnYesterday.addActionListener(e -> loadReport(ReportService.FilterPreset.YESTERDAY));

        JButton btnThisWeek = UIHelper.createSecondaryButton("This Week");
        btnThisWeek.addActionListener(e -> loadReport(ReportService.FilterPreset.THIS_WEEK));

        JButton btnThisMonth = UIHelper.createSecondaryButton("This Month");
        btnThisMonth.addActionListener(e -> loadReport(ReportService.FilterPreset.THIS_MONTH));

        JButton btnAllTime = UIHelper.createSecondaryButton("All Time");
        btnAllTime.addActionListener(e -> loadReport(ReportService.FilterPreset.ALL_TIME));

        txtStartDate = new JTextField(DateUtil.startOfMonth(), 9);
        txtEndDate = new JTextField(DateUtil.today(), 9);
        JButton btnCustom = UIHelper.createPrimaryButton("Apply Range");
        btnCustom.addActionListener(e -> loadReport(ReportService.FilterPreset.CUSTOM));

        filterBar.add(new JLabel("Quick Ranges:"));
        filterBar.add(btnToday);
        filterBar.add(btnYesterday);
        filterBar.add(btnThisWeek);
        filterBar.add(btnThisMonth);
        filterBar.add(btnAllTime);
        filterBar.add(new JSeparator(JSeparator.VERTICAL));
        filterBar.add(new JLabel("From:"));
        filterBar.add(txtStartDate);
        filterBar.add(new JLabel("To:"));
        filterBar.add(txtEndDate);
        filterBar.add(btnCustom);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center Content: KPI Summary Cards + Sub-tabs
        JPanel centerContent = new JPanel(new BorderLayout(10, 10));
        centerContent.setBackground(UIHelper.BG_LIGHT);

        // 1. KPI Cards Row
        JPanel summaryGrid = new JPanel(new GridLayout(2, 5, 10, 10));
        summaryGrid.setBackground(UIHelper.BG_LIGHT);
        summaryGrid.setPreferredSize(new Dimension(Integer.MAX_VALUE, 160));

        lblTotalSales = new JLabel("Rs. 0.00");
        lblPartsRev = new JLabel("Rs. 0.00");
        lblServiceRev = new JLabel("Rs. 0.00");
        lblDiscounts = new JLabel("Rs. 0.00");
        lblCogs = new JLabel("Rs. 0.00");
        lblGrossProfit = new JLabel("Rs. 0.00");
        lblExpenses = new JLabel("Rs. 0.00");
        lblNetProfit = new JLabel("Rs. 0.00");
        lblPurchases = new JLabel("Rs. 0.00");
        lblInvoicesCount = new JLabel("0 Bills");

        summaryGrid.add(createCard("Total Sales Revenue", lblTotalSales, UIHelper.PRIMARY_COLOR));
        summaryGrid.add(createCard("Cost of Parts Sold (COGS)", lblCogs, UIHelper.SECONDARY_COLOR));
        summaryGrid.add(createCard("Gross Profit", lblGrossProfit, UIHelper.SUCCESS_COLOR));
        summaryGrid.add(createCard("Operating Expenses", lblExpenses, UIHelper.DANGER_COLOR));
        summaryGrid.add(createCard("Net Workshop Profit", lblNetProfit, new Color(16, 185, 129)));

        summaryGrid.add(createCard("Parts Revenue", lblPartsRev, UIHelper.TEXT_DARK));
        summaryGrid.add(createCard("Service Labor Revenue", lblServiceRev, UIHelper.TEXT_DARK));
        summaryGrid.add(createCard("Total Discounts Given", lblDiscounts, UIHelper.WARNING_COLOR));
        summaryGrid.add(createCard("Stock Purchases", lblPurchases, UIHelper.SECONDARY_COLOR));
        summaryGrid.add(createCard("Total Bills Issued", lblInvoicesCount, UIHelper.INFO_COLOR));

        centerContent.add(summaryGrid, BorderLayout.NORTH);

        // 2. Sub Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.FONT_SUBHEADER);

        tabbedPane.addTab("Profit & Loss Statement", createPnlTab());
        tabbedPane.addTab("Sales Invoices", createSalesTab());
        tabbedPane.addTab("Purchases List", createPurchasesTab());
        tabbedPane.addTab("Expenses List", createExpensesTab());
        tabbedPane.addTab("Top Selling Parts & Services", createTopItemsTab());

        centerContent.add(tabbedPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, JLabel valLabel, Color col) {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(2, 2));
        JLabel lbl = new JLabel(title);
        lbl.setFont(UIHelper.FONT_SMALL);
        lbl.setForeground(UIHelper.TEXT_MUTED);

        valLabel.setFont(UIHelper.FONT_HEADER);
        valLabel.setForeground(col);

        card.add(lbl, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createPnlTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIHelper.CARD_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"Financial Line Item", "Amount (Rs.)", "Notes / Calculation"};
        pnlModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        pnlTable = new JTable(pnlModel);
        UIHelper.styleTable(pnlTable);

        panel.add(new JScrollPane(pnlTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSalesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.CARD_BG);
        String[] cols = {"S.N.", "Invoice #", "Date", "Customer Name", "Phone", "Vehicle", "Parts Total", "Service Charge", "Discount", "Total Paid", "Gross Profit", "Payment"};
        salesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        salesTable = new JTable(salesModel);
        UIHelper.styleTable(salesTable);
        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPurchasesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.CARD_BG);
        String[] cols = {"S.N.", "Date", "Supplier Bill #", "Supplier Name", "Total Amount (Rs.)", "Payment Status", "Notes"};
        purchasesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        purchasesTable = new JTable(purchasesModel);
        UIHelper.styleTable(purchasesTable);
        panel.add(new JScrollPane(purchasesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createExpensesTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIHelper.CARD_BG);
        String[] cols = {"S.N.", "Date", "Category", "Description", "Amount (Rs.)", "Payment Method", "Notes"};
        expensesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        expensesTable = new JTable(expensesModel);
        UIHelper.styleTable(expensesTable);
        panel.add(new JScrollPane(expensesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopItemsTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 12));
        panel.setBackground(UIHelper.CARD_BG);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Left: Top Parts
        JPanel left = new JPanel(new BorderLayout(4, 4));
        left.setBackground(UIHelper.CARD_BG);
        left.setBorder(new TitledBorder("Top Selling Parts"));
        String[] partCols = {"S.N.", "Part Name", "Units Sold", "Total Revenue"};
        topPartsModel = new DefaultTableModel(partCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        topPartsTable = new JTable(topPartsModel);
        UIHelper.styleTable(topPartsTable);
        left.add(new JScrollPane(topPartsTable), BorderLayout.CENTER);
        panel.add(left);

        // Right: Top Services
        JPanel right = new JPanel(new BorderLayout(4, 4));
        right.setBackground(UIHelper.CARD_BG);
        right.setBorder(new TitledBorder("Top Performing Workshop Services"));
        String[] servCols = {"S.N.", "Service Name", "Jobs Done", "Total Revenue"};
        topServicesModel = new DefaultTableModel(servCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        topServicesTable = new JTable(topServicesModel);
        UIHelper.styleTable(topServicesTable);
        right.add(new JScrollPane(topServicesTable), BorderLayout.CENTER);
        panel.add(right);

        return panel;
    }

    public void refreshData() {
        loadReport(currentPreset != null ? currentPreset : ReportService.FilterPreset.THIS_MONTH);
    }

    public void loadReport(ReportService.FilterPreset preset) {
        this.currentPreset = preset;
        SwingUtilities.invokeLater(() -> {
            try {
                if (preset == ReportService.FilterPreset.TODAY) {
                    txtStartDate.setText(DateUtil.today());
                    txtEndDate.setText(DateUtil.today());
                } else if (preset == ReportService.FilterPreset.YESTERDAY) {
                    txtStartDate.setText(DateUtil.yesterday());
                    txtEndDate.setText(DateUtil.yesterday());
                } else if (preset == ReportService.FilterPreset.THIS_WEEK) {
                    txtStartDate.setText(DateUtil.startOfWeek());
                    txtEndDate.setText(DateUtil.endOfWeek());
                } else if (preset == ReportService.FilterPreset.THIS_MONTH) {
                    txtStartDate.setText(DateUtil.startOfMonth());
                    txtEndDate.setText(DateUtil.endOfMonth());
                } else if (preset == ReportService.FilterPreset.ALL_TIME) {
                    txtStartDate.setText("");
                    txtEndDate.setText("");
                }

                currentReportData = reportService.generateReportForPreset(preset, txtStartDate.getText(), txtEndDate.getText());

                // Update Labels
                lblTotalSales.setText(FormatUtil.formatCurrency(currentReportData.getTotalSales()));
                lblPartsRev.setText(FormatUtil.formatCurrency(currentReportData.getPartsRevenue()));
                lblServiceRev.setText(FormatUtil.formatCurrency(currentReportData.getServiceRevenue()));
                lblDiscounts.setText(FormatUtil.formatCurrency(currentReportData.getTotalDiscount()));
                lblCogs.setText(FormatUtil.formatCurrency(currentReportData.getTotalCogs()));
                lblGrossProfit.setText(FormatUtil.formatCurrency(currentReportData.getGrossProfit()));
                lblExpenses.setText(FormatUtil.formatCurrency(currentReportData.getTotalExpenses()));
                lblNetProfit.setText(FormatUtil.formatCurrency(currentReportData.getNetProfit()));
                lblPurchases.setText(FormatUtil.formatCurrency(currentReportData.getTotalPurchases()));
                lblInvoicesCount.setText(currentReportData.getTotalInvoices() + " Bills");

                // Populate P&L Table
                pnlModel.setRowCount(0);
                pnlModel.addRow(new Object[]{"1. Total Sales Revenue", FormatUtil.formatCurrency(currentReportData.getTotalSales()), "Gross sales from parts and services"});
                pnlModel.addRow(new Object[]{"   - Parts Revenue", FormatUtil.formatCurrency(currentReportData.getPartsRevenue()), "Revenue from spare parts and consumables"});
                pnlModel.addRow(new Object[]{"   - Service Labor Revenue", FormatUtil.formatCurrency(currentReportData.getServiceRevenue()), "Revenue from repair services and labor"});
                pnlModel.addRow(new Object[]{"   - Total Discounts Allowed", "- " + FormatUtil.formatCurrency(currentReportData.getTotalDiscount()), "Discounts granted to customers"});
                pnlModel.addRow(new Object[]{"2. Less: Cost of Goods Sold (COGS)", "- " + FormatUtil.formatCurrency(currentReportData.getTotalCogs()), "Purchase cost of parts and oils sold"});
                pnlModel.addRow(new Object[]{"3. GROSS PROFIT", FormatUtil.formatCurrency(currentReportData.getGrossProfit()), "Sales Revenue minus Cost of Goods Sold"});
                pnlModel.addRow(new Object[]{"4. Less: Operating Expenses", "- " + FormatUtil.formatCurrency(currentReportData.getTotalExpenses()), "Rent, electricity, salary, food/tea, miscellaneous"});
                pnlModel.addRow(new Object[]{"5. NET WORKSHOP PROFIT", FormatUtil.formatCurrency(currentReportData.getNetProfit()), "Gross Profit minus Operating Expenses"});
                pnlModel.addRow(new Object[]{"----------------------------------------", "--------------------", "----------------------------------------"});
                pnlModel.addRow(new Object[]{"* Memo: Inventory Purchases in Period", FormatUtil.formatCurrency(currentReportData.getTotalPurchases()), "Asset stock inward (not an operating expense)"});

                // Populate Sales Table
                salesModel.setRowCount(0);
                int snSales = 1;
                for (Sale s : currentReportData.getSalesList()) {
                    salesModel.addRow(new Object[]{
                            snSales++,
                            s.getInvoiceNumber(),
                            s.getDate(),
                            s.getCustomerName(),
                            s.getCustomerPhone() != null ? s.getCustomerPhone() : "-",
                            s.getVehicleBrand() + " " + s.getVehicleModel(),
                            FormatUtil.formatCurrencyPlain(s.getPartsTotal()),
                            FormatUtil.formatCurrencyPlain(s.getServiceCharge()),
                            FormatUtil.formatCurrencyPlain(s.getDiscount()),
                            FormatUtil.formatCurrencyPlain(s.getTotalAmount()),
                            FormatUtil.formatCurrencyPlain(s.getGrossProfit()),
                            s.getPaymentMethod()
                    });
                }

                // Populate Purchases Table
                purchasesModel.setRowCount(0);
                int snPurchases = 1;
                for (Purchase p : currentReportData.getPurchaseList()) {
                    purchasesModel.addRow(new Object[]{
                            snPurchases++,
                            p.getDate(),
                            p.getInvoiceNumber(),
                            p.getSupplierName() != null ? p.getSupplierName() : "-",
                            FormatUtil.formatCurrencyPlain(p.getTotalAmount()),
                            p.getPaymentStatus(),
                            p.getNotes() != null ? p.getNotes() : ""
                    });
                }

                // Populate Expenses Table
                expensesModel.setRowCount(0);
                int snExpenses = 1;
                for (Expense exp : currentReportData.getExpenseList()) {
                    expensesModel.addRow(new Object[]{
                            snExpenses++,
                            exp.getDate(),
                            exp.getCategory(),
                            exp.getDescription(),
                            FormatUtil.formatCurrencyPlain(exp.getAmount()),
                            exp.getPaymentMethod(),
                            exp.getNotes() != null ? exp.getNotes() : ""
                    });
                }

                // Populate Top Parts
                topPartsModel.setRowCount(0);
                int snParts = 1;
                for (DashboardStats.TopItemMetric m : currentReportData.getTopParts()) {
                    topPartsModel.addRow(new Object[]{snParts++, m.getName(), m.getQuantity(), FormatUtil.formatCurrency(m.getTotalRevenue())});
                }

                // Populate Top Services
                topServicesModel.setRowCount(0);
                int snServ = 1;
                for (DashboardStats.TopItemMetric m : currentReportData.getTopServices()) {
                    topServicesModel.addRow(new Object[]{snServ++, m.getName(), m.getQuantity(), FormatUtil.formatCurrency(m.getTotalRevenue())});
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onExportCurrentTab() {
        int idx = tabbedPane.getSelectedIndex();
        switch (idx) {
            case 0:
                CsvExporter.exportTableToCsv(this, pnlTable, "profit_and_loss_report");
                break;
            case 1:
                CsvExporter.exportTableToCsv(this, salesTable, "sales_report");
                break;
            case 2:
                CsvExporter.exportTableToCsv(this, purchasesTable, "purchases_report");
                break;
            case 3:
                CsvExporter.exportTableToCsv(this, expensesTable, "expenses_report");
                break;
            case 4:
                CsvExporter.exportTableToCsv(this, topPartsTable, "top_selling_parts");
                break;
        }
    }
}
