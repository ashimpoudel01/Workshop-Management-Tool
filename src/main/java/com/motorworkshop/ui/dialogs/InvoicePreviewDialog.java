package com.motorworkshop.ui.dialogs;

import com.motorworkshop.model.Sale;
import com.motorworkshop.model.WorkshopSetting;
import com.motorworkshop.util.InvoicePrinter;
import com.motorworkshop.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Printable invoice preview modal.
 */
public class InvoicePreviewDialog extends JDialog {
    private final Sale sale;
    private final WorkshopSetting settings;
    private JEditorPane editorPane;

    public InvoicePreviewDialog(Frame owner, Sale sale, WorkshopSetting settings) {
        super(owner, "Invoice Preview - " + sale.getInvoiceNumber(), true);
        this.sale = sale;
        this.settings = settings;

        initComponents();
        setSize(650, 750);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(12, 12, 12, 12));
        contentPane.setBackground(UIHelper.CARD_BG);

        // Header controls
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIHelper.CARD_BG);
        JLabel lblTitle = new JLabel("Invoice / Receipt Preview (" + sale.getInvoiceNumber() + ")");
        lblTitle.setFont(UIHelper.FONT_HEADER);
        lblTitle.setForeground(UIHelper.PRIMARY_COLOR);
        topBar.add(lblTitle, BorderLayout.WEST);

        contentPane.add(topBar, BorderLayout.NORTH);

        // HTML Editor Pane
        editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText(InvoicePrinter.generateInvoiceHtml(sale, settings));
        editorPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action buttons
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setBackground(UIHelper.CARD_BG);

        JButton btnPrint = UIHelper.createSuccessButton("Print Invoice");
        btnPrint.addActionListener(e -> InvoicePrinter.printInvoice(editorPane));

        JButton btnClose = UIHelper.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dispose());

        bottomBar.add(btnPrint);
        bottomBar.add(btnClose);
        contentPane.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(contentPane);
    }
}
