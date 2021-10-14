package com.github.service;

import com.github.controller.MainController;
import com.github.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
    protected static final File ffmpeg = new File("./ffmpeg/ffmpeg.exe");
    protected static String hideBanner = "-hide_banner";

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

    public void startTask() {
        logger.debug("Задание стартовало: {}", tasks);
        Consumer consumer = new Consumer(tasks, mainController);
        thread = new Thread(consumer);
        thread.start();
    }

    public void cancel() {

        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        stopProcesses();
    }

    public static void stopProcesses() {
        PROCESSES.forEach(process -> process.descendants().forEach(ProcessHandle::destroy));
    }

}
