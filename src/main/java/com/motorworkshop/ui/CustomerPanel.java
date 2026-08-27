package com.motorworkshop.ui;

import com.motorworkshop.model.Customer;
import com.motorworkshop.model.Sale;
import com.motorworkshop.service.CustomerService;
import com.motorworkshop.service.SaleService;
import com.motorworkshop.ui.dialogs.CustomerDialog;
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
 * Customer Directory, Vehicle History, and CRM management panel.
 */
public class CustomerPanel extends JPanel {
    private final CustomerService customerService;
    private final SaleService saleService;
    private final Frame parentFrame;

    private JTextField txtSearch;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private List<Customer> currentCustomerList = new ArrayList<>();
    private JLabel lblTotalCustomers;

    public CustomerPanel(CustomerService customerService, SaleService saleService, Frame parentFrame) {
        this.customerService = customerService;
        this.saleService = saleService;
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

        // Header & Actions
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);

        JLabel lblTitle = new JLabel("Customer Profiles & Vehicle History");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.BG_LIGHT);

        JButton btnAdd = UIHelper.createSuccessButton("+ Add Customer");
        btnAdd.addActionListener(e -> onAddCustomer());

        JButton btnEdit = UIHelper.createPrimaryButton("Edit Customer");
        btnEdit.addActionListener(e -> onEditCustomer());

        JButton btnHistory = UIHelper.createSecondaryButton("Past Visits & Bills");
        btnHistory.addActionListener(e -> onViewCustomerHistory());

        JButton btnDelete = UIHelper.createDangerButton("Delete");
        btnDelete.addActionListener(e -> onDeleteCustomer());

        JButton btnExport = UIHelper.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, customerTable, "customer_profiles"));

        actionBtns.add(btnAdd);
        actionBtns.add(btnEdit);
        actionBtns.add(btnHistory);
        actionBtns.add(btnDelete);
        actionBtns.add(btnExport);

        headerBar.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterBar = UIHelper.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search name, phone, plate no, bike model...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        filterBar.add(new JLabel("Search Customers:"));
        filterBar.add(txtSearch);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"S.N.", "Name", "Phone Number", "Address", "Plate / Reg No", "Brand", "Model", "Total Visits", "Total Spent (Rs.)", "Last Visit"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        customerTable = new JTable(tableModel);
        UIHelper.styleTable(customerTable);

        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        // Footer Total
        JPanel footerBar = UIHelper.createCard();
        footerBar.setLayout(new BorderLayout());

        lblTotalCustomers = new JLabel("Total Registered Customers: 0");
        lblTotalCustomers.setFont(UIHelper.FONT_BOLD);
        footerBar.add(lblTotalCustomers, BorderLayout.WEST);
        add(footerBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                String query = txtSearch.getText().trim();
                currentCustomerList = customerService.searchCustomers(query);
                tableModel.setRowCount(0);

                int sn = 1;
                for (Customer c : currentCustomerList) {
                    tableModel.addRow(new Object[]{
                            sn++,
                            c.getName(),
                            c.getPhone(),
                            c.getAddress() != null ? c.getAddress() : "-",
                            c.getVehicleNumber() != null ? c.getVehicleNumber() : "-",
                            c.getVehicleBrand() != null ? c.getVehicleBrand() : "-",
                            c.getVehicleModel() != null ? c.getVehicleModel() : "-",
                            c.getTotalVisits(),
                            FormatUtil.formatCurrencyPlain(c.getTotalSpent()),
                            c.getLastVisitDate() != null ? DateUtil.formatForDisplay(c.getLastVisitDate()) : "Never"
                    });
                }
                lblTotalCustomers.setText("Total Registered Customers: " + currentCustomerList.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Customer getSelectedCustomer() {
        int row = customerTable.getSelectedRow();
        if (row >= 0 && row < currentCustomerList.size()) {
            return currentCustomerList.get(row);
        }
        return null;
    }

    private void onAddCustomer() {
        CustomerDialog dlg = new CustomerDialog(parentFrame, customerService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void onEditCustomer() {
        Customer c = getSelectedCustomer();
        if (c == null) {
            UIHelper.showWarning(this, "Please select a customer from the table to edit.");
            return;
        }
        CustomerDialog dlg = new CustomerDialog(parentFrame, customerService, c);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void onViewCustomerHistory() {
        Customer c = getSelectedCustomer();
        if (c == null) {
            UIHelper.showWarning(this, "Please select a customer to view visit history.");
            return;
        }

        try {
            List<Sale> sales = saleService.searchSales(c.getPhone(), null, null, null);
            if (sales.isEmpty()) {
                UIHelper.showInfo(this, "No previous bills or service visits found for " + c.getName());
                return;
            }

            String[] cols = {"S.N.", "Invoice #", "Date", "Vehicle", "Parts Fee (Rs.)", "Service Fee (Rs.)", "Total Paid (Rs.)", "Payment Method"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            int sn = 1;
            for (Sale s : sales) {
                model.addRow(new Object[]{
                        sn++,
                        s.getInvoiceNumber(),
                        s.getDate(),
                        s.getVehicleBrand() + " " + s.getVehicleModel() + " (" + s.getVehicleRegNo() + ")",
                        FormatUtil.formatCurrencyPlain(s.getPartsTotal()),
                        FormatUtil.formatCurrencyPlain(s.getServiceCharge()),
                        FormatUtil.formatCurrencyPlain(s.getTotalAmount()),
                        s.getPaymentMethod()
                });
            }

            JTable table = new JTable(model);
            UIHelper.styleTable(table);
            JScrollPane sp = new JScrollPane(table);
            sp.setPreferredSize(new Dimension(750, 300));
            JOptionPane.showMessageDialog(this, sp, "Visit History for " + c.getName() + " (" + c.getPhone() + ")", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            UIHelper.showError(this, "Error fetching history: " + e.getMessage());
        }
    }

    private void onDeleteCustomer() {
        Customer c = getSelectedCustomer();
        if (c == null) {
            UIHelper.showWarning(this, "Please select a customer to delete.");
            return;
        }
        if (UIHelper.showConfirm(this, "Are you sure you want to delete customer '" + c.getName() + "'?", "Confirm Customer Deletion")) {
            try {
                customerService.deleteCustomer(c.getCustomerId());
                loadData();
            } catch (Exception e) {
                UIHelper.showError(this, "Error deleting customer: " + e.getMessage());
            }
        }
    }
}
