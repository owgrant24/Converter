package com.github.util;

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

    public static String convertSecInMin(String sec) {
        int number = Integer.parseInt(sec);
        int min = number / 60;
        int minOst = number % 60;
        String result;
        if (number < 60) {
            result = sec + " sec";
        } else if (minOst == 0) {
            result = min + " min ";
        } else {
            result = min + " min " + minOst + " sec";
        }
        return result;
    }

}
