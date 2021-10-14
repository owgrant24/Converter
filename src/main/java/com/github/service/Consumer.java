package com.github.service;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.service.ConverterService.PROCESSES;
import static com.github.service.ConverterService.FFMPEG;
import static com.github.service.ConverterService.HIDE_BANNER;


public class Consumer implements Runnable {

    private ConverterService converterService;

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public Consumer(ConverterService converterService) {
        this.converterService = converterService;
    }

    @Override
    public void run() {
        logger.debug("Задание стартовало: {}", converterService.getTasks());
        Task current;
        while ((current = converterService.getTasks().poll()) != null) {
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
                            + "."
                            + converterService.getMainController().getOutputFileExtensionChoiceBox().getValue().toString() + "\"";
                    String parameters = input + current.getParam() + output;
                    StartedProcess startedProcess = new ProcessExecutor()
                            .command(FFMPEG.getAbsolutePath(), HIDE_BANNER, "-i", parameters)
                            .readOutput(true)
                            .start();
                    Process process = startedProcess.getProcess();
                    PROCESSES.add(process);
                    Future<ProcessResult> future = startedProcess.getFuture();

                    String status = future.get().outputUTF8();
                    converterService.getMainController().getLogTextArea().appendText(status);
                    logger.debug("Работу выполнил над: {}", current.getName());
                    PROCESSES.remove(process);
                    current.setStatus("Done");
                    long duration = (System.currentTimeMillis() - startTime);
                    String timeOperation = (duration / 1_000) + " с.";
                    current.setTime(timeOperation);
                    converterService.getMainController().getTaskTable().refresh();

                } catch (IOException | ExecutionException e) {
                    logger.info("Ошибка выполнения задания: {}", e.getMessage());
                } catch (InterruptedException e) {
                    logger.info("Ошибка InterruptedException: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.info("Все задания выполнены");
    }

    private void checkDirectoryForOutputFile(Path outputParent) throws IOException {
        if (!outputParent.toFile().exists()) {
            logger.info("Создаю новую директорию {}", outputParent);
            Files.createDirectory(outputParent);
        }
    }

}
