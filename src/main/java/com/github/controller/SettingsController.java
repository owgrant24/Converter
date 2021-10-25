package com.github.controller;

import com.github.Main;
import com.github.entity.Language;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.github.util.HelperUtil.definitionLanguage;
import static com.github.util.HelperUtil.readProperties;


public class SettingsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
    private ResourceBundle resources;

    @FXML private ChoiceBox<Language> languageChoiceBox;

    @FXML private Button applyButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        languageChoiceBox.setItems(FXCollections.observableArrayList(Language.values()));
        languageChoiceBox.setValue(definitionLanguage());
        applyButton.setOnAction(event -> applyLanguage());
    }

    private void applyLanguage() {
        try {
            Properties properties = readProperties("./settings.properties");
            String locale = (String) properties.get("locale");
            if (!locale.equals(languageChoiceBox.getValue().getLocale())) {
                try (Writer bufferedWriter = new BufferedWriter(
                        new FileWriter("./settings.properties", StandardCharsets.UTF_8))) {
                    properties.setProperty("locale", languageChoiceBox.getValue().getLocale());
                    properties.store(bufferedWriter, null);
                    logger.debug("Произошло сохранение в файл");
                }

            }
        } catch (IOException e) {
            logger.error("Ошибка сохранения настроек в файл");
        }

    }

}

