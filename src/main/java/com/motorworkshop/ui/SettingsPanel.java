package com.motorworkshop.ui;

import com.motorworkshop.database.DatabaseManager;
import com.motorworkshop.model.WorkshopSetting;
import com.motorworkshop.service.BackupRestoreService;
import com.motorworkshop.service.SettingService;
import com.motorworkshop.util.FormatUtil;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

/**
 * Workshop Profile, General Preferences, and SQLite Database Backup & Restore settings.
 */
public class SettingsPanel extends JPanel {
    private final SettingService settingService;
    private final Frame parentFrame;

    private JTextField txtWorkshopName;
    private JTextField txtAddress;
    private JTextField txtPhone;
    private JTextField txtPanVat;
    private JTextField txtInvoicePrefix;
    private JTextField txtDefaultCurrency;
    private JTextField txtDefaultServiceCharge;
    private JTextField txtLowStockThreshold;
    private JCheckBox chkAllowNegativeStock;
    private JLabel lblDbPath;

    public SettingsPanel(SettingService settingService, Frame parentFrame) {
        this.settingService = settingService;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 18, 15, 18));
        setBackground(UIHelper.BG_LIGHT);

        initComponents();
        loadSettings();
    }

    private void initComponents() {
        // Header
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIHelper.BG_LIGHT);

        JLabel lblTitle = new JLabel("Workshop Settings & Data Backup");
        lblTitle.setFont(UIHelper.FONT_TITLE);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        headerBar.add(lblTitle, BorderLayout.WEST);

        JButton btnSave = UIHelper.createSuccessButton("Save Settings");
        btnSave.addActionListener(e -> onSaveSettings());
        headerBar.add(btnSave, BorderLayout.EAST);

        add(headerBar, BorderLayout.NORTH);

        // Center Content Grid
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        centerGrid.setBackground(UIHelper.BG_LIGHT);

        // Left: Workshop Profile & Invoicing
        JPanel leftCard = UIHelper.createCard();
        leftCard.setLayout(new BorderLayout(8, 8));
        leftCard.setBorder(new TitledBorder("Workshop Profile & Invoice Information"));

        JPanel profileForm = new JPanel(new GridLayout(6, 2, 8, 12));
        profileForm.setBackground(UIHelper.CARD_BG);

        txtWorkshopName = new JTextField(20);
        txtAddress = new JTextField(20);
        txtPhone = new JTextField(15);
        txtPanVat = new JTextField(15);
        txtInvoicePrefix = new JTextField("INV-", 8);
        txtDefaultCurrency = new JTextField("Rs.", 8);

        profileForm.add(new JLabel("Workshop Name *:"));
        profileForm.add(txtWorkshopName);
        profileForm.add(new JLabel("Workshop Address:"));
        profileForm.add(txtAddress);
        profileForm.add(new JLabel("Phone / Mobile:"));
        profileForm.add(txtPhone);
        profileForm.add(new JLabel("PAN / VAT Number:"));
        profileForm.add(txtPanVat);
        profileForm.add(new JLabel("Invoice Number Prefix:"));
        profileForm.add(txtInvoicePrefix);
        profileForm.add(new JLabel("Currency Symbol:"));
        profileForm.add(txtDefaultCurrency);

        leftCard.add(profileForm, BorderLayout.NORTH);
        centerGrid.add(leftCard);

        // Right: Operational Rules & Database Management
        JPanel rightContainer = new JPanel(new GridLayout(2, 1, 12, 12));
        rightContainer.setBackground(UIHelper.BG_LIGHT);

        // Rules Card
        JPanel rulesCard = UIHelper.createCard();
        rulesCard.setLayout(new BorderLayout(8, 8));
        rulesCard.setBorder(new TitledBorder("Inventory & Business Rules"));

        JPanel rulesForm = new JPanel(new GridLayout(3, 2, 8, 12));
        rulesForm.setBackground(UIHelper.CARD_BG);

        txtDefaultServiceCharge = new JTextField("250.00", 8);
        txtLowStockThreshold = new JTextField("5", 8);
        chkAllowNegativeStock = new JCheckBox("Allow sales when stock is 0 (Admin override)");
        chkAllowNegativeStock.setBackground(UIHelper.CARD_BG);

        rulesForm.add(new JLabel("Default Service Labor Fee (Rs.):"));
        rulesForm.add(txtDefaultServiceCharge);
        rulesForm.add(new JLabel("Low Stock Alert Threshold:"));
        rulesForm.add(txtLowStockThreshold);
        rulesForm.add(new JLabel("Negative Stock Policy:"));
        rulesForm.add(chkAllowNegativeStock);

        rulesCard.add(rulesForm, BorderLayout.NORTH);
        rightContainer.add(rulesCard);

        // Database Backup / Restore Card
        JPanel dbCard = UIHelper.createCard();
        dbCard.setLayout(new BorderLayout(8, 8));
        dbCard.setBorder(new TitledBorder("Offline Database Safety & Backup"));

        JPanel dbContent = new JPanel();
        dbContent.setLayout(new BoxLayout(dbContent, BoxLayout.Y_AXIS));
        dbContent.setBackground(UIHelper.CARD_BG);

        lblDbPath = new JLabel("Database Path: " + DatabaseManager.getDatabaseFile().getAbsolutePath());
        lblDbPath.setFont(UIHelper.FONT_SMALL);
        lblDbPath.setForeground(UIHelper.TEXT_MUTED);

        JLabel lblDbNote = new JLabel("Regularly backup your workshop database to a USB drive or external folder.");
        lblDbNote.setFont(UIHelper.FONT_BODY);

        JPanel dbBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        dbBtns.setBackground(UIHelper.CARD_BG);

        JButton btnBackup = UIHelper.createPrimaryButton("Backup Database (.db)");
        btnBackup.addActionListener(e -> onBackupDatabase());

        JButton btnRestore = UIHelper.createDangerButton("Restore Database");
        btnRestore.addActionListener(e -> onRestoreDatabase());

        dbBtns.add(btnBackup);
        dbBtns.add(btnRestore);

        dbContent.add(lblDbNote);
        dbContent.add(Box.createRigidArea(new Dimension(0, 6)));
        dbContent.add(lblDbPath);
        dbContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dbContent.add(dbBtns);

        dbCard.add(dbContent, BorderLayout.CENTER);
        rightContainer.add(dbCard);

        centerGrid.add(rightContainer);
        add(centerGrid, BorderLayout.CENTER);
    }

    private void loadSettings() {
        WorkshopSetting s = settingService.getSettings();
        txtWorkshopName.setText(s.getWorkshopName());
        txtAddress.setText(s.getAddress());
        txtPhone.setText(s.getPhoneNumber());
        txtPanVat.setText(s.getPanVatNumber());
        txtInvoicePrefix.setText(s.getInvoicePrefix());
        txtDefaultCurrency.setText(s.getDefaultCurrency());
        txtDefaultServiceCharge.setText(String.valueOf(s.getDefaultServiceCharge()));
        txtLowStockThreshold.setText(String.valueOf(s.getLowStockThreshold()));
        chkAllowNegativeStock.setSelected(s.isAllowNegativeStock());
    }

    private void onSaveSettings() {
        String name = txtWorkshopName.getText().trim();
        if (name.isEmpty()) {
            UIHelper.showWarning(this, "Workshop Name cannot be empty!");
            txtWorkshopName.requestFocus();
            return;
        }

        WorkshopSetting s = new WorkshopSetting();
        s.setWorkshopName(name);
        s.setAddress(txtAddress.getText().trim());
        s.setPhoneNumber(txtPhone.getText().trim());
        s.setPanVatNumber(txtPanVat.getText().trim());
        s.setInvoicePrefix(txtInvoicePrefix.getText().trim());
        s.setDefaultCurrency(txtDefaultCurrency.getText().trim());
        s.setDefaultServiceCharge(FormatUtil.parseDouble(txtDefaultServiceCharge.getText(), 250.0));
        s.setLowStockThreshold(FormatUtil.parseInt(txtLowStockThreshold.getText(), 5));
        s.setAllowNegativeStock(chkAllowNegativeStock.isSelected());

        try {
            settingService.saveSettings(s);
            UIHelper.showInfo(this, "Settings saved successfully!");
        } catch (Exception ex) {
            UIHelper.showError(this, "Error saving settings: " + ex.getMessage());
        }
    }

    private void onBackupDatabase() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Database Backup File");
        String defaultName = "motorcycle_workshop_backup_" + System.currentTimeMillis() + ".db";
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("SQLite Database Files (*.db, *.sqlite)", "db", "sqlite"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try {
                BackupRestoreService.backupDatabase(target);
                UIHelper.showInfo(this, "Database backup created successfully at:\n" + target.getAbsolutePath());
            } catch (Exception ex) {
                UIHelper.showError(this, "Backup failed: " + ex.getMessage());
            }
        }
    }

    private void onRestoreDatabase() {
        if (!UIHelper.showConfirm(this,
                "Restoring a database will overwrite current workshop records!\n" +
                "An automatic safety copy will be made before restore.\nProceed?", "Confirm Database Restore")) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Database Backup File to Restore");
        chooser.setFileFilter(new FileNameExtensionFilter("SQLite Database Files (*.db, *.sqlite, *.bak)", "db", "sqlite", "bak"));

        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File backup = chooser.getSelectedFile();
            try {
                BackupRestoreService.restoreDatabase(backup);
                UIHelper.showInfo(this, "Database restored successfully!\nPlease restart the application or refresh sections.");
                loadSettings();
            } catch (Exception ex) {
                UIHelper.showError(this, "Restore failed: " + ex.getMessage());
            }
        }
    }
}
