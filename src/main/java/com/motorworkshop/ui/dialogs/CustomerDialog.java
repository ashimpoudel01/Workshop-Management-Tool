package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Customer;
import com.motorworkshop.service.CustomerService;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog to Add or Edit Customer and Motorcycle profiles.
 */
public class CustomerDialog extends JDialog {
    private final CustomerService customerService;
    private final Customer customerToEdit;
    private boolean saved = false;

    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtVehicleNo;
    private JTextField txtBrand;
    private JTextField txtModel;
    private JTextField txtNotes;

    public CustomerDialog(Frame owner, CustomerService customerService, Customer customerToEdit) {
        super(owner, customerToEdit == null ? "Add New Customer" : "Edit Customer Details", true);
        this.customerService = customerService;
        this.customerToEdit = customerToEdit;

        initComponents();
        populateData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(18, 20, 18, 20));
        contentPane.setBackground(UIHelper.CARD_BG);

        JLabel lblTitle = new JLabel(customerToEdit == null ? "Customer & Vehicle Information" : "Edit Customer");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        contentPane.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtName = new JTextField(18);
        txtPhone = new JTextField(15);
        txtAddress = new JTextField(20);
        txtVehicleNo = new JTextField(15);
        txtBrand = new JTextField(12);
        txtModel = new JTextField(15);
        txtNotes = new JTextField(20);

        int row = 0;
        addFormField(form, gbc, "Full Name *:", txtName, 0, row, 1);
        addFormField(form, gbc, "Phone Number *:", txtPhone, 1, row, 1);

        row++;
        addFormField(form, gbc, "Address / Location:", txtAddress, 0, row, 2);

        row++;
        addFormField(form, gbc, "Vehicle Plate / Reg No:", txtVehicleNo, 0, row, 1);
        addFormField(form, gbc, "Vehicle Brand (e.g. Honda, Bajaj):", txtBrand, 1, row, 1);

        row++;
        addFormField(form, gbc, "Vehicle Model (e.g. Pulsar 150, Dio):", txtModel, 0, row, 1);
        addFormField(form, gbc, "Customer Notes:", txtNotes, 1, row, 1);

        contentPane.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIHelper.CARD_BG);
        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JButton btnSave = UIHelper.createPrimaryButton(customerToEdit == null ? "Save Customer" : "Update Customer");
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
        if (customerToEdit != null) {
            txtName.setText(customerToEdit.getName());
            txtPhone.setText(customerToEdit.getPhone());
            txtAddress.setText(customerToEdit.getAddress());
            txtVehicleNo.setText(customerToEdit.getVehicleNumber());
            txtBrand.setText(customerToEdit.getVehicleBrand());
            txtModel.setText(customerToEdit.getVehicleModel());
            txtNotes.setText(customerToEdit.getNotes());
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();

        if (name.isEmpty()) {
            UIHelper.showWarning(this, "Customer Name is required!");
            txtName.requestFocus();
            return;
        }
        if (phone.isEmpty()) {
            UIHelper.showWarning(this, "Customer Phone number is required!");
            txtPhone.requestFocus();
            return;
        }

        try {
            if (customerToEdit == null) {
                Customer c = new Customer();
                c.setName(name);
                c.setPhone(phone);
                c.setAddress(txtAddress.getText().trim());
                c.setVehicleNumber(txtVehicleNo.getText().trim());
                c.setVehicleBrand(txtBrand.getText().trim());
                c.setVehicleModel(txtModel.getText().trim());
                c.setNotes(txtNotes.getText().trim());

                customerService.addCustomer(c);
                UIHelper.showInfo(this, "Customer successfully saved!");
            } else {
                customerToEdit.setName(name);
                customerToEdit.setPhone(phone);
                customerToEdit.setAddress(txtAddress.getText().trim());
                customerToEdit.setVehicleNumber(txtVehicleNo.getText().trim());
                customerToEdit.setVehicleBrand(txtBrand.getText().trim());
                customerToEdit.setVehicleModel(txtModel.getText().trim());
                customerToEdit.setNotes(txtNotes.getText().trim());

                customerService.updateCustomer(customerToEdit);
                UIHelper.showInfo(this, "Customer details updated!");
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving customer: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
