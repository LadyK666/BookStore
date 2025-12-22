package com.bookstore.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 网上书店管理系统 - JavaFX 主应用入口
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("网上书店管理系统");
        
        // 显示登录界面
        showLoginView();
    }

    public static void showLoginView() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 400, 350);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void showCustomerView(long customerId, String customerName) {
        CustomerView customerView = new CustomerView(customerId, customerName);
        Scene scene = new Scene(customerView.getView(), 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    public static void showAdminView(String adminName) {
        AdminView adminView = new AdminView(adminName);
        Scene scene = new Scene(adminView.getView(), 1100, 750);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

