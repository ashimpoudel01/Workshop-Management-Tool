package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Expense;
import com.motorworkshop.service.ExpenseService;
import com.motorworkshop.util.DateUtil;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog to Add or Edit Workshop Operating Expenses.
 */
public class ExpenseDialog extends JDialog {
    private final ExpenseService expenseService;
    private final Expense expenseToEdit;
    private boolean saved = false;

    private JTextField txtDate;
    private JComboBox<String> cbCategory;
    private JTextField txtDescription;
    private JTextField txtAmount;
    private JComboBox<String> cbPaymentMethod;
    private JTextField txtNotes;

    public ExpenseDialog(Frame owner, ExpenseService expenseService, Expense expenseToEdit) {
        super(owner, expenseToEdit == null ? "Record Workshop Expense" : "Edit Expense", true);
        this.expenseService = expenseService;
        this.expenseToEdit = expenseToEdit;

        initComponents();
        populateData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(18, 20, 18, 20));
        contentPane.setBackground(UIHelper.CARD_BG);

        JLabel lblTitle = new JLabel(expenseToEdit == null ? "Record New Expense" : "Edit Expense");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        contentPane.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setBackground(UIHelper.CARD_BG);

        txtDate = new JTextField(DateUtil.today(), 10);
        cbCategory = new JComboBox<>(ExpenseService.EXPENSE_CATEGORIES.toArray(new String[0]));
        txtDescription = new JTextField(20);
        txtAmount = new JTextField("0.00", 10);
        cbPaymentMethod = new JComboBox<>(new String[]{"Cash", "Bank", "eSewa", "Khalti", "Other"});
        txtNotes = new JTextField(20);

        form.add(new JLabel("Date (yyyy-MM-dd) *:"));
        form.add(txtDate);
        form.add(new JLabel("Expense Category *:"));
        form.add(cbCategory);
        form.add(new JLabel("Description / Title *:"));
        form.add(txtDescription);
        form.add(new JLabel("Amount (Rs.) *:"));
        form.add(txtAmount);
        form.add(new JLabel("Payment Method:"));
        form.add(cbPaymentMethod);
        form.add(new JLabel("Remarks / Notes:"));
        form.add(txtNotes);

        contentPane.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIHelper.CARD_BG);
        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JButton btnSave = UIHelper.createPrimaryButton(expenseToEdit == null ? "Save Expense" : "Update Expense");
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        contentPane.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void populateData() {
        if (expenseToEdit != null) {
            txtDate.setText(expenseToEdit.getDate());
            cbCategory.setSelectedItem(expenseToEdit.getCategory());
            txtDescription.setText(expenseToEdit.getDescription());
            txtAmount.setText(String.valueOf(expenseToEdit.getAmount()));
            cbPaymentMethod.setSelectedItem(expenseToEdit.getPaymentMethod());
            txtNotes.setText(expenseToEdit.getNotes());
        }
    }

    private void onSave() {
        String date = txtDate.getText().trim();
        String desc = txtDescription.getText().trim();
        double amount = FormatUtil.parseDouble(txtAmount.getText(), -1);

        if (date.isEmpty()) {
            UIHelper.showWarning(this, "Date is required!");
            txtDate.requestFocus();
            return;
        }
        if (desc.isEmpty()) {
            UIHelper.showWarning(this, "Description is required!");
            txtDescription.requestFocus();
            return;
        }
        if (amount <= 0) {
            UIHelper.showWarning(this, "Expense amount must be greater than 0!");
            txtAmount.requestFocus();
            return;
        }

        try {
            if (expenseToEdit == null) {
                Expense exp = new Expense();
                exp.setDate(date);
                exp.setCategory((String) cbCategory.getSelectedItem());
                exp.setDescription(desc);
                exp.setAmount(amount);
                exp.setPaymentMethod((String) cbPaymentMethod.getSelectedItem());
                exp.setNotes(txtNotes.getText().trim());

                expenseService.addExpense(exp);
                UIHelper.showInfo(this, "Expense recorded successfully!");
            } else {
                expenseToEdit.setDate(date);
                expenseToEdit.setCategory((String) cbCategory.getSelectedItem());
                expenseToEdit.setDescription(desc);
                expenseToEdit.setAmount(amount);
                expenseToEdit.setPaymentMethod((String) cbPaymentMethod.getSelectedItem());
                expenseToEdit.setNotes(txtNotes.getText().trim());

                expenseService.updateExpense(expenseToEdit);
                UIHelper.showInfo(this, "Expense updated successfully!");
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving expense: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
