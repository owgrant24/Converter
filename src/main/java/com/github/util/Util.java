package com.github.util;

import com.github.controller.MainController;
import com.github.entity.Extension;
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
    private final static Logger logger = LoggerFactory.getLogger(Util.class);

    private final MainController mainController;

    public static final Set<Process> PROCESSES = new HashSet<>();
    public static List<Task> list = new ArrayList<>();
    public static Queue<Task> taskArrayDeque = new ArrayDeque<>();
    public static Extension[] extension = Extension.values();


    private static String ffmpeg = "D:/ffmpeg.exe";
//    private static String ffmpeg = "./ffmpeg/ffmpeg.exe";


    public Util(MainController mainController) {
        this.mainController = mainController;
    }


    public void startTask(String param) {
        logger.debug("Задание стартовало: " + taskArrayDeque);
        thread = new Thread(() -> {
            Task current;
            while ((current = taskArrayDeque.poll()) != null) {
                if (!Thread.currentThread().isInterrupted()) {
                    try {
                        logger.debug("Взял в работу: " + current.getName());
                        String hideBanner = " -hide_banner";
                        String input = " -i \"" + current.getFile().getPath() + "\" ";

                        Path outputParent = Path.of(current.getFile().getParent() + "/converted/");
                        if (!outputParent.toFile().exists()) {
                            Files.createDirectory(outputParent);
                        }

                        String output = " \"" + outputParent + File.separator + current.getName().replaceFirst("[.][^.]+$", "")
                                + "." + mainController.getOutput_file_extension_choice_box().getValue().toString() + "\"";
                        String parameters = ffmpeg + hideBanner + input + param + output;
                        StartedProcess startedProcess = new ProcessExecutor()
                                .command("cmd.exe", "/C", parameters)
                                .readOutput(true)
                                .start();
                        Process process = startedProcess.getProcess();
                        PROCESSES.add(process);
                        Future<ProcessResult> future = startedProcess.getFuture();

                        String status = future.get().outputUTF8();
                        mainController.getLog_text_area().appendText(status);
                        logger.debug("Информация о проведенной работе: \n" + status);
                        logger.debug("Работу выполнил над: " + current.getName());
                        PROCESSES.remove(process);
                        current.setStatus("Done");
                        mainController.getTask_table().refresh();

                    } catch (IOException e) {
                        logger.info("Ошибка IOException: " + e.getMessage());
                    } catch (ExecutionException e) {
                        logger.info("Ошибка ExecutionException: " + e.getMessage());
                    } catch (InterruptedException e) {
                        logger.info("Ошибка InterruptedException: " + e.getMessage());
                    }
                }
            }
        });
        thread.start();

    }

    public void stop() {
        thread.interrupt();
    }
}
