package com.motorworkshop.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.ChronoUnit;

/**
 * Utility functions for date calculations, ranges, and formatting.
 */
public class DateUtil {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        private static final DateTimeFormatter BS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        private static final int FIRST_BS_YEAR = 2080;
        private static final int[][] BS_MONTH_LENGTHS = {
            {31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30},
            {31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31},
            {31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30},
            {31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30},
            {31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30},
            {31, 32, 31, 32, 30, 31, 30, 30, 29, 30, 30, 30},
            {30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30},
            {31, 31, 32, 31, 31, 31, 30, 29, 30, 30, 30, 30},
            {30, 31, 32, 32, 30, 31, 30, 30, 29, 30, 30, 30},
            {30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30},
            {30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30},
            {31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30},
            {30, 31, 32, 32, 31, 30, 30, 30, 29, 30, 30, 30},
            {30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30},
            {31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30},
            {31, 31, 32, 31, 31, 31, 30, 29, 30, 30, 30, 30},
            {30, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30},
            {31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30},
            {31, 31, 32, 31, 31, 31, 29, 30, 29, 30, 29, 31},
            {31, 31, 32, 31, 31, 31, 30, 29, 29, 30, 30, 30},
            {31, 32, 31, 32, 30, 31, 30, 29, 30, 29, 30, 30}
        };
        private static final LocalDate[] BS_NEW_YEAR_DATES = {
            LocalDate.of(2023, 4, 14), LocalDate.of(2024, 4, 13), LocalDate.of(2025, 4, 14),
            LocalDate.of(2026, 4, 14), LocalDate.of(2027, 4, 14), LocalDate.of(2028, 4, 13),
            LocalDate.of(2029, 4, 14), LocalDate.of(2030, 4, 14), LocalDate.of(2031, 4, 15),
            LocalDate.of(2032, 4, 14), LocalDate.of(2033, 4, 14), LocalDate.of(2034, 4, 14),
            LocalDate.of(2035, 4, 15), LocalDate.of(2036, 4, 14), LocalDate.of(2037, 4, 14),
            LocalDate.of(2038, 4, 14), LocalDate.of(2039, 4, 15), LocalDate.of(2040, 4, 13),
            LocalDate.of(2041, 4, 14), LocalDate.of(2042, 4, 14), LocalDate.of(2043, 4, 13)
        };

    public static String today() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String todayBs() {
        return formatBs(LocalDate.now());
    }

    public static String startOfMonthBs() {
        return formatBs(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()));
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
                return formatBs(date);
            }
        } catch (Exception ignored) {}
        return dateStr;
    }

    public static String formatBs(LocalDate date) {
        for (int year = FIRST_BS_YEAR; year < FIRST_BS_YEAR + BS_NEW_YEAR_DATES.length; year++) {
            LocalDate newYear = BS_NEW_YEAR_DATES[year - FIRST_BS_YEAR];
            LocalDate nextNewYear = year - FIRST_BS_YEAR + 1 < BS_NEW_YEAR_DATES.length
                    ? BS_NEW_YEAR_DATES[year - FIRST_BS_YEAR + 1] : newYear.plusDays(totalDays(year));
            if (!date.isBefore(newYear) && date.isBefore(nextNewYear)) {
                int dayOfYear = (int) ChronoUnit.DAYS.between(newYear, date);
                int month = 1;
                while (dayOfYear >= BS_MONTH_LENGTHS[year - FIRST_BS_YEAR][month - 1]) {
                    dayOfYear -= BS_MONTH_LENGTHS[year - FIRST_BS_YEAR][month - 1];
                    month++;
                }
                return String.format("%04d-%02d-%02d BS", year, month, dayOfYear + 1);
            }
        }
        return date.format(DISPLAY_FORMATTER);
    }

    public static String toDisplayDate(String storageDate) {
        if (storageDate == null || storageDate.trim().isEmpty()) return "";
        try {
            return formatBs(LocalDate.parse(storageDate.substring(0, 10), DATE_FORMATTER));
        } catch (Exception ignored) {
            return storageDate;
        }
    }

    public static String toStorageDate(String userDate) {
        if (userDate == null || userDate.trim().isEmpty()) return "";
        String value = userDate.trim().replace(" BS", "");
        try {
            return bsToAd(LocalDate.parse(value, BS_FORMATTER)).format(DATE_FORMATTER);
        } catch (Exception ignored) {
            return value;
        }
    }

    public static String toStorageDateOrEmpty(String userDate) {
        return userDate == null || userDate.trim().isEmpty() ? "" : toStorageDate(userDate);
    }

    private static LocalDate bsToAd(LocalDate bsDate) {
        int year = bsDate.getYear();
        int index = year - FIRST_BS_YEAR;
        if (index < 0 || index >= BS_NEW_YEAR_DATES.length || bsDate.getMonthValue() < 1 || bsDate.getMonthValue() > 12
                || bsDate.getDayOfMonth() > BS_MONTH_LENGTHS[index][bsDate.getMonthValue() - 1]) {
            throw new IllegalArgumentException("Unsupported BS date");
        }
        int days = bsDate.getDayOfMonth() - 1;
        for (int month = 0; month < bsDate.getMonthValue() - 1; month++) days += BS_MONTH_LENGTHS[index][month];
        return BS_NEW_YEAR_DATES[index].plusDays(days);
    }

    private static int totalDays(int year) {
        int total = 0;
        for (int days : BS_MONTH_LENGTHS[year - FIRST_BS_YEAR]) total += days;
        return total;
    }
}
