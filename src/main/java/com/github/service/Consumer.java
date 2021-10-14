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
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.service.ConverterService.PROCESSES;
import static com.github.service.ConverterService.ffmpeg;
import static com.github.service.ConverterService.hideBanner;


public class Consumer implements Runnable {
    private Queue<Task> tasks;
    private MainController mainController;

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public Consumer(Queue<Task> tasks, MainController mainController) {
        this.tasks = tasks;
        this.mainController = mainController;
    }

    @Override
    public void run() {
        Task current;
        while ((current = tasks.poll()) != null) {
            if (!Thread.currentThread().isInterrupted()) {
                try {
                    long startTime = System.currentTimeMillis();
                    logger.debug("Взял в работу: {}", current.getName());
                    current.setStatus("In process");
                    String input = "\"" + current.getFile().getPath() + "\" ";

                    Path outputParent = Path.of(current.getFile().getParent() + "/converted/");
                    checkDirectoryForOutputFile(outputParent);

                    String output = " \"" + outputParent + File.separator
                            + current.getName().replaceFirst("[.][^.]+$", "")
                            + "." + mainController.getOutputFileExtensionChoiceBox().getValue().toString() + "\"";
                    String parameters = input + current.getParam() + output;
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
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void checkDirectoryForOutputFile(Path outputParent) throws IOException {
        if (!outputParent.toFile().exists()) {
            Files.createDirectory(outputParent);
        }
    }

}
