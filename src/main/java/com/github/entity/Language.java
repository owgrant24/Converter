package com.github.entity;

public enum Language {
    RUSSIAN("Русский", "ru"),
    ENGLISH("English", "en");

    private String name;
    private String locale;

    Language(String name, String locale) {
        this.name = name;
        this.locale = locale;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getLocale() {
        return locale;
    }
}
