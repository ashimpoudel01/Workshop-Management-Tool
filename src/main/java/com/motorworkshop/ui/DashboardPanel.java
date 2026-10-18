package com.motorworkshop.ui;

import com.motorworkshop.model.DashboardStats;
import com.motorworkshop.service.SaleService;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Main Overview Dashboard showing KPIs, Quick Actions, Top Selling items, and monthly aggregates.
 */
public class DashboardPanel extends JPanel {
    private final SaleService saleService;
    private final Consumer<String> navigationCallback;
    private final Runnable openNewSaleCallback;
    private final Runnable openNewPurchaseCallback;

    // Stat Label references for dynamic refresh
    private JLabel lblTodaySales;
    private JLabel lblTodayExpenses;
    private JLabel lblTodayPurchases;
    private JLabel lblTodayGrossProfit;
    private JLabel lblTodayNetProfit;
    private JLabel lblInventoryValue;
    private JLabel lblLowStock;
    private JLabel lblTodayBills;

    private JLabel lblMonthSales;
    private JLabel lblMonthExpenses;
    private JLabel lblMonthPurchases;
    private JLabel lblMonthNetProfit;
    private JLabel lblAvgDailySales;

    private DefaultTableModel topPartsModel;
    private DefaultTableModel topServicesModel;

