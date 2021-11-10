package com.github.util;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class SettingsCreator {

    private static final Logger logger = LoggerFactory.getLogger(SettingsCreator.class);
    private static PropertiesConfiguration config = new PropertiesConfiguration();
    private static final String SETTINGS_PROPERTIES = "./settings.properties";

    static {
        try {
            readConfigurationFromFile(config);
            checkConfiguration(config);
        } catch (ConfigurationException e) {
            logger.info("Файл настроек не существует");
            config = createDefaultConfiguration();
            saveConfigurationInFile(config);
        }
    }

    private static void checkConfiguration(PropertiesConfiguration configuration) {
        if (!configuration.containsKey("vlc") || !configuration.containsKey("avidemux")
                || !configuration.containsKey("locale")) {
            PropertiesConfiguration defaultConfiguration = createDefaultConfiguration();
            saveConfigurationInFile(defaultConfiguration);
            config = defaultConfiguration;
        }
    }

    private SettingsCreator() {
    }

    public static String readStringValueFromConfiguration(String key) {
        return config.getString(key);
    }

    public static void readConfigurationFromFile(PropertiesConfiguration configuration) throws ConfigurationException {
        new FileHandler(configuration).load(SETTINGS_PROPERTIES);
    }

    public static PropertiesConfiguration createDefaultConfiguration() {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setProperty("locale", "en");
        propertiesConfiguration.setProperty("vlc", "C:/Program Files/VideoLAN/VLC/vlc.exe");
        propertiesConfiguration.setProperty("avidemux", "C:/Program Files/Avidemux 2.7 VC++ 64bits/avidemux.exe");
        return propertiesConfiguration;
    }

    public static void saveConfigurationInFile(PropertiesConfiguration config) {
        FileHandler fileHandler = new FileHandler(config);
        File out = new File(SETTINGS_PROPERTIES);
        try {
            fileHandler.save(out);
            logger.info("Стандартные настройки записаны в {}", SETTINGS_PROPERTIES);
        } catch (ConfigurationException e) {
            logger.error("Ошибка сохранения настроек {}", e.getMessage());
        }
    }

}
