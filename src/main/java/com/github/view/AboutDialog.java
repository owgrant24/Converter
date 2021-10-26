package com.github.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;


public class AboutDialog extends Dialog<ButtonType> {

    private static final Logger logger = LoggerFactory.getLogger(AboutDialog.class);
    private final ResourceBundle resource;

    public AboutDialog(Window window, ResourceBundle resource) {
        this.resource = resource;
        initializePropertyDialog(window);
        initializeIcon();
        addKeyEvent();
    }

    private void initializePropertyDialog(Window window) {
        setTitle(resource.getString("about_dialog.about"));
        setContentText(resource.getString("about_dialog.text"));

        getDialogPane().getButtonTypes().add(ButtonType.OK);
        initOwner(window);
        setX(window.getX());
        setY(window.getY());
    }

    private void initializeIcon() {
        try {
            ((Stage) this.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/images/icon.png"));
        } catch (Exception e) {
            logger.error("Иконка исчезла. Причина - {}", e.getMessage());
        }
    }

    private void addKeyEvent() {
        getDialogPane().addEventHandler(
                KeyEvent.KEY_PRESSED,
                eventZ -> {
                    if (eventZ.getCode() == KeyCode.F1) {
                        Alert alert = new Alert(
                                Alert.AlertType.INFORMATION, "Congratulations.\nYou found an easter egg"
                        );
                        alert.setHeaderText("Easter egg");
                        alert.showAndWait();
                    }
                }
        );
    }

}
