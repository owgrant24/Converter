package com.github;

import com.github.util.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Main extends Application {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/main.fxml")));
        primaryStage.setTitle("Converter");
        try {
            primaryStage.getIcons().add(new Image("/images/icon.png"));
        } catch (Exception e) {
            logger.info("Иконка исчезла");
        }
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            // TODO Разработать удаление процесса, если он завис
            Util.PROCESSES.forEach(process -> process.descendants().forEach(ProcessHandle::destroy));
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}