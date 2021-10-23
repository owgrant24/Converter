package com.github.controller;

import com.github.entity.Extension;
import com.github.entity.Task;
import com.github.service.ConverterService;
import com.github.view.AboutDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static com.github.util.HelperUtil.printCollection;


public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final ConverterService converterService;

    @FXML private BorderPane rootLayout;

    @FXML private MenuItem libx264MenuItem;
    @FXML private MenuItem libx265MenuItem;
    @FXML private MenuItem copyMenuItem;
    @FXML private MenuItem aboutButton;
    @FXML private MenuItem docFFmpegMenuItem;
    @FXML private MenuItem docFFplayMenuItem;
    @FXML private MenuItem examplesMenuItem;
    @FXML private MenuItem exitMenuItem;

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> filenameColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> timeColumn;

    @FXML private Button startButton;
    @FXML private Button stopAllButton;
    @FXML private Button addFilesButton;
    @FXML private Button removeFilesButton;
    @FXML private Button removeAllFilesButton;
    @FXML private Button startAllButton;
    @FXML private Button clearCompletedButton;
    @FXML private Button clearLogButton;
    @FXML private Button copyToFile;
    @FXML private Button openFolderButton;
    @FXML private Button playButton480;
    @FXML private Button playButton720;
    @FXML private Button deleteSelectedFilesToTrashButton;


    @FXML private TextField paramField;
    @FXML private TextField beforeInputField;
    @FXML private TextArea logTextArea;
    @FXML private ChoiceBox<Extension> outputFileExtensionChoiceBox;

    private ObservableList<Task> observableList;

    private File directory = null;
    private FileChooser fileChooser;

    public MainController() {
        converterService = new ConverterService(this);
        fileChooser = getFileChooser();
    }

    private FileChooser getFileChooser() {
        FileChooser localFileChooser = new FileChooser();
        localFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "video", "*.mkv", "*.mp4", "*.m2v", "*.avi", "*.mpg", "*.ts", "*.flv"),
                new FileChooser.ExtensionFilter(
                        "all", "*.*")
        );
        return localFileChooser;
    }

    public ChoiceBox<Extension> getOutputFileExtensionChoiceBox() {
        return outputFileExtensionChoiceBox;
    }

    public TableView<Task> getTaskTable() {
        return taskTable;
    }

    public TextArea getLogTextArea() {
        return logTextArea;
    }

    public static ObservableList<Task> getObservableList(List<Task> list) {
        return FXCollections.observableList(list);
    }

    @FXML
    void initialize() {
        initializeTable();
        initializeButton();
        initializeMenu();
        initializeOutputFileExtensionChoiceBox();
    }

    private void initializeOutputFileExtensionChoiceBox() {
        outputFileExtensionChoiceBox.getItems().setAll(FXCollections.observableArrayList(Extension.values()));
        outputFileExtensionChoiceBox.setValue(Extension.MP4);
    }

    private void initializeMenu() {
        libx265MenuItem.setOnAction(event -> paramField.setText("-c:v libx265"));
        libx264MenuItem.setOnAction(event -> paramField.setText("-c:v libx264"));
        copyMenuItem.setOnAction(event -> paramField.setText("-c copy"));
        docFFmpegMenuItem.setOnAction(event -> openDocumentationInBrowser("https://www.ffmpeg.org/ffmpeg.html"));
        docFFplayMenuItem.setOnAction(event -> openDocumentationInBrowser("https://www.ffmpeg.org/ffplay.html"));
        exitMenuItem.setOnAction(event -> exitFromApp());
    }

    private void initializeTable() {
        // Поддержка выбора нескольких строк через Ctrl, Shift
        taskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addDragAndDrop();
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
    }

    private void addDragAndDrop() {
        taskTable.setOnDragOver(event -> event.acceptTransferModes(TransferMode.LINK));
        taskTable.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (event.getDragboard().hasFiles()) {
                List<File> files = db.getFiles();
                files.stream()
                        .sorted()
                        .forEach(file -> converterService.getList().add(new Task(file.getName(), file)));
                observableList = getObservableList(converterService.getList());
                taskTable.setItems(observableList);
            }
        });
    }

    private void initializeButton() {
        addFilesButton.setOnAction(event -> addFilesInTable());
        removeFilesButton.setOnAction(event -> removeSelectedFilesFromTable());
        removeAllFilesButton.setOnAction(event -> removeAllFilesFromTable());
        startButton.setOnAction(event -> start(taskTable.getSelectionModel().getSelectedItems()));
        startAllButton.setOnAction(event -> start(taskTable.getItems()));
        stopAllButton.setOnAction(event -> cancelAllItems());
        clearCompletedButton.setOnAction(event -> clearCompletedFromTable());
        clearLogButton.setOnAction(event -> logTextArea.clear());
        openFolderButton.setOnAction(event -> openFolder());
        aboutButton.setOnAction(event -> createAboutDialog());
        copyToFile.setOnAction(event -> copyToFile());
        playButton480.setOnAction(event -> playFFplay("480"));
        playButton720.setOnAction(event -> playFFplay("720"));
        deleteSelectedFilesToTrashButton.setOnAction(event -> deleteSelectedFilesToTrash());
    }

    private void playFFplay(String height) {
        if (taskTable.getSelectionModel().getSelectedItems().size() == 1) {
            converterService.playFF(
                    taskTable.getSelectionModel().getSelectedItems().get(0).getFile().getAbsolutePath(), height
            );
        } else {
            logger.debug("Не выбран файл или выбрано > 1");
        }
    }

    private void addFilesInTable() {
        List<File> files = fileChooser.showOpenMultipleDialog(rootLayout.getScene().getWindow());
        if (files != null) {
            logger.debug(
                    "Содержимое taskList до добавления файлов: {}",
                    printCollection(converterService.getList())
            );
            files.stream().map(file -> new Task(file.getName(), file))
                    .filter(task -> !(converterService.getList().contains(task)))
                    .forEach(task -> converterService.getList().add(task));
            // Запоминаем последний путь
            if (!files.isEmpty()) {
                directory = new File(files.get(0).getParent());
                fileChooser.setInitialDirectory(directory);
            }
            observableList = getObservableList(converterService.getList());
            taskTable.setItems(observableList);
            logger.debug(
                    "Содержимое taskList после добавления файлов: {}",
                    printCollection(converterService.getList())
            );
        }
    }

    private void removeSelectedFilesFromTable() {
        if (!taskTable.getSelectionModel().getSelectedItems().isEmpty()) {
            logger.debug(
                    "Содержимое taskList до удаления {}",
                    converterService.getList()
            );
            taskTable.getItems().removeAll(List.copyOf(taskTable.getSelectionModel().getSelectedItems()));
            logger.debug(
                    "Содержимое taskList после удаления: {}",
                    printCollection(converterService.getList())
            );
            taskTable.refresh();
        } else {
            logger.debug("Не выбрано ни одного файла");
        }
    }

    private void removeAllFilesFromTable() {
        if (!taskTable.getItems().isEmpty()) {
            logger.debug(
                    "Содержимое taskList до нажатия кнопки \"Удалить все файлы\" {}",
                    printCollection(converterService.getList())
            );
            taskTable.getItems().clear();
            logger.debug(
                    "Содержимое taskList после нажатия кнопки \"Удалить все файлы\": {}",
                    printCollection(converterService.getList())
            );
            taskTable.refresh();
        } else {
            logger.debug("Отсутствуют файлы в таблице");
        }
    }

    private void start(ObservableList<Task> items) {
        if (!items.isEmpty() && !(paramField.getText().isBlank())) {
            Predicate<Task> predicate = task -> task.getStatus().equals("");
            List<Task> tasks = new ArrayList<>(items.filtered(predicate));
            taskTable.refresh();
            items.filtered(predicate).forEach(task -> task.setStatus("In queue"));
            tasks.forEach(task -> task.setParam(paramField.getText()));
            tasks.forEach(task -> task.setBeforeInput(beforeInputField.getText()));
            converterService.getTasks().addAll(tasks);
            converterService.startTask();
        } else {
            logger.info("Нет подходяших заданий или параметры конвертации не заданы");
        }
    }

    private void cancelAllItems() {
        converterService.getTasks().clear();
        converterService.cancel();
        taskTable.getItems()
                .filtered(task -> !task.getStatus().equals("Done"))
                .filtered(task -> !task.getStatus().equals("See log"))
                .forEach(task -> task.setStatus(""));
        taskTable.refresh();
    }

    private void clearCompletedFromTable() {
        taskTable.getItems().removeIf(task -> task.getStatus().equals("Done"));
    }

    private void openFolder() {
        if (Desktop.isDesktopSupported()) {
            try {
                File directoryCurrent = null;
                if (taskTable.getSelectionModel().getSelectedItems().size() == 1) {
                    directoryCurrent = taskTable.getSelectionModel().getSelectedItems().get(0).getFile().getParentFile();
                } else if (directory != null) {
                    directoryCurrent = directory;
                }
                Desktop desktop = Desktop.getDesktop();
                desktop.open(directoryCurrent);
            } catch (Exception e) {
                logger.debug("Нет параметров для открытия директории {}", e.getMessage());
            }
        }
    }

    private void createAboutDialog() {
        new AboutDialog(aboutButton.getParentPopup().getScene().getWindow()).showAndWait();
    }

    private void copyToFile() {
        if (!logTextArea.getText().isBlank()) {
            FileChooser fileSaveChooser = new FileChooser();
            fileSaveChooser.setTitle("Select directory for save");
            String format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            fileSaveChooser.setInitialFileName("ffmpeg_log_" + format);
            fileSaveChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Text Document", "*.txt"));
            File file = fileSaveChooser.showSaveDialog(rootLayout.getScene().getWindow());
            if (file != null) {
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                    bufferedWriter.write(logTextArea.getText());
                } catch (IOException e) {
                    logger.info("Запись в файл не получилась {}", e.getMessage());
                }
            }

        } else {
            logger.debug("Лог пустой");
        }
    }

    private void deleteSelectedFilesToTrash() {
        if (!taskTable.getSelectionModel().getSelectedItems().isEmpty()) {
            if (Desktop.isDesktopSupported()) {
                logger.debug("Запушена команда на удаление файлов исходников из ФС");
                List<Task> listTasks = taskTable.getSelectionModel().getSelectedItems();
                listTasks.forEach(task -> Desktop.getDesktop().moveToTrash(task.getFile()));
                removeSelectedFilesFromTable();
            }
        } else {
            logger.debug("Не выбрано ни одного файла");
        }
    }

    private void openDocumentationInBrowser(String link) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URI.create(link));
            } catch (IOException e) {
                logger.error("Ошибка открытия документации в браузере: {}", e.getMessage());
            }
        }
    }

    private void exitFromApp() {
        ConverterService.stopProcesses();
        System.exit(0);
    }

}
