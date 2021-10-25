package com.github;

import com.github.service.ConverterService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.github.util.HelperUtil.createSettings;
import static com.github.util.HelperUtil.readProperties;


public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static Locale locale;

    @Override
    public void start(Stage primaryStage) throws Exception {
        locale = loadLocale();
        ResourceBundle resourceBundle = ResourceBundle.getBundle("bundles.resources", locale);

        Parent root = FXMLLoader.load(
                Objects.requireNonNull(getClass().getResource("/fxml/main.fxml")), resourceBundle
        );

        primaryStage.setTitle("Converter 0.5");
        try {
            primaryStage.getIcons().add(new Image("/images/icon.png"));
        } catch (Exception e) {
            logger.error("Иконка исчезла. Причина - {}", e.getMessage());
        }
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            ConverterService.stopProcesses();
            System.exit(0);
        });
    }

    private static Locale loadLocale() throws IOException {
        Properties properties;
        try {
            properties = readProperties("./settings.properties");
        } catch (IOException e) {
            logger.info("Файл настроек не существует");
            createSettings();
            return new Locale("en");
        }
        String lang = (String) properties.get("locale");
        return new Locale(lang);

    }

    public static void main(String[] args) {
        launch(args);
    }

}