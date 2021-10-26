package com.github.service;

import com.github.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.github.service.ConverterService.FFMPEG;
import static com.github.service.ConverterService.HIDE_BANNER;
import static com.github.service.ConverterService.PROCESSES;
import static com.github.util.HelperUtil.printCollection;


public class Consumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private ConverterService converterService;
    private Duration duration;

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
                try (OutputStream dur = new PipedOutputStream()) {
                    long startTime = System.currentTimeMillis();
                    logger.debug("Взял в работу: {}", current.getName());
                    current.setStatus("In process");
                    List<String> parameters = getParameters(current);
                    StartedProcess startedProcess = new ProcessExecutor()
                            .command(parameters)
                            .readOutput(true)
                            .redirectOutputAlsoTo(dur)
                            .start();
                    Process process = startedProcess.getProcess();
                    duration.showDuration(dur, current, startTime);
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

    private List<String> getParameters(Task current) throws IOException {
        List<String> parameters = new ArrayList<>();
        parameters.add(FFMPEG.getAbsolutePath());                                   // path ffmpeg
        parameters.add(HIDE_BANNER);                                                // -hide_banner
        List<String> beforeInputList = parserParam(current.getSpecParam());
        if (!beforeInputList.isEmpty() && !beforeInputList.get(0).isBlank()) {
            parameters.addAll(beforeInputList);                                     // beforeInput
        }
        parameters.add(definingInputParameters(current));                           // -i input
        parameters.addAll(parserParam(current.getParam()));                         // parameters
        parameters.add(definingOutputParameters(current));                          // output
        return parameters;
    }

    private List<String> parserParam(String parametersFromGUI) {
        return new ArrayList<>(Arrays.asList(parametersFromGUI.split("\\p{Space}+")));
    }

    private String definingInputParameters(Task task) {
        return "\"-i\" \"" + task.getFile().getPath() + "\"";
    }

    private String definingOutputParameters(Task task) throws IOException {
        Path outputParent = Path.of(task.getFile().getParent() + "/converted/");
        checkDirectoryForOutputFile(outputParent);
        return "\"" + outputParent + File.separator
                + task.getName().replaceFirst("[.][^.]+$", "")
                + "."
                + converterService.getMainController()
                .getOutputFileExtensionChoiceBox().getValue().toString() + "\"";
    }

    private void checkDirectoryForOutputFile(Path outputParent) throws IOException {
        if (!outputParent.toFile().exists()) {
            logger.info("Создаю новую директорию {}", outputParent);
            Files.createDirectory(outputParent);
        }
    }

}
