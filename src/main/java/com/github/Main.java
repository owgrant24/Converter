package com.github;

import com.github.util.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/main.fxml")));
        primaryStage.setTitle("Converter");
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