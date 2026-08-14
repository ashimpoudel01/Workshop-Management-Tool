package com.motorworkshop.util;

import com.motorworkshop.model.Sale;
import com.motorworkshop.model.SaleItem;
import com.motorworkshop.model.WorkshopSetting;

import javax.swing.*;
import java.awt.print.PrinterException;

/**
 * Generates formatted HTML receipts / invoices and handles print workflows.
 */
public class InvoicePrinter {

    public static String generateInvoiceHtml(Sale sale, WorkshopSetting settings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>")
          .append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 15px; color: #1e293b; }")
          .append(".header { text-align: center; border-bottom: 2px solid #0f172a; padding-bottom: 8px; margin-bottom: 12px; }")
          .append(".title { font-size: 18px; font-weight: bold; margin-bottom: 2px; }")
          .append(".subtitle { font-size: 11px; color: #475569; }")
          .append(".inv-title { font-size: 14px; font-weight: bold; margin-top: 6px; letter-spacing: 1px; }")
          .append(".info-table { width: 100%; font-size: 12px; margin-bottom: 12px; }")
          .append(".info-table td { padding: 2px 4px; vertical-align: top; }")
          .append(".items-table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 12px; }")
          .append(".items-table th { background-color: #f1f5f9; border-top: 1px solid #cbd5e1; border-bottom: 1px solid #cbd5e1; padding: 6px 4px; text-align: left; }")
          .append(".items-table td { border-bottom: 1px dashed #e2e8f0; padding: 5px 4px; }")
          .append(".text-right { text-align: right; }")
          .append(".text-center { text-align: center; }")
          .append(".totals-table { width: 100%; font-size: 12px; margin-top: 6px; }")
          .append(".totals-table td { padding: 3px 4px; }")
          .append(".grand-total { font-size: 14px; font-weight: bold; border-top: 1px solid #0f172a; border-bottom: 2px solid #0f172a; }")
          .append(".footer { text-align: center; font-size: 11px; color: #64748b; margin-top: 20px; border-top: 1px solid #e2e8f0; padding-top: 8px; }")
          .append("</style></head><body>");

        // Header
        sb.append("<div class='header'>")
          .append("<div class='title'>").append(escapeHtml(settings.getWorkshopName())).append("</div>")
          .append("<div class='subtitle'>").append(escapeHtml(settings.getAddress())).append(" | Phone: ").append(escapeHtml(settings.getPhoneNumber())).append("</div>");
        
        if (settings.getPanVatNumber() != null && !settings.getPanVatNumber().trim().isEmpty()) {
            sb.append("<div class='subtitle'>PAN / VAT No: ").append(escapeHtml(settings.getPanVatNumber())).append("</div>");
        }
        sb.append("<div class='inv-title'>TAX INVOICE / CASH BILL</div>")
          .append("</div>");

        // Customer & Vehicle Info
        sb.append("<table class='info-table'>")
          .append("<tr>")
          .append("<td><b>Invoice No:</b> ").append(sale.getInvoiceNumber()).append("</td>")
          .append("<td class='text-right'><b>Date:</b> ").append(sale.getDate()).append("</td>")
          .append("</tr><tr>")
          .append("<td><b>Customer:</b> ").append(escapeHtml(sale.getCustomerName())).append(" (").append(escapeHtml(sale.getCustomerPhone())).append(")</td>")
          .append("<td class='text-right'><b>Payment:</b> ").append(escapeHtml(sale.getPaymentMethod())).append("</td>")
          .append("</tr><tr>")
          .append("<td><b>Vehicle:</b> ").append(escapeHtml(sale.getVehicleBrand())).append(" ").append(escapeHtml(sale.getVehicleModel())).append("</td>")
          .append("<td class='text-right'><b>Reg No:</b> ").append(escapeHtml(sale.getVehicleRegNo())).append("</td>")
          .append("</tr></table>");

        // Items Table
        sb.append("<table class='items-table'>")
          .append("<thead><tr>")
          .append("<th style='width: 30px;'>SN</th>")
          .append("<th>Description</th>")
          .append("<th class='text-center' style='width: 50px;'>Type</th>")
          .append("<th class='text-center' style='width: 40px;'>Qty</th>")
          .append("<th class='text-right' style='width: 70px;'>Rate</th>")
          .append("<th class='text-right' style='width: 80px;'>Amount</th>")
          .append("</tr></thead><tbody>");

        int sn = 1;
        for (SaleItem item : sale.getItems()) {
            sb.append("<tr>")
              .append("<td class='text-center'>").append(sn++).append("</td>")
              .append("<td>").append(escapeHtml(item.getItemName())).append("</td>")
              .append("<td class='text-center'>").append(item.getItemType() == SaleItem.ItemType.PART ? "Part" : "Service").append("</td>")
              .append("<td class='text-center'>").append(item.getQuantity()).append("</td>")
              .append("<td class='text-right'>").append(FormatUtil.formatCurrencyPlain(item.getUnitPrice())).append("</td>")
              .append("<td class='text-right'>").append(FormatUtil.formatCurrencyPlain(item.getTotalPrice())).append("</td>")
              .append("</tr>");
        }

        sb.append("</tbody></table>");

        // Totals
        sb.append("<table class='totals-table'>")
          .append("<tr><td style='width:60%;'></td><td style='width:20%;'>Parts Total:</td><td class='text-right' style='width:20%;'>").append(FormatUtil.formatCurrency(sale.getPartsTotal())).append("</td></tr>")
          .append("<tr><td></td><td>Service Charges:</td><td class='text-right'>").append(FormatUtil.formatCurrency(sale.getServiceCharge())).append("</td></tr>")
          .append("<tr><td></td><td>Subtotal:</td><td class='text-right'>").append(FormatUtil.formatCurrency(sale.getSubtotal())).append("</td></tr>");

        if (sale.getDiscount() > 0) {
            sb.append("<tr><td></td><td>Discount:</td><td class='text-right'>- ").append(FormatUtil.formatCurrency(sale.getDiscount())).append("</td></tr>");
        }

        sb.append("<tr class='grand-total'><td></td><td><b>Net Total:</b></td><td class='text-right'><b>").append(FormatUtil.formatCurrency(sale.getTotalAmount())).append("</b></td></tr>")
          .append("</table>");

        if (sale.getNotes() != null && !sale.getNotes().trim().isEmpty()) {
            sb.append("<div style='font-size: 11px; margin-top: 10px;'><b>Notes:</b> ").append(escapeHtml(sale.getNotes())).append("</div>");
        }

        // Footer
        sb.append("<div class='footer'>")
          .append("Thank you for your business! Please visit us again.<br>")
          .append("Generated by Motorcycle Workshop Management System")
          .append("</div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    public static void printInvoice(JEditorPane editorPane) {
        try {
            boolean complete = editorPane.print();
            if (complete) {
                JOptionPane.showMessageDialog(editorPane, "Invoice printed successfully!", "Print Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(editorPane, "Error printing invoice: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
