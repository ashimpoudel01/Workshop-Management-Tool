package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Expense;
import com.motorworkshop.service.OwnerExpenseService;
import com.motorworkshop.util.DateUtil;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class OwnerExpenseDialog extends JDialog {
    private final OwnerExpenseService expenseService;
    private final Expense expenseToEdit;
    private boolean saved;
    private final JTextField txtDate = new JTextField(10);
    private final JComboBox<String> cbCategory;
    private final JTextField txtDescription = new JTextField(20);
    private final JTextField txtAmount = new JTextField("0.00", 10);
    private final JComboBox<String> cbPayment = new JComboBox<>(new String[]{"Cash", "Bank", "eSewa", "Khalti", "Other"});
    private final JTextField txtNotes = new JTextField(20);

    public OwnerExpenseDialog(Frame owner, OwnerExpenseService expenseService, Expense expenseToEdit) {
        super(owner, expenseToEdit == null ? "Record Owner Expense" : "Edit Owner Expense", true);
        this.expenseService = expenseService;
        this.expenseToEdit = expenseToEdit;
        cbCategory = new JComboBox<>(OwnerExpenseService.CATEGORIES.toArray(new String[0]));
        initComponents();
        populateData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBorder(new EmptyBorder(18, 20, 18, 20));
        content.setBackground(UIHelper.CARD_BG);
        content.add(UIHelper.createSectionHeader(
                expenseToEdit == null ? "Record Owner Expense" : "Edit Owner Expense",
                "Track personal or owner spending separately from workshop operating expenses"
        ), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setBackground(UIHelper.CARD_BG);
        txtDate.setText(DateUtil.todayBs());
        form.add(new JLabel("Date:")); form.add(txtDate);
        form.add(new JLabel("Category:")); form.add(cbCategory);
        form.add(new JLabel("Description *:")); form.add(txtDescription);
        form.add(new JLabel("Amount (Rs.) *:")); form.add(txtAmount);
        form.add(new JLabel("Payment Method:")); form.add(cbPayment);
        form.add(new JLabel("Notes:")); form.add(txtNotes);
        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(UIHelper.CARD_BG);
        JButton cancel = UIHelper.createSecondaryButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = UIHelper.createSuccessButton(expenseToEdit == null ? "Save Owner Expense" : "Update Owner Expense");
        save.addActionListener(e -> onSave());
        buttons.add(cancel); buttons.add(save);
        content.add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(save);
        setContentPane(content);
    }

    private void populateData() {
        if (expenseToEdit == null) return;
        txtDate.setText(DateUtil.toDisplayDate(expenseToEdit.getDate()));
        cbCategory.setSelectedItem(expenseToEdit.getCategory());
        txtDescription.setText(expenseToEdit.getDescription());
        txtAmount.setText(String.valueOf(expenseToEdit.getAmount()));
        cbPayment.setSelectedItem(expenseToEdit.getPaymentMethod());
        txtNotes.setText(expenseToEdit.getNotes());
    }

    private void onSave() {
        try {
            Expense expense = expenseToEdit == null ? new Expense() : expenseToEdit;
            expense.setDate(DateUtil.toStorageDate(txtDate.getText().trim()));
            expense.setCategory((String) cbCategory.getSelectedItem());
            expense.setDescription(txtDescription.getText().trim());
            expense.setAmount(FormatUtil.parseDouble(txtAmount.getText(), -1));
            expense.setPaymentMethod((String) cbPayment.getSelectedItem());
            expense.setNotes(txtNotes.getText().trim());
            if (expenseToEdit == null) expenseService.addExpense(expense); else expenseService.updateExpense(expense);
            saved = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving owner expense: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}