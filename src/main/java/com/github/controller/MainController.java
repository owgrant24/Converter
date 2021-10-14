package com.github.controller;

import com.github.entity.Extension;
import com.github.entity.Task;
import com.github.service.ConverterService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
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
    private Button openFolderButton;
    @FXML
    private TextArea logTextArea;
    @FXML
    private MenuItem aboutButton;
    @FXML
    private ChoiceBox<Extension> outputFileExtensionChoiceBox;

    private ObservableList<Task> observableList;

    private FileChooser fileChooser;

    private File directory = null;

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
                        .forEach(file -> converterService.getList()
                                .add(new Task(file.getName(), file)));
                observableList = getObservableList(converterService.getList());
                taskTable.setItems(observableList);
            }
        });
    }

    private void initializeButton() {
        actionAddFilesInTable();
        actionRemoveSelectedFilesFromTable();
        actionRemoveAllFilesFromTable();
        actionStartSelectedItem();
        actionStartAllItems();
        actionCancelAllItems();
        actionClearCompleted();
        actionClearLog();
        actionOpenFolder();
        actionAbout();
    }

    private void actionAbout() {
        aboutButton.setOnAction(event -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("About the program");
            dialog.setContentText("The program works on the basis of FFMPEG\nDeveloper - Aleksandr Shabelskii\n2021");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            dialog.initOwner(aboutButton.getParentPopup().getScene().getWindow());
            dialog.setX(aboutButton.getParentPopup().getScene().getWindow().getX());
            dialog.setY(aboutButton.getParentPopup().getScene().getWindow().getY());
            try {
                ((Stage) dialog.getDialogPane().getScene().getWindow())
                        .getIcons().add(new Image("/images/icon.png"));
            } catch (Exception e) {
                logger.error("Иконка исчезла. Причина - {}", e.getMessage());
            }
            dialog.showAndWait();
        });
    }

    private void actionOpenFolder() {
        openFolderButton.setOnAction(event -> {
            if (directory != null) {
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(directory);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        });
    }

    private void actionCancelAllItems() {
        stopAllButton.setOnAction(event -> {
            converterService.getTasks().clear();
            converterService.cancel();
            taskTable.getItems()
                    .filtered(task -> !task.getStatus().equals("Done"))
                    .forEach(task -> task.setStatus(""));
            taskTable.refresh();
        });
    }

    private void actionStartAllItems() {
        startAllButton.setOnAction(event -> start(taskTable.getItems()));
    }

    private void actionStartSelectedItem() {
        startButton.setOnAction(event -> start(taskTable.getSelectionModel().getSelectedItems()));
    }

    private void actionClearCompleted() {
        clearCompletedButton.setOnAction(
                event -> taskTable.getItems()
                        .removeIf(task -> task.getStatus().equals("Done"))
        );
    }

    private void actionClearLog() {
        clearLogButton.setOnAction(event -> logTextArea.clear());
    }

    private void actionRemoveAllFilesFromTable() {
        removeAllFilesButton.setOnAction(event -> {
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
        });
    }

    private void actionRemoveSelectedFilesFromTable() {
        removeFilesButton.setOnAction(event -> {
            // https://coderoad.ru/52449706/JavaFX-%D1%83%D0%B4%D0%B0%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-%D0%B8%D0%B7-TableView
            if (!taskTable.getSelectionModel().getSelectedItems().isEmpty()) {
                logger.debug("Содержимое taskList до нажатия кнопки \"Удалить файлы\" {}", converterService.getList());
                taskTable.getItems().removeAll(List.copyOf(taskTable.getSelectionModel().getSelectedItems()));
                logger.debug(
                        "Содержимое taskList после нажатия кнопки \"Удалить файлы\": {}",
                        printCollection(converterService.getList())
                );
                taskTable.refresh();
            } else {
                logger.debug("Не выбрано ни одного файла");
            }

        });
    }

    private void actionAddFilesInTable() {
        addFilesButton.setOnAction(event -> {

            List<File> files = fileChooser.showOpenMultipleDialog(rootLayout.getScene().getWindow());
            if (files != null) {
                logger.debug(
                        "Содержимое taskList до нажатия кнопки \"Добавить файлы\": {}",
                        printCollection(converterService.getList())
                );
                files.forEach(file -> converterService.getList().add(new Task(file.getName(), file)));
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
        });
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

}