    public DashboardPanel(SaleService saleService, Consumer<String> navigationCallback,
                          Runnable openNewSaleCallback, Runnable openNewPurchaseCallback) {
        this.saleService = saleService;
        this.navigationCallback = navigationCallback;
        this.openNewSaleCallback = openNewSaleCallback;
        this.openNewPurchaseCallback = openNewPurchaseCallback;

        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(UIHelper.BG_LIGHT);

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // 1. Top Header Bar
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JPanel titleGroup = UIHelper.createSectionHeader(
                "Workshop Dashboard",
                "Today's financial metrics, quick garage actions, and monthly performance summary"
        );
        topHeader.add(titleGroup, BorderLayout.WEST);

        JButton btnRefresh = UIHelper.createSecondaryButton("🔄  Refresh Data");
        btnRefresh.addActionListener(e -> refreshData());
        topHeader.add(btnRefresh, BorderLayout.EAST);

        add(topHeader, BorderLayout.NORTH);

        // Center Scrollable Container
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // A. Primary KPI Cards Grid (Row 1: Today's Metrics, Row 2: Inventory & Volume)
        JPanel kpiGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 175));

        lblTodaySales = new JLabel("Rs. 0.00");
        lblTodayExpenses = new JLabel("Rs. 0.00");
        lblTodayGrossProfit = new JLabel("Rs. 0.00");
        lblTodayNetProfit = new JLabel("Rs. 0.00");
        lblTodayPurchases = new JLabel("Rs. 0.00");
        lblInventoryValue = new JLabel("Rs. 0.00");
        lblLowStock = new JLabel("0 Items");
        lblTodayBills = new JLabel("0 Bills");

        kpiGrid.add(UIHelper.createStatCard("Today's Sales", lblTodaySales, UIHelper.PRIMARY_COLOR, "Total revenue billed today"));
        kpiGrid.add(UIHelper.createStatCard("Gross Profit", lblTodayGrossProfit, UIHelper.SUCCESS_COLOR, "Sales revenue - Part costs"));
        kpiGrid.add(UIHelper.createStatCard("Today's Expenses", lblTodayExpenses, UIHelper.DANGER_COLOR, "Daily garage running costs"));
        kpiGrid.add(UIHelper.createStatCard("Net Profit Today", lblTodayNetProfit, UIHelper.SUCCESS_COLOR, "Gross profit - Expenses"));

        kpiGrid.add(UIHelper.createStatCard("Today's Purchases", lblTodayPurchases, UIHelper.SECONDARY_COLOR, "Supplier stock inward"));
        kpiGrid.add(UIHelper.createStatCard("Inventory Value", lblInventoryValue, UIHelper.INFO_COLOR, "Current stock valuation"));
        kpiGrid.add(UIHelper.createStatCard("Low Stock Alert", lblLowStock, UIHelper.WARNING_COLOR, "Parts below minimum stock"));
        kpiGrid.add(UIHelper.createStatCard("Completed Bills", lblTodayBills, UIHelper.INFO_COLOR, "Job cards completed today"));

        centerContent.add(kpiGrid);
        centerContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // B. Quick Action Buttons Grid
        JPanel quickActions = new JPanel(new GridLayout(1, 4, 12, 12));
        quickActions.setOpaque(false);
        quickActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnNewSale = UIHelper.createSuccessButton("+ New Job Card / Sale");
        btnNewSale.addActionListener(e -> {
            if (openNewSaleCallback != null) openNewSaleCallback.run();
        });

        JButton btnNewPurchase = UIHelper.createPrimaryButton("+ New Parts Purchase");
        btnNewPurchase.addActionListener(e -> {
            if (openNewPurchaseCallback != null) openNewPurchaseCallback.run();
        });

        JButton btnInventory = UIHelper.createSecondaryButton("📦  Manage Inventory");
        btnInventory.addActionListener(e -> navigationCallback.accept("Inventory"));

        JButton btnReports = UIHelper.createSecondaryButton("📈  P&L Reports");
        btnReports.addActionListener(e -> navigationCallback.accept("Reports"));

        quickActions.add(btnNewSale);
        quickActions.add(btnNewPurchase);
        quickActions.add(btnInventory);
        quickActions.add(btnReports);

        centerContent.add(quickActions);
        centerContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // C. Bottom Row: Monthly Summary (Left) + Top Selling Parts & Services (Right)
        JPanel bottomRow = new JPanel(new GridLayout(1, 3, 14, 14));
        bottomRow.setOpaque(false);

        // 1. Monthly Performance Card
        JPanel monthCard = UIHelper.createCard();
        monthCard.setLayout(new BorderLayout(8, 8));
        JLabel lblMonthTitle = new JLabel("Monthly Performance");
        lblMonthTitle.setFont(UIHelper.FONT_SUBHEADER);
        lblMonthTitle.setForeground(UIHelper.TEXT_DARK);
        monthCard.add(lblMonthTitle, BorderLayout.NORTH);

        JPanel monthGrid = new JPanel(new GridLayout(5, 2, 6, 10));
        monthGrid.setBackground(UIHelper.CARD_BG);

        lblMonthSales = new JLabel("Rs. 0.00");
        lblMonthExpenses = new JLabel("Rs. 0.00");
        lblMonthPurchases = new JLabel("Rs. 0.00");
        lblMonthNetProfit = new JLabel("Rs. 0.00");
        lblMonthNetProfit.setFont(UIHelper.FONT_BOLD);
        lblMonthNetProfit.setForeground(UIHelper.SUCCESS_COLOR);
        lblAvgDailySales = new JLabel("Rs. 0.00");

        monthGrid.add(createFieldLabel("Monthly Sales:"));
        monthGrid.add(lblMonthSales);
        monthGrid.add(createFieldLabel("Monthly Expenses:"));
        monthGrid.add(lblMonthExpenses);
        monthGrid.add(createFieldLabel("Monthly Purchases:"));
        monthGrid.add(lblMonthPurchases);
        monthGrid.add(createFieldLabel("Monthly Net Profit:"));
        monthGrid.add(lblMonthNetProfit);
        monthGrid.add(createFieldLabel("Avg Daily Sales:"));
        monthGrid.add(lblAvgDailySales);

        monthCard.add(monthGrid, BorderLayout.CENTER);
        bottomRow.add(monthCard);

        // 2. Top 5 Selling Parts Table
        JPanel topPartsCard = UIHelper.createCard();
        topPartsCard.setLayout(new BorderLayout(6, 6));
        JLabel lblPartsTitle = new JLabel("Top 5 Selling Parts (This Month)");
        lblPartsTitle.setFont(UIHelper.FONT_SUBHEADER);
        lblPartsTitle.setForeground(UIHelper.TEXT_DARK);
        topPartsCard.add(lblPartsTitle, BorderLayout.NORTH);

        String[] partsCols = {"S.N.", "Part Name", "Qty", "Revenue"};
        topPartsModel = new DefaultTableModel(partsCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topPartsTable = new JTable(topPartsModel);
        UIHelper.styleTable(topPartsTable);
        topPartsCard.add(new JScrollPane(topPartsTable), BorderLayout.CENTER);
        bottomRow.add(topPartsCard);

        // 3. Top 5 Services Table
        JPanel topServicesCard = UIHelper.createCard();
        topServicesCard.setLayout(new BorderLayout(6, 6));
        JLabel lblServTitle = new JLabel("Top 5 Services (This Month)");
        lblServTitle.setFont(UIHelper.FONT_SUBHEADER);
        lblServTitle.setForeground(UIHelper.TEXT_DARK);
        topServicesCard.add(lblServTitle, BorderLayout.NORTH);

        String[] servCols = {"S.N.", "Service Name", "Jobs", "Revenue"};
        topServicesModel = new DefaultTableModel(servCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable topServTable = new JTable(topServicesModel);
        UIHelper.styleTable(topServTable);
        topServicesCard.add(new JScrollPane(topServTable), BorderLayout.CENTER);
        bottomRow.add(topServicesCard);

        centerContent.add(bottomRow);

        JScrollPane scrollPane = new JScrollPane(centerContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIHelper.FONT_BODY);
        lbl.setForeground(UIHelper.TEXT_MUTED);
        return lbl;
    }

    public void refreshData() {
        SwingUtilities.invokeLater(() -> {
            try {
                DashboardStats stats = saleService.getDashboardMetrics();

                lblTodaySales.setText(FormatUtil.formatCurrency(stats.getTodaySales()));
                lblTodayGrossProfit.setText(FormatUtil.formatCurrency(stats.getTodayGrossProfit()));
                lblTodayExpenses.setText(FormatUtil.formatCurrency(stats.getTodayExpenses()));
                lblTodayNetProfit.setText(FormatUtil.formatCurrency(stats.getTodayNetProfit()));
                lblTodayPurchases.setText(FormatUtil.formatCurrency(stats.getTodayPurchases()));
                lblInventoryValue.setText(FormatUtil.formatCurrency(stats.getCurrentInventoryValue()));
                lblLowStock.setText(stats.getLowStockCount() + " Items");
                lblTodayBills.setText(stats.getTodayInvoicesCount() + " Bills");

                lblMonthSales.setText(FormatUtil.formatCurrency(stats.getMonthlySales()));
                lblMonthExpenses.setText(FormatUtil.formatCurrency(stats.getMonthlyExpenses()));
                lblMonthPurchases.setText(FormatUtil.formatCurrency(stats.getMonthlyPurchases()));
                lblMonthNetProfit.setText(FormatUtil.formatCurrency(stats.getMonthlyNetProfit()));
                lblAvgDailySales.setText(FormatUtil.formatCurrency(stats.getAverageDailySales()));

                // Top Parts
                topPartsModel.setRowCount(0);
                int snParts = 1;
                for (DashboardStats.TopItemMetric m : stats.getTopSellingParts()) {
                    topPartsModel.addRow(new Object[]{snParts++, m.getName(), m.getQuantity(), FormatUtil.formatCurrency(m.getTotalRevenue())});
                }

                // Top Services
                topServicesModel.setRowCount(0);
                int snServ = 1;
                for (DashboardStats.TopItemMetric m : stats.getTopPerformingServices()) {
                    topServicesModel.addRow(new Object[]{snServ++, m.getName(), m.getQuantity(), FormatUtil.formatCurrency(m.getTotalRevenue())});
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
