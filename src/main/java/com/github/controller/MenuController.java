package com.github.controller;

import com.github.entity.Language;
import com.github.service.ConverterService;
import com.github.view.AboutDialog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.github.util.HelperUtil.definitionLanguage;


public class MenuController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);
    private ResourceBundle resources;

    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem docFFmpegMenuItem;
    @FXML private MenuItem docFFplayMenuItem;
    @FXML private MenuItem helpMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem settingsMenuItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        docFFmpegMenuItem.setOnAction(event -> openDocumentationInBrowser("https://www.ffmpeg.org/ffmpeg.html"));
        docFFplayMenuItem.setOnAction(event -> openDocumentationInBrowser("https://www.ffmpeg.org/ffplay.html"));
        exitMenuItem.setOnAction(event -> exitFromApp());
        settingsMenuItem.setOnAction(event -> openSettings());
        if (!definitionLanguage().equals(Language.RUSSIAN)) {
            helpMenuItem.setDisable(true);
        }
        helpMenuItem.setOnAction(event -> openExamples());
        aboutMenuItem.setOnAction(event -> createAboutDialog());
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
            window.initOwner(helpMenuItem.getParentPopup().getScene().getWindow());
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

    private void createAboutDialog() {
        new AboutDialog(aboutMenuItem.getParentPopup().getScene().getWindow(), resources).showAndWait();
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
