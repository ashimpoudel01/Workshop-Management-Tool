package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.InventoryTransaction;
import com.motorworkshop.model.Product;
import com.motorworkshop.service.InventoryService;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog to adjust stock up or down (Adjustment, Damage, Return) with mandatory audit logging.
 */
public class StockAdjustDialog extends JDialog {
    private final InventoryService inventoryService;
    private final Product product;
    private boolean adjusted = false;

    private JComboBox<String> cbAction;
    private JTextField txtQuantity;
    private JComboBox<InventoryTransaction.Type> cbType;
    private JTextField txtNotes;

    public StockAdjustDialog(Frame owner, InventoryService inventoryService, Product product) {
        super(owner, "Adjust Stock - " + product.getPartName(), true);
        this.inventoryService = inventoryService;
        this.product = product;

        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBorder(new EmptyBorder(18, 20, 18, 20));
        contentPane.setBackground(UIHelper.CARD_BG);

        // Header info
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBackground(UIHelper.CARD_BG);
        JLabel lblTitle = new JLabel("Adjust Inventory Stock");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        JLabel lblSub = new JLabel("Item: " + product.getPartName() + " | Current Stock: " + product.getCurrentQuantity() + " " + product.getUnit());
        lblSub.setFont(UIHelper.FONT_BOLD);
        headerPanel.add(lblTitle);
        headerPanel.add(lblSub);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBackground(UIHelper.CARD_BG);

        cbAction = new JComboBox<>(new String[]{"Increase Stock (+)", "Decrease Stock (-)"});
        txtQuantity = new JTextField("1", 8);
        cbType = new JComboBox<>(new InventoryTransaction.Type[]{
                InventoryTransaction.Type.STOCK_ADJUSTMENT,
                InventoryTransaction.Type.DAMAGE,
                InventoryTransaction.Type.RETURN,
                InventoryTransaction.Type.SERVICE_USAGE
        });
        txtNotes = new JTextField(20);

        form.add(new JLabel("Adjustment Mode:"));
        form.add(cbAction);
        form.add(new JLabel("Quantity:"));
        form.add(txtQuantity);
        form.add(new JLabel("Movement Reason / Type:"));
        form.add(cbType);
        form.add(new JLabel("Remarks / Notes:"));
        form.add(txtNotes);

        contentPane.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(UIHelper.CARD_BG);
        JButton btnCancel = UIHelper.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        JButton btnApply = UIHelper.createPrimaryButton("Apply Adjustment");
        btnApply.addActionListener(e -> onApply());

        btnPanel.add(btnCancel);
        btnPanel.add(btnApply);
        contentPane.add(btnPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }

    private void onApply() {
        int qty = FormatUtil.parseInt(txtQuantity.getText(), 0);
        if (qty <= 0) {
            UIHelper.showWarning(this, "Quantity must be greater than 0!");
            txtQuantity.requestFocus();
            return;
        }

        boolean isIncrease = cbAction.getSelectedIndex() == 0;
        int delta = isIncrease ? qty : -qty;

        if (!isIncrease && (product.getCurrentQuantity() - qty < 0)) {
            UIHelper.showWarning(this, "Cannot decrease stock below 0! Current Stock: " + product.getCurrentQuantity());
            return;
        }

        InventoryTransaction.Type type = (InventoryTransaction.Type) cbType.getSelectedItem();
        String notes = txtNotes.getText().trim();
        if (notes.isEmpty()) {
            notes = isIncrease ? "Manual stock addition" : "Manual stock reduction";
        }

        try {
            inventoryService.adjustStock(product.getItemId(), delta, type, notes);
            UIHelper.showInfo(this, "Stock successfully updated! New Stock: " + (product.getCurrentQuantity() + delta));
            adjusted = true;
            dispose();
        } catch (Exception ex) {
            UIHelper.showError(this, "Error adjusting stock: " + ex.getMessage());
        }
    }

    public boolean isAdjusted() {
        return adjusted;
    }
}
