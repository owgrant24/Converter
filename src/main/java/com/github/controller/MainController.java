package com.github.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    @FXML private MainTabController mainTabController;
    @FXML private LogTabController logTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerMediatorImpl.getInstance().registerMainTabController(mainTabController);
        ControllerMediatorImpl.getInstance().registerLogTabController(logTabController);
    }

}
