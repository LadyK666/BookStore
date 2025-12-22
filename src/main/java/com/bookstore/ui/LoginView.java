package com.bookstore.ui;

import com.bookstore.dao.CustomerDao;
import com.bookstore.model.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * 登录界面
 */
public class LoginView {

    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> userTypeCombo;
    private Label messageLabel;

    // 管理员账号（简化处理，实际应存数据库）
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public LoginView() {
        createView();
    }

    private void createView() {
        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);");

        // 标题
        Label titleLabel = new Label("网上书店管理系统");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Online Bookstore Management System");
        subtitleLabel.setFont(Font.font("Arial", 12));
        subtitleLabel.setTextFill(Color.web("#e0e0e0"));

        // 登录表单容器
        VBox formBox = new VBox(12);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(25));
        formBox.setMaxWidth(320);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // 用户类型选择
        Label typeLabel = new Label("用户类型");
        typeLabel.setFont(Font.font("Microsoft YaHei", 13));
        userTypeCombo = new ComboBox<>();
        userTypeCombo.getItems().addAll("顾客", "管理员");
        userTypeCombo.setValue("顾客");
        userTypeCombo.setPrefWidth(260);
        userTypeCombo.setStyle("-fx-font-size: 13px;");

        // 用户名
        Label userLabel = new Label("用户名");
        userLabel.setFont(Font.font("Microsoft YaHei", 13));
        usernameField = new TextField();
        usernameField.setPromptText("请输入用户名");
        usernameField.setPrefWidth(260);
        usernameField.setStyle("-fx-font-size: 13px;");

        // 密码
        Label passLabel = new Label("密码");
        passLabel.setFont(Font.font("Microsoft YaHei", 13));
        passwordField = new PasswordField();
        passwordField.setPromptText("请输入密码");
        passwordField.setPrefWidth(260);
        passwordField.setStyle("-fx-font-size: 13px;");

        // 提示消息
        messageLabel = new Label();
        messageLabel.setTextFill(Color.RED);
        messageLabel.setFont(Font.font("Microsoft YaHei", 12));

        // 登录按钮
        Button loginBtn = new Button("登 录");
        loginBtn.setPrefWidth(260);
        loginBtn.setPrefHeight(38);
        loginBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 5;");
        loginBtn.setOnAction(e -> handleLogin());

        // 注册按钮（仅顾客）
        Button registerBtn = new Button("注册新账号");
        registerBtn.setPrefWidth(260);
        registerBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #667eea; -fx-font-size: 12px;");
        registerBtn.setOnAction(e -> showRegisterDialog());

        formBox.getChildren().addAll(
                typeLabel, userTypeCombo,
                userLabel, usernameField,
                passLabel, passwordField,
                messageLabel,
                loginBtn, registerBtn
        );

        // 提示信息
        Label hintLabel = new Label("管理员账号: admin / admin123");
        hintLabel.setFont(Font.font("Microsoft YaHei", 11));
        hintLabel.setTextFill(Color.web("#cccccc"));

        root.getChildren().addAll(titleLabel, subtitleLabel, formBox, hintLabel);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String userType = userTypeCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("请输入用户名和密码");
            return;
        }

        if ("管理员".equals(userType)) {
            // 管理员登录
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                messageLabel.setText("");
                MainApp.showAdminView(username);
            } else {
                messageLabel.setText("管理员账号或密码错误");
            }
        } else {
            // 顾客登录
            try {
                CustomerDao customerDao = new CustomerDao();
                Customer customer = customerDao.findByUsername(username);
                if (customer == null) {
                    messageLabel.setText("用户不存在");
                } else if (!customer.getPasswordHash().equals(password)) {
                    // 简化处理：直接比较密码（实际应比较哈希）
                    messageLabel.setText("密码错误");
                } else if ("FROZEN".equals(customer.getAccountStatus())) {
                    messageLabel.setText("账户已被冻结，请联系管理员");
                } else {
                    messageLabel.setText("");
                    MainApp.showCustomerView(customer.getCustomerId(), customer.getRealName());
                }
            } catch (Exception ex) {
                messageLabel.setText("登录失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void showRegisterDialog() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("注册新账号");
        dialog.setHeaderText("请填写注册信息");

        ButtonType registerButtonType = new ButtonType("注册", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 100, 10, 10));

        TextField regUsername = new TextField();
        regUsername.setPromptText("用户名");
        PasswordField regPassword = new PasswordField();
        regPassword.setPromptText("密码");
        TextField regRealName = new TextField();
        regRealName.setPromptText("真实姓名");
        TextField regPhone = new TextField();
        regPhone.setPromptText("手机号");
        TextField regEmail = new TextField();
        regEmail.setPromptText("邮箱");

        grid.add(new Label("用户名:"), 0, 0);
        grid.add(regUsername, 1, 0);
        grid.add(new Label("密码:"), 0, 1);
        grid.add(regPassword, 1, 1);
        grid.add(new Label("真实姓名:"), 0, 2);
        grid.add(regRealName, 1, 2);
        grid.add(new Label("手机号:"), 0, 3);
        grid.add(regPhone, 1, 3);
        grid.add(new Label("邮箱:"), 0, 4);
        grid.add(regEmail, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                try {
                    String uname = regUsername.getText().trim();
                    String pwd = regPassword.getText();
                    String realName = regRealName.getText().trim();
                    String phone = regPhone.getText().trim();
                    String email = regEmail.getText().trim();

                    if (uname.isEmpty() || pwd.isEmpty() || realName.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "注册失败", "用户名、密码和真实姓名不能为空");
                        return null;
                    }

                    CustomerDao customerDao = new CustomerDao();
                    if (customerDao.findByUsername(uname) != null) {
                        showAlert(Alert.AlertType.ERROR, "注册失败", "用户名已存在");
                        return null;
                    }

                    Customer newCustomer = new Customer();
                    newCustomer.setUsername(uname);
                    newCustomer.setPasswordHash(pwd); // 简化处理
                    newCustomer.setRealName(realName);
                    newCustomer.setMobilePhone(phone);
                    newCustomer.setEmail(email);
                    newCustomer.setCreditLevelId(1); // 默认一级
                    
                    int result = customerDao.insert(newCustomer);
                    if (result > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "注册成功", "账号注册成功，请登录");
                        usernameField.setText(uname);
                    }
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "注册失败", ex.getMessage());
                    ex.printStackTrace();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}

