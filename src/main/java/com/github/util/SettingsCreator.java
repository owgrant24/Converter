package com.github.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;


public class SettingsCreator {

    private static final Logger logger = LoggerFactory.getLogger(SettingsCreator.class);
    private static final Properties properties = new Properties();
    public static final String SETTINGS_PROPERTIES = "./settings.properties";

    static {
        try {
            properties.load(new BufferedReader(new FileReader(SETTINGS_PROPERTIES)));
        } catch (IOException e) {
            logger.info("Файл настроек не существует");
            properties.put("locale", "en");
            properties.put("vlc", "C:/Program Files/VideoLAN/VLC/vlc.exe");
            properties.put("avidemux", "C:/Program Files/Avidemux 2.7 VC++ 64bits/avidemux.exe");
            createSettings();
        }
    }

    private SettingsCreator() {
    }

    public static Properties getProperties() {
        return properties;
    }

    public static Properties readPropertiesFromFile() throws IOException {
        Properties properties = new Properties();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(SETTINGS_PROPERTIES))) {
            properties.load(bufferedReader);
        }
        return properties;
    }


    private static void createSettings() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(SETTINGS_PROPERTIES, StandardCharsets.UTF_8))) {
            properties.store(bufferedWriter, null);
            logger.info("Стандартные настройки записаны в {}", SETTINGS_PROPERTIES);
        } catch (IOException e) {
            logger.error("Ошибка сохранения настроек");
        }
    }

}
