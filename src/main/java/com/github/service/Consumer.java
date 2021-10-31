package com.github.service;

import com.github.controller.ControllerMediatorImpl;
import com.github.controller.LogTabController;
import com.github.controller.MainTabController;
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

import static com.github.util.HelperUtil.printCollection;


public class Consumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    private final ConverterService converterService = ConverterService.getInstance();
    private final MainTabController mainTabController = ControllerMediatorImpl.getInstance().getMainTabController();
    private final LogTabController logTabController = ControllerMediatorImpl.getInstance().getLogTabController();
    private final Duration duration;


    public Consumer() {
        this.duration = new Duration();
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
                    converterService.getProcesses().put(current, process);
                    Future<ProcessResult> future = startedProcess.getFuture();
                    String status = future.get().outputUTF8();
                    logTabController.getLogTextArea().appendText(status);
                    logTabController.getLogTextArea().appendText("\n\n\n");
                    logger.debug("Работу выполнил над: {}", current.getName());
                    converterService.getProcesses().remove(current);
                    String statusAfterCheck = CheckStatusService.checkStatus(status);
                    current.setStatus(statusAfterCheck);
                    mainTabController.getTaskTable().refresh();
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
        parameters.add(converterService.getFfmpeg().getAbsolutePath());             // path ffmpeg
        parameters.add(ConverterService.HIDE_BANNER);                               // -hide_banner
        List<String> beforeInputList = parserParam(current.getSpecParam());
        if (!beforeInputList.isEmpty() && !beforeInputList.get(0).isBlank()) {
            parameters.addAll(beforeInputList);                                     // special parameters
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
                + changeFilenameExtensionToExtensionFromGui(task.getName())
                + "\"";
    }

    private String changeFilenameExtensionToExtensionFromGui(String fileName) {
        String nameWithoutExtension = getFileNameWithoutExtension(fileName);
        return nameWithoutExtension + "." + mainTabController.getOutputFileExtensionChoiceBox().getValue().toString();
    }

    private String getFileNameWithoutExtension(String fileName) {
        return fileName.replaceFirst("[.][^.]+$", "");
    }

    private void checkDirectoryForOutputFile(Path outputParent) throws IOException {
        if (!outputParent.toFile().exists()) {
            logger.info("Создаю новую директорию {}", outputParent);
            Files.createDirectory(outputParent);
        }
    }

}
