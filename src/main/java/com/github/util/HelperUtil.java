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

}
