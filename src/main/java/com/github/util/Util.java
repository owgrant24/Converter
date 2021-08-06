package com.github.util;

import com.github.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final static Logger logger = LoggerFactory.getLogger(Util.class);

    public static final Set<Process> PROCESSES = new HashSet<>();
    public static List<Task> taskList = new ArrayList<>();
    private static String ffmpeg = "D:/ffmpeg.exe";


    public static void startTask(List<Task> taskList, String param) {
        logger.debug("Задание стартовало: " + taskList);
        Thread thread = new Thread(() ->
                taskList.forEach(task -> {
                    if (!Thread.interrupted()) {
                        try {
                            logger.debug("Взял в работу: " + task.getName());
                            String parameters =
                                    ffmpeg + " -i " + "\"" + task.getFile().getPath() + "\""
                                            + " "
                                            + param
                                            + " \"" + task.getFile().getName().replaceFirst("[.][^.]+$", "") + ".mp4" + "\"";
                            StartedProcess startedProcess = new ProcessExecutor()
                                    .command("cmd.exe", "/C", parameters)
                                    .readOutput(true)
                                    .start();
                            Process process = startedProcess.getProcess();
                            PROCESSES.add(process);
                            Future<ProcessResult> future = startedProcess.getFuture();

                            logger.debug("Информация о проведенной работе: \n" + future.get().outputUTF8());
                            logger.debug("Работу выполнил над: " + task.getName());
                            PROCESSES.remove(process);
                            task.setStatus("Done");

                        } catch (IOException e) {
                            logger.info("Ошибка IOException: " + e.getMessage());
                        } catch (ExecutionException e) {
                            logger.info("Ошибка ExecutionException: " + e.getMessage());
                        } catch (InterruptedException e) {
                            logger.info("Ошибка InterruptedException: " + e.getMessage());
                        }
                    }
                }));
        thread.start();


    }
}
