package com.github.controller;

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

public class MainController {
    private final static Logger logger = LoggerFactory.getLogger(MainController.class);

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
    private Button stop_button;
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

    private ObservableList<Task> observableList;

    private FileChooser fileChooser;

    public TableView<Task> getTask_table() {
        return task_table;
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
            fileChooser = new FileChooser();
            List<File> files = fileChooser.showOpenMultipleDialog(root_layout.getScene().getWindow());
            if (files != null) {
                logger.debug("Содержимое taskList до нажатия кнопки \"Добавить файлы\": " + Util.taskList);
                files.forEach(file -> Util.taskList.add(new Task(file.getName(), file, "")));
                observableList = getObservableList(Util.taskList);
                task_table.setItems(observableList);
                logger.debug("Содержимое taskList после нажатия кнопки \"Добавить файлы\": " + Util.taskList);
            }
        });
        /**
         * Удаление выбранных файлов из таблицы
         */
        remove_files_button.setOnAction(event -> {
            // https://coderoad.ru/52449706/JavaFX-%D1%83%D0%B4%D0%B0%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5-%D0%B8%D0%B7-TableView
            logger.debug("Содержимое taskList до нажатия кнопки \"Удалить файлы\"" + Util.taskList);
            task_table.getItems().removeAll(List.copyOf(task_table.getSelectionModel().getSelectedItems()));
            logger.debug("Содержимое taskList после нажатия кнопки \"Удалить файлы\": " + Util.taskList);
            task_table.refresh();
        });

        remove_all_files_button.setOnAction(event -> {
            logger.debug("Содержимое taskList до нажатия кнопки \"Удалить все файлы\"" + Util.taskList);
            task_table.getItems().clear();
            logger.debug("Содержимое taskList после нажатия кнопки \"Удалить все файлы\": " + Util.taskList);
            task_table.refresh();
        });

        /**
         * Старт выбранных файлов из таблицы
         */
        start_button.setOnAction(event -> {
            ObservableList<Task> selectedItems = task_table.getSelectionModel().getSelectedItems();
            if (!param_field.getText().isBlank()) {
                Util.startTask(new ArrayList<>(selectedItems), param_field.getText());
            }

        });
        /**
         * Старт всех файлов из таблицы
         */
        start_all_button.setOnAction(event -> {
            ObservableList<Task> allItems = task_table.getItems();
            if (!param_field.getText().isBlank()) {
                Util.startTask(new ArrayList<>(allItems), param_field.getText());
            }
        });


    }
}

