package com.github.controller;

import com.github.entity.Language;
import com.github.util.SettingsCreator;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static com.github.util.HelperUtil.defineLanguage;


public class SettingsController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @FXML private ChoiceBox<Language> languageChoiceBox;

    @FXML private Button applyButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        languageChoiceBox.setItems(FXCollections.observableArrayList(Language.values()));
        languageChoiceBox.setValue(defineLanguage());
        applyButton.setOnAction(event -> applyLanguage());
    }

    private void applyLanguage() {
        try {
            PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration();
            SettingsCreator.readConfigurationFromFile(propertiesConfiguration);
            String localeFromFile = propertiesConfiguration.getString("locale");
            String localeFromGui = languageChoiceBox.getValue().getLocale();
            if (!localeFromFile.equals(localeFromGui)) {
                propertiesConfiguration.setProperty("locale", localeFromGui);
                SettingsCreator.saveConfigurationInFile(propertiesConfiguration);
                logger.debug("Cохранение в файл произведено успешно");
            }
        } catch (ConfigurationException e) {
            logger.error("Ошибка сохранения настроек в файл");
        }

    }

}

