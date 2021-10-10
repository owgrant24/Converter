package com.github.util;

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
public class Util {

    private Thread thread;
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private final MainController mainController;

    public static final Set<Process> PROCESSES = new HashSet<>();
    private final List<Task> list;
    private final Queue<Task> tasks;
    private String hideBanner = "-hide_banner";

    private static final File ffmpeg = new File("./ffmpeg/ffmpeg.exe");


    public Util(MainController mainController) {
        this.mainController = mainController;
        this.tasks = new ConcurrentLinkedQueue<>();
        this.list = new ArrayList<>();

    }

    public List<Task> getList() {
        return list;
    }

    public Queue<Task> getTasks() {
        return tasks;
    }

    public void startTask(String param) {
        logger.debug("Задание стартовало: {}", tasks);
        long startTime = System.currentTimeMillis();
        thread = new Thread(() -> {
            Task current;
            while ((current = tasks.poll()) != null) {
                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        logger.debug("Взял в работу: {}", current.getName());
                        current.setStatus("In process");
                        String input = "\"" + current.getFile().getPath() + "\" ";

                        Path outputParent = Path.of(current.getFile().getParent() + "/converted/");
                        checkDirectoryForOutputFile(outputParent);

                        String output = " \"" + outputParent + File.separator
                                + current.getName().replaceFirst("[.][^.]+$", "")
                                + "." + mainController.getOutputFileExtensionChoiceBox().getValue().toString() + "\"";
                        String parameters = input + param + output;
                        StartedProcess startedProcess = new ProcessExecutor()
                                .command(ffmpeg.getAbsolutePath(), hideBanner, "-i", parameters)
                                .readOutput(true)
                                .start();
                        Process process = startedProcess.getProcess();
                        PROCESSES.add(process);
                        Future<ProcessResult> future = startedProcess.getFuture();

                        String status = future.get().outputUTF8();
                        mainController.getLogTextArea().appendText(status);
                        logger.debug("Информация о проведенной работе: {}\n", status);
                        logger.debug("Работу выполнил над: {}", current.getName());
                        PROCESSES.remove(process);
                        current.setStatus("Done");
                        long duration = (System.currentTimeMillis() - startTime);
                        String timeOperation = (duration / 1_000) + " с.";
                        current.setTime(timeOperation);
                        mainController.getTaskTable().refresh();

                    } catch (IOException | ExecutionException e) {
                        logger.info("Ошибка выполнения задания: {}", e.getMessage());
                    } catch (InterruptedException e) {
                        logger.info("Ошибка InterruptedException: {}", e.getMessage());
                    }
                }
            }
        });
        thread.start();
    }

    private void checkDirectoryForOutputFile(Path outputParent) throws IOException {
        if (!outputParent.toFile().exists()) {
            Files.createDirectory(outputParent);
        }
    }

    public void cancel() {

        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
        }
        PROCESSES.forEach(process -> process.descendants().forEach(ProcessHandle::destroy));
    }

}
