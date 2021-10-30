package com.github;

import com.github.util.SettingsCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;


public class CustomLocale {

    private static final Logger logger = LoggerFactory.getLogger(CustomLocale.class);

    private Locale locale = new Locale("en");

    public Locale getLocale() {
        return locale;
    }

    public Locale loadLocale() throws IOException {
        String lang = (String) SettingsCreator.getProperties().get("locale");
        locale = new Locale(lang);
        logger.info("Загружен язык - {}", locale.getLanguage());
        return locale;
    }

    public static CustomLocale getInstance() {
        return CustomLocale.CustomLocaleHolder.INSTANCE;
    }

    private static class CustomLocaleHolder {

        private static final CustomLocale INSTANCE = new CustomLocale();

    }

}
