package com.github.service;

import com.github.controller.ControllerMediatorImpl;
import com.github.entity.Task;
import com.github.util.SettingsCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


public class ConverterService {

    private static final Logger logger = LoggerFactory.getLogger(ConverterService.class);

    private final Map<Task, Process> processes = new HashMap<>();
    private final File ffmpeg = new File("./ffmpeg/bin/ffmpeg.exe");
    private final File ffplay = new File("./ffmpeg/bin/ffplay.exe");
    private final File vlc = new File((String) SettingsCreator.getProperties().get("vlc"));
    private final File avidemux = new File((String) SettingsCreator.getProperties().get("avidemux"));
    public static final String HIDE_BANNER = "-hide_banner";


    private final List<Task> list = new ArrayList<>();
    private final Queue<Task> tasks = new LinkedBlockingQueue<>();
    private Thread thread;

    public static ConverterService getInstance() {
        return ConverterService.ConverterServiceHolder.INSTANCE;
    }

    private static class ConverterServiceHolder {

        private static final ConverterService INSTANCE = new ConverterService();

    }


    public List<Task> getList() {
        return list;
    }

    public Queue<Task> getTasks() {
        return tasks;
    }

    public Map<Task, Process> getProcesses() {
        return processes;
    }

    public File getFfmpeg() {
        return ffmpeg;
    }

    public File getFfplay() {
        return ffplay;
    }

    public void startTask() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(new Consumer());
            thread.start();
        }
    }

    public void stopSelectedTasks(List<Task> list) {
        list.stream().filter(task -> !task.getStatus().equals("Done")).forEach(task -> {
            if (processes.containsKey(task)) {
                stopProcesses();
            } else {
                tasks.remove(task);
                task.setStatus("");
            }
        });
        ControllerMediatorImpl.getInstance().getMainTabController().getTaskTable().refresh();
    }

    public void cancel() {

        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        stopProcesses();
    }

    public void stopProcesses() {
        logger.info("Запущено убивание процесса");
        for (Map.Entry<Task, Process> task : processes.entrySet()) {
            task.getValue().descendants().forEach(ProcessHandle::destroy);
            task.getValue().destroy();
        }
        processes.clear();
    }

    public void playInFF(String input, String height) {
        try {
            new ProcessExecutor()
                    .command(ffplay.getAbsolutePath(), HIDE_BANNER, "-nostats", "-y", height, "-i", input)
                    .start();
        } catch (IOException e) {
            logger.error("ffplay отсутствует");
        }
    }

    public void playInVlc(String input) {
        try {
            new ProcessExecutor()
                    .command(vlc.getAbsolutePath(), input)
                    .start();
        } catch (IOException e) {
            logger.error("VLC отсутствует");
        }
    }

    /**
     * Редактировать в Avidemux
     * https://www.avidemux.org/admWiki/doku.php?id=using:command_line_usage
     */
    public void editInAvidemux(String input) {
        try {
            new ProcessExecutor()
                    .command(avidemux.getAbsolutePath(), "--load", input)
                    .start();
        } catch (IOException e) {
            logger.error("Avidemux отсутствует");
        }
    }

}
