package com.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import static com.github.util.HelperUtil.createSettings;
import static com.github.util.HelperUtil.readProperties;


public class CustomLocale {

    private static final Logger logger = LoggerFactory.getLogger(CustomLocale.class);

    private Locale locale = new Locale("en");

    public Locale getLocale() {
        return locale;
    }

    public Locale loadLocale() throws IOException {
        Properties properties;
        try {
            properties = readProperties("./settings.properties");
            String lang = (String) properties.get("locale");
            locale = new Locale(lang);
            return locale;
        } catch (IOException e) {
            logger.info("Файл настроек не существует");
            createSettings();
            return locale;
        }
    }

    public static CustomLocale getInstance() {
        return CustomLocale.CustomLocaleHolder.INSTANCE;
    }

    private static class CustomLocaleHolder {

        private static final CustomLocale INSTANCE = new CustomLocale();

    }

}
