package com.github.service;

import static com.github.util.HelperUtil.convertSecInMin;

import com.github.controller.ControllerMediatorImpl;
import com.github.controller.MainTabController;
import com.github.entity.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Duration {

    private final MainTabController mainTabController;

    private static final Logger logger = LoggerFactory.getLogger(Duration.class);

    public Duration() {
        this.mainTabController = ControllerMediatorImpl.getInstance().getMainTabController();
    }

    public void showDuration(OutputStream stream, Task task, long startTime) {
        new Thread(() -> {
            try (PipedInputStream input = new PipedInputStream();
                 Scanner sc = new Scanner(input)) {
                input.connect((PipedOutputStream) stream);
                Pattern durPattern = Pattern.compile("(?<=Duration: )[^,]*");
                String dur = sc.findWithinHorizon(durPattern, 0);
                if (dur == null) {
                    throw new IOException("Could not parse duration.");
                }
                String[] hms = dur.split(":");
                double totalSecs =
                        Integer.parseInt(hms[0]) * 3600 + Integer.parseInt(hms[1]) * 60 + Double.parseDouble(hms[2]);

                Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
                String match;
                String[] matchSplit;
                while (null != (match = sc.findWithinHorizon(timePattern, 0))) {
                    matchSplit = match.split("[:.]");
                    double progress = (Integer.parseInt(matchSplit[0]) * 3600 +
                            Integer.parseInt(matchSplit[1]) * 60 +
                            Double.parseDouble(matchSplit[2])) / totalSecs;
                    task.setStatus(String.format("%.2f %%", progress * 100));
                    long timeOperation = (System.currentTimeMillis() - startTime) / 1_000;
                    task.setTime(convertSecInMin(timeOperation));
                    mainTabController.getTaskTable().refresh();
                }
            } catch (IOException e) {
                logger.error("Ошибка выполнения задания при расчете длительности: {}", e.getMessage());
            }
        }).start();
    }

}
