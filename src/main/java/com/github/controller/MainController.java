package com.github.controller;

import com.github.entity.Extension;
import com.github.entity.Language;
import com.github.entity.Task;
import com.github.service.ConverterService;
import com.github.view.AboutDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static com.github.util.HelperUtil.definitionLanguage;
import static com.github.util.HelperUtil.printCollection;


public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final ConverterService converterService;

    @FXML private BorderPane rootLayout;

    @FXML private MenuItem changeVideoSettingsMenuItem1;
    @FXML private MenuItem changeVideoSettingsMenuItem2;
    @FXML private MenuItem changeVideoSettingsMenuItem3;
    @FXML private MenuItem copyMenuItem1;
    @FXML private MenuItem copyMenuItem2;
    @FXML private MenuItem copyMenuItem3;
    @FXML private MenuItem timeTrimMenuItem1;
    @FXML private MenuItem timeTrimMenuItem2;
    @FXML private MenuItem timeTrimMenuItem3;
    @FXML private MenuItem timeTrimMenuItem4;
    @FXML private MenuItem timeTrimMenuItem5;
    @FXML private MenuItem resolutionChangesMenuItem;
    @FXML private MenuItem cropMenuItem1;
    @FXML private MenuItem cropMenuItem2;
    @FXML private MenuItem setptsMenuItem1;
    @FXML private MenuItem setptsMenuItem2;
    @FXML private MenuItem setptsMenuItem3;
    @FXML private MenuItem setptsMenuItem4;
    @FXML private MenuItem transposeMenuItem1;
    @FXML private MenuItem transposeMenuItem2;
    @FXML private MenuItem aspectMenuItem;

    @FXML private MenuItem aboutButton;
    @FXML private MenuItem docFFmpegMenuItem;
    @FXML private MenuItem docFFplayMenuItem;
    @FXML private MenuItem examplesMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem settingsMenuItem;

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
    private ResourceBundle resources;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
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
        initializePresets();
        docFFmpegMenuItem.setOnAction(event -> openDocumentationInBrowser("https://www.ffmpeg.org/ffmpeg.html"));
        docFFplayMenuItem.setOnAction(event -> openDocumentationInBrowser("https://www.ffmpeg.org/ffplay.html"));
        exitMenuItem.setOnAction(event -> exitFromApp());
        settingsMenuItem.setOnAction(event -> openSettings());
        if (!definitionLanguage().equals(Language.RUSSIAN)) {
            examplesMenuItem.setDisable(true);
        }
        examplesMenuItem.setOnAction(event -> openExamples());
    }


    private void openSettings() {
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/fxml/settings.fxml")), resources
            );
            Stage window = new Stage();
            window.setScene(new Scene(root));
            window.setTitle(resources.getString("settings"));
            initializeIcon(window);
            window.initModality(Modality.APPLICATION_MODAL);
            window.initOwner(settingsMenuItem.getParentPopup().getScene().getWindow());
            window.setResizable(false);
            window.show();
        } catch (IOException e) {
            logger.error("Ошибка открытия вкладки: Настройки");
        }
    }

    private void openExamples() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/docRU.fxml")));
            Stage window = new Stage();
            window.setScene(new Scene(root));
            window.setTitle("Справка");
            initializeIcon(window);
            window.initModality(Modality.NONE);
            window.initOwner(examplesMenuItem.getParentPopup().getScene().getWindow());
            window.setResizable(false);
            window.show();
        } catch (IOException e) {
            logger.error("Ошибка открытия вкладки: Примеры");
        }
    }

    private void initializeIcon(Stage window) {
        try {
            window.getIcons().add(new Image("/images/icon.png"));
        } catch (Exception e) {
            logger.error("Иконка исчезла. Причина - {}", e.getMessage());
        }
    }


    private void initializeTable() {
        // Поддержка выбора нескольких строк через Ctrl, Shift
        taskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        taskTable.setPlaceholder(new Label(resources.getString("task_table_placeholder")));
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
            tasks.forEach(task -> task.setSpecParam(beforeInputField.getText()));
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
        new AboutDialog(aboutButton.getParentPopup().getScene().getWindow(), resources).showAndWait();
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

    private void initializePresets() {
        changeVideoSettingsMenuItem1.setOnAction(event -> {
            paramField.setText("-c:v libx264");
            beforeInputField.setText("");
        });
        changeVideoSettingsMenuItem2.setOnAction(event -> {
            paramField.setText("-c:v libx265");
            beforeInputField.setText("");
        });
        changeVideoSettingsMenuItem3.setOnAction(event -> {
            paramField.setText("-c:v libx265 -c:a aac -b:v 1500k -b:a 320k");
            beforeInputField.setText("");
        });
        copyMenuItem1.setOnAction(event -> {
            paramField.setText("-c copy");
            beforeInputField.setText("");
        });
        copyMenuItem2.setOnAction(event -> {
            paramField.setText("-c:v copy");
            beforeInputField.setText("");
        });
        copyMenuItem3.setOnAction(event -> {
            paramField.setText("-c:a copy");
            beforeInputField.setText("");
        });
        timeTrimMenuItem1.setOnAction(event -> {
            beforeInputField.setText("-ss 00:00:05");
            paramField.setText("-t 00:05:15 -c copy");
        });
        timeTrimMenuItem2.setOnAction(event -> {
            beforeInputField.setText("-ss 00:42:00");
            paramField.setText("-t 10*60 -c copy");
        });
        timeTrimMenuItem3.setOnAction(event -> {
            beforeInputField.setText("-ss 00:14:00");
            paramField.setText("-to 00:28:00");
        });
        timeTrimMenuItem4.setOnAction(event -> {
            beforeInputField.setText("-sseof 30");
            paramField.setText("");
        });
        timeTrimMenuItem5.setOnAction(event -> {
            beforeInputField.setText("-sseof 60");
            paramField.setText("-t 30");
        });
        timeTrimMenuItem5.setOnAction(event -> {
            beforeInputField.setText("-sseof 60");
            paramField.setText("-t 30");
        });
        resolutionChangesMenuItem.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-s 1280x720");
        });
        cropMenuItem1.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-vf crop=640:480:200:150");
        });
        cropMenuItem2.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-vf crop=300:220");
        });
        setptsMenuItem1.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-an -vf setpts=5*PTS");
        });
        setptsMenuItem2.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-an -vf setpts=0.25*PTS");
        });
        setptsMenuItem3.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-vf setpts=PTS/2 -af atempo=2");
        });
        setptsMenuItem4.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-vf setpts=PTS*2 -af atempo=0.5");
        });
        transposeMenuItem1.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-vf transpose=0");
        });
        transposeMenuItem2.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-vf rotate=45*PI/180");
        });
        aspectMenuItem.setOnAction(event -> {
            beforeInputField.setText("");
            paramField.setText("-aspect 16:9");
        });
    }

}
