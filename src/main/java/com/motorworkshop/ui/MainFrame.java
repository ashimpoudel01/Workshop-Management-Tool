package com.motorworkshop.ui;

import com.motorworkshop.service.*;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Primary Application Window with a clean modern navigation sidebar, responsive card layout, and live view sync.
 */
public class MainFrame extends JFrame {
    private final SaleService saleService = new SaleService();
    private final InventoryService inventoryService = new InventoryService();
    private final PurchaseService purchaseService = new PurchaseService();
    private final ExpenseService expenseService = new ExpenseService();
    private final CustomerService customerService = new CustomerService();
    private final PricingService pricingService = new PricingService();
    private final ReportService reportService = new ReportService();
    private final SettingService settingService = new SettingService();

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private final Map<String, JButton> navButtons = new HashMap<>();

    // Sub-panels
    private DashboardPanel dashboardPanel;
    private SalesPanel salesPanel;
    private PurchasePanel purchasePanel;
    private InventoryPanel inventoryPanel;
    private ExpensePanel expensePanel;
    private PricingPanel pricingPanel;
    private CustomerPanel customerPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    public MainFrame() {
        super("Motorcycle & Scooter Workshop Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);

        initComponents();
        showSection("Dashboard");
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UIHelper.BG_LIGHT);

        // 1. Left Sidebar Navigation
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 800));
        sidebar.setBackground(new Color(15, 23, 42)); // Dark slate navy
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(30, 41, 59)));

        // Sidebar Brand Logo / Header
        JPanel brandPanel = new JPanel(new BorderLayout(4, 4));
        brandPanel.setBackground(new Color(15, 23, 42));
        brandPanel.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel lblBrand = new JLabel("MOTOR WORKSHOP");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Management System v1.0");
        lblSub.setFont(UIHelper.FONT_SMALL);
        lblSub.setForeground(new Color(148, 163, 184));

        brandPanel.add(lblBrand, BorderLayout.NORTH);
        brandPanel.add(lblSub, BorderLayout.SOUTH);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Nav Items list
        JPanel navList = new JPanel(new GridLayout(9, 1, 0, 4));
        navList.setBackground(new Color(15, 23, 42));
        navList.setBorder(new EmptyBorder(10, 8, 10, 8));

        String[] sections = {
                "Dashboard",
                "Sales",
                "Purchases",
                "Inventory",
                "Expenses",
                "Pricing",
                "Customers",
                "Reports",
                "Settings"
        };

        for (String sec : sections) {
            JButton btn = createNavButton(sec);
            navButtons.put(sec, btn);
            navList.add(btn);
        }

        sidebar.add(navList, BorderLayout.CENTER);

        // Sidebar Footer
        JPanel sidebarFooter = new JPanel(new BorderLayout());
        sidebarFooter.setBackground(new Color(15, 23, 42));
        sidebarFooter.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel lblStatus = new JLabel("Offline SQLite Ready");
        lblStatus.setFont(UIHelper.FONT_SMALL);
        lblStatus.setForeground(new Color(52, 211, 153)); // Emerald
        sidebarFooter.add(lblStatus, BorderLayout.CENTER);
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);

        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Main Content Card Container
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UIHelper.BG_LIGHT);

        // Initialize panels
        dashboardPanel = new DashboardPanel(saleService, this::showSection, this::openNewSale, this::openNewPurchase);
        salesPanel = new SalesPanel(saleService, inventoryService, pricingService, customerService, settingService, this);
        purchasePanel = new PurchasePanel(purchaseService, inventoryService, this);
        inventoryPanel = new InventoryPanel(inventoryService, this);
        expensePanel = new ExpensePanel(expenseService, this);
        pricingPanel = new PricingPanel(pricingService, this);
        customerPanel = new CustomerPanel(customerService, saleService, this);
        reportsPanel = new ReportsPanel(reportService);
        settingsPanel = new SettingsPanel(settingService, this);

        mainContentPanel.add(dashboardPanel, "Dashboard");
        mainContentPanel.add(salesPanel, "Sales");
        mainContentPanel.add(purchasePanel, "Purchases");
        mainContentPanel.add(inventoryPanel, "Inventory");
        mainContentPanel.add(expensePanel, "Expenses");
        mainContentPanel.add(pricingPanel, "Pricing");
        mainContentPanel.add(customerPanel, "Customers");
        mainContentPanel.add(reportsPanel, "Reports");
        mainContentPanel.add(settingsPanel, "Settings");

        rootPanel.add(mainContentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    private JButton createNavButton(String name) {
        JButton btn = new JButton(name);
        btn.setFont(UIHelper.FONT_BOLD);
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(15, 23, 42));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> showSection(name));
        return btn;
    }

    public void showSection(String name) {
        cardLayout.show(mainContentPanel, name);

        // Update button active states
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                entry.getValue().setBackground(UIHelper.PRIMARY_COLOR);
                entry.getValue().setForeground(Color.WHITE);
            } else {
                entry.getValue().setBackground(new Color(15, 23, 42));
                entry.getValue().setForeground(new Color(203, 213, 225));
            }
        }

        // Trigger dynamic refresh on panel visit
        switch (name) {
            case "Dashboard":
                dashboardPanel.refreshData();
                break;
            case "Sales":
                salesPanel.loadData();
                break;
            case "Purchases":
                purchasePanel.loadData();
                break;
            case "Inventory":
                inventoryPanel.loadData();
                break;
            case "Expenses":
                expensePanel.loadData();
                break;
            case "Pricing":
                pricingPanel.loadPartsData();
                pricingPanel.loadServicesData();
                break;
            case "Customers":
                customerPanel.loadData();
                break;
            case "Reports":
                reportsPanel.refreshData();
                break;
        }
    }

    public void notifyDataChanged() {
        if (dashboardPanel != null) dashboardPanel.refreshData();
        if (reportsPanel != null) reportsPanel.refreshData();
        if (salesPanel != null) salesPanel.loadData();
        if (purchasePanel != null) purchasePanel.loadData();
        if (inventoryPanel != null) inventoryPanel.loadData();
        if (expensePanel != null) expensePanel.loadData();
        if (pricingPanel != null) {
            pricingPanel.loadPartsData();
            pricingPanel.loadServicesData();
        }
        if (customerPanel != null) customerPanel.loadData();
    }

    private void openNewSale() {
        salesPanel.openNewSale();
        notifyDataChanged();
    }

    private void openNewPurchase() {
        purchasePanel.openNewPurchase();
        notifyDataChanged();
    }
}
