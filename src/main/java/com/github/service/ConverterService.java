package com.github.service;

import com.github.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;


public class ConverterService {

    private static final Logger logger = LoggerFactory.getLogger(ConverterService.class);

    private final Set<Process> processes = new HashSet<>();
    private final File ffmpeg = new File("./ffmpeg/bin/ffmpeg.exe");
    private final File ffplay = new File("./ffmpeg/bin/ffplay.exe");
    private final File vlc = new File("C:/Program Files/VideoLAN/VLC/vlc.exe");
    private final File avidemux = new File("C:/Program Files/Avidemux 2.7 VC++ 64bits/avidemux.exe");
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

    public Set<Process> getProcesses() {
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

    public void cancel() {

        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        stopProcesses();
    }

    public void stopProcesses() {
        logger.info("Запущено убивание процесса");
        for (Process process : processes) {
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
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
