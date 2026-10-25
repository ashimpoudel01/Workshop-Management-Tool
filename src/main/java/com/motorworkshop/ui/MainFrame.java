package com.motorworkshop.ui;

import com.motorworkshop.service.*;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Primary Application Window featuring a crafted, human-designed industrial workshop theme,
 * tactile navigation sidebar, responsive card layout, and live view sync.
 */
public class MainFrame extends JFrame {
    private static final Color SIDEBAR_BG = new Color(30, 36, 46);
    private static final Color SIDEBAR_BORDER = new Color(44, 53, 67);
    private static final Color NAV_TEXT_DEFAULT = new Color(160, 174, 192);
    private static final Color NAV_BG_ACTIVE = new Color(45, 55, 72);
    private static final Color NAV_BG_HOVER = new Color(38, 47, 60);
    private static final Color ACCENT_BAR = new Color(245, 158, 11); // Warm automotive amber

    private final SaleService saleService = new SaleService();
    private final InventoryService inventoryService = new InventoryService();
    private final PurchaseService purchaseService = new PurchaseService();
    private final ExpenseService expenseService = new ExpenseService();
    private final OwnerExpenseService ownerExpenseService = new OwnerExpenseService();
    private final CustomerService customerService = new CustomerService();
    private final PricingService pricingService = new PricingService();
    private final ReportService reportService = new ReportService();
    private final SettingService settingService = new SettingService();
    private final UserRole userRole;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentSection = "Dashboard";

