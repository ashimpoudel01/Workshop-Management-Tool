package com.motorworkshop.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * Utility functions for date calculations, ranges, and formatting.
 */
public class DateUtil {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static String today() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String now() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static String yesterday() {
        return LocalDate.now().minusDays(1).format(DATE_FORMATTER);
    }

    public static String startOfWeek() {
        LocalDate today = LocalDate.now();
        // Assume Monday or 7 days ago
        return today.minusDays(today.getDayOfWeek().getValue() - 1).format(DATE_FORMATTER);
    }

    public static String endOfWeek() {
        LocalDate today = LocalDate.now();
        return today.plusDays(7 - today.getDayOfWeek().getValue()).format(DATE_FORMATTER);
    }

    public static String startOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).format(DATE_FORMATTER);
    }

    public static String endOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).format(DATE_FORMATTER);
    }

    public static String formatForDisplay(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "-";
        try {
            if (dateStr.length() >= 10) {
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10), DATE_FORMATTER);
                return date.format(DISPLAY_FORMATTER);
            }
        } catch (Exception ignored) {}
        return dateStr;
    }
}
