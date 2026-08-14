package com.motorworkshop.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility functions for formatting currency (NPR / Rs.), numbers, and percentages.
 */
public class FormatUtil {
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat QUANTITY_FORMAT = new DecimalFormat("#,##0");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.0'%'");

    public static String formatCurrency(double amount) {
        return "Rs. " + CURRENCY_FORMAT.format(amount);
    }

    public static String formatCurrencyPlain(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatQuantity(int quantity) {
        return QUANTITY_FORMAT.format(quantity);
    }

    public static String formatPercent(double percent) {
        return PERCENT_FORMAT.format(percent);
    }

    public static double parseDouble(String text, double defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int parseInt(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
