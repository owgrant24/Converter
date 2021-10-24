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

import java.util.Objects;
import java.util.ResourceBundle;


public class Main extends Application {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Objects.requireNonNull(getClass().getResource("/fxml/main.fxml")));
        fxmlLoader.setResources(ResourceBundle.getBundle("bundles.resources"));
        Parent root = fxmlLoader.load();

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

    public static void main(String[] args) {
        launch(args);
    }

}