package com.github.controller;

public class ControllerMediator implements IMediateControllers {

    private MainTabController mainTabController;
    private LogTabController logTabController;

    @Override
    public void registerMainTabController(MainTabController controller) {
        this.mainTabController = controller;
    }

    @Override
    public void registerLogTabController(LogTabController controller) {
        this.logTabController = controller;
    }

    public MainTabController getMainTabController() {
        return mainTabController;
    }

    public LogTabController getLogTabController() {
        return logTabController;
    }

    public static ControllerMediator getInstance() {
        return ControllerMediatorHolder.INSTANCE;
    }

    private static class ControllerMediatorHolder {

        private static final ControllerMediator INSTANCE = new ControllerMediator();

    }

}
