package com.github.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;


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

    public static Properties readProperties(String path) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            properties.load(bufferedReader);
        }
        return properties;
    }

    public static void createSettings() throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter("./settings.properties", StandardCharsets.UTF_8))) {
            bufferedWriter.write("locale=en");
        }
    }

}
