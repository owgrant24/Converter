package com.github.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;


public class LogTabController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(LogTabController.class);

    @FXML private AnchorPane logTabAnchorPaneTabLayout;
    @FXML private Button clearLogButton;
    @FXML private Button copyToFile;
    @FXML private TextArea logTextArea;

    public TextArea getLogTextArea() {
        return logTextArea;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clearLogButton.setOnAction(event -> logTextArea.clear());
        copyToFile.setOnAction(event -> copyToFile());
    }

    private void copyToFile() {
        if (!logTextArea.getText().isBlank()) {
            FileChooser fileSaveChooser = new FileChooser();
            fileSaveChooser.setTitle("Select directory for save");
            String format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            fileSaveChooser.setInitialFileName("ffmpeg_log_" + format);
            fileSaveChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Text Document", "*.txt"));
            File file = fileSaveChooser.showSaveDialog(logTabAnchorPaneTabLayout.getScene().getWindow());
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

}
