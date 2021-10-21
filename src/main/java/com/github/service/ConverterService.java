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


/**
 * ffmpeg [опции источника] -i [источник] [основные опции] [кодеки] [преемник]
 * <p>
 * Опции источника - указывают параметры чтения файла, настройки и так далее;
 * Источник - опция -i задает источник, откуда будет читаться файл, источников может быть несколько
 * и это может быть не только файл, но и устройство;
 * Основные опции - задают параметры работы всей утилиты;
 * Кодеки - кодек, который будет использоваться для сохранения видео и аудио;
 * Преемник - файл или устройство, куда будут записаны данные.
 * <p>
 * https://trofimovdigital.ru/blog/convert-video-with-ffmpeg
 */
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

    public void playFF(String parameters) {
        try {
            new ProcessExecutor()
                    .command(FFPLAY.getAbsolutePath(), HIDE_BANNER, "-nostats", "-x", "480", "-i", parameters)
                    .start();
        } catch (IOException e) {
            logger.error("ffplay отсутствует");
        }
    }

}
