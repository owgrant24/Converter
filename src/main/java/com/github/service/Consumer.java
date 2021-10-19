package com.github.service;

import com.github.entity.Task;
import java.io.OutputStream;
import java.io.PipedOutputStream;
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
import static com.github.util.HelperUtil.printCollection;


public class Consumer implements Runnable {

    private ConverterService converterService;
    private Duration duration;

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public Consumer(ConverterService converterService) {
        this.converterService = converterService;
        this.duration = new Duration(converterService);
    }

    @Override
    public void run() {
        logger.debug("Задание стартовало: {}", printCollection(converterService.getTasks()));
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
                            + converterService.getMainController()
                            .getOutputFileExtensionChoiceBox().getValue().toString() + "\"";
                    String parameters = input + current.getParam() + output;
                    OutputStream dur = new PipedOutputStream();
                    StartedProcess startedProcess = new ProcessExecutor()
                            .command(FFMPEG.getAbsolutePath(), HIDE_BANNER, "-i", parameters)
                            .readOutput(true)
                            .redirectOutputAlsoTo(dur)
                            .start();
                    Process process = startedProcess.getProcess();
                    duration.showDuration(dur, current, startTime);
                    dur.close();
                    PROCESSES.add(process);
                    Future<ProcessResult> future = startedProcess.getFuture();
                    String status = future.get().outputUTF8();
                    converterService.getMainController().getLogTextArea().appendText(status);
                    converterService.getMainController().getLogTextArea().appendText("\n\n\n");
                    logger.debug("Работу выполнил над: {}", current.getName());
                    PROCESSES.remove(process);
                    String statusAfterCheck = CheckStatusService.checkStatus(status);
                    current.setStatus(statusAfterCheck);
                    converterService.getMainController().getTaskTable().refresh();
                } catch (IOException | ExecutionException e) {
                    logger.error("Ошибка выполнения задания: {}", e.getMessage());
                } catch (InterruptedException e) {
                    logger.error("Ошибка InterruptedException: {}", e.getMessage());
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
