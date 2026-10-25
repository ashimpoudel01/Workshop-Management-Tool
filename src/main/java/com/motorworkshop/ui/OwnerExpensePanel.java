package com.motorworkshop.ui;

import com.motorworkshop.model.Expense;
import com.motorworkshop.service.OwnerExpenseService;
import com.motorworkshop.ui.dialogs.OwnerExpenseDialog;
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

public class OwnerExpensePanel extends JPanel {
    private final OwnerExpenseService expenseService;
    private final Frame parentFrame;
    private JTextField txtSearch;
    private JComboBox<String> cbCategory;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private List<Expense> currentExpenses = new ArrayList<>();
    private JLabel lblTotal;

    public OwnerExpensePanel(OwnerExpenseService expenseService, Frame parentFrame) {
        this.expenseService = expenseService;
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(15, 18, 15, 18));
        setBackground(UIHelper.BG_LIGHT);
        initComponents();
        loadData();
    }

    private void initComponents() {
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(UIHelper.BG_LIGHT);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.BG_LIGHT);
        header.add(UIHelper.createSectionHeader("Owner Expenses", "Track personal owner spending separately from workshop operating expenses"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton add = UIHelper.createSuccessButton("+ Record Owner Expense");
        add.addActionListener(e -> onAdd());
        JButton edit = UIHelper.createPrimaryButton("Edit Expense");
        edit.addActionListener(e -> onEdit());
        JButton delete = UIHelper.createDangerButton("Delete Expense");
        delete.addActionListener(e -> onDelete());
        JButton export = UIHelper.createSecondaryButton("Export CSV");
        export.addActionListener(e -> CsvExporter.exportTableToCsv(this, expenseTable, "owner_expenses"));
        actions.add(add); actions.add(edit); actions.add(delete); actions.add(export);
        header.add(actions, BorderLayout.EAST);
        top.add(header, BorderLayout.NORTH);

        JPanel filters = UIHelper.createCard();
        filters.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));
        txtSearch = new JTextField(14);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search description, notes...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(); }
            public void removeUpdate(DocumentEvent e) { loadData(); }
            public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        cbCategory = new JComboBox<>();
        cbCategory.addItem("All Categories");
        for (String category : OwnerExpenseService.CATEGORIES) cbCategory.addItem(category);
        cbCategory.addActionListener(e -> loadData());
        txtStartDate = new JTextField(DateUtil.startOfMonthBs(), 9);
        txtEndDate = new JTextField(DateUtil.todayBs(), 9);
        JButton filter = UIHelper.createSecondaryButton("Filter Dates");
        filter.addActionListener(e -> loadData());
        JButton today = UIHelper.createSecondaryButton("Today");
        today.addActionListener(e -> { txtStartDate.setText(DateUtil.todayBs()); txtEndDate.setText(DateUtil.todayBs()); loadData(); });
        JButton all = UIHelper.createSecondaryButton("All Time");
        all.addActionListener(e -> { txtStartDate.setText(""); txtEndDate.setText(""); loadData(); });
        filters.add(new JLabel("Search:")); filters.add(txtSearch);
        filters.add(new JLabel("Category:")); filters.add(cbCategory);
        filters.add(new JLabel("From:")); filters.add(txtStartDate);
        filters.add(new JLabel("To:")); filters.add(txtEndDate);
        filters.add(filter); filters.add(today); filters.add(all);
        top.add(filters, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"S.N.", "Date", "Category", "Description", "Amount (Rs.)", "Payment Method", "Notes"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        expenseTable = new JTable(tableModel);
        UIHelper.styleTable(expenseTable);
        add(new JScrollPane(expenseTable), BorderLayout.CENTER);

        JPanel footer = UIHelper.createCard();
        footer.setLayout(new BorderLayout());
        lblTotal = new JLabel("Total Owner Expenses: Rs. 0.00");
        lblTotal.setFont(UIHelper.FONT_BIG_NUMBER);
        lblTotal.setForeground(UIHelper.DANGER_COLOR);
        footer.add(lblTotal, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                currentExpenses = expenseService.searchExpenses(txtSearch.getText().trim(), (String) cbCategory.getSelectedItem(),
                        DateUtil.toStorageDateOrEmpty(txtStartDate.getText()), DateUtil.toStorageDateOrEmpty(txtEndDate.getText()));
                tableModel.setRowCount(0);
                double total = 0.0;
                int serial = 1;
                for (Expense expense : currentExpenses) {
                    total += expense.getAmount();
                    tableModel.addRow(new Object[]{serial++, DateUtil.toDisplayDate(expense.getDate()), expense.getCategory(),
                            expense.getDescription(), FormatUtil.formatCurrencyPlain(expense.getAmount()), expense.getPaymentMethod(),
                            expense.getNotes() != null ? expense.getNotes() : ""});
                }
                lblTotal.setText("Total Owner Expenses: " + FormatUtil.formatCurrency(total));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Expense getSelected() {
        int row = expenseTable.getSelectedRow();
        return row >= 0 && row < currentExpenses.size() ? currentExpenses.get(row) : null;
    }

    private void onAdd() {
        OwnerExpenseDialog dialog = new OwnerExpenseDialog(parentFrame, expenseService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void onEdit() {
        Expense expense = getSelected();
        if (expense == null) { UIHelper.showWarning(this, "Please select an owner expense to edit."); return; }
        OwnerExpenseDialog dialog = new OwnerExpenseDialog(parentFrame, expenseService, expense);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void onDelete() {
        Expense expense = getSelected();
        if (expense == null) { UIHelper.showWarning(this, "Please select an owner expense to delete."); return; }
        if (UIHelper.showConfirm(this, "Delete owner expense '" + expense.getDescription() + "'?", "Confirm Deletion")) {
            try { expenseService.deleteExpense(expense.getExpenseId()); loadData(); }
            catch (Exception e) { UIHelper.showError(this, "Error deleting owner expense: " + e.getMessage()); }
        }
    }
}
