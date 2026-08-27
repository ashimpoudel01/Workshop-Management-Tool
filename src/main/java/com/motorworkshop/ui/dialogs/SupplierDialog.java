package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Supplier;
import com.motorworkshop.service.PurchaseService;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog to Add or Edit Vendor / Supplier details.
 */
public class SupplierDialog extends JDialog {
    private final PurchaseService purchaseService;
    private final Supplier supplierToEdit;
    private Supplier savedSupplier = null;
    private boolean saved = false;

    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JTextField txtNotes;

    public SupplierDialog(Window owner, PurchaseService purchaseService, Supplier supplierToEdit) {
        super(owner, supplierToEdit == null ? "Add New Vendor / Supplier" : "Edit / Change Vendor Name & Details", ModalityType.APPLICATION_MODAL);
        this.purchaseService = purchaseService;
        this.supplierToEdit = supplierToEdit;

        initComponents();
        populateData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(18, 20, 18, 20));
        contentPane.setBackground(UIHelper.CARD_BG);

        JLabel lblTitle = new JLabel(supplierToEdit == null ? "Add New Vendor / Supplier" : "Edit Vendor Details");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        contentPane.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtName = new JTextField(20);
        txtPhone = new JTextField(15);
        txtEmail = new JTextField(20);
        txtAddress = new JTextField(20);
        txtNotes = new JTextField(20);

        int row = 0;
        addFormField(form, gbc, "Vendor / Supplier Name *:", txtName, 0, row, 2);

        row++;
        addFormField(form, gbc, "Contact Phone:", txtPhone, 0, row, 1);
        addFormField(form, gbc, "Email Address:", txtEmail, 1, row, 1);

        row++;
        addFormField(form, gbc, "Office / Store Address:", txtAddress, 0, row, 2);

        row++;
        addFormField(form, gbc, "Notes / Payment Terms:", txtNotes, 0, row, 2);

        contentPane.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIHelper.CARD_BG);
        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JButton btnSave = UIHelper.createPrimaryButton(supplierToEdit == null ? "Save Vendor" : "Update Vendor");
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        contentPane.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int gridX, int gridY, int gridWidth) {
        gbc.gridx = gridX;
        gbc.gridy = gridY;
        gbc.gridwidth = gridWidth;

        JPanel wrapper = new JPanel(new BorderLayout(4, 4));
        wrapper.setBackground(UIHelper.CARD_BG);
        JLabel label = new JLabel(labelText);
        label.setFont(UIHelper.FONT_BOLD);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);

        panel.add(wrapper, gbc);
    }

    private void populateData() {
        if (supplierToEdit != null) {
            txtName.setText(supplierToEdit.getName());
            txtPhone.setText(supplierToEdit.getPhone() != null ? supplierToEdit.getPhone() : "");
            txtEmail.setText(supplierToEdit.getEmail() != null ? supplierToEdit.getEmail() : "");
            txtAddress.setText(supplierToEdit.getAddress() != null ? supplierToEdit.getAddress() : "");
            txtNotes.setText(supplierToEdit.getNotes() != null ? supplierToEdit.getNotes() : "");
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            UIHelper.showWarning(this, "Vendor Name is required!");
            txtName.requestFocus();
            return;
        }

        try {
            if (supplierToEdit == null) {
                Supplier s = new Supplier();
                s.setName(name);
                s.setPhone(txtPhone.getText().trim());
                s.setEmail(txtEmail.getText().trim());
                s.setAddress(txtAddress.getText().trim());
                s.setNotes(txtNotes.getText().trim());

                purchaseService.addSupplier(s);
                savedSupplier = s;
                UIHelper.showInfo(this, "Vendor '" + name + "' successfully created!");
            } else {
                supplierToEdit.setName(name);
                supplierToEdit.setPhone(txtPhone.getText().trim());
                supplierToEdit.setEmail(txtEmail.getText().trim());
                supplierToEdit.setAddress(txtAddress.getText().trim());
                supplierToEdit.setNotes(txtNotes.getText().trim());

                purchaseService.updateSupplier(supplierToEdit);
                savedSupplier = supplierToEdit;
                UIHelper.showInfo(this, "Vendor '" + name + "' successfully updated!");
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving vendor: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Supplier getSavedSupplier() {
        return savedSupplier;
    }
}
