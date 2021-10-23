package com.github.service;

import com.github.controller.MainController;
import com.github.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

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

    protected static final Set<Process> PROCESSES = new HashSet<>();
    protected static final File FFMPEG = new File("./ffmpeg/bin/ffmpeg.exe");
    protected static final File FFPLAY = new File("./ffmpeg/bin/ffplay.exe");
    protected static final String HIDE_BANNER = "-hide_banner";

    private final MainController mainController;

    private final List<Task> list;
    private final Queue<Task> tasks;
    private Thread thread;


    public ConverterService(MainController mainController) {
        this.mainController = mainController;
        this.tasks = new LinkedBlockingQueue<>();
        this.list = new ArrayList<>();
    }

    public List<Task> getList() {
        return list;
    }

    public Queue<Task> getTasks() {
        return tasks;
    }

    public MainController getMainController() {
        return mainController;
    }

    public void startTask() {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(new Consumer(this));
            thread.start();
        }
    }

    public void cancel() {

        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        stopProcesses();
    }

    public static void stopProcesses() {
        logger.info("Запущено убивание процесса");
        for (Process process : PROCESSES) {
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
        }
        PROCESSES.clear();
    }

    public void playFF(String input, String height) {
        try {
            new ProcessExecutor()
                    .command(FFPLAY.getAbsolutePath(), HIDE_BANNER, "-nostats", "-y", height, "-i", input)
                    .start();
        } catch (IOException e) {
            logger.error("ffplay отсутствует");
        }
    }

}
