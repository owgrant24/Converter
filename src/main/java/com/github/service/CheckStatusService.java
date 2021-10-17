package com.github.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CheckStatusService {

    private CheckStatusService() {
    }

    public static String checkStatus(String matcherStr) {
        Pattern pattern = Pattern.compile(
                "video:\\d+kB audio:\\d+kB subtitle:\\d+kB other streams:\\d+kB");
        Matcher matcher = pattern.matcher(matcherStr);
        boolean res = matcher.find();
        return res ? "Done" : "See log";
    }

}
