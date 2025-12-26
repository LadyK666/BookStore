package com.bookstore.ui;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.service.OrderService;
import com.bookstore.service.ShipmentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

/**
 * 顾客界面
 */
public class CustomerView {

    private BorderPane root;
    private long customerId;
    private String customerName;
    private Customer currentCustomer;
    private CreditLevel creditLevel;

    // 购物车：bookId -> quantity
    private Map<String, Integer> cart = new HashMap<>();


    // UI 组件
    private TableView<Book> bookTable;
    private TableView<CartItem> cartTable;
    private Label balanceLabel;
    private Label creditLabel;

    public CustomerView(long customerId, String customerName) {
        this.customerId = customerId;
        this.customerName = customerName;
        loadCustomerInfo();
        createView();
    }

    private void loadCustomerInfo() {
        try {
            CustomerDao customerDao = new CustomerDao();
            currentCustomer = customerDao.findById(customerId);
            CreditLevelDao creditLevelDao = new CreditLevelDao();
            creditLevel = creditLevelDao.findById(currentCustomer.getCreditLevelId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // 顶部导航栏
        root.setTop(createHeader());

        // 左侧：书目浏览
        root.setLeft(createBookPanel());

        // 右侧：购物车
        root.setRight(createCartPanel());

        // 底部：个人信息
        root.setBottom(createFooter());
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: #667eea;");

        Label titleLabel = new Label("网上书店");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label welcomeLabel = new Label("欢迎，" + customerName);
        welcomeLabel.setFont(Font.font("Microsoft YaHei", 14));
        welcomeLabel.setTextFill(Color.WHITE);

        Button myOrdersBtn = new Button("我的订单");
        myOrdersBtn.setStyle("-fx-background-color: white; -fx-text-fill: #667eea;");
        myOrdersBtn.setOnAction(e -> showMyOrders());

        Button noticeBtn = new Button("通知");
        noticeBtn.setStyle("-fx-background-color: #ffd700; -fx-text-fill: #333333;");
        noticeBtn.setOnAction(e -> showOutOfStockNotificationList());

        Button logoutBtn = new Button("退出登录");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white;");
        logoutBtn.setOnAction(e -> MainApp.showLoginView());

        header.getChildren().addAll(titleLabel, spacer, welcomeLabel, myOrdersBtn, noticeBtn, logoutBtn);
        return header;
    }

    private VBox createBookPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(600);

        Label titleLabel = new Label("书目浏览");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        // 搜索栏：单一文本框，支持在书号 / 书名 / 出版社 / 作者 / 关键字上模糊检索
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("输入书名、作者、关键字、书号或出版社搜索...");
        searchField.setPrefWidth(320);
        Button searchBtn = new Button("搜索");
        searchBtn.setOnAction(e -> searchBooks(searchField.getText()));
        Button refreshBtn = new Button("刷新");
        refreshBtn.setOnAction(e -> loadAllBooks());
        searchBox.getChildren().addAll(searchField, searchBtn, refreshBtn);

        // 书目表格
        bookTable = new TableView<>();
        bookTable.setPrefHeight(450);

        TableColumn<Book, String> idCol = new TableColumn<>("书号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        idCol.setPrefWidth(70);
        // 点击书号查看书目详细信息（只读）
        idCol.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    if (book != null) {
                        showBookDetailDialog(book);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    link.setText(item);
                    setGraphic(link);
                }
            }
        });

        TableColumn<Book, String> titleCol = new TableColumn<>("书名");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(180);

        TableColumn<Book, String> publisherCol = new TableColumn<>("出版社");
        publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        publisherCol.setPrefWidth(120);

        TableColumn<Book, BigDecimal> priceCol = new TableColumn<>("定价");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(70);

