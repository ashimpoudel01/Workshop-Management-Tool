package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.ServiceItem;
import com.motorworkshop.service.PricingService;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog to Add or Edit Workshop predefined services and charges.
 */
public class ServiceDialog extends JDialog {
    private final PricingService pricingService;
    private final ServiceItem serviceToEdit;
    private boolean saved = false;

    private JTextField txtServiceName;
    private JTextField txtDefaultPrice;
    private JTextField txtDuration;
    private JTextField txtDescription;

    public ServiceDialog(Frame owner, PricingService pricingService, ServiceItem serviceToEdit) {
        super(owner, serviceToEdit == null ? "Add Workshop Service" : "Edit Service Details", true);
        this.pricingService = pricingService;
        this.serviceToEdit = serviceToEdit;

        initComponents();
        populateData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(18, 20, 18, 20));
        contentPane.setBackground(UIHelper.CARD_BG);

        JLabel lblTitle = new JLabel(serviceToEdit == null ? "Define Workshop Service" : "Edit Service");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        contentPane.add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setBackground(UIHelper.CARD_BG);

        txtServiceName = new JTextField(20);
        txtDefaultPrice = new JTextField("0.00", 10);
        txtDuration = new JTextField("30", 8);
        txtDescription = new JTextField(25);

        form.add(new JLabel("Service Name *:"));
        form.add(txtServiceName);
        form.add(new JLabel("Default Charge (Rs.) *:"));
        form.add(txtDefaultPrice);
        form.add(new JLabel("Estimated Duration (Mins):"));
        form.add(txtDuration);
        form.add(new JLabel("Description / Scope:"));
        form.add(txtDescription);

        contentPane.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIHelper.CARD_BG);
        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JButton btnSave = UIHelper.createPrimaryButton(serviceToEdit == null ? "Save Service" : "Update Service");
        btnSave.addActionListener(e -> onSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        contentPane.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void populateData() {
        if (serviceToEdit != null) {
            txtServiceName.setText(serviceToEdit.getServiceName());
            txtDefaultPrice.setText(String.valueOf(serviceToEdit.getDefaultPrice()));
            txtDuration.setText(String.valueOf(serviceToEdit.getEstimatedDurationMinutes()));
            txtDescription.setText(serviceToEdit.getDescription());
        }
    }

    private void onSave() {
        String name = txtServiceName.getText().trim();
        if (name.isEmpty()) {
            UIHelper.showWarning(this, "Service Name is required!");
            txtServiceName.requestFocus();
            return;
        }

        double price = FormatUtil.parseDouble(txtDefaultPrice.getText(), -1);
        if (price < 0) {
            UIHelper.showWarning(this, "Please enter a valid price!");
            txtDefaultPrice.requestFocus();
            return;
        }

        int duration = FormatUtil.parseInt(txtDuration.getText(), 30);

        try {
            if (serviceToEdit == null) {
                ServiceItem s = new ServiceItem();
                s.setServiceName(name);
                s.setDefaultPrice(price);
                s.setEstimatedDurationMinutes(duration);
                s.setDescription(txtDescription.getText().trim());
                s.setActive(true);

                pricingService.addService(s);
                UIHelper.showInfo(this, "Service successfully created!");
            } else {
                serviceToEdit.setServiceName(name);
                serviceToEdit.setDefaultPrice(price);
                serviceToEdit.setEstimatedDurationMinutes(duration);
                serviceToEdit.setDescription(txtDescription.getText().trim());

                pricingService.updateService(serviceToEdit);
                UIHelper.showInfo(this, "Service successfully updated!");
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving service: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
