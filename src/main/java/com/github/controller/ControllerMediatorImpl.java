package com.github.controller;

public class ControllerMediatorImpl implements ControllerMediator {

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

    public static ControllerMediatorImpl getInstance() {
        return ControllerMediatorHolder.INSTANCE;
    }

    private static class ControllerMediatorHolder {

        private static final ControllerMediatorImpl INSTANCE = new ControllerMediatorImpl();

    }

}