        TableColumn<Book, String> discountPriceCol = new TableColumn<>("折后价");
        discountPriceCol.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getPrice();
            BigDecimal discounted = price.multiply(creditLevel.getDiscountRate()).setScale(2, RoundingMode.HALF_UP);
            return new SimpleStringProperty(discounted.toString());
        });
        discountPriceCol.setPrefWidth(70);

        TableColumn<Book, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("加入");
            {
                addBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 11px;");
                addBtn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    showAddToCartDialog(book);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : addBtn);
            }
        });

        bookTable.getColumns().addAll(idCol, titleCol, publisherCol, priceCol, discountPriceCol, actionCol);
        loadAllBooks();

        panel.getChildren().addAll(titleLabel, searchBox, bookTable);
        return panel;
    }

    private VBox createCartPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setPrefWidth(380);
        panel.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("购物车");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));

        cartTable = new TableView<>();
        cartTable.setPrefHeight(300);

        TableColumn<CartItem, String> bookCol = new TableColumn<>("书名");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookTitle"));
        bookCol.setPrefWidth(150);

        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("数量");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(50);

        TableColumn<CartItem, BigDecimal> subCol = new TableColumn<>("小计");
        subCol.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        subCol.setPrefWidth(70);

        TableColumn<CartItem, Void> removeCol = new TableColumn<>("操作");
        removeCol.setPrefWidth(60);
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("删除");
            {
                removeBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-size: 10px;");
                removeBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    removeFromCart(item.getBookId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        cartTable.getColumns().addAll(bookCol, qtyCol, subCol, removeCol);

        // 合计
        Label totalLabel = new Label("应付金额：¥0.00");
        totalLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        totalLabel.setTextFill(Color.web("#e74c3c"));

        // 提交订单按钮
        Button submitBtn = new Button("提交订单");
        submitBtn.setPrefWidth(340);
        submitBtn.setPrefHeight(40);
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px;");
        submitBtn.setOnAction(e -> submitOrder(totalLabel));

        Button clearBtn = new Button("清空购物车");
        clearBtn.setPrefWidth(340);
        clearBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        clearBtn.setOnAction(e -> {
            cart.clear();
            refreshCartTable(totalLabel);
        });

        panel.getChildren().addAll(titleLabel, cartTable, totalLabel, submitBtn, clearBtn);

        // 保存 totalLabel 引用以便更新
        cartTable.setUserData(totalLabel);

        return panel;
    }

    private HBox createFooter() {
        HBox footer = new HBox(30);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 25, 10, 25));
        footer.setStyle("-fx-background-color: #ecf0f1;");

        balanceLabel = new Label("账户余额：¥" + currentCustomer.getAccountBalance());
        balanceLabel.setFont(Font.font("Microsoft YaHei", 13));

        creditLabel = new Label("信用等级：" + creditLevel.getLevelName() + 
                " (折扣 " + creditLevel.getDiscountRate().multiply(BigDecimal.valueOf(100)).intValue() + "%)");
        creditLabel.setFont(Font.font("Microsoft YaHei", 13));

        // 显示信用等级权限说明
        String privilegeText = getCreditPrivilegeText(creditLevel);
        Label privilegeLabel = new Label(privilegeText);
        privilegeLabel.setFont(Font.font("Microsoft YaHei", 11));
        privilegeLabel.setTextFill(Color.web("#e67e22"));
        privilegeLabel.setWrapText(true);
        privilegeLabel.setMaxWidth(300);

        Button rechargeBtn = new Button("充值");
        rechargeBtn.setOnAction(e -> showRechargeDialog());

        Button addrBtn = new Button("管理地址");
        addrBtn.setOnAction(e -> showAddressManagement());

        Button editInfoBtn = new Button("修改信息");
        editInfoBtn.setOnAction(e -> showEditCustomerInfoDialog());

        footer.getChildren().addAll(balanceLabel, creditLabel, privilegeLabel, rechargeBtn, addrBtn, editInfoBtn);
        return footer;
    }

    private void loadAllBooks() {
        try {
            BookDao bookDao = new BookDao();
            List<Book> books = bookDao.findAll();
            bookTable.setItems(FXCollections.observableArrayList(books));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载书目失败：" + e.getMessage());
        }
    }

    private void searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllBooks();
            return;
        }
        try {
            String kw = keyword.trim();
            BookDao bookDao = new BookDao();

            // 1. 先按书号 / 书名 / 出版社做模糊匹配
            List<Book> base = bookDao.findByConditions(kw, kw, kw);

            // 使用 LinkedHashMap 去重并保留顺序
            Map<String, Book> map = new java.util.LinkedHashMap<>();
            for (Book b : base) {
                map.put(b.getBookId(), b);
            }

            // 2. 按作者名模糊匹配
            AuthorDao authorDao = new AuthorDao();
            Set<String> byAuthor = authorDao.findBookIdsByAuthorNameLike(kw);
            for (String bookId : byAuthor) {
                if (!map.containsKey(bookId)) {
                    Book b = bookDao.findById(bookId);
                    if (b != null) {
                        map.put(bookId, b);
                    }
                }
            }

            // 3. 按关键字文本模糊匹配
            KeywordDao keywordDao = new KeywordDao();
            Set<String> byKeyword = keywordDao.findBookIdsByKeywordTextLike(kw);
            for (String bookId : byKeyword) {
                if (!map.containsKey(bookId)) {
                    Book b = bookDao.findById(bookId);
                    if (b != null) {
                        map.put(bookId, b);
                    }
                }
            }

            bookTable.setItems(FXCollections.observableArrayList(map.values()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "搜索失败：" + e.getMessage());
        }
    }

    /**
     * 选择数量后加入购物车
     */
    private void showAddToCartDialog(Book book) {
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("加入购物车");
        dialog.setHeaderText("加入《" + book.getTitle() + "》到购物车");
        dialog.setContentText("请输入数量：");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            try {
                int qty = Integer.parseInt(text.trim());
                if (qty <= 0) {
                    showAlert(Alert.AlertType.WARNING, "提示", "数量必须是正整数");
                    return;
                }
                cart.merge(book.getBookId(), qty, Integer::sum);
                Label totalLabel = (Label) cartTable.getUserData();
                refreshCartTable(totalLabel);
                showAlert(Alert.AlertType.INFORMATION, "提示", "已将 " + qty + " 本《" + book.getTitle() + "》加入购物车");
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "提示", "请输入正确的整数数量");
            }
        });
    }

    /**
     * 顾客查看书目详细信息（只读对话框）。
     */
    private void showBookDetailDialog(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("书目详情 - " + book.getTitle());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(500);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        int row = 0;
        grid.add(new Label("书号："), 0, row);
        grid.add(new Label(book.getBookId()), 1, row++);

        grid.add(new Label("ISBN："), 0, row);
        grid.add(new Label(book.getIsbn() == null ? "" : book.getIsbn()), 1, row++);

        grid.add(new Label("书名："), 0, row);
        grid.add(new Label(book.getTitle()), 1, row++);

        grid.add(new Label("出版社："), 0, row);
        grid.add(new Label(book.getPublisher() == null ? "" : book.getPublisher()), 1, row++);

        grid.add(new Label("出版日期："), 0, row);
        grid.add(new Label(book.getPublishDate() == null ? "" : book.getPublishDate().toString()), 1, row++);

        grid.add(new Label("版次："), 0, row);
        grid.add(new Label(book.getEdition() == null ? "" : book.getEdition()), 1, row++);

        grid.add(new Label("定价："), 0, row);
        grid.add(new Label(book.getPrice() == null ? "" : ("¥" + book.getPrice().toPlainString())), 1, row++);

        grid.add(new Label("状态："), 0, row);
        grid.add(new Label(book.getStatus() == null ? "" : book.getStatus()), 1, row++);

        // 目录/简介区域（多行文本，仅查看）
        Label catalogLabel = new Label("目录 / 简介：");
        TextArea catalogArea = new TextArea(book.getCatalog() == null ? "" : book.getCatalog());
        catalogArea.setEditable(false);
        catalogArea.setWrapText(true);
        catalogArea.setPrefRowCount(6);

        GridPane.setColumnSpan(catalogLabel, 2);
        GridPane.setColumnSpan(catalogArea, 2);
        grid.add(catalogLabel, 0, row);
        grid.add(catalogArea, 0, row + 1);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    /**
     * 地址管理对话框：查看 / 新增 / 设为默认 / 删除地址。
     */
    private void showAddressManagement() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("收货地址管理");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(700, 450);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TableView<CustomerAddress> table = new TableView<>();
        table.setPrefHeight(320);

        TableColumn<CustomerAddress, String> receiverCol = new TableColumn<>("收件人");
        receiverCol.setCellValueFactory(new PropertyValueFactory<>("receiver"));

        TableColumn<CustomerAddress, String> phoneCol = new TableColumn<>("电话");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<CustomerAddress, String> addrCol = new TableColumn<>("地址");
        addrCol.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getProvince() == null ? "" : c.getValue().getProvince()) +
                        (c.getValue().getCity() == null ? "" : c.getValue().getCity()) +
                        (c.getValue().getDistrict() == null ? "" : c.getValue().getDistrict()) +
                        (c.getValue().getDetail() == null ? "" : c.getValue().getDetail())
        ));
        addrCol.setPrefWidth(350);

        TableColumn<CustomerAddress, String> defaultCol = new TableColumn<>("默认");
        defaultCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDefault() ? "是" : ""));

        table.getColumns().addAll(receiverCol, phoneCol, addrCol, defaultCol);

        // 按钮栏
        HBox btnBar = new HBox(10);
        Button addBtn = new Button("新增地址");
        Button setDefaultBtn = new Button("设为默认");
        Button delBtn = new Button("删除");
        btnBar.getChildren().addAll(addBtn, setDefaultBtn, delBtn);

        // 绑定事件
        addBtn.setOnAction(e -> showAddAddressDialog(table));
        setDefaultBtn.setOnAction(e -> {
            CustomerAddress selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "提示", "请先选择一条地址");
                return;
            }
            try {
                CustomerAddressDao dao = new CustomerAddressDao();
                dao.updateDefault(customerId, selected.getAddressId());
                loadAddresses(table);
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "错误", "设置默认地址失败：" + ex.getMessage());
            }
        });
        delBtn.setOnAction(e -> {
            CustomerAddress selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "提示", "请先选择一条地址");
                return;
            }
            try {
                CustomerAddressDao dao = new CustomerAddressDao();
                dao.delete(selected.getAddressId());
                loadAddresses(table);
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "错误", "删除地址失败：" + ex.getMessage());
            }
        });

        loadAddresses(table);

        root.getChildren().addAll(table, btnBar);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    private void loadAddresses(TableView<CustomerAddress> table) {
        try {
            CustomerAddressDao dao = new CustomerAddressDao();
            List<CustomerAddress> list = dao.findByCustomerId(customerId);
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载地址列表失败：" + e.getMessage());
        }
    }

    private void showAddAddressDialog(TableView<CustomerAddress> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("新增收货地址");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField receiverField = new TextField();
        TextField phoneField = new TextField();
        TextField provinceField = new TextField();
        TextField cityField = new TextField();
        TextField districtField = new TextField();
        TextField detailField = new TextField();
        CheckBox defaultCheck = new CheckBox("设为默认地址");

        grid.add(new Label("收件人:"), 0, 0);
        grid.add(receiverField, 1, 0);
        grid.add(new Label("电话:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("省:"), 0, 2);
        grid.add(provinceField, 1, 2);
        grid.add(new Label("市:"), 0, 3);
        grid.add(cityField, 1, 3);
        grid.add(new Label("区/县:"), 0, 4);
        grid.add(districtField, 1, 4);
        grid.add(new Label("详细地址:"), 0, 5);
        grid.add(detailField, 1, 5);
        grid.add(defaultCheck, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (receiverField.getText().trim().isEmpty() || detailField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "提示", "收件人和详细地址不能为空");
                    return null;
                }
                try {
                    CustomerAddress addr = new CustomerAddress();
                    addr.setCustomerId(customerId);
                    addr.setReceiver(receiverField.getText().trim());
                    addr.setPhone(phoneField.getText().trim());
                    addr.setProvince(provinceField.getText().trim());
                    addr.setCity(cityField.getText().trim());
                    addr.setDistrict(districtField.getText().trim());
                    addr.setDetail(detailField.getText().trim());
                    addr.setDefault(defaultCheck.isSelected());

                    CustomerAddressDao dao = new CustomerAddressDao();
                    dao.insert(addr);
                    loadAddresses(table);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "错误", "新增地址失败：" + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void removeFromCart(String bookId) {
        cart.remove(bookId);
        Label totalLabel = (Label) cartTable.getUserData();
        refreshCartTable(totalLabel);
    }

    private void refreshCartTable(Label totalLabel) {
        try {
            BookDao bookDao = new BookDao();
            List<CartItem> items = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;

            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                Book book = bookDao.findById(entry.getKey());
                if (book != null) {
                    BigDecimal unitPrice = book.getPrice().multiply(creditLevel.getDiscountRate())
                            .setScale(2, RoundingMode.HALF_UP);
                    BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(entry.getValue()));
                    items.add(new CartItem(book.getBookId(), book.getTitle(), entry.getValue(), unitPrice, subtotal));
                    total = total.add(subtotal);
                }
            }

            cartTable.setItems(FXCollections.observableArrayList(items));
            totalLabel.setText("应付金额：¥" + total.setScale(2, RoundingMode.HALF_UP));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void submitOrder(Label totalLabel) {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "提示", "购物车为空，请先添加商品");
            return;
        }

        try {
            // 计算总金额
            BookDao bookDao = new BookDao();
            List<SalesOrderItem> items = new ArrayList<>();
            BigDecimal goodsAmount = BigDecimal.ZERO;

            for (Map.Entry<String, Integer> entry : cart.entrySet()) {
                Book book = bookDao.findById(entry.getKey());
                BigDecimal unitPrice = book.getPrice().multiply(creditLevel.getDiscountRate())
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(entry.getValue()));

                SalesOrderItem item = new SalesOrderItem();
                item.setBookId(book.getBookId());
                item.setQuantity(entry.getValue());
                item.setUnitPrice(unitPrice);
                item.setSubAmount(subtotal);
                item.setItemStatus("ORDERED");
                items.add(item);

                goodsAmount = goodsAmount.add(subtotal);
            }

            // 选择收货地址（如有多条地址则弹窗选择）
            String addressSnapshot = chooseShippingAddressSnapshot();
            if (addressSnapshot == null) {
                // 用户取消了选择
                return;
            }

            // 创建订单
            SalesOrder order = new SalesOrder();
            order.setCustomerId(customerId);
            order.setOrderTime(LocalDateTime.now());
            order.setOrderStatus("PENDING_PAYMENT");
            order.setGoodsAmount(goodsAmount);
            order.setDiscountRateSnapshot(creditLevel.getDiscountRate());
            order.setPayableAmount(goodsAmount);
            order.setShippingAddressSnapshot(addressSnapshot);

            SalesOrderDao salesOrderDao = new SalesOrderDao();
            salesOrderDao.createOrder(order, items);

            // 检查本次订单中哪些书“库存不足”
            InventoryDao invDao = new InventoryDao();
            List<SalesOrderItem> shortageItems = new ArrayList<>();
            for (SalesOrderItem item : items) {
                int currentQty = invDao.getQuantity(item.getBookId());
                if (currentQty < item.getQuantity()) {
                    shortageItems.add(item);
                }
            }

            boolean needPayNow = false;
            String customerNote = null;

            if (!shortageItems.isEmpty()) {
                // 弹出缺书登记对话框
                MissingStockDecision decision = showOutOfStockRequestDialog(order, shortageItems);
                if (decision == null) {
                    // 用户取消，保留订单为待付款状态
                    showAlert(Alert.AlertType.INFORMATION, "订单已创建",
                            "订单已保存，您可在【我的订单】中稍后处理。");
                } else {
                    customerNote = decision.note;
                    if (decision.payAndCreateRecord) {
                        // 方案一：付款并自动生成缺书记录
                        createCustomerRequestsAndOutOfStockRecords(order, shortageItems, customerNote, true);
                        needPayNow = true;
                    } else {
                        // 方案二：暂不付款，仅提交顾客缺书登记，等待管理员决策
                        createCustomerRequestsAndOutOfStockRecords(order, shortageItems, customerNote, false);
                        // 将订单状态标记为“缺货待确认”
                        SalesOrderDao dao = new SalesOrderDao();
                        dao.updateStatusAndPaymentTime(order.getOrderId(), "OUT_OF_STOCK_PENDING", null);
                        order.setOrderStatus("OUT_OF_STOCK_PENDING");
                        showAlert(Alert.AlertType.INFORMATION, "缺书登记已提交",
                                "您的缺书登记已提交，订单状态为【缺货待确认】，管理员会根据情况决定是否生成缺书记录。");
                    }
                }
            } else {
                // 没有缺书项，直接询问是否付款
                needPayNow = true;
            }

            if (needPayNow) {
                // 询问是否立即付款
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("订单已创建");
                confirm.setHeaderText("订单号：" + order.getOrderId());
                confirm.setContentText("应付金额：¥" + goodsAmount + "\n\n是否立即付款？");
                Optional<ButtonType> result = confirm.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.OK) {
                    // 付款
                    OrderService orderService = new OrderService();
                    orderService.payOrder(order.getOrderId());

                    // 刷新客户信息
                    loadCustomerInfo();
                    balanceLabel.setText("账户余额：¥" + currentCustomer.getAccountBalance());

                    showAlert(Alert.AlertType.INFORMATION, "付款成功",
                            "订单已付款，等待发货\n剩余余额：¥" + currentCustomer.getAccountBalance());
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "订单已创建",
                            "订单已保存，请在【我的订单】中完成付款");
                }
            }

            // 清空购物车
            cart.clear();
            refreshCartTable(totalLabel);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "下单失败", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 缺书登记决策结果封装。
     */
    private static class MissingStockDecision {
        boolean payAndCreateRecord;
        String note;
    }

    /**
     * 弹出缺书登记对话框，返回顾客选择的方案及备注；如用户取消返回 null。
     */
    private MissingStockDecision showOutOfStockRequestDialog(SalesOrder order, List<SalesOrderItem> shortageItems) {
        Dialog<MissingStockDecision> dialog = new Dialog<>();
        dialog.setTitle("缺书登记");
        dialog.setHeaderText("订单中部分图书库存不足，请选择处理方式");

        ButtonType payAndCreateBtn = new ButtonType("付款并生成缺书记录", ButtonBar.ButtonData.OK_DONE);
        ButtonType onlyRequestBtn = new ButtonType("仅提交缺书登记(暂不付款)", ButtonBar.ButtonData.OTHER);
        ButtonType cancelBtn = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(payAndCreateBtn, onlyRequestBtn, cancelBtn);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label info = new Label("订单号：" + order.getOrderId() + "，以下图书当前库存不足：");

        TableView<SalesOrderItem> table = new TableView<>();
        table.setPrefHeight(200);

        TableColumn<SalesOrderItem, String> bookIdCol = new TableColumn<>("书号");
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<SalesOrderItem, Integer> qtyCol = new TableColumn<>("订购数量");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<SalesOrderItem, String> stockCol = new TableColumn<>("当前库存");
        stockCol.setCellValueFactory(cell -> {
            try {
                InventoryDao invDao = new InventoryDao();
                int q = invDao.getQuantity(cell.getValue().getBookId());
                return new SimpleStringProperty(String.valueOf(q));
            } catch (Exception e) {
                return new SimpleStringProperty("-");
            }
        });

        table.getColumns().addAll(bookIdCol, qtyCol, stockCol);
        table.setItems(FXCollections.observableArrayList(shortageItems));

        Label noteLabel = new Label("额外请求备注（可选）：");
        TextArea noteArea = new TextArea();
        noteArea.setPromptText("例如：希望本书到货后第一时间通知我...");
        noteArea.setPrefRowCount(3);

        root.getChildren().addAll(info, table, noteLabel, noteArea);
        dialog.getDialogPane().setContent(root);

        dialog.setResultConverter(btn -> {
            if (btn == payAndCreateBtn || btn == onlyRequestBtn) {
                MissingStockDecision d = new MissingStockDecision();
                d.payAndCreateRecord = (btn == payAndCreateBtn);
                d.note = noteArea.getText().trim();
                return d;
            }
            return null;
        });

        Optional<MissingStockDecision> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * 根据顾客缺书登记创建正式缺书记录和/或顾客缺书登记记录。
     *
     * @param order          当前订单
     * @param shortageItems  库存不足的订单明细
     * @param note           顾客备注
     * @param paidAndAuto    true: 已付款并自动生成缺书记录；false: 仅登记，待管理员决定
     */
    private void createCustomerRequestsAndOutOfStockRecords(SalesOrder order,
                                                            List<SalesOrderItem> shortageItems,
                                                            String note,
                                                            boolean paidAndAuto) throws SQLException {
        CustomerOutOfStockRequestDao requestDao = new CustomerOutOfStockRequestDao();
        OutOfStockRecordDao oosDao = new OutOfStockRecordDao();

        for (SalesOrderItem item : shortageItems) {
            String bookId = item.getBookId();
            int requestedQty = item.getQuantity();

            Long relatedRecordId = null;
            if (paidAndAuto) {
                // 已付款：直接生成/累加正式缺书记录
                OutOfStockRecord record = new OutOfStockRecord();
                record.setBookId(bookId);
                // 这里按顾客订购数量登记缺书需求
                record.setRequiredQuantity(requestedQty);
                record.setRecordDate(java.time.LocalDate.now());
                record.setSource("CUSTOMER_REQUEST");
                record.setRelatedCustomerId(order.getCustomerId());
                record.setStatus("PENDING");
                record.setPriority(1);
                long rid = oosDao.insert(record);
                relatedRecordId = rid;
            }

            // 无论是否已经生成正式缺书记录，都记录一条顾客缺书登记，便于后续查询与反馈
            CustomerOutOfStockRequest req = new CustomerOutOfStockRequest();
            req.setOrderId(order.getOrderId());
            req.setCustomerId(order.getCustomerId());
            req.setBookId(bookId);
            req.setRequestedQty(requestedQty);
            req.setCustomerNote(note);
            req.setPaid(paidAndAuto);
            req.setProcessedStatus(paidAndAuto ? "ACCEPTED" : "PENDING");
            req.setRelatedRecordId(relatedRecordId);
            // 直接付款并自动生成缺书记录的场景无需再通知顾客
            if (paidAndAuto) {
                req.setCustomerNotified(true);
                req.setProcessedAt(LocalDateTime.now());
            }
            requestDao.insert(req);
        }
    }

    /**
     * 选择收货地址并返回快照字符串；若用户取消则返回 null。
     */
    private String chooseShippingAddressSnapshot() {
        try {
            CustomerAddressDao addrDao = new CustomerAddressDao();
            List<CustomerAddress> list = addrDao.findByCustomerId(customerId);
            if (list.isEmpty()) {
                // 没有地址时，退回到简单快照（姓名 + 手机），提示用户可以去“管理地址”维护
                showAlert(Alert.AlertType.INFORMATION, "提示",
                        "您还没有维护收货地址，将使用账户姓名和手机作为临时地址。\n" +
                                "可在界面底部点击【管理地址】新增地址。");
                return currentCustomer.getRealName() + ", " + currentCustomer.getMobilePhone();
            }

            // 若只有一条地址，直接使用
            if (list.size() == 1) {
                return list.get(0).toDisplayString();
            }

            // 多条地址时弹出选择对话框
            List<String> choices = new ArrayList<>();
            for (CustomerAddress addr : list) {
                choices.add(addr.toDisplayString());
            }
            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("选择收货地址");
            dialog.setHeaderText("请选择本次订单使用的收货地址");
            dialog.setContentText("地址：");

            Optional<String> result = dialog.showAndWait();
            return result.orElse(null);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载收货地址失败：" + e.getMessage());
            return null;
        }
    }

    private void showMyOrders() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("我的订单");
        dialog.setHeaderText("订单列表");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(700, 500);

        VBox rootBox = new VBox(10);
        rootBox.setPadding(new Insets(10));

        // 筛选栏：按订单状态筛选
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("全部", "PENDING_PAYMENT", "OUT_OF_STOCK_PENDING", "PENDING_SHIPMENT", "DELIVERING", "SHIPPED", "COMPLETED", "CANCELLED");
        statusCombo.setValue("全部");
        Button filterBtn = new Button("筛选");
        filterBox.getChildren().addAll(new Label("订单状态："), statusCombo, filterBtn);

        TableView<SalesOrder> orderTable = new TableView<>();

        TableColumn<SalesOrder, Long> idCol = new TableColumn<>("订单号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<SalesOrder, String> timeCol = new TableColumn<>("下单时间");
        timeCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrderTime().toString()));
        timeCol.setPrefWidth(150);

        TableColumn<SalesOrder, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

        TableColumn<SalesOrder, BigDecimal> amountCol = new TableColumn<>("金额");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("payableAmount"));

        TableColumn<SalesOrder, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(260);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button detailBtn = new Button("详情");
            private final Button payBtn = new Button("付款");
            private final Button receiveBtn = new Button("已收货");
            private final HBox box = new HBox(10, detailBtn, payBtn, receiveBtn);
            {
                detailBtn.setOnAction(e -> {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    showOrderDetail(order);
                });
                payBtn.setOnAction(e -> {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    if ("PENDING_PAYMENT".equals(order.getOrderStatus())
                            || "OUT_OF_STOCK_PENDING".equals(order.getOrderStatus())) {
                        try {
                            OrderService orderService = new OrderService();
                            orderService.payOrder(order.getOrderId());
                            loadCustomerInfo();
                            balanceLabel.setText("账户余额：¥" + currentCustomer.getAccountBalance());
                            showAlert(Alert.AlertType.INFORMATION, "付款成功", "订单已付款");
                            loadOrders(orderTable, statusCombo.getValue());
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "付款失败", ex.getMessage());
                        }
                    }
                });
                receiveBtn.setOnAction(e -> {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    showReceiveDialog(order, orderTable, statusCombo.getValue());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    payBtn.setDisable(!("PENDING_PAYMENT".equals(order.getOrderStatus())
                            || "OUT_OF_STOCK_PENDING".equals(order.getOrderStatus())));
                    // 发货完成或运输中均可确认收货
                    boolean canReceive = "DELIVERING".equals(order.getOrderStatus()) || "SHIPPED".equals(order.getOrderStatus());
                    receiveBtn.setDisable(!canReceive);
                    setGraphic(box);
                }
            }
        });

        orderTable.getColumns().addAll(idCol, timeCol, statusCol, amountCol, actionCol);

        // 加载订单前，先检查是否有缺书登记处理结果需要通知顾客
        notifyOutOfStockDecisions();
        // 首次加载全部订单
        loadOrders(orderTable, "全部");

        // 按状态筛选
        filterBtn.setOnAction(e -> loadOrders(orderTable, statusCombo.getValue()));

        rootBox.getChildren().addAll(filterBox, orderTable);
        dialog.getDialogPane().setContent(rootBox);
        dialog.showAndWait();
    }

    /**
     * 检查并通知当前顾客其缺书登记的处理结果（通过/未通过），每条只通知一次。
     */
    private void notifyOutOfStockDecisions() {
        try {
            CustomerOutOfStockRequestDao dao = new CustomerOutOfStockRequestDao();
            List<CustomerOutOfStockRequest> list = dao.findUnnotifiedByCustomerId(customerId);
            if (list.isEmpty()) {
                return;
            }
            StringBuilder msg = new StringBuilder();
            List<Long> ids = new ArrayList<>();
            // 按订单去重，一张订单只提示一行
            java.util.Set<Long> seenOrders = new java.util.HashSet<>();
            CustomerNotificationDao nDao = new CustomerNotificationDao();
            for (CustomerOutOfStockRequest req : list) {
                ids.add(req.getRequestId());
                Long oid = req.getOrderId();

                String text = null;
                if ("ACCEPTED".equals(req.getProcessedStatus())) {
                    text = "您的订单（" + oid + "）的缺货登记已通过，请抓紧付款。";
                } else if ("REJECTED".equals(req.getProcessedStatus())) {
                    text = "您的订单（" + oid + "）的缺货登记未通过，订单已取消。";
                }

                // 写入通用通知表
                if (text != null) {
                    try {
                        CustomerNotification n = new CustomerNotification();
                        n.setCustomerId(customerId);
                        n.setOrderId(oid);
                        n.setType("OUT_OF_STOCK");
                        n.setTitle("缺书登记处理结果");
                        n.setContent(text);
                        n.setReadFlag(false);
                        nDao.insert(n);
                    } catch (Exception ignore) {
                        // 忽略单条写入失败
                    }
                }

                // 组装一次性弹窗提示（按订单去重）
                if (text != null && !seenOrders.contains(oid)) {
                    seenOrders.add(oid);
                    msg.append(text).append("\n");
                }
            }
            if (msg.length() > 0) {
                showAlert(Alert.AlertType.INFORMATION, "缺货登记处理结果", msg.toString());
            }
            dao.markNotified(ids);
        } catch (Exception e) {
            // 通知失败不影响主流程
            e.printStackTrace();
        }
    }

    /**
     * 打开缺书登记通知列表，对所有历史“已处理”登记逐条展示。
     */
    private void showOutOfStockNotificationList() {
        try {
            CustomerNotificationDao dao = new CustomerNotificationDao();
            List<CustomerNotification> list = dao.findByCustomerId(customerId);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("消息通知");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setPrefSize(700, 420);

            TableView<CustomerNotification> table = new TableView<>();
            table.setPrefHeight(360);

            TableColumn<CustomerNotification, String> typeCol = new TableColumn<>("类型");
            typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));

            TableColumn<CustomerNotification, Long> orderCol = new TableColumn<>("订单号");
            orderCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

            TableColumn<CustomerNotification, String> titleCol = new TableColumn<>("标题");
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            titleCol.setPrefWidth(160);

            TableColumn<CustomerNotification, String> contentCol = new TableColumn<>("内容");
            contentCol.setCellValueFactory(new PropertyValueFactory<>("content"));
            contentCol.setPrefWidth(320);

            TableColumn<CustomerNotification, String> timeCol = new TableColumn<>("时间");
            timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getCreatedTime() != null ? c.getValue().getCreatedTime().toString() : ""));

            table.getColumns().addAll(typeCol, orderCol, titleCol, contentCol, timeCol);
            table.setItems(FXCollections.observableArrayList(list));

            dialog.getDialogPane().setContent(table);
            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载通知列表失败：" + e.getMessage());
        }
    }

    /**
     * 显示单个订单的明细及发货信息
     */
    private void showOrderDetail(SalesOrder order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("订单详情");
        dialog.setHeaderText("订单号：" + order.getOrderId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(750, 520);

        VBox rootBox = new VBox(10);
        rootBox.setPadding(new Insets(10));

        // 顶部基本信息
        Label baseInfo = new Label(
                "状态：" + order.getOrderStatus() +
                "，下单时间：" + (order.getOrderTime() != null ? order.getOrderTime() : "") +
                "，应付金额：¥" + order.getPayableAmount()
        );

        // 明细表
        TableView<SalesOrderItem> itemTable = new TableView<>();
        itemTable.setPrefHeight(250);

        TableColumn<SalesOrderItem, String> bookIdCol = new TableColumn<>("书号");
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<SalesOrderItem, Integer> qtyCol = new TableColumn<>("数量");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<SalesOrderItem, BigDecimal> unitPriceCol = new TableColumn<>("成交单价");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<SalesOrderItem, BigDecimal> subCol = new TableColumn<>("小计");
        subCol.setCellValueFactory(new PropertyValueFactory<>("subAmount"));

        itemTable.getColumns().addAll(bookIdCol, qtyCol, unitPriceCol, subCol);

        List<SalesOrderItem> items = null;
        try {
            SalesOrderDao orderDao = new SalesOrderDao();
            items = orderDao.findItemsByOrderId(order.getOrderId());
            itemTable.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载订单明细失败：" + e.getMessage());
        }

        // 分次发货通知提示：仅在确实发生“部分发货”时展示
        Label partialShipInfo = new Label();
        boolean hasPartial = false;
        if (items != null && !items.isEmpty()) {
            for (SalesOrderItem it : items) {
                int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                int qty = it.getQuantity() != null ? it.getQuantity() : 0;
                if (shipped > 0 && shipped < qty) {
                    hasPartial = true;
                    break;
                }
            }
            if (hasPartial) {
                StringBuilder shippedMsg = new StringBuilder();
                StringBuilder unshippedMsg = new StringBuilder();
                for (SalesOrderItem it : items) {
                    int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                    int qty = it.getQuantity() != null ? it.getQuantity() : 0;
                    if (shipped > 0) {
                        shippedMsg.append(it.getBookId())
                                .append(" 已发 ").append(shipped).append(" 本；");
                    }
                    if (shipped < qty) {
                        unshippedMsg.append(it.getBookId())
                                .append(" 未发 ").append(qty - shipped).append(" 本；");
                    }
                }
                StringBuilder info = new StringBuilder();
                if (shippedMsg.length() > 0) {
                    info.append("已发货：").append(shippedMsg);
                }
                if (unshippedMsg.length() > 0) {
                    if (info.length() > 0) info.append("  ");
                    info.append("未发货：").append(unshippedMsg);
                }
                if (info.length() > 0) {
                    partialShipInfo.setText("【分次发货提示】" + info);
                    partialShipInfo.setTextFill(Color.web("#e67e22"));
                }
            }
        }

        // 发货信息（可能为空或多条发货记录）
        Label shipLabel = new Label("发货信息：");
        TableView<Shipment> shipTable = new TableView<>();
        shipTable.setPrefHeight(150);

        TableColumn<Shipment, Long> shipIdCol = new TableColumn<>("发货单号");
        shipIdCol.setCellValueFactory(new PropertyValueFactory<>("shipmentId"));

        TableColumn<Shipment, String> carrierCol = new TableColumn<>("快递公司");
        carrierCol.setCellValueFactory(new PropertyValueFactory<>("carrier"));

        TableColumn<Shipment, String> trackCol = new TableColumn<>("快递单号");
        trackCol.setCellValueFactory(new PropertyValueFactory<>("trackingNumber"));

        TableColumn<Shipment, String> shipTimeCol = new TableColumn<>("发货时间");
        shipTimeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getShipTime() != null ? c.getValue().getShipTime().toString() : ""));

        shipTable.getColumns().addAll(shipIdCol, carrierCol, trackCol, shipTimeCol);

        try {
            ShipmentDao shipmentDao = new ShipmentDao();
            List<Shipment> shipments = shipmentDao.findByOrderId(order.getOrderId());
            shipTable.setItems(FXCollections.observableArrayList(shipments));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载发货信息失败：" + e.getMessage());
        }

        if (hasPartial && partialShipInfo.getText() != null && !partialShipInfo.getText().isEmpty()) {
            rootBox.getChildren().addAll(baseInfo, partialShipInfo, new Label("订单明细："), itemTable, shipLabel, shipTable);
        } else {
            rootBox.getChildren().addAll(baseInfo, new Label("订单明细："), itemTable, shipLabel, shipTable);
        }
        dialog.getDialogPane().setContent(rootBox);
        dialog.showAndWait();
    }

    /**
     * 顾客确认收货（可分次确认已发货数量）。
     */
    private void showReceiveDialog(SalesOrder order, TableView<SalesOrder> table, String statusFilter) {
        try {
            SalesOrderDao orderDao = new SalesOrderDao();
            List<SalesOrderItem> items = orderDao.findItemsByOrderId(order.getOrderId());
            List<SalesOrderItem> receivable = new ArrayList<>();
            ShipmentDao shipmentDao = new ShipmentDao();
            for (SalesOrderItem it : items) {
                int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                int received = it.getReceivedQuantity() == null ? 0 : it.getReceivedQuantity();
                // 兜底：若历史数据 shipped_quantity 仍为 0，则以发货明细汇总代替
                if (shipped == 0) {
                    shipped = shipmentDao.sumShippedQuantityByOrderItem(it.getOrderItemId());
                }
                if (shipped > received) {
                    it.setShippedQuantity(shipped);
                    receivable.add(it);
                }
            }
            if (receivable.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "提示", "当前无可确认收货的图书。");
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("确认收货 - 订单 " + order.getOrderId());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            VBox root = new VBox(10);
            root.setPadding(new Insets(12));
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(6);
            grid.add(new Label("书号"), 0, 0);
            grid.add(new Label("本次收货"), 1, 0);
            grid.add(new Label("已发/已收"), 2, 0);
            List<TextField> qtyFields = new ArrayList<>();
            for (int i = 0; i < receivable.size(); i++) {
                SalesOrderItem it = receivable.get(i);
                int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                int received = it.getReceivedQuantity() == null ? 0 : it.getReceivedQuantity();
                int remain = shipped - received;
                Label bookLabel = new Label(it.getBookId());
                TextField qtyField = new TextField(String.valueOf(remain));
                Label statusLabel = new Label(shipped + " / " + received);
                grid.add(bookLabel, 0, i + 1);
                grid.add(qtyField, 1, i + 1);
                grid.add(statusLabel, 2, i + 1);
                qtyFields.add(qtyField);
            }
            root.getChildren().addAll(new Label("仅能确认已发出的数量，未发货部分不可收货。"), grid);
            dialog.getDialogPane().setContent(root);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        Map<Long, Integer> receiveMap = new HashMap<>();
                        for (int i = 0; i < receivable.size(); i++) {
                            SalesOrderItem it = receivable.get(i);
                            int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                            int received = it.getReceivedQuantity() == null ? 0 : it.getReceivedQuantity();
                            int remain = shipped - received;
                            String txt = qtyFields.get(i).getText().trim();
                            if (txt.isEmpty()) continue;
                            int qty = Integer.parseInt(txt);
                            // 允许输入 0：表示本次不确认这本书；仅校验 0 <= qty <= remain
                            if (qty < 0 || qty > remain) {
                                showAlert(Alert.AlertType.WARNING, "提示",
                                        "书号 " + it.getBookId() + " 的收货数量需在 0~" + remain + " 之间");
                                return null;
                            }
                            if (qty == 0) continue;
                            receiveMap.put(it.getOrderItemId(), qty);
                        }
                        if (receiveMap.isEmpty()) {
                            showAlert(Alert.AlertType.WARNING, "提示", "请输入收货数量");
                            return null;
                        }
                        ShipmentService shipmentService = new ShipmentService();
                        shipmentService.confirmReceiptByItems(order.getOrderId(), receiveMap);
                        showAlert(Alert.AlertType.INFORMATION, "成功", "收货确认成功");
                        loadOrders(table, statusFilter);
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.WARNING, "提示", "收货数量必须是整数");
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载订单数据失败：" + e.getMessage());
        }
    }

    private void loadOrders(TableView<SalesOrder> table, String statusFilter) {
        try {
            SalesOrderDao salesOrderDao = new SalesOrderDao();
            List<SalesOrder> orders = salesOrderDao.findByCustomerId(customerId);
            if (statusFilter != null && !"全部".equals(statusFilter)) {
                orders = orders.stream()
                        .filter(o -> statusFilter.equals(o.getOrderStatus()))
                        .toList();
            }
            table.setItems(FXCollections.observableArrayList(orders));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载订单失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取信用等级权限说明文本
     */
    private String getCreditPrivilegeText(CreditLevel level) {
        int levelId = level.getLevelId();
        StringBuilder sb = new StringBuilder();
        sb.append("权限：");
        if (levelId <= 2) {
            sb.append("不能透支，必须先付款后发货");
        } else if (levelId <= 4) {
            sb.append("可透支");
            if (level.getOverdraftLimit() != null && level.getOverdraftLimit().compareTo(BigDecimal.valueOf(-1)) != 0) {
                sb.append("（额度：¥").append(level.getOverdraftLimit()).append("）");
            }
            sb.append("，可先发货后付款");
        } else {
            sb.append("可无限透支，可先发货后付款");
        }
        return sb.toString();
    }

    /**
     * 显示充值对话框（顾客主动充值）
     */
    private void showRechargeDialog() {
        Dialog<BigDecimal> dialog = new Dialog<>();
        dialog.setTitle("账户充值");
        dialog.setHeaderText("请选择充值金额");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        // 预充项按钮
        Label presetLabel = new Label("快速充值：");
        HBox presetBox = new HBox(10);
        Button btn50 = new Button("¥50");
        Button btn100 = new Button("¥100");
        Button btn200 = new Button("¥200");
        Button btn500 = new Button("¥500");
        Button btn1000 = new Button("¥1000");
        presetBox.getChildren().addAll(btn50, btn100, btn200, btn500, btn1000);

        // 自定义金额
        Label customLabel = new Label("自定义金额：");
        TextField customField = new TextField();
        customField.setPromptText("请输入充值金额");

        content.getChildren().addAll(presetLabel, presetBox, customLabel, customField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 预充项按钮事件
        btn50.setOnAction(e -> customField.setText("50"));
        btn100.setOnAction(e -> customField.setText("100"));
        btn200.setOnAction(e -> customField.setText("200"));
        btn500.setOnAction(e -> customField.setText("500"));
        btn1000.setOnAction(e -> customField.setText("1000"));

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String amountStr = customField.getText().trim();
                    if (amountStr.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "提示", "请输入充值金额");
                        return null;
                    }
                    BigDecimal amount = new BigDecimal(amountStr);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        showAlert(Alert.AlertType.WARNING, "提示", "充值金额必须大于0");
                        return null;
                    }
                    return amount;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "错误", "请输入有效的金额");
                    return null;
                }
            }
            return null;
        });

        Optional<BigDecimal> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            // 模拟付款流程（实际项目中这里应该调用支付接口）
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认充值");
            confirm.setHeaderText("充值金额：¥" + amount);
            confirm.setContentText("确认支付此金额？");
            Optional<ButtonType> payResult = confirm.showAndWait();
            if (payResult.isPresent() && payResult.get() == ButtonType.OK) {
                try {
                    BigDecimal newBalance = currentCustomer.getAccountBalance().add(amount);
                    CustomerDao dao = new CustomerDao();
                    dao.updateAccountBalance(customerId, newBalance);
                    currentCustomer.setAccountBalance(newBalance);
                    balanceLabel.setText("账户余额：¥" + newBalance);
                    showAlert(Alert.AlertType.INFORMATION, "充值成功", 
                            "充值金额：¥" + amount + "\n当前余额：¥" + newBalance);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "错误", "充值失败：" + e.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public BorderPane getView() {
        return root;
    }

    // 购物车项内部类
    public static class CartItem {
        private String bookId;
        private String bookTitle;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public CartItem(String bookId, String bookTitle, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.subtotal = subtotal;
        }

        public String getBookId() { return bookId; }
        public String getBookTitle() { return bookTitle; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    /**
     * 显示修改客户信息的对话框。
     */
    private void showEditCustomerInfoDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("修改个人信息");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefSize(450, 300);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // 用户名（只读）
        Label usernameLabel = new Label("用户名：");
        TextField usernameField = new TextField(currentCustomer.getUsername());
        usernameField.setEditable(false);
        usernameField.setStyle("-fx-background-color: #e0e0e0;");
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        // 真实姓名
        Label realNameLabel = new Label("真实姓名：");
        TextField realNameField = new TextField(currentCustomer.getRealName() != null ? currentCustomer.getRealName() : "");
        grid.add(realNameLabel, 0, 1);
        grid.add(realNameField, 1, 1);

        // 手机号
        Label mobileLabel = new Label("手机号：");
        TextField mobileField = new TextField(currentCustomer.getMobilePhone() != null ? currentCustomer.getMobilePhone() : "");
        grid.add(mobileLabel, 0, 2);
        grid.add(mobileField, 1, 2);

        // 邮箱
        Label emailLabel = new Label("邮箱：");
        TextField emailField = new TextField(currentCustomer.getEmail() != null ? currentCustomer.getEmail() : "");
        grid.add(emailLabel, 0, 3);
        grid.add(emailField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // 设置按钮操作
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String realName = realNameField.getText().trim();
            String mobilePhone = mobileField.getText().trim();
            String email = emailField.getText().trim();

            // 基本验证
            if (realName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "提示", "真实姓名不能为空");
                e.consume(); // 阻止对话框关闭
                return;
            }

            // 邮箱格式验证（简单验证）
            if (!email.isEmpty() && !email.contains("@")) {
                showAlert(Alert.AlertType.WARNING, "提示", "邮箱格式不正确");
                e.consume(); // 阻止对话框关闭
                return;
            }

            try {
                CustomerDao dao = new CustomerDao();
                int result = dao.updateCustomerInfo(customerId, realName, mobilePhone.isEmpty() ? null : mobilePhone, email.isEmpty() ? null : email);
                if (result > 0) {
                    // 重新加载客户信息
                    loadCustomerInfo();
                    // 更新客户名称
                    customerName = realName;
                    showAlert(Alert.AlertType.INFORMATION, "成功", "个人信息已更新");
                } else {
                    showAlert(Alert.AlertType.ERROR, "错误", "更新失败");
                    e.consume(); // 阻止对话框关闭
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "错误", "更新失败：" + ex.getMessage());
                e.consume(); // 阻止对话框关闭
            }
        });

        dialog.showAndWait();
    }
}

