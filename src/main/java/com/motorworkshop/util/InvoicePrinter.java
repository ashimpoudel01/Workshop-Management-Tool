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
          .append("body { font-family: 'Segoe UI', Arial, sans-serif; margin: 18px; color: #111827; }")
          .append(".header { text-align: center; border-bottom: 2px solid #1f2937; padding-bottom: 10px; margin-bottom: 14px; }")
          .append(".title { font-size: 19px; font-weight: bold; letter-spacing: 0.5px; color: #111827; }")
          .append(".subtitle { font-size: 11px; color: #4b5563; margin-top: 2px; }")
          .append(".inv-title { font-size: 13px; font-weight: bold; margin-top: 8px; letter-spacing: 1.5px; color: #1d4ed8; }")
          .append(".info-table { width: 100%; font-size: 12px; margin-bottom: 12px; }")
          .append(".info-table td { padding: 3px 4px; vertical-align: top; }")
          .append(".items-table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 14px; }")
          .append(".items-table th { background-color: #f3f4f6; border-top: 1px solid #9ca3af; border-bottom: 1px solid #9ca3af; padding: 6px 6px; text-align: left; font-weight: bold; color: #1f2937; }")
          .append(".items-table td { border-bottom: 1px solid #e5e7eb; padding: 6px 6px; }")
          .append(".text-right { text-align: right; }")
          .append(".text-center { text-align: center; }")
          .append(".totals-table { width: 100%; font-size: 12px; margin-top: 8px; }")
          .append(".totals-table td { padding: 4px 6px; }")
          .append(".grand-total { font-size: 14px; font-weight: bold; border-top: 1.5px solid #111827; border-bottom: 2px solid #111827; color: #111827; background-color: #f9fafb; }")
          .append(".footer { text-align: center; font-size: 11px; color: #6b7280; margin-top: 24px; border-top: 1px solid #e5e7eb; padding-top: 10px; }")
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
          .append("<td class='text-right'><b>Date:</b> ").append(DateUtil.toDisplayDate(sale.getDate())).append("</td>")
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
          .append("Thank you for choosing our workshop! Ride safe.<br>")
          .append("Computer Generated Tax Invoice | ").append(escapeHtml(settings.getWorkshopName()))
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
