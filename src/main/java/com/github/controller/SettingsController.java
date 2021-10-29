package com.github.controller;

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

    @FXML private ChoiceBox<Language> languageChoiceBox;

    @FXML private Button applyButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageChoiceBox.setItems(FXCollections.observableArrayList(Language.values()));
        languageChoiceBox.setValue(definitionLanguage());
        applyButton.setOnAction(event -> applyLanguage());
    }

    private void applyLanguage() {
        try {
            Properties properties = readProperties("./settings.properties");
            String localeFromFile = (String) properties.get("locale");
            String localeFromGui = languageChoiceBox.getValue().getLocale();
            if (!localeFromFile.equals(localeFromGui)) {
                try (Writer bufferedWriter = new BufferedWriter(
                        new FileWriter("./settings.properties", StandardCharsets.UTF_8))) {
                    properties.setProperty("locale", localeFromGui);
                    properties.store(bufferedWriter, null);
                    logger.debug("Cохранение в файл произведено");
                }

            }
        } catch (IOException e) {
            logger.error("Ошибка сохранения настроек в файл");
        }

    }

}

