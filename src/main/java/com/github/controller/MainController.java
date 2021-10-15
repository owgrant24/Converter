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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.util.HelperUtil.printCollection;


public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final ConverterService converterService;

    @FXML
    private MenuItem libx264MenuItem;
    @FXML
    private MenuItem libx265MenuItem;
    @FXML
    private MenuItem copyMenuItem;
    @FXML
    private BorderPane rootLayout;
    @FXML
    private TableView<Task> taskTable;
    @FXML
    private TableColumn<Task, String> filenameColumn;
    @FXML
    private TableColumn<Task, String> statusColumn;
    @FXML
    private TableColumn<Task, String> timeColumn;

    @FXML
    private TextField paramField;

    @FXML
    private Button startButton;
    @FXML
    private Button stopAllButton;
    @FXML
    private Button addFilesButton;
    @FXML
    private Button removeFilesButton;
    @FXML
    private Button removeAllFilesButton;
    @FXML
    private Button startAllButton;
    @FXML
    private Button clearCompletedButton;
    @FXML
    private Button clearLogButton;
    @FXML
    private Button copyToFile;
    @FXML
    private Button openFolderButton;
    @FXML
    private TextArea logTextArea;
    @FXML
    private MenuItem aboutButton;
    @FXML
    private ChoiceBox<Extension> outputFileExtensionChoiceBox;

    private ObservableList<Task> observableList;


    private File directory = null;

    public MainController() {
        converterService = new ConverterService(this);
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
    }

    private void addFilesInTable() {
        FileChooser fileChooser = getFileChooser();
        List<File> files = fileChooser.showOpenMultipleDialog(rootLayout.getScene().getWindow());
        if (files != null) {
            logger.debug(
                    "Содержимое taskList до нажатия кнопки \"Добавить файлы\": {}",
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
                    "Содержимое taskList после нажатия кнопки \"Добавить файлы\": {}",
                    printCollection(converterService.getList())
            );
        }
    }

    public void removeSelectedFilesFromTable() {
        if (!taskTable.getSelectionModel().getSelectedItems().isEmpty()) {
            logger.debug(
                    "Содержимое taskList до нажатия кнопки \"Удалить файлы\" {}",
                    converterService.getList()
            );
            taskTable.getItems().removeAll(List.copyOf(taskTable.getSelectionModel().getSelectedItems()));
            logger.debug(
                    "Содержимое taskList после нажатия кнопки \"Удалить файлы\": {}",
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
        }
    }

    private void start(ObservableList<Task> items) {
        if (!items.isEmpty() && !(paramField.getText().isBlank())) {
            Predicate<Task> predicate =
                    task -> !task.getStatus().equals("In queue")
                            && !task.getStatus().equals("In process")
                            && !task.getStatus().equals("Done");
            List<Task> tasks = new ArrayList<>(items.filtered(predicate));
            taskTable.refresh();
            items.filtered(predicate).forEach(task -> task.setStatus("In queue"));
            tasks.forEach(task -> task.setParam(paramField.getText()));
            converterService.getTasks().addAll(tasks);
            converterService.startTask();
        } else {
            logger.info("Нет походяших заданий или параметры конвертации не заданы");
        }
    }

    private void cancelAllItems() {
        converterService.getTasks().clear();
        converterService.cancel();
        taskTable.getItems()
                .filtered(task -> !task.getStatus().equals("Done"))
                .forEach(task -> task.setStatus(""));
        taskTable.refresh();
    }

    private boolean clearCompletedFromTable() {
        return taskTable.getItems()
                .removeIf(task -> task.getStatus().equals("Done"));
    }

    private void openFolder() {
        if (directory != null) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(directory);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void createAboutDialog() {
        new AboutDialog(aboutButton.getParentPopup().getScene().getWindow()).showAndWait();
    }

    private void copyToFile() {
        if(!logTextArea.getText().isBlank()){
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select directory for save");
            fileChooser.setInitialFileName("log");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Document", "*.txt"));
            File file = fileChooser.showSaveDialog(rootLayout.getScene().getWindow());

            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
                bufferedWriter.write(logTextArea.getText());
            } catch (IOException e) {
                logger.info("Запись в файл не получилась {}", e.getMessage());
            }
        } else {
            logger.debug("Лог пустой");
        }
    }

}