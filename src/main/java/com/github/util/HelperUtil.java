package com.github.util;

import com.github.CustomLocale;
import com.github.entity.Language;

import java.util.Collection;


public class HelperUtil {

    private HelperUtil() {
    }

    public static String printCollection(Collection<?> collection) {
        StringBuilder stringBuilder = new StringBuilder("\n");
        for (Object element : collection) {
            stringBuilder.append(element).append("\n");
        }
        return stringBuilder.toString();
    }

    public static String convertSecInMin(Long sec) {
        long min = sec / 60;
        long minOst = sec % 60;
        String result;
        if (sec < 60) {
            result = sec + " sec";
        } else if (minOst == 0) {
            result = min + " min ";
        } else {
            result = min + " min " + minOst + " sec";
        }
        return result;
    }

    public static Language definitionLanguage() {
        if (CustomLocale.getInstance().getLocale().getLanguage().equalsIgnoreCase("ru")) {
            return Language.RUSSIAN;
        }
        return Language.ENGLISH;
    }

    public static String formatSizeFile(long size) {
        return String.format("%,d kB", size / 1_000);
    }

}
