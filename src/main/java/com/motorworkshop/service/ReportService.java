package com.motorworkshop.service;

import com.motorworkshop.dao.ReportDAO;
import com.motorworkshop.model.ReportData;
import com.motorworkshop.util.DateUtil;

import java.sql.SQLException;

/**
 * Business logic service for generating daily, weekly, monthly, and custom financial reports.
 */
public class ReportService {
    private final ReportDAO reportDAO = new ReportDAO();

    public enum FilterPreset {
        TODAY,
        YESTERDAY,
        THIS_WEEK,
        THIS_MONTH,
        ALL_TIME,
        CUSTOM
    }

    public ReportData generateReportForPreset(FilterPreset preset, String customStart, String customEnd) throws SQLException {
        String start;
        String end;

        switch (preset) {
            case TODAY:
                start = DateUtil.today();
                end = DateUtil.today();
                break;
            case YESTERDAY:
                start = DateUtil.yesterday();
                end = DateUtil.yesterday();
                break;
            case THIS_WEEK:
                start = DateUtil.startOfWeek();
                end = DateUtil.endOfWeek();
                break;
            case THIS_MONTH:
                start = DateUtil.startOfMonth();
                end = DateUtil.endOfMonth();
                break;
            case ALL_TIME:
                start = "";
                end = "";
                break;
            case CUSTOM:
            default:
                start = customStart != null && !customStart.trim().isEmpty() ? customStart.trim() : DateUtil.today();
                end = customEnd != null && !customEnd.trim().isEmpty() ? customEnd.trim() : DateUtil.today();
                break;
        }

        return reportDAO.generateReport(start, end, preset.name());
    }

    public ReportData generateCustomReport(String startDate, String endDate) throws SQLException {
        return reportDAO.generateReport(startDate, endDate, "CUSTOM");
    }
}
