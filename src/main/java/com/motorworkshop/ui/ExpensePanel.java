package com.motorworkshop.ui;

import com.motorworkshop.model.Expense;
import com.motorworkshop.service.ExpenseService;
import com.motorworkshop.ui.dialogs.ExpenseDialog;
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
 * Operating Expenses management panel.
 */
public class ExpensePanel extends JPanel {
    private final ExpenseService expenseService;
    private final Frame parentFrame;

    private JTextField txtSearch;
    private JComboBox<String> cbCategoryFilter;
    private JTextField txtStartDate;
    private JTextField txtEndDate;

    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private List<Expense> currentExpenseList = new ArrayList<>();
    private JLabel lblTotalExpenses;

    public ExpensePanel(ExpenseService expenseService, Frame parentFrame) {
        this.expenseService = expenseService;
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

        JLabel lblTitle = new JLabel("Daily Workshop Operating Expenses");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setBackground(UIHelper.BG_LIGHT);

        JButton btnAdd = UIHelper.createSuccessButton("+ Record Expense");
        btnAdd.addActionListener(e -> onAddExpense());

        JButton btnEdit = UIHelper.createPrimaryButton("Edit Expense");
        btnEdit.addActionListener(e -> onEditExpense());

        JButton btnDelete = UIHelper.createDangerButton("Delete Expense");
        btnDelete.addActionListener(e -> onDeleteExpense());

        JButton btnExport = UIHelper.createSecondaryButton("Export CSV");
        btnExport.addActionListener(e -> CsvExporter.exportTableToCsv(this, expenseTable, "expenses_report"));

        actionBtns.add(btnAdd);
        actionBtns.add(btnEdit);
        actionBtns.add(btnDelete);
        actionBtns.add(btnExport);

        headerBar.add(actionBtns, BorderLayout.EAST);
        topContainer.add(headerBar, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterBar = UIHelper.createCard();
        filterBar.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        txtSearch = new JTextField(12);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search description, notes...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });

        List<String> catList = new ArrayList<>();
        catList.add("All Categories");
        catList.addAll(ExpenseService.EXPENSE_CATEGORIES);
        cbCategoryFilter = new JComboBox<>(catList.toArray(new String[0]));
        cbCategoryFilter.addActionListener(e -> loadData());

        txtStartDate = new JTextField(DateUtil.startOfMonth(), 9);
        txtEndDate = new JTextField(DateUtil.today(), 9);

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
        filterBar.add(new JLabel("Category:"));
        filterBar.add(cbCategoryFilter);
        filterBar.add(new JLabel("From:"));
        filterBar.add(txtStartDate);
        filterBar.add(new JLabel("To:"));
        filterBar.add(txtEndDate);
        filterBar.add(btnFilter);
        filterBar.add(btnToday);
        filterBar.add(btnAllTime);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Date", "Category", "Description / Title", "Amount (Rs.)", "Payment Method", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        expenseTable = new JTable(tableModel);
        UIHelper.styleTable(expenseTable);

        add(new JScrollPane(expenseTable), BorderLayout.CENTER);

        // Footer Total
        JPanel footerBar = UIHelper.createCard();
        footerBar.setLayout(new BorderLayout());

        lblTotalExpenses = new JLabel("Total Operating Expenses: Rs. 0.00");
        lblTotalExpenses.setFont(UIHelper.FONT_BIG_NUMBER);
        lblTotalExpenses.setForeground(UIHelper.DANGER_COLOR);

        footerBar.add(lblTotalExpenses, BorderLayout.EAST);
        add(footerBar, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                String search = txtSearch.getText().trim();
                String category = (String) cbCategoryFilter.getSelectedItem();
                String start = txtStartDate.getText().trim();
                String end = txtEndDate.getText().trim();

                currentExpenseList = expenseService.searchExpenses(search, category, start, end);
                tableModel.setRowCount(0);

                double total = 0.0;
                for (Expense exp : currentExpenseList) {
                    total += exp.getAmount();
                    tableModel.addRow(new Object[]{
                            exp.getExpenseId(),
                            exp.getDate(),
                            exp.getCategory(),
                            exp.getDescription(),
                            FormatUtil.formatCurrencyPlain(exp.getAmount()),
                            exp.getPaymentMethod(),
                            exp.getNotes() != null ? exp.getNotes() : ""
                    });
                }
                lblTotalExpenses.setText("Total Operating Expenses: " + FormatUtil.formatCurrency(total));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void onAddExpense() {
        ExpenseDialog dlg = new ExpenseDialog(parentFrame, expenseService, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void onEditExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0 || row >= currentExpenseList.size()) {
            UIHelper.showWarning(this, "Please select an expense entry to edit.");
            return;
        }
        Expense exp = currentExpenseList.get(row);
        ExpenseDialog dlg = new ExpenseDialog(parentFrame, expenseService, exp);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadData();
        }
    }

    private void onDeleteExpense() {
        int row = expenseTable.getSelectedRow();
        if (row < 0 || row >= currentExpenseList.size()) {
            UIHelper.showWarning(this, "Please select an expense entry to delete.");
            return;
        }
        Expense exp = currentExpenseList.get(row);
        if (UIHelper.showConfirm(this, "Delete expense '" + exp.getDescription() + "' (" + FormatUtil.formatCurrency(exp.getAmount()) + ")?", "Confirm Deletion")) {
            try {
                expenseService.deleteExpense(exp.getExpenseId());
                loadData();
            } catch (Exception e) {
                UIHelper.showError(this, "Error deleting expense: " + e.getMessage());
            }
        }
    }
}
