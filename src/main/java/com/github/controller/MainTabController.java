package com.github.controller;

import com.github.entity.Extension;
import com.github.entity.Task;
import com.github.service.ConverterService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static com.github.util.HelperUtil.printCollection;


public class MainTabController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainTabController.class);

    @FXML private AnchorPane rootMainTabLayout;

    @FXML private MenuItem changeVideoSettingsMenuItem1;
    @FXML private MenuItem changeVideoSettingsMenuItem2;
    @FXML private MenuItem changeVideoSettingsMenuItem3;
    @FXML private MenuItem copyMenuItem1;
    @FXML private MenuItem copyMenuItem2;
    @FXML private MenuItem copyMenuItem3;
    @FXML private MenuItem timeTrimMenuItem1;
    @FXML private MenuItem timeTrimMenuItem2;
    @FXML private MenuItem timeTrimMenuItem3;
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

    @FXML private TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> filenameColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> timeColumn;

    @FXML private Button addFilesButton;
    @FXML private Button stopAllTasksButton;
    @FXML private SplitMenuButton removeSelectedTasksButton;
    @FXML private MenuItem removeAllTasksButton;
    @FXML private MenuItem removeCompletedTasksButton;
    @FXML private SplitMenuButton startSelectedTasksButton;
    @FXML private MenuItem startAllTasksMenuItem;


    @FXML private TextField paramField;
    @FXML private TextField beforeInputField;
    @FXML private ChoiceBox<Extension> outputFileExtensionChoiceBox;

    private ObservableList<Task> observableList;
    private final ConverterService converterService;

    private ResourceBundle resources;

    private File directory = null;
    private FileChooser fileChooser;

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

    public static ObservableList<Task> getObservableList(List<Task> list) {
        return FXCollections.observableList(list);
    }

    public MainTabController() {
        converterService = ConverterService.getInstance();
        fileChooser = getFileChooser();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        initializeTable();
        initializeButton();
        initializePresets();
        initializeOutputFileExtensionChoiceBox();
    }

    private void initializeOutputFileExtensionChoiceBox() {
        outputFileExtensionChoiceBox.getItems().setAll(FXCollections.observableArrayList(Extension.values()));
        outputFileExtensionChoiceBox.setValue(Extension.MP4);
    }

    private void initializeTable() {
        // Поддержка выбора нескольких строк через Ctrl, Shift
        taskTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        taskTable.setPlaceholder(new Label(resources.getString("task_table_placeholder")));
        addDragAndDrop();
        filenameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        taskTable.setRowFactory(param -> {
            TableRow<Task> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem startItem = new MenuItem(resources.getString("start"));
            startItem.setOnAction(event -> start(taskTable.getSelectionModel().getSelectedItems()));
            MenuItem deleteInTrashItem = new MenuItem(resources.getString("delete_files_to_trash"));
            deleteInTrashItem.setOnAction(event -> deleteSelectedFilesToTrash());
            MenuItem menuItem1 = new MenuItem(resources.getString("play_ffplay_360"));
            menuItem1.setOnAction(event -> playFFplay("360"));
            MenuItem menuItem2 = new MenuItem(resources.getString("play_ffplay_480"));
            menuItem2.setOnAction(event -> playFFplay("480"));
            MenuItem menuItem3 = new MenuItem(resources.getString("play_ffplay_720"));
            menuItem3.setOnAction(event -> playFFplay("720"));
            MenuItem menuItem4 = new MenuItem(resources.getString("play_vlc"));
            menuItem4.setOnAction(event -> converterService.playInVlc(
                    taskTable.getSelectionModel().getSelectedItems().get(0).getFile().getAbsolutePath()));
            MenuItem menuItem5 = new MenuItem(resources.getString("edit_avidemux"));
            menuItem5.setOnAction(event -> converterService.editInAvidemux(
                    taskTable.getSelectionModel().getSelectedItems().get(0).getFile().getAbsolutePath()));
            MenuItem menuItem6 = new MenuItem(resources.getString("open_folder"));
            menuItem6.setOnAction(event -> openFolder());
            MenuItem[] menuItems1 = {startItem};
            MenuItem[] menuItemsForOneRow = {menuItem1, menuItem2, menuItem3, menuItem4, menuItem5, menuItem6};
            MenuItem[] menuItems2 = {deleteInTrashItem};
            Arrays.stream(menuItemsForOneRow).forEach(menuItem -> menuItem.visibleProperty()
                    .bind(Bindings.size(taskTable.getSelectionModel().getSelectedItems()).isEqualTo(1)));
            contextMenu.getItems().addAll(menuItems1);
            contextMenu.getItems().addAll(menuItemsForOneRow);
            contextMenu.getItems().addAll(menuItems2);

            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu));
            return row;
        });
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
        removeSelectedTasksButton.setOnAction(event -> removeSelectedTasksFromTable());
        removeAllTasksButton.setOnAction(event -> removeAllTasksFromTable());
        removeCompletedTasksButton.setOnAction(event -> removeCompletedTasksFromTable());
        startSelectedTasksButton.setOnAction(event -> start(taskTable.getSelectionModel().getSelectedItems()));
        startAllTasksMenuItem.setOnAction(event -> start(taskTable.getItems()));
        stopAllTasksButton.setOnAction(event -> stopAllTasks());
    }

    private void playFFplay(String height) {
        if (taskTable.getSelectionModel().getSelectedItems().size() == 1) {
            converterService.playInFF(
                    taskTable.getSelectionModel().getSelectedItems().get(0).getFile().getAbsolutePath(), height
            );
        } else {
            logger.debug("Не выбран файл или выбрано > 1");
        }
    }

    private void addFilesInTable() {
        List<File> files = fileChooser.showOpenMultipleDialog(rootMainTabLayout.getScene().getWindow());
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

    private void removeSelectedTasksFromTable() {
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

    private void removeAllTasksFromTable() {
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
            Predicate<Task> predicate = task -> task.getStatus().equals("") || task.getStatus().equals("See log");
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

    private void stopAllTasks() {
        converterService.getTasks().clear();
        converterService.cancel();
        taskTable.getItems()
                .filtered(task -> !task.getStatus().equals("Done"))
                .filtered(task -> !task.getStatus().equals("See log"))
                .forEach(task -> task.setStatus(""));
        taskTable.refresh();
    }

    private void removeCompletedTasksFromTable() {
        taskTable.getItems().removeIf(task -> task.getStatus().equals("Done"));
    }

    private void openFolder() {
        if (Desktop.isDesktopSupported()) {
            try {
                File directoryCurrent = taskTable.getSelectionModel().getSelectedItems().get(0).getFile().getParentFile();
                Desktop desktop = Desktop.getDesktop();
                desktop.open(directoryCurrent);
            } catch (Exception e) {
                logger.debug("Нет параметров для открытия директории {}", e.getMessage());
            }
        }
    }

    private void deleteSelectedFilesToTrash() {
        if (Desktop.isDesktopSupported()) {
            logger.debug("Запушена команда на удаление файлов исходников из ФС");
            List<Task> listTasks = taskTable.getSelectionModel().getSelectedItems();
            listTasks.forEach(task -> Desktop.getDesktop().moveToTrash(task.getFile()));
            removeSelectedTasksFromTable();
        }
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
