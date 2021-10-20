package com.github.service;

import com.github.controller.MainController;
import com.github.entity.Task;

import java.io.File;
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

    protected static final Set<Process> PROCESSES = new HashSet<>();
    protected static final File FFMPEG = new File("./ffmpeg/ffmpeg.exe");
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
        PROCESSES.forEach(process -> process.descendants().forEach(ProcessHandle::destroy));
    }

}
