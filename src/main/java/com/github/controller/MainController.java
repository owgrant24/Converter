package com.github.controller;

import com.github.entity.Extension;
import com.github.entity.Task;
import com.github.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.util.Util.list;

public class MainController {
    private final static Logger logger = LoggerFactory.getLogger(MainController.class);
    private final Util util;

    @FXML
    private MenuItem libx264_menu_item;
    @FXML
    private MenuItem libx265_menu_item;
    @FXML
    private BorderPane root_layout;
    @FXML
    private TableView<Task> task_table;
    @FXML
    private TableColumn<Task, String> filename_column;
    @FXML
    private TableColumn<Task, String> status_column;

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
    private TextArea log_text_area;
    @FXML
    private ChoiceBox<Extension> output_file_extension_choice_box;

    private ObservableList<Task> observableList;

    private final FileChooser fileChooser;

    public MainController() {
        util = new Util(this);
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "video", "*.mkv", "*.mp4", "*.m2v", "*.avi", "*.mpg", "*.ts", "*.flv"),
                new FileChooser.ExtensionFilter(
                        "all", "*.*"));
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
        // Поддержка выбора нескольких строк через Ctrl, Shift
        task_table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        initializeTable();
        initializeButton();
        initializeMenu();
        output_file_extension_choice_box.getItems().setAll(FXCollections.observableArrayList(Util.extension));
        output_file_extension_choice_box.setValue(Extension.MP4);
    }

    private void initializeMenu() {
        libx265_menu_item.setOnAction(event -> param_field.setText("-c:v libx265"));
        libx264_menu_item.setOnAction(event -> param_field.setText("-c:v libx264"));
    }

    private void initializeTable() {
        filename_column.setCellValueFactory(new PropertyValueFactory<>("name"));
        status_column.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void initializeButton() {
        /**
         * Добавление файлов в таблицу
         */
        add_files_button.setOnAction(event -> {

            List<File> files = fileChooser.showOpenMultipleDialog(root_layout.getScene().getWindow());
            if (files != null) {
                logger.debug("Содержимое taskList до нажатия кнопки \"Добавить файлы\": " + list);
                files.forEach(file -> list.add(new Task(file.getName(), file, "")));
                // Запоминаем последний путь
                if (files.size() > 0) {
                    fileChooser.setInitialDirectory(new File(files.get(0).getParent()));
                }

                observableList = getObservableList(list);
                task_table.setItems(observableList);
                logger.debug("Содержимое taskList после нажатия кнопки \"Добавить файлы\": " + list);
            }
        });
        /**
         * Удаление выбранных файлов из таблицы
         */
        remove_files_button.setOnAction(event -> {
            // https://coderoad.ru/52449706/JavaFX-%D1%83%D0%B4%D0%B0%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-%D0%B8%D0%B7-TableView
            logger.debug("Содержимое taskList до нажатия кнопки \"Удалить файлы\"" + list);
            task_table.getItems().removeAll(List.copyOf(task_table.getSelectionModel().getSelectedItems()));
            logger.debug("Содержимое taskList после нажатия кнопки \"Удалить файлы\": " + list);
            task_table.refresh();
        });
        /**
         * Удаление всех файлов из таблицы
         */
        remove_all_files_button.setOnAction(event -> {
            logger.debug("Содержимое taskList до нажатия кнопки \"Удалить все файлы\"" + list);
            task_table.getItems().clear();
            logger.debug("Содержимое taskList после нажатия кнопки \"Удалить все файлы\": " + list);
            task_table.refresh();
        });

        /**
         * Старт выбранных файлов из таблицы
         */
        start_button.setOnAction(event -> start(task_table.getSelectionModel().getSelectedItems()));
        /**
         * Старт всех файлов из таблицы
         */
        start_all_button.setOnAction(event -> start(task_table.getItems()));

        stop_all_button.setOnAction(event -> {
            task_table.getItems().filtered(task -> task.getStatus().equals("In process")).forEach(task -> task.setStatus("")); // FIXME
            task_table.refresh();
            util.stop();
            Util.PROCESSES.forEach(process -> process.descendants().forEach(ProcessHandle::destroy));
            Util.taskArrayDeque.clear();
        });
        /**
         * Очистка завершенных файлов
         */
        clear_completed_button.setOnAction(
                event -> task_table.getItems().removeIf(task -> task.getStatus().equals("Done"))
        );
    }

    private void start(ObservableList<Task> items) {
        List<Task> tasks = new ArrayList<>(items.filtered(task -> !task.getStatus().equals("In process")));
        task_table.refresh();
        items.forEach(task -> task.setStatus("In process"));
        Util.taskArrayDeque.addAll(tasks);
        if (!param_field.getText().isBlank() && param_field.getText().matches("[a-zA-Z\\s\\d]+")) {     // TODO regex проверить бы
            util.startTask(param_field.getText());
        }
    }
}

