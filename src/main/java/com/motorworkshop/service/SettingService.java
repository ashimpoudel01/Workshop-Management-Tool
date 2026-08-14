package com.motorworkshop.service;

import com.motorworkshop.dao.SettingDAO;
import com.motorworkshop.model.WorkshopSetting;

/**
 * Business logic service for workshop profile and configuration preferences.
 */
public class SettingService {
    private final SettingDAO settingDAO = new SettingDAO();

    public WorkshopSetting getSettings() {
        return settingDAO.getSettings();
    }

    public boolean saveSettings(WorkshopSetting settings) {
        if (settings.getWorkshopName() == null || settings.getWorkshopName().trim().isEmpty()) {
            throw new IllegalArgumentException("Workshop Name cannot be empty!");
        }
        return settingDAO.saveSettings(settings);
    }
}
