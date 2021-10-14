package com.github.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelperUtilTest {
    @Test
    void printCollection() {
        List<String> list = new ArrayList<>(Arrays.asList("Привет", "Корова", "Принеси","Молоко"));
        String out = HelperUtil.printCollection(list);
        String equals = "\nПривет\nКорова\nПринеси\nМолоко\n";
        assertEquals(out,equals);
    }

}