    // Sub-panels
    private DashboardPanel dashboardPanel;
    private SalesPanel salesPanel;
    private PurchasePanel purchasePanel;
    private InventoryPanel inventoryPanel;
    private ExpensePanel expensePanel;
    private OwnerExpensePanel ownerExpensePanel;
    private PricingPanel pricingPanel;
    private CustomerPanel customerPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    public MainFrame(UserRole userRole) {
        super("Motorcycle & Scooter Workshop Management Tool");
        this.userRole = userRole;
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
        sidebar.setPreferredSize(new Dimension(236, 800));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, SIDEBAR_BORDER));

        // Sidebar Brand Logo / Header
        JPanel brandPanel = new JPanel(new BorderLayout(10, 0));
        brandPanel.setBackground(SIDEBAR_BG);
        brandPanel.setBorder(new EmptyBorder(18, 16, 18, 16));

        // Workshop Icon Badge
        JLabel lblBadge = new JLabel("⚙", SwingConstants.CENTER);
        lblBadge.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(new Color(234, 88, 12)); // Engine amber/orange
        lblBadge.setPreferredSize(new Dimension(36, 36));
        lblBadge.setBorder(new MatteBorder(1, 1, 1, 1, new Color(251, 146, 60)));

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandTextPanel.setOpaque(false);

        JLabel lblBrand = new JLabel("MOTOR WORKSHOP");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Garage Management Tool");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(156, 163, 175));

        brandTextPanel.add(lblBrand);
        brandTextPanel.add(lblSub);

        brandPanel.add(lblBadge, BorderLayout.WEST);
        brandPanel.add(brandTextPanel, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Nav Items list
        String[][] sectionDefs = userRole == UserRole.ADMIN
            ? new String[][]{
            {"Dashboard", "📊   Admin Dashboard"},
            {"Sales", "🧾   Sales & Job Cards"},
            {"Purchases", "🛒   Parts Purchases"},
            {"Inventory", "📦   Parts & Inventory"},
            {"Expenses", "💸   Workshop Expenses"},
                {"OwnerExpenses", "💰   Owner Expenses"},
            {"Pricing", "🏷️   Pricing & Labor"},
            {"Customers", "👥   Customer Directory"},
            {"Reports", "📈   Financial Reports"},
            {"Settings", "⚙️   System Settings"}
        }
            : new String[][]{
            {"Dashboard", "📊   Employee Dashboard"},
            {"Sales", "🧾   Sales & Job Cards"},
            {"Inventory", "📦   View Inventory"},
            {"Customers", "👥   Customer Directory"}
        };
        JPanel navList = new JPanel(new GridLayout(sectionDefs.length, 1, 0, 4));
        navList.setBackground(SIDEBAR_BG);
        navList.setBorder(new EmptyBorder(10, 8, 10, 8));

        for (String[] def : sectionDefs) {
            String secKey = def[0];
            String secLabel = def[1];
            JButton btn = createNavButton(secKey, secLabel);
            navButtons.put(secKey, btn);
            navList.add(btn);
        }

        sidebar.add(navList, BorderLayout.CENTER);

        // Sidebar Footer
        JPanel sidebarFooter = new JPanel(new GridLayout(2, 1, 0, 2));
        sidebarFooter.setBackground(SIDEBAR_BG);
        sidebarFooter.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, SIDEBAR_BORDER),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblStatus = new JLabel("● Database Connected");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(new Color(34, 197, 94)); // Emerald green

        JLabel lblVer = new JLabel("Local SQLite • v1.2 Ready");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setForeground(new Color(148, 163, 184));

        sidebarFooter.add(lblStatus);
        sidebarFooter.add(lblVer);
        sidebar.add(sidebarFooter, BorderLayout.SOUTH);

        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Main Content Card Container
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UIHelper.BG_LIGHT);

        // Initialize panels
        dashboardPanel = new DashboardPanel(saleService, userRole, this::showSection, this::openNewSale, this::openNewPurchase);
        salesPanel = new SalesPanel(saleService, inventoryService, pricingService, customerService, settingService, this, userRole == UserRole.ADMIN);
        purchasePanel = new PurchasePanel(purchaseService, inventoryService, this);
        inventoryPanel = new InventoryPanel(inventoryService, this, userRole == UserRole.ADMIN);
        expensePanel = new ExpensePanel(expenseService, this);
        ownerExpensePanel = new OwnerExpensePanel(ownerExpenseService, this);
        pricingPanel = new PricingPanel(pricingService, this);
        customerPanel = new CustomerPanel(customerService, saleService, settingService, this);
        reportsPanel = new ReportsPanel(reportService);
        settingsPanel = new SettingsPanel(settingService, this);

        mainContentPanel.add(dashboardPanel, "Dashboard");
        mainContentPanel.add(salesPanel, "Sales");
        mainContentPanel.add(purchasePanel, "Purchases");
        mainContentPanel.add(inventoryPanel, "Inventory");
        mainContentPanel.add(expensePanel, "Expenses");
        mainContentPanel.add(ownerExpensePanel, "OwnerExpenses");
        mainContentPanel.add(pricingPanel, "Pricing");
        mainContentPanel.add(customerPanel, "Customers");
        mainContentPanel.add(reportsPanel, "Reports");
        mainContentPanel.add(settingsPanel, "Settings");

        rootPanel.add(mainContentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    private JButton createNavButton(String key, String displayText) {
        JButton btn = new JButton(displayText);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(NAV_TEXT_DEFAULT);
        btn.setBackground(SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new MatteBorder(0, 4, 0, 0, SIDEBAR_BG),
                new EmptyBorder(9, 12, 9, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equalsIgnoreCase(currentSection)) {
                    btn.setBackground(NAV_BG_HOVER);
                    btn.setForeground(Color.WHITE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!key.equalsIgnoreCase(currentSection)) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(NAV_TEXT_DEFAULT);
                }
            }
        });

        btn.addActionListener(e -> showSection(key));
        return btn;
    }

    public void showSection(String name) {
        this.currentSection = name;
        cardLayout.show(mainContentPanel, name);

        // Update button active states with indicator bar and highlighted styling
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equalsIgnoreCase(name)) {
                btn.setBackground(NAV_BG_ACTIVE);
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setBorder(new CompoundBorder(
                        new MatteBorder(0, 4, 0, 0, ACCENT_BAR),
                        new EmptyBorder(9, 12, 9, 12)
                ));
            } else {
                btn.setBackground(SIDEBAR_BG);
                btn.setForeground(NAV_TEXT_DEFAULT);
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                btn.setBorder(new CompoundBorder(
                        new MatteBorder(0, 4, 0, 0, SIDEBAR_BG),
                        new EmptyBorder(9, 12, 9, 12)
                ));
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
            case "OwnerExpenses":
                ownerExpensePanel.loadData();
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
        if (ownerExpensePanel != null) ownerExpensePanel.loadData();
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
        if (userRole != UserRole.ADMIN) {
            UIHelper.showWarning(this, "Only administrators can record parts purchases.");
            return;
        }
        purchasePanel.openNewPurchase();
        notifyDataChanged();
    }
}
