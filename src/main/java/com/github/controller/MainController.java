package com.github.controller;

import com.github.entity.Extension;
import com.github.entity.Task;
import com.github.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    private final Util util;

    @FXML
    private MenuItem libx264_menu_item;
    @FXML
    private MenuItem libx265_menu_item;
    @FXML
    private MenuItem copy_menu_item;
    @FXML
    private BorderPane root_layout;
    @FXML
    private TableView<Task> task_table;
    @FXML
    private TableColumn<Task, String> filename_column;
    @FXML
    private TableColumn<Task, String> status_column;
    @FXML
    private TableColumn<?, ?> time_column;

    @FXML
    private TextField param_field;

    @FXML
    private Button start_button;
    @FXML
    private Button stop_all_button;
    @FXML
    private Button add_files_button;
    @FXML
    private Button remove_files_button;
    @FXML
    private Button remove_all_files_button;
    @FXML
    private Button start_all_button;
    @FXML
    private Button clear_completed_button;
    @FXML
    private Button clear_log_button;
    @FXML
    private Button open_folder_button;
    @FXML
    private TextArea log_text_area;
    @FXML
    private ChoiceBox<Extension> output_file_extension_choice_box;

    private ObservableList<Task> observableList;

    private FileChooser fileChooser;

    File directory = null;

    public MainController() {
        util = new Util(this);
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

    public ChoiceBox<Extension> getOutput_file_extension_choice_box() {
        return output_file_extension_choice_box;
    }

    public TableView<Task> getTask_table() {
        return task_table;
    }

    public TextArea getLog_text_area() {
        return log_text_area;
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
        output_file_extension_choice_box.getItems().setAll(FXCollections.observableArrayList(Extension.values()));
        output_file_extension_choice_box.setValue(Extension.MP4);
    }

    private void initializeMenu() {
        libx265_menu_item.setOnAction(event -> param_field.setText("-c:v libx265"));
        libx264_menu_item.setOnAction(event -> param_field.setText("-c:v libx264"));
        copy_menu_item.setOnAction(event -> param_field.setText("-c copy"));
    }

    private void initializeTable() {
        // Поддержка выбора нескольких строк через Ctrl, Shift
        task_table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        addDragAndDrop();
        filename_column.setCellValueFactory(new PropertyValueFactory<>("name"));
        status_column.setCellValueFactory(new PropertyValueFactory<>("status"));
        time_column.setCellValueFactory(new PropertyValueFactory<>("time"));
    }

    private void addDragAndDrop() {
        task_table.setOnDragOver(event -> event.acceptTransferModes(TransferMode.LINK));
        task_table.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (event.getDragboard().hasFiles()) {
                List<File> files = db.getFiles();
                files.stream()
                        .sorted()
                        .forEach(file -> util.getList()
                        .add(new Task(file.getName(), file, "", "")));
                observableList = getObservableList(util.getList());
                task_table.setItems(observableList);
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
    }

    private void actionOpenFolder() {
        open_folder_button.setOnAction(event -> {
            if (directory != null){
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
        stop_all_button.setOnAction(event -> {
            util.getTasks().clear();
            util.cancel();
            task_table.getItems()
                    .filtered(task -> !task.getStatus().equals("Done"))
                    .forEach(task -> task.setStatus(""));
            task_table.refresh();
        });
    }

    private void actionStartAllItems() {
        start_all_button.setOnAction(event -> start(task_table.getItems()));
    }

    private void actionStartSelectedItem() {
        start_button.setOnAction(event -> start(task_table.getSelectionModel().getSelectedItems()));
    }

    private void actionClearCompleted() {
        clear_completed_button.setOnAction(
                event -> task_table.getItems()
                        .removeIf(task -> task.getStatus().equals("Done"))
        );
    }

    private void actionClearLog() {
        clear_log_button.setOnAction(
                event -> log_text_area.clear()
        );
    }

    private void actionRemoveAllFilesFromTable() {
        remove_all_files_button.setOnAction(event -> {
            if (!task_table.getItems().isEmpty()) {
                logger.debug("Содержимое taskList до нажатия кнопки \"Удалить все файлы\" {}", util.getList());
                task_table.getItems().clear();
                logger.debug("Содержимое taskList после нажатия кнопки \"Удалить все файлы\": {}", util.getList());
                task_table.refresh();
            }
        });
    }

    private void actionRemoveSelectedFilesFromTable() {
        remove_files_button.setOnAction(event -> {
            // https://coderoad.ru/52449706/JavaFX-%D1%83%D0%B4%D0%B0%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-%D0%B8%D0%B7-TableView
            if (!task_table.getSelectionModel().getSelectedItems().isEmpty()) {
                logger.debug("Содержимое taskList до нажатия кнопки \"Удалить файлы\" {}", util.getList());
                task_table.getItems().removeAll(List.copyOf(task_table.getSelectionModel().getSelectedItems()));
                logger.debug("Содержимое taskList после нажатия кнопки \"Удалить файлы\": {}", util.getList());
                task_table.refresh();
            } else {
                logger.debug("Не выбрано ни одного файла");
            }

        });
    }

    private void actionAddFilesInTable() {
        add_files_button.setOnAction(event -> {

            List<File> files = fileChooser.showOpenMultipleDialog(root_layout.getScene().getWindow());
            if (files != null) {
                logger.debug("Содержимое taskList до нажатия кнопки \"Добавить файлы\": {}", util.getList());
                files.forEach(file -> util.getList().add(new Task(file.getName(), file, "", "")));
                // Запоминаем последний путь
                if (!files.isEmpty()) {
                    directory = new File(files.get(0).getParent());
                    fileChooser.setInitialDirectory(directory);
                }
                observableList = getObservableList(util.getList());
                task_table.setItems(observableList);
                logger.debug("Содержимое taskList после нажатия кнопки \"Добавить файлы\": {}", util.getList());
            }
        });
    }

    private void start(ObservableList<Task> items) {
        if (!items.isEmpty() && !(param_field.getText().isBlank())) {
            List<Task> tasks = new ArrayList<>(
                    items.filtered(
                            task -> !task.getStatus().equals("In queue")
                                    && !task.getStatus().equals("In process")
                    )
            );
            task_table.refresh();
            items.forEach(task -> task.setStatus("In queue"));
            util.getTasks().addAll(tasks);
            util.startTask(param_field.getText());
        } else {
            logger.info("Error");
        }
    }

}

