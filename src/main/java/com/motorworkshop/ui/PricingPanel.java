package com.motorworkshop.ui;

import com.motorworkshop.model.PriceHistory;
import com.motorworkshop.model.Product;
import com.motorworkshop.model.ServiceItem;
import com.motorworkshop.service.PricingService;
import com.motorworkshop.ui.dialogs.ServiceDialog;
import com.motorworkshop.util.CsvExporter;
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
 * Dedicated Pricing Management panel for Parts margins and Workshop Service rate cards.
 */
public class PricingPanel extends JPanel {
    private final PricingService pricingService;
    private final Frame parentFrame;

    private JTabbedPane tabbedPane;

    // Parts Tab
    private JTextField txtSearchParts;
    private JTable partsTable;
    private DefaultTableModel partsModel;
    private List<Product> currentProducts = new ArrayList<>();

    // Services Tab
    private JTable servicesTable;
    private DefaultTableModel servicesModel;
    private List<ServiceItem> currentServices = new ArrayList<>();

    public PricingPanel(PricingService pricingService, Frame parentFrame) {
        this.pricingService = pricingService;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 18, 15, 18));
        setBackground(UIHelper.BG_LIGHT);

        initComponents();
        loadPartsData();
        loadServicesData();
    }

    private void initComponents() {
        // Header
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);

        JLabel lblTitle = new JLabel("Pricing Strategy & Service Rate Card");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        add(headerBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIHelper.FONT_SUBHEADER);

        // Tab 1: Parts Pricing & Margins
        tabbedPane.addTab("Parts & Spare Pricing", createPartsTab());

        // Tab 2: Workshop Services Rate Card
        tabbedPane.addTab("Workshop Services & Labor Rates", createServicesTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPartsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIHelper.CARD_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UIHelper.CARD_BG);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setBackground(UIHelper.CARD_BG);
        txtSearchParts = new JTextField(18);
        txtSearchParts.putClientProperty("JTextField.placeholderText", "Search part name, brand...");
        txtSearchParts.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadPartsData(); }
            public void removeUpdate(DocumentEvent e) { loadPartsData(); }
            public void changedUpdate(DocumentEvent e) { loadPartsData(); }
        });
        searchBox.add(new JLabel("Search Parts:"));
        searchBox.add(txtSearchParts);
        toolbar.add(searchBox, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.CARD_BG);

        JButton btnUpdatePrices = UIHelper.createPrimaryButton("Update Selling Prices");
        btnUpdatePrices.addActionListener(e -> onUpdatePartPricing());

        JButton btnViewHistory = UIHelper.createSecondaryButton("Price Change History");
        btnViewHistory.addActionListener(e -> onViewPriceHistory());

        JButton btnExport = UIHelper.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, partsTable, "parts_pricing_margins"));

        actionBtns.add(btnUpdatePrices);
        actionBtns.add(btnViewHistory);
        actionBtns.add(btnExport);
        toolbar.add(actionBtns, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        // Table
        String[] cols = {"S.N.", "Part Name", "Brand", "Part No", "Purchase Price (Rs.)", "Retail Selling (Rs.)", "Workshop Price (Rs.)", "Profit / Unit (Rs.)", "Profit Margin %", "Supplier"};
        partsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        partsTable = new JTable(partsModel);
        UIHelper.styleTable(partsTable);

        panel.add(new JScrollPane(partsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createServicesTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UIHelper.CARD_BG);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UIHelper.CARD_BG);

        JLabel lblSub = new JLabel("Predefined Workshop Labor & Repair Rates");
        lblSub.setFont(UIHelper.FONT_SUBHEADER);
        toolbar.add(lblSub, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.CARD_BG);

        JButton btnAddService = UIHelper.createSuccessButton("+ Add New Service");
        btnAddService.addActionListener(e -> onAddService());

        JButton btnEditService = UIHelper.createPrimaryButton("Edit Service Rate");
        btnEditService.addActionListener(e -> onEditService());

        JButton btnDeleteService = UIHelper.createDangerButton("Deactivate Service");
        btnDeleteService.addActionListener(e -> onDeleteService());

        actionBtns.add(btnAddService);
        actionBtns.add(btnEditService);
        actionBtns.add(btnDeleteService);
        toolbar.add(actionBtns, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"S.N.", "Service Name", "Default Rate (Rs.)", "Est. Duration", "Description", "Status"};
        servicesModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        servicesTable = new JTable(servicesModel);
        UIHelper.styleTable(servicesTable);

        panel.add(new JScrollPane(servicesTable), BorderLayout.CENTER);
        return panel;
    }

    public void loadPartsData() {
        SwingUtilities.invokeLater(() -> {
            try {
                String search = txtSearchParts != null ? txtSearchParts.getText().trim().toLowerCase() : "";
                List<Product> all = pricingService.getAllPricingProducts();
                currentProducts.clear();

                for (Product p : all) {
                    if (search.isEmpty() || p.getPartName().toLowerCase().contains(search) ||
                            (p.getBrand() != null && p.getBrand().toLowerCase().contains(search)) ||
                            (p.getPartNumber() != null && p.getPartNumber().toLowerCase().contains(search))) {
                        currentProducts.add(p);
                    }
                }

                partsModel.setRowCount(0);
                int sn = 1;
                for (Product p : currentProducts) {
                    partsModel.addRow(new Object[]{
                            sn++,
                            p.getPartName(),
                            p.getBrand() != null ? p.getBrand() : "-",
                            p.getPartNumber() != null ? p.getPartNumber() : "-",
                            FormatUtil.formatCurrencyPlain(p.getPurchasePrice()),
                            FormatUtil.formatCurrencyPlain(p.getSellingPrice()),
                            FormatUtil.formatCurrencyPlain(p.getWorkshopPrice()),
                            FormatUtil.formatCurrencyPlain(p.getProfitPerUnit()),
                            FormatUtil.formatPercent(p.getProfitMarginPercent()),
                            p.getSupplierName() != null ? p.getSupplierName() : "-"
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadServicesData() {
        SwingUtilities.invokeLater(() -> {
            try {
                currentServices = pricingService.getAllServices(false);
                servicesModel.setRowCount(0);
                int sn = 1;
                for (ServiceItem s : currentServices) {
                    servicesModel.addRow(new Object[]{
                            sn++,
                            s.getServiceName(),
                            FormatUtil.formatCurrencyPlain(s.getDefaultPrice()),
                            s.getEstimatedDurationMinutes() + " mins",
                            s.getDescription() != null ? s.getDescription() : "-",
                            s.isActive() ? "Active" : "Inactive"
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onUpdatePartPricing() {
        int row = partsTable.getSelectedRow();
        if (row < 0 || row >= currentProducts.size()) {
            UIHelper.showWarning(this, "Please select a product from the table to update pricing.");
            return;
        }

        Product p = currentProducts.get(row);

        JTextField txtRetail = new JTextField(String.valueOf(p.getSellingPrice()), 10);
        JTextField txtWorkshop = new JTextField(String.valueOf(p.getWorkshopPrice()), 10);
        JTextField txtReason = new JTextField("Market price adjustment", 20);

        JPanel pnl = new JPanel(new GridLayout(4, 2, 8, 8));
        pnl.add(new JLabel("Product:"));
        pnl.add(new JLabel("<b>" + p.getPartName() + "</b>"));
        pnl.add(new JLabel("New Retail Selling Price (Rs.):"));
        pnl.add(txtRetail);
        pnl.add(new JLabel("New Workshop Price (Rs.):"));
        pnl.add(txtWorkshop);
        pnl.add(new JLabel("Reason for Price Change:"));
        pnl.add(txtReason);

        int res = JOptionPane.showConfirmDialog(this, pnl, "Update Pricing for " + p.getPartName(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            double newRetail = FormatUtil.parseDouble(txtRetail.getText(), -1);
            double newWorkshop = FormatUtil.parseDouble(txtWorkshop.getText(), -1);
            if (newRetail < 0 || newWorkshop < 0) {
                UIHelper.showWarning(this, "Prices cannot be negative!");
                return;
            }

            try {
                pricingService.updateProductPricing(p.getItemId(), newRetail, newWorkshop, txtReason.getText().trim());
                UIHelper.showInfo(this, "Pricing updated and logged to history!");
                loadPartsData();
            } catch (Exception ex) {
                UIHelper.showError(this, "Failed to update pricing: " + ex.getMessage());
            }
        }
    }

    private void onViewPriceHistory() {
        int row = partsTable.getSelectedRow();
        if (row < 0 || row >= currentProducts.size()) {
            UIHelper.showWarning(this, "Please select a product to view its price change history.");
            return;
        }

        Product p = currentProducts.get(row);
        try {
            List<PriceHistory> history = pricingService.getPriceHistory(p.getItemId());
            if (history.isEmpty()) {
                UIHelper.showInfo(this, "No previous price change records found for " + p.getPartName());
                return;
            }

            String[] cols = {"S.N.", "Date", "Old Buy Price", "New Buy Price", "Old Sell Price", "New Sell Price", "Reason"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            int sn = 1;
            for (PriceHistory h : history) {
                model.addRow(new Object[]{
                        sn++,
                        h.getChangeDate(),
                        FormatUtil.formatCurrencyPlain(h.getOldPurchasePrice()),
                        FormatUtil.formatCurrencyPlain(h.getNewPurchasePrice()),
                        FormatUtil.formatCurrencyPlain(h.getOldSellingPrice()),
                        FormatUtil.formatCurrencyPlain(h.getNewSellingPrice()),
                        h.getReason()
                });
            }

            JTable table = new JTable(model);
            UIHelper.styleTable(table);
            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(650, 300));
            JOptionPane.showMessageDialog(this, sp, "Price History: " + p.getPartName(), JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            UIHelper.showError(this, "Error fetching price history: " + ex.getMessage());
        }
    }

    private void onAddService() {
        ServiceDialog dlg = new ServiceDialog(parentFrame, pricingService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadServicesData();
        }
    }

    private void onEditService() {
        int row = servicesTable.getSelectedRow();
        if (row < 0 || row >= currentServices.size()) {
            UIHelper.showWarning(this, "Please select a service from the table to edit.");
            return;
        }
        ServiceItem s = currentServices.get(row);
        ServiceDialog dlg = new ServiceDialog(parentFrame, pricingService, s);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadServicesData();
        }
    }

    private void onDeleteService() {
        int row = servicesTable.getSelectedRow();
        if (row < 0 || row >= currentServices.size()) {
            UIHelper.showWarning(this, "Please select a service to deactivate.");
            return;
        }
        ServiceItem s = currentServices.get(row);
        if (UIHelper.showConfirm(this, "Deactivate workshop service '" + s.getServiceName() + "'?", "Confirm Deactivation")) {
            try {
                pricingService.deleteService(s.getServiceId());
                loadServicesData();
            } catch (Exception ex) {
                UIHelper.showError(this, "Error deactivating service: " + ex.getMessage());
            }
        }
    }
}
