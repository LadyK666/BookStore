package com.bookstore.ui;

import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.service.PurchaseService;
import com.bookstore.service.ShipmentService;
import com.bookstore.model.ShipmentItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.util.converter.IntegerStringConverter;

/**
 * 管理员界面
 */
public class AdminView {

    private BorderPane root;
    private String adminName;
    private VBox contentArea;

    public AdminView(String adminName) {
        this.adminName = adminName;
        createView();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");

        // 顶部
        root.setTop(createHeader());

        // 左侧菜单
        root.setLeft(createMenu());

        // 中间内容区
        contentArea = new VBox();
        contentArea.setPadding(new Insets(20));
        root.setCenter(contentArea);

        // 默认显示订单管理
        showOrderManagement();
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: #2c3e50;");

        Label titleLabel = new Label("书店管理后台");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminLabel = new Label("管理员: " + adminName);
        adminLabel.setFont(Font.font("Microsoft YaHei", 14));
        adminLabel.setTextFill(Color.WHITE);

        Button logoutBtn = new Button("退出");
        logoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white;");
        logoutBtn.setOnAction(e -> MainApp.showLoginView());

        header.getChildren().addAll(titleLabel, spacer, adminLabel, logoutBtn);
        return header;
    }

    private VBox createMenu() {
        VBox menu = new VBox(5);
        menu.setPrefWidth(180);
        menu.setPadding(new Insets(10));
        menu.setStyle("-fx-background-color: #34495e;");

        String btnStyle = "-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: CENTER-LEFT; -fx-font-size: 13px;";

        Button orderBtn = createMenuButton("📦 订单管理", btnStyle);
        orderBtn.setOnAction(e -> showOrderManagement());

        Button shipBtn = createMenuButton("🚚 发货管理", btnStyle);
        shipBtn.setOnAction(e -> showShipmentManagement());

        Button inventoryBtn = createMenuButton("📚 库存管理", btnStyle);
        inventoryBtn.setOnAction(e -> showInventoryManagement());

        Button purchaseBtn = createMenuButton("🛒 采购管理", btnStyle);
        purchaseBtn.setOnAction(e -> showPurchaseManagement());

        Button customerBtn = createMenuButton("👤 客户管理", btnStyle);
        customerBtn.setOnAction(e -> showCustomerManagement());

        Button supplierBtn = createMenuButton("🏭 供应商管理", btnStyle);
        supplierBtn.setOnAction(e -> showSupplierManagement());

        Button bookBtn = createMenuButton("📖 书目管理", btnStyle);
        bookBtn.setOnAction(e -> showBookManagement());

        menu.getChildren().addAll(orderBtn, shipBtn, inventoryBtn, purchaseBtn, customerBtn, supplierBtn, bookBtn);
        return menu;
    }

    private Button createMenuButton(String text, String style) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setPrefHeight(40);
        btn.setStyle(style);
        return btn;
    }

    // ========== 订单管理 ==========
    private void showOrderManagement() {
        contentArea.getChildren().clear();

        Label title = new Label("订单管理");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        // 筛选栏
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("全部", "PENDING_PAYMENT", "OUT_OF_STOCK_PENDING", "PENDING_SHIPMENT", "DELIVERING", "SHIPPED", "COMPLETED");
        statusCombo.setValue("全部");
        Button filterBtn = new Button("筛选");
        filterBox.getChildren().addAll(new Label("订单状态:"), statusCombo, filterBtn);

        TableView<SalesOrder> table = new TableView<>();
        setupOrderTable(table);

        filterBtn.setOnAction(e -> loadOrders(table, statusCombo.getValue()));
        loadOrders(table, "全部");

        contentArea.getChildren().addAll(title, filterBox, table);
    }

    private void setupOrderTable(TableView<SalesOrder> table) {
        table.setPrefHeight(500);

        TableColumn<SalesOrder, Long> idCol = new TableColumn<>("订单号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        idCol.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    if (order != null) {
                        showOrderDetailForAdmin(order);
                    }
                });
            }
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    link.setText(String.valueOf(item));
                    setGraphic(link);
                }
            }
        });

        TableColumn<SalesOrder, Long> customerCol = new TableColumn<>("客户ID");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerCol.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    if (order != null) {
                        showCustomerDetail(order.getCustomerId());
                    }
                });
            }
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    link.setText(String.valueOf(item));
                    setGraphic(link);
                }
            }
        });

        TableColumn<SalesOrder, String> timeCol = new TableColumn<>("下单时间");
        timeCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOrderTime() != null ? c.getValue().getOrderTime().toString() : ""));
        timeCol.setPrefWidth(150);

        TableColumn<SalesOrder, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));

        TableColumn<SalesOrder, BigDecimal> amountCol = new TableColumn<>("金额");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("payableAmount"));

        table.getColumns().addAll(idCol, customerCol, timeCol, statusCol, amountCol);
    }

    private void loadOrders(TableView<SalesOrder> table, String statusFilter) {
        try {
            SalesOrderDao dao = new SalesOrderDao();
            List<SalesOrder> orders;
            if ("全部".equals(statusFilter)) {
                orders = dao.findAll();
            } else {
                orders = dao.findByStatus(statusFilter);
            }
            table.setItems(FXCollections.observableArrayList(orders));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }
    }

    // ========== 发货管理 ==========
    private void showShipmentManagement() {
        contentArea.getChildren().clear();

        Label title = new Label("发货管理 - 待发货订单");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        TableView<SalesOrder> table = new TableView<>();
        setupOrderTable(table);

        // 添加发货按钮列
        TableColumn<SalesOrder, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button shipBtn = new Button("发货");
            private final Button partialBtn = new Button("分次发货");
            private final HBox box = new HBox(8, shipBtn, partialBtn);
            {
                shipBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                partialBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
                shipBtn.setOnAction(e -> {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    showShipDialog(order, table);
                });
                partialBtn.setOnAction(e -> {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    showPartialShipDialog(order, table);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SalesOrder order = getTableView().getItems().get(getIndex());
                    boolean canShip = "PENDING_SHIPMENT".equals(order.getOrderStatus()) || "DELIVERING".equals(order.getOrderStatus());
                    if (!canShip && "PENDING_PAYMENT".equals(order.getOrderStatus())) {
                        try {
                            CustomerDao customerDao = new CustomerDao();
                            CreditLevelDao creditLevelDao = new CreditLevelDao();
                            Customer customer = customerDao.findById(order.getCustomerId());
                            if (customer != null) {
                                CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
                                if (level != null && level.isAllowOverdraft()) {
                                    canShip = true; // 三级及以上可先发货
                                }
                            }
                        } catch (SQLException e) {
                            // 忽略错误，不允许发货
                        }
                    }
                    shipBtn.setDisable(!canShip || "DELIVERING".equals(order.getOrderStatus())); // 整单发货仅在未发过时使用
                    partialBtn.setDisable(!canShip);
                    setGraphic(box);
                }
            }
        });
        table.getColumns().add(actionCol);

        try {
            SalesOrderDao dao = new SalesOrderDao();
            // 显示待发货订单和待付款订单（三级及以上可先发货）
            List<SalesOrder> paidOrders = dao.findByStatus("PENDING_SHIPMENT");
            List<SalesOrder> deliveringOrders = dao.findByStatus("DELIVERING");
            List<SalesOrder> unpaidOrders = dao.findByStatus("PENDING_PAYMENT");
            List<SalesOrder> allOrders = new ArrayList<>(paidOrders);
            allOrders.addAll(deliveringOrders);
            
            // 筛选出三级及以上信用等级的待付款订单
            CustomerDao customerDao = new CustomerDao();
            CreditLevelDao creditLevelDao = new CreditLevelDao();
            for (SalesOrder order : unpaidOrders) {
                try {
                    Customer customer = customerDao.findById(order.getCustomerId());
                    if (customer != null) {
                        CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
                        if (level != null && level.isAllowOverdraft()) {
                            allOrders.add(order);
                        }
                    }
                } catch (SQLException e) {
                    // 忽略错误
                }
            }
            
            table.setItems(FXCollections.observableArrayList(allOrders));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }

        contentArea.getChildren().addAll(title, table);
    }

    /**
     * 管理员查看订单详情（只读，与顾客端类似）。
     */
    private void showOrderDetailForAdmin(SalesOrder order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("订单详情（管理员）");
        dialog.setHeaderText("订单号：" + order.getOrderId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(750, 520);

        VBox rootBox = new VBox(10);
        rootBox.setPadding(new Insets(10));

        Label baseInfo = new Label(
                "客户ID：" + order.getCustomerId() +
                "，状态：" + order.getOrderStatus() +
                "，下单时间：" + (order.getOrderTime() != null ? order.getOrderTime() : "") +
                "，应付金额：¥" + order.getPayableAmount()
        );

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

        try {
            SalesOrderDao orderDao = new SalesOrderDao();
            List<SalesOrderItem> items = orderDao.findItemsByOrderId(order.getOrderId());
            itemTable.setItems(FXCollections.observableArrayList(items));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载订单明细失败：" + e.getMessage());
        }

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

        rootBox.getChildren().addAll(baseInfo, new Label("订单明细："), itemTable, shipLabel, shipTable);
        dialog.getDialogPane().setContent(rootBox);
        dialog.showAndWait();
    }

    /**
     * 管理员查看客户详细信息（只读）。
     */
    private void showCustomerDetail(long customerId) {
        try {
            CustomerDao dao = new CustomerDao();
            Customer customer = dao.findById(customerId);
            if (customer == null) {
                showAlert(Alert.AlertType.WARNING, "提示", "未找到该客户信息，ID=" + customerId);
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("客户详情");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setPrefSize(450, 380);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(15));

            int row = 0;
            grid.add(new Label("客户ID："), 0, row);
            grid.add(new Label(String.valueOf(customer.getCustomerId())), 1, row++);

            grid.add(new Label("用户名："), 0, row);
            grid.add(new Label(customer.getUsername()), 1, row++);

            grid.add(new Label("真实姓名："), 0, row);
            grid.add(new Label(customer.getRealName() != null ? customer.getRealName() : ""), 1, row++);

            grid.add(new Label("手机："), 0, row);
            grid.add(new Label(customer.getMobilePhone() != null ? customer.getMobilePhone() : ""), 1, row++);

            grid.add(new Label("邮箱："), 0, row);
            grid.add(new Label(customer.getEmail() != null ? customer.getEmail() : ""), 1, row++);

            grid.add(new Label("账户余额："), 0, row);
            grid.add(new Label(customer.getAccountBalance() != null ? customer.getAccountBalance().toPlainString() : "0"), 1, row++);

            grid.add(new Label("累计消费："), 0, row);
            grid.add(new Label(customer.getTotalConsumption() != null ? customer.getTotalConsumption().toPlainString() : "0"), 1, row++);

            grid.add(new Label("信用等级ID："), 0, row);
            grid.add(new Label(customer.getCreditLevelId() != null ? String.valueOf(customer.getCreditLevelId()) : ""), 1, row++);

            grid.add(new Label("账户状态："), 0, row);
            grid.add(new Label(customer.getAccountStatus() != null ? customer.getAccountStatus() : ""), 1, row++);

            dialog.getDialogPane().setContent(grid);
            dialog.showAndWait();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载客户信息失败：" + e.getMessage());
        }
    }

    private void showShipDialog(SalesOrder order, TableView<SalesOrder> table) {
        // 检查订单状态和信用等级
        boolean isPaid = "PENDING_SHIPMENT".equals(order.getOrderStatus());
        boolean canShipWithoutPayment = false;
        
        if (!isPaid && "PENDING_PAYMENT".equals(order.getOrderStatus())) {
            try {
                CustomerDao customerDao = new CustomerDao();
                CreditLevelDao creditLevelDao = new CreditLevelDao();
                Customer customer = customerDao.findById(order.getCustomerId());
                if (customer != null) {
                    CreditLevel level = creditLevelDao.findById(customer.getCreditLevelId());
                    if (level != null && level.isAllowOverdraft()) {
                        canShipWithoutPayment = true;
                    }
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "错误", "查询客户信息失败：" + e.getMessage());
                return;
            }
        }
        
        if (!isPaid && !canShipWithoutPayment) {
            showAlert(Alert.AlertType.WARNING, "提示",
                    "当前订单状态为【" + order.getOrderStatus() + "】，且客户信用等级不允许先发货后付款，不可发货。");
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("发货");
        dialog.setHeaderText("订单号: " + order.getOrderId());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField carrierField = new TextField("顺丰速运");
        TextField trackingField = new TextField("SF" + System.currentTimeMillis());
        TextField operatorField = new TextField(adminName);

        grid.add(new Label("快递公司:"), 0, 0);
        grid.add(carrierField, 1, 0);
        grid.add(new Label("快递单号:"), 0, 1);
        grid.add(trackingField, 1, 1);
        grid.add(new Label("操作员:"), 0, 2);
        grid.add(operatorField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    // 发货前再次校验库存是否充足，若不足则提示管理员先处理缺书记录
                    SalesOrderDao orderDao = new SalesOrderDao();
                    List<SalesOrderItem> items = orderDao.findItemsByOrderId(order.getOrderId());
                    InventoryDao invDao = new InventoryDao();
                    StringBuilder shortageMsg = new StringBuilder();
                    for (SalesOrderItem item : items) {
                        int currentQty = invDao.getQuantity(item.getBookId());
                        if (currentQty < item.getQuantity()) {
                            if (shortageMsg.length() == 0) {
                                shortageMsg.append("以下图书库存不足，无法完成本次发货，请先处理对应的缺书记录：\n");
                            }
                            shortageMsg.append("书号 ").append(item.getBookId())
                                    .append("，需发货 ").append(item.getQuantity())
                                    .append(" 本，当前库存 ").append(currentQty).append(" 本\n");
                        }
                    }
                    if (shortageMsg.length() > 0) {
                        showAlert(Alert.AlertType.WARNING, "库存不足", shortageMsg.toString());
                        return null;
                    }

                    ShipmentService service = new ShipmentService();
                    service.shipOrder(order.getOrderId(), carrierField.getText(),
                            trackingField.getText(), operatorField.getText());
                    showAlert(Alert.AlertType.INFORMATION, "成功", "发货成功！");
                    // 刷新
                    showShipmentManagement();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "发货失败", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * 分次发货：逐本书填写本次发货数量。
     */
    private void showPartialShipDialog(SalesOrder order, TableView<SalesOrder> table) {
        try {
            SalesOrderDao orderDao = new SalesOrderDao();
            List<SalesOrderItem> items = orderDao.findItemsByOrderId(order.getOrderId());
            List<SalesOrderItem> remainingItems = new ArrayList<>();
            for (SalesOrderItem it : items) {
                int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                int remain = it.getQuantity() - shipped;
                if (remain > 0) remainingItems.add(it);
            }
            if (remainingItems.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "提示", "该订单所有图书已发完。");
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("分次发货 - 订单 " + order.getOrderId());

            VBox root = new VBox(10);
            root.setPadding(new Insets(15));

            GridPane headGrid = new GridPane();
            headGrid.setHgap(10);
            headGrid.setVgap(10);
            TextField carrierField = new TextField("顺丰速运");
            TextField trackingField = new TextField("SF" + System.currentTimeMillis());
            TextField operatorField = new TextField(adminName);
            headGrid.add(new Label("快递公司:"), 0, 0); headGrid.add(carrierField, 1, 0);
            headGrid.add(new Label("快递单号:"), 0, 1); headGrid.add(trackingField, 1, 1);
            headGrid.add(new Label("操作员:"), 0, 2); headGrid.add(operatorField, 1, 2);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(6);
            grid.add(new Label("书号"), 0, 0);
            grid.add(new Label("本次发货"), 1, 0);
            grid.add(new Label("剩余待发"), 2, 0);

            List<TextField> qtyFields = new ArrayList<>();
            for (int i = 0; i < remainingItems.size(); i++) {
                SalesOrderItem it = remainingItems.get(i);
                int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                int remain = it.getQuantity() - shipped;
                Label bookLabel = new Label(it.getBookId());
                TextField qtyField = new TextField(String.valueOf(remain));
                Label remainLabel = new Label(String.valueOf(remain));
                grid.add(bookLabel, 0, i + 1);
                grid.add(qtyField, 1, i + 1);
                grid.add(remainLabel, 2, i + 1);
                qtyFields.add(qtyField);
            }

            root.getChildren().addAll(headGrid, new Label("填写本次要发出的数量："), grid);
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        List<ShipmentItem> toShip = new ArrayList<>();
                        for (int i = 0; i < remainingItems.size(); i++) {
                            SalesOrderItem it = remainingItems.get(i);
                            int shipped = it.getShippedQuantity() == null ? 0 : it.getShippedQuantity();
                            int remain = it.getQuantity() - shipped;
                            String text = qtyFields.get(i).getText().trim();
                            if (text.isEmpty()) continue;
                            int qty = Integer.parseInt(text);
                            if (qty < 0 || qty > remain) {
                                showAlert(Alert.AlertType.WARNING, "提示",
                                        "书号 " + it.getBookId() + " 的发货数量必须在 0~" + remain + " 之间");
                                return null;
                            }
                            if (qty == 0) continue;
                            ShipmentItem si = new ShipmentItem();
                            si.setOrderItemId(it.getOrderItemId());
                            si.setShipQuantity(qty);
                            toShip.add(si);
                        }
                        if (toShip.isEmpty()) {
                            showAlert(Alert.AlertType.WARNING, "提示", "请至少为一条图书填写发货数量");
                            return null;
                        }

                        ShipmentService service = new ShipmentService();
                        service.shipOrderPartially(order.getOrderId(), toShip,
                                carrierField.getText(), trackingField.getText(), operatorField.getText());
                        showAlert(Alert.AlertType.INFORMATION, "成功", "本次分次发货已创建");
                        showShipmentManagement();
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.WARNING, "提示", "发货数量必须是整数");
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
                    }
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载订单明细失败：" + e.getMessage());
        }
    }

    // ========== 库存管理 ==========
    private void showInventoryManagement() {
        contentArea.getChildren().clear();

        Label title = new Label("库存管理");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        TableView<Inventory> table = new TableView<>();
        table.setEditable(true);
        table.setPrefHeight(500);

        TableColumn<Inventory, String> bookIdCol = new TableColumn<>("书号");
        bookIdCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<Inventory, Integer> qtyCol = new TableColumn<>("库存数量");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Inventory, Integer> minCol = new TableColumn<>("安全库存");
        minCol.setCellValueFactory(new PropertyValueFactory<>("safetyStock"));
        minCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        minCol.setOnEditCommit(e -> {
            Inventory inv = e.getRowValue();
            Integer newVal = e.getNewValue();
            if (newVal == null || newVal < 0) {
                showAlert(Alert.AlertType.WARNING, "提示", "安全库存必须是非负整数");
                table.refresh();
                return;
            }
            try {
                InventoryDao dao = new InventoryDao();
                dao.updateSafetyStock(inv.getBookId(), newVal);
                inv.setSafetyStock(newVal);
                table.refresh();
                // 修改安全库存后，如当前库存已低于安全库存，则自动生成缺书记录
                checkAndCreateLowStockRecord(inv.getBookId());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "错误", "更新安全库存失败：" + ex.getMessage());
                table.refresh();
            }
        });

        TableColumn<Inventory, String> locCol = new TableColumn<>("库位");
        locCol.setCellValueFactory(new PropertyValueFactory<>("locationCode"));

        TableColumn<Inventory, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button addBtn = new Button("+10");
            private final Button subBtn = new Button("-10");
            private final HBox box = new HBox(5, addBtn, subBtn);
            {
                addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                subBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                addBtn.setOnAction(e -> {
                    Inventory inv = getTableView().getItems().get(getIndex());
                    adjustInventory(inv.getBookId(), 10, table);
                });
                subBtn.setOnAction(e -> {
                    Inventory inv = getTableView().getItems().get(getIndex());
                    adjustInventory(inv.getBookId(), -10, table);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(bookIdCol, qtyCol, minCol, locCol, actionCol);
        loadInventory(table);

        contentArea.getChildren().addAll(title, table);
    }

    private void loadInventory(TableView<Inventory> table) {
        try {
            InventoryDao dao = new InventoryDao();
            List<Inventory> list = dao.findAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }
    }

    private void adjustInventory(String bookId, int delta, TableView<Inventory> table) {
        try {
            InventoryDao dao = new InventoryDao();
            if (delta > 0) {
                dao.increaseQuantity(bookId, delta);
            } else {
                dao.decreaseQuantity(bookId, -delta);
            }
            // 调整库存后检查是否触发低库存缺书记录
            checkAndCreateLowStockRecord(bookId);
            loadInventory(table);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }
    }

    /**
     * 当某本书库存低于安全库存时，自动生成（或累加）一条缺书记录（source=LOW_STOCK, status=PENDING）。
     * 具体去重与数量累加规则由 OutOfStockRecordDao.insert 内部结合唯一键保证。
     */
    private void checkAndCreateLowStockRecord(String bookId) {
        try {
            InventoryDao invDao = new InventoryDao();
            int qty = invDao.getQuantity(bookId);
            int safety = invDao.getSafetyStock(bookId);
            if (safety > 0 && qty < safety) {
                OutOfStockRecord record = new OutOfStockRecord();
                record.setBookId(bookId);
                // 按“缺口量”登记
                record.setRequiredQuantity(safety - qty);
                record.setRecordDate(LocalDate.now());
                record.setSource("LOW_STOCK");
                record.setStatus("PENDING");
                record.setPriority(1);
                OutOfStockRecordDao oosDao = new OutOfStockRecordDao();
                oosDao.insert(record);
            }
        } catch (Exception ex) {
            // 为避免影响主流程，这里仅打印日志，不中断操作
            ex.printStackTrace();
        }
    }

    // ========== 采购管理 ==========
    private void showPurchaseManagement() {
        contentArea.getChildren().clear();

        Label title = new Label("采购管理");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        TabPane tabPane = new TabPane();

        // 缺书记录
        Tab outOfStockTab = new Tab("缺书记录");
        outOfStockTab.setClosable(false);
        TableView<OutOfStockRecord> osTable = createOutOfStockTable();
        outOfStockTab.setContent(osTable);

        // 顾客缺书登记
        Tab customerReqTab = new Tab("顾客缺书登记");
        customerReqTab.setClosable(false);
        TableView<CustomerOutOfStockRequest> reqTable = createCustomerOutOfStockRequestTable();
        customerReqTab.setContent(reqTable);

        // 采购单
        Tab poTab = new Tab("采购单");
        poTab.setClosable(false);
        TableView<PurchaseOrder> poTable = createPurchaseOrderTable();
        poTab.setContent(poTable);

        tabPane.getTabs().addAll(outOfStockTab, customerReqTab, poTab);

        // 按钮区域：添加缺书记录 + 从缺书记录生成采购单
        HBox buttonBar = new HBox(10);
        Button addOsBtn = new Button("添加缺书记录");
        addOsBtn.setOnAction(e -> showAddOutOfStockDialog(osTable));

        Button createPoBtn = new Button("根据选中缺书生成采购单");
        createPoBtn.setOnAction(e -> showCreatePurchaseFromOutOfStockDialog(osTable));

        buttonBar.getChildren().addAll(addOsBtn, createPoBtn);

        contentArea.getChildren().addAll(title, buttonBar, tabPane);
    }

    private TableView<OutOfStockRecord> createOutOfStockTable() {
        TableView<OutOfStockRecord> table = new TableView<>();
        table.setPrefHeight(400);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<OutOfStockRecord, Long> idCol = new TableColumn<>("记录ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));

        TableColumn<OutOfStockRecord, String> bookCol = new TableColumn<>("书号");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<OutOfStockRecord, Integer> qtyCol = new TableColumn<>("需求数量");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("requiredQuantity"));

        TableColumn<OutOfStockRecord, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, bookCol, qtyCol, statusCol);

        try {
            OutOfStockRecordDao dao = new OutOfStockRecordDao();
            List<OutOfStockRecord> list = dao.findByStatus("PENDING");
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return table;
    }

    /**
     * 顾客缺书登记列表：仅展示未付款且待处理的登记，供管理员决定是否生成正式缺书记录。
     */
    private TableView<CustomerOutOfStockRequest> createCustomerOutOfStockRequestTable() {
        TableView<CustomerOutOfStockRequest> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<CustomerOutOfStockRequest, Long> idCol = new TableColumn<>("登记ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));

        TableColumn<CustomerOutOfStockRequest, Long> orderCol = new TableColumn<>("订单号");
        orderCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));

        TableColumn<CustomerOutOfStockRequest, Long> customerCol = new TableColumn<>("客户ID");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        TableColumn<CustomerOutOfStockRequest, String> bookCol = new TableColumn<>("书号");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<CustomerOutOfStockRequest, Integer> qtyCol = new TableColumn<>("订购数量");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("requestedQty"));

        TableColumn<CustomerOutOfStockRequest, String> noteCol = new TableColumn<>("备注");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("customerNote"));
        noteCol.setPrefWidth(200);

        TableColumn<CustomerOutOfStockRequest, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button acceptBtn = new Button("生成缺书记录");
            private final Button rejectBtn = new Button("不生成");
            private final HBox box = new HBox(10, acceptBtn, rejectBtn);
            {
                acceptBtn.setOnAction(e -> {
                    CustomerOutOfStockRequest req = getTableView().getItems().get(getIndex());
                    try {
                        OutOfStockRecordDao oosDao = new OutOfStockRecordDao();
                        OutOfStockRecord record = new OutOfStockRecord();
                        record.setBookId(req.getBookId());
                        record.setRequiredQuantity(req.getRequestedQty());
                        record.setRecordDate(LocalDate.now());
                        record.setSource("CUSTOMER_REQUEST");
                        record.setRelatedCustomerId(req.getCustomerId());
                        record.setStatus("PENDING");
                        record.setPriority(1);
                        long rid = oosDao.insert(record);

                        CustomerOutOfStockRequestDao dao = new CustomerOutOfStockRequestDao();
                        dao.updateProcessedStatus(req.getRequestId(), "ACCEPTED", rid);

                        // 管理员选择生成缺书记录：订单状态从缺货待确认改为待支付（由顾客后续付款）
                        SalesOrderDao soDao = new SalesOrderDao();
                        soDao.updateStatusAndPaymentTime(req.getOrderId(), "PENDING_PAYMENT", null);

                        showAlert(Alert.AlertType.INFORMATION, "成功", "已生成缺书记录，record_id = " + rid);
                        showPurchaseManagement();
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", "生成缺书记录失败：" + ex.getMessage());
                    }
                });
                rejectBtn.setOnAction(e -> {
                    CustomerOutOfStockRequest req = getTableView().getItems().get(getIndex());
                    try {
                        CustomerOutOfStockRequestDao dao = new CustomerOutOfStockRequestDao();
                        dao.updateProcessedStatus(req.getRequestId(), "REJECTED", null);

                         // 管理员拒绝生成缺书记录：订单状态从缺货待确认改为已取消
                        SalesOrderDao soDao = new SalesOrderDao();
                        soDao.updateStatusAndPaymentTime(req.getOrderId(), "CANCELLED", null);

                        showAlert(Alert.AlertType.INFORMATION, "处理完成", "已标记为不生成缺书记录");
                        showPurchaseManagement();
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", "处理失败：" + ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, orderCol, customerCol, bookCol, qtyCol, noteCol, actionCol);

        try {
            CustomerOutOfStockRequestDao dao = new CustomerOutOfStockRequestDao();
            List<CustomerOutOfStockRequest> list = dao.findPendingUnpaid();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return table;
    }

    /**
     * 从选中的缺书记录批量生成采购单
     */
    private void showCreatePurchaseFromOutOfStockDialog(TableView<OutOfStockRecord> osTable) {
        List<OutOfStockRecord> selected = osTable.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "提示", "请先在缺书记录表中选择至少一条记录。");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("根据缺书记录生成采购单");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField supplierIdField = new TextField();
        supplierIdField.setPromptText("供应商ID，如 1");

        DatePicker expectedDatePicker = new DatePicker(LocalDate.now().plusDays(7));

        TextField buyerField = new TextField(adminName);

        grid.add(new Label("供应商ID:"), 0, 0);
        grid.add(supplierIdField, 1, 0);
        grid.add(new Label("期望到货日期:"), 0, 1);
        grid.add(expectedDatePicker, 1, 1);
        grid.add(new Label("采购员:"), 0, 2);
        grid.add(buyerField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    long supplierId = Long.parseLong(supplierIdField.getText().trim());
                    LocalDate expectedDate = expectedDatePicker.getValue();
                    String buyer = buyerField.getText().trim();

                    List<Long> ids = selected.stream()
                            .map(OutOfStockRecord::getRecordId)
                            .toList();

                    PurchaseService service = new PurchaseService();
                    long poId = service.createPurchaseOrderFromOutOfStock(ids, supplierId, expectedDate, buyer);

                    showAlert(Alert.AlertType.INFORMATION, "成功",
                            "已创建采购单，ID = " + poId + "；相关缺书记录状态已更新为 PURCHASING。");
                    showPurchaseManagement();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private TableView<PurchaseOrder> createPurchaseOrderTable() {
        TableView<PurchaseOrder> table = new TableView<>();
        table.setPrefHeight(400);

        TableColumn<PurchaseOrder, Long> idCol = new TableColumn<>("采购单号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("purchaseOrderId"));

        TableColumn<PurchaseOrder, Long> supplierCol = new TableColumn<>("供应商ID");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));

        TableColumn<PurchaseOrder, String> dateCol = new TableColumn<>("创建日期");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreateDate() != null ? c.getValue().getCreateDate().toString() : ""));

        TableColumn<PurchaseOrder, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<PurchaseOrder, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button receiveBtn = new Button("到货");
            {
                receiveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                receiveBtn.setOnAction(e -> {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    if ("ISSUED".equals(po.getStatus())) {
                        try {
                            PurchaseService service = new PurchaseService();
                            service.receiveGoods(po.getPurchaseOrderId());
                            showAlert(Alert.AlertType.INFORMATION, "成功", "采购单已到货处理完成");
                            showPurchaseManagement();
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    receiveBtn.setDisable(!"ISSUED".equals(po.getStatus()));
                    setGraphic(receiveBtn);
                }
            }
        });

        table.getColumns().addAll(idCol, supplierCol, dateCol, statusCol, actionCol);

        try {
            PurchaseOrderDao dao = new PurchaseOrderDao();
            List<PurchaseOrder> list = dao.findAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return table;
    }

    private void showAddOutOfStockDialog(TableView<OutOfStockRecord> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("添加缺书记录");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField bookIdField = new TextField();
        bookIdField.setPromptText("书号");
        TextField qtyField = new TextField();
        qtyField.setPromptText("需求数量");

        grid.add(new Label("书号:"), 0, 0);
        grid.add(bookIdField, 1, 0);
        grid.add(new Label("需求数量:"), 0, 1);
        grid.add(qtyField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    OutOfStockRecord record = new OutOfStockRecord();
                    record.setBookId(bookIdField.getText().trim());
                    record.setRequiredQuantity(Integer.parseInt(qtyField.getText().trim()));
                    record.setRecordDate(LocalDate.now());
                    record.setSource("MANUAL");
                    record.setStatus("PENDING");
                    record.setPriority(1);

                    OutOfStockRecordDao dao = new OutOfStockRecordDao();
                    dao.insert(record);

                    showAlert(Alert.AlertType.INFORMATION, "成功", "缺书记录已添加");
                    showPurchaseManagement();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // ========== 客户管理 ==========
    private void showCustomerManagement() {
        contentArea.getChildren().clear();

        Label title = new Label("客户管理");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        TableView<Customer> table = new TableView<>();
        table.setPrefHeight(500);

        TableColumn<Customer, Long> idCol = new TableColumn<>("客户ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        idCol.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        showCustomerDetail(customer.getCustomerId());
                    }
                });
            }
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    link.setText(String.valueOf(item));
                    setGraphic(link);
                }
            }
        });

        TableColumn<Customer, String> nameCol = new TableColumn<>("用户名");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Customer, String> realNameCol = new TableColumn<>("真实姓名");
        realNameCol.setCellValueFactory(new PropertyValueFactory<>("realName"));

        TableColumn<Customer, BigDecimal> balanceCol = new TableColumn<>("余额");
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("accountBalance"));

        TableColumn<Customer, BigDecimal> consumptionCol = new TableColumn<>("累积消费");
        consumptionCol.setCellValueFactory(new PropertyValueFactory<>("totalConsumption"));

        TableColumn<Customer, Integer> creditCol = new TableColumn<>("信用等级ID");
        creditCol.setCellValueFactory(new PropertyValueFactory<>("creditLevelId"));

        TableColumn<Customer, Void> actionCol = new TableColumn<>("操作");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button creditBtn = new Button("调整信用");
            {
                creditBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                creditBtn.setOnAction(e -> {
                    Customer c = getTableView().getItems().get(getIndex());
                    showCreditDialog(c, table);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : creditBtn);
            }
        });

        table.getColumns().addAll(idCol, nameCol, realNameCol, balanceCol, consumptionCol, creditCol, actionCol);

        try {
            CustomerDao dao = new CustomerDao();
            List<Customer> list = dao.findAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }

        contentArea.getChildren().addAll(title, table);
    }


    private void showCreditDialog(Customer customer, TableView<Customer> table) {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(customer.getCreditLevelId(), 1, 2, 3, 4, 5);
        dialog.setTitle("调整信用等级");
        dialog.setHeaderText("调整客户 " + customer.getRealName() + " 的信用等级");
        dialog.setContentText("选择新等级:");

        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(newLevel -> {
            try {
                CustomerDao dao = new CustomerDao();
                dao.updateCreditLevel(customer.getCustomerId(), newLevel);
                showAlert(Alert.AlertType.INFORMATION, "成功", "信用等级已调整为 " + newLevel + " 级");
                showCustomerManagement();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
            }
        });
    }

    // ========== 供应商管理 ==========
    private void showSupplierManagement() {
        contentArea.getChildren().clear();

        Label title = new Label("供应商管理");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        TableView<Supplier> table = new TableView<>();
        table.setPrefHeight(500);

        TableColumn<Supplier, Long> idCol = new TableColumn<>("供应商ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));

        TableColumn<Supplier, String> nameCol = new TableColumn<>("名称");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        nameCol.setPrefWidth(150);

        TableColumn<Supplier, String> contactCol = new TableColumn<>("联系人");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));

        TableColumn<Supplier, String> phoneCol = new TableColumn<>("电话");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Supplier, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("cooperationStatus"));

        table.getColumns().addAll(idCol, nameCol, contactCol, phoneCol, statusCol);

        try {
            SupplierDao dao = new SupplierDao();
            List<Supplier> list = dao.findAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }

        // 添加供应商按钮
        Button addBtn = new Button("添加供应商");
        addBtn.setOnAction(e -> showAddSupplierDialog(table));

        contentArea.getChildren().addAll(title, addBtn, table);
    }

    private void showAddSupplierDialog(TableView<Supplier> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("添加供应商");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField contactField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        TextField addressField = new TextField();

        grid.add(new Label("名称:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("联系人:"), 0, 1);
        grid.add(contactField, 1, 1);
        grid.add(new Label("电话:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("邮箱:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("地址:"), 0, 4);
        grid.add(addressField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    Supplier supplier = new Supplier();
                    supplier.setSupplierName(nameField.getText().trim());
                    supplier.setContactPerson(contactField.getText().trim());
                    supplier.setPhone(phoneField.getText().trim());
                    supplier.setEmail(emailField.getText().trim());
                    supplier.setAddress(addressField.getText().trim());
                    supplier.setCooperationStatus("ACTIVE");

                    SupplierDao dao = new SupplierDao();
                    dao.insert(supplier);

                    showAlert(Alert.AlertType.INFORMATION, "成功", "供应商已添加");
                    showSupplierManagement();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // ========== 书目管理 ==========
    private void showBookManagement() {
        contentArea.getChildren().clear();

        Label title = new Label("书目管理");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));

        TableView<Book> table = new TableView<>();
        table.setPrefHeight(500);

        TableColumn<Book, String> idCol = new TableColumn<>("书号");
        idCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));

        TableColumn<Book, String> titleCol = new TableColumn<>("书名");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));

        TableColumn<Book, String> publisherCol = new TableColumn<>("出版社");
        publisherCol.setCellValueFactory(new PropertyValueFactory<>("publisher"));

        TableColumn<Book, BigDecimal> priceCol = new TableColumn<>("定价");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Book, String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, titleCol, isbnCol, publisherCol, priceCol, statusCol);

        try {
            BookDao dao = new BookDao();
            List<Book> list = dao.findAll();
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", e.getMessage());
        }

        // 按钮区域：添加书目、作者/关键字、供货关系、编辑详情
        HBox btnBar = new HBox(10);
        Button addBtn = new Button("添加书目");
        addBtn.setOnAction(e -> showAddBookDialog(table));

        Button metaBtn = new Button("作者/关键字");
        metaBtn.setOnAction(e -> {
            Book book = table.getSelectionModel().getSelectedItem();
            if (book == null) {
                showAlert(Alert.AlertType.WARNING, "提示", "请先在表中选择一本书目");
            } else {
                showBookMetaDialog(book);
            }
        });

        Button supplyBtn = new Button("供货关系");
        supplyBtn.setOnAction(e -> {
            Book book = table.getSelectionModel().getSelectedItem();
            if (book == null) {
                showAlert(Alert.AlertType.WARNING, "提示", "请先在表中选择一本书目");
            } else {
                showBookSupplyDialog(book);
            }
        });

        Button detailBtn = new Button("编辑详情");
        detailBtn.setOnAction(e -> {
            Book book = table.getSelectionModel().getSelectedItem();
            if (book == null) {
                showAlert(Alert.AlertType.WARNING, "提示", "请先在表中选择一本书目");
            } else {
                showEditBookDetailDialog(book);
            }
        });

        btnBar.getChildren().addAll(addBtn, metaBtn, supplyBtn, detailBtn);

        contentArea.getChildren().addAll(title, btnBar, table);
    }

    private void showAddBookDialog(TableView<Book> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("添加书目");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField bookIdField = new TextField();
        TextField isbnField = new TextField();
        TextField titleField = new TextField();
        TextField publisherField = new TextField();
        TextField priceField = new TextField("0.00");
        TextField coverField = new TextField();
        TextArea catalogArea = new TextArea();
        catalogArea.setPrefRowCount(4);

        TextField initQtyField = new TextField("0");
        TextField safetyField = new TextField("10");

        grid.add(new Label("书号:"), 0, 0);
        grid.add(bookIdField, 1, 0);
        grid.add(new Label("ISBN:"), 0, 1);
        grid.add(isbnField, 1, 1);
        grid.add(new Label("书名:"), 0, 2);
        grid.add(titleField, 1, 2);
        grid.add(new Label("出版社:"), 0, 3);
        grid.add(publisherField, 1, 3);
        grid.add(new Label("定价:"), 0, 4);
        grid.add(priceField, 1, 4);
        grid.add(new Label("封面URL(可选):"), 0, 5);
        grid.add(coverField, 1, 5);
        grid.add(new Label("目录(可选):"), 0, 6);
        grid.add(catalogArea, 1, 6);
        grid.add(new Label("初始库存数量:"), 0, 7);
        grid.add(initQtyField, 1, 7);
        grid.add(new Label("安全库存:"), 0, 8);
        grid.add(safetyField, 1, 8);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    int initQty = Integer.parseInt(initQtyField.getText().trim());
                    int safety = Integer.parseInt(safetyField.getText().trim());
                    if (initQty < 0 || safety < 0) {
                        showAlert(Alert.AlertType.WARNING, "提示", "库存数量和安全库存必须为非负整数");
                        return null;
                    }

                    Book book = new Book();
                    book.setBookId(bookIdField.getText().trim());
                    book.setIsbn(isbnField.getText().trim());
                    book.setTitle(titleField.getText().trim());
                    book.setPublisher(publisherField.getText().trim());
                    book.setPrice(new BigDecimal(priceField.getText().trim()));
                    book.setStatus("AVAILABLE");
                     book.setCoverImageUrl(coverField.getText().trim().isEmpty() ? null : coverField.getText().trim());
                     book.setCatalog(catalogArea.getText().trim().isEmpty() ? null : catalogArea.getText().trim());

                    BookDao dao = new BookDao();
                    dao.insert(book);

                    // 初始化库存
                    InventoryDao invDao = new InventoryDao();
                    Inventory inv = new Inventory();
                    inv.setBookId(book.getBookId());
                    inv.setQuantity(initQty);
                    inv.setSafetyStock(safety);
                    invDao.insert(inv);

                    showAlert(Alert.AlertType.INFORMATION, "成功", "书目已添加");
                    showBookManagement();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "错误", ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * 管理书目的作者与关键字：仅支持新增，数量限制为作者≤4，关键字≤10。
     */
    private void showBookMetaDialog(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("作者与关键字 - " + book.getTitle());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(600, 400);

        HBox root = new HBox(20);
        root.setPadding(new Insets(10));

        VBox authorBox = new VBox(5);
        Label authorLabel = new Label("作者（最多4人，有序，可编辑/删除）");
        ListView<Author> authorList = new ListView<>();

        VBox keywordBox = new VBox(5);
        Label kwLabel = new Label("关键字（最多10个，可编辑/删除）");
        ListView<Keyword> kwList = new ListView<>();

        // 载入当前作者和关键字
        try {
            AuthorDao authorDao = new AuthorDao();
            KeywordDao keywordDao = new KeywordDao();
            List<Author> authors = authorDao.findByBookId(book.getBookId());
            authorList.getItems().addAll(authors);
            List<Keyword> kws = keywordDao.findByBookId(book.getBookId());
            kwList.getItems().addAll(kws);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载作者/关键字失败：" + e.getMessage());
        }

        authorList.setCellFactory(listView -> new ListCell<Author>() {
            @Override
            protected void updateItem(Author item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    Integer order = item.getAuthorOrder();
                    String prefix = (order != null ? ("#" + order + " ") : "");
                    setText(prefix + item.getAuthorName());
                }
            }
        });

        kwList.setCellFactory(listView -> new ListCell<Keyword>() {
            @Override
            protected void updateItem(Keyword item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getKeywordText());
                }
            }
        });

        Button addAuthorBtn = new Button("新增作者");
        addAuthorBtn.setOnAction(e -> {
            if (authorList.getItems().size() >= 4) {
                showAlert(Alert.AlertType.WARNING, "提示", "每本书最多只能有 4 位作者");
                return;
            }
            Dialog<Void> ad = new Dialog<>();
            ad.setTitle("新增作者");
            GridPane g = new GridPane();
            g.setHgap(10);
            g.setVgap(10);
            g.setPadding(new Insets(15));
            TextField nameField = new TextField();
            TextField orderField = new TextField(String.valueOf(authorList.getItems().size() + 1));
            TextField nationField = new TextField();
            TextArea bioArea = new TextArea();
            bioArea.setPrefRowCount(3);

            g.add(new Label("姓名:"), 0, 0);
            g.add(nameField, 1, 0);
            g.add(new Label("作者序号(1~4):"), 0, 1);
            g.add(orderField, 1, 1);
            g.add(new Label("国籍(可选):"), 0, 2);
            g.add(nationField, 1, 2);
            g.add(new Label("简介(可选):"), 0, 3);
            g.add(bioArea, 1, 3);

            ad.getDialogPane().setContent(g);
            ad.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            ad.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        int order = Integer.parseInt(orderField.getText().trim());
                        if (order < 1 || order > 4) {
                            showAlert(Alert.AlertType.WARNING, "提示", "作者序号必须在 1~4 之间");
                            return null;
                        }
                        Author author = new Author();
                        author.setAuthorName(nameField.getText().trim());
                        author.setNationality(nationField.getText().trim());
                        author.setBiography(bioArea.getText().trim());
                        author.setAuthorOrder(order);

                        AuthorDao authorDao = new AuthorDao();
                        Long authorId = authorDao.insert(author);
                        BookAuthorKeywordDao relDao = new BookAuthorKeywordDao();
                        relDao.addBookAuthor(book.getBookId(), authorId, order);
                        author.setAuthorId(authorId);
                        authorList.getItems().add(author);
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", "新增作者失败：" + ex.getMessage());
                    }
                }
                return null;
            });
            ad.showAndWait();
        });

        Button editAuthorBtn = new Button("编辑选中作者");
        Button deleteAuthorBtn = new Button("删除选中作者");

        editAuthorBtn.setOnAction(e -> {
            Author selected = authorList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "提示", "请先选择要编辑的作者");
                return;
            }
            Dialog<Void> ad = new Dialog<>();
            ad.setTitle("编辑作者");
            GridPane g = new GridPane();
            g.setHgap(10);
            g.setVgap(10);
            g.setPadding(new Insets(15));

            TextField nameField = new TextField(selected.getAuthorName());
            TextField orderField = new TextField(String.valueOf(
                    selected.getAuthorOrder() != null ? selected.getAuthorOrder() : 1));
            TextField nationField = new TextField(selected.getNationality());
            TextArea bioArea = new TextArea(selected.getBiography());
            bioArea.setPrefRowCount(3);

            g.add(new Label("姓名:"), 0, 0);
            g.add(nameField, 1, 0);
            g.add(new Label("作者序号(1~4):"), 0, 1);
            g.add(orderField, 1, 1);
            g.add(new Label("国籍(可选):"), 0, 2);
            g.add(nationField, 1, 2);
            g.add(new Label("简介(可选):"), 0, 3);
            g.add(bioArea, 1, 3);

            ad.getDialogPane().setContent(g);
            ad.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            ad.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        int order = Integer.parseInt(orderField.getText().trim());
                        if (order < 1 || order > 4) {
                            showAlert(Alert.AlertType.WARNING, "提示", "作者序号必须在 1~4 之间");
                            return null;
                        }
                        selected.setAuthorName(nameField.getText().trim());
                        selected.setNationality(nationField.getText().trim());
                        selected.setBiography(bioArea.getText().trim());
                        selected.setAuthorOrder(order);

                        AuthorDao authorDao = new AuthorDao();
                        authorDao.update(selected);
                        BookAuthorKeywordDao relDao = new BookAuthorKeywordDao();
                        relDao.updateBookAuthorOrder(book.getBookId(), selected.getAuthorId(), order);

                        authorList.refresh();
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", "更新作者失败：" + ex.getMessage());
                    }
                }
                return null;
            });
            ad.showAndWait();
        });

        deleteAuthorBtn.setOnAction(e -> {
            Author selected = authorList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "提示", "请先选择要删除的作者");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "确定要将该作者从当前书目中移除吗？此操作不会删除作者在其他书目中的关联。",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText("确认删除作者");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        BookAuthorKeywordDao relDao = new BookAuthorKeywordDao();
                        relDao.removeBookAuthor(book.getBookId(), selected.getAuthorId());
                        authorList.getItems().remove(selected);
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", "删除作者失败：" + ex.getMessage());
                    }
                }
            });
        });

        Button addKwBtn = new Button("新增关键字");
        addKwBtn.setOnAction(e -> {
            if (kwList.getItems().size() >= 10) {
                showAlert(Alert.AlertType.WARNING, "提示", "每本书最多只能有 10 个关键字");
                return;
            }
            TextInputDialog kd = new TextInputDialog();
            kd.setTitle("新增关键字");
            kd.setHeaderText("为《" + book.getTitle() + "》新增关键字");
            kd.setContentText("关键字：");
            kd.showAndWait().ifPresent(text -> {
                try {
                    String kw = text.trim();
                    if (kw.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "提示", "关键字不能为空");
                        return;
                    }
                    Keyword keyword = new Keyword();
                    keyword.setKeywordText(kw);
                    KeywordDao keywordDao = new KeywordDao();
                    Long kid = keywordDao.insert(keyword);
                    keyword.setKeywordId(kid);
                    BookAuthorKeywordDao relDao = new BookAuthorKeywordDao();
                    relDao.addBookKeyword(book.getBookId(), kid);
                    kwList.getItems().add(keyword);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "错误", "新增关键字失败：" + ex.getMessage());
                }
            });
        });

        Button editKwBtn = new Button("编辑选中关键字");
        Button deleteKwBtn = new Button("删除选中关键字");

        editKwBtn.setOnAction(e -> {
            Keyword selected = kwList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "提示", "请先选择要编辑的关键字");
                return;
            }
            TextInputDialog kd = new TextInputDialog(selected.getKeywordText());
            kd.setTitle("编辑关键字");
            kd.setHeaderText("编辑《" + book.getTitle() + "》的关键字");
            kd.setContentText("关键字：");
            kd.showAndWait().ifPresent(text -> {
                try {
                    String kw = text.trim();
                    if (kw.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "提示", "关键字不能为空");
                        return;
                    }
                    selected.setKeywordText(kw);
                    KeywordDao keywordDao = new KeywordDao();
                    keywordDao.update(selected);
                    kwList.refresh();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "错误", "更新关键字失败：" + ex.getMessage());
                }
            });
        });

        deleteKwBtn.setOnAction(e -> {
            Keyword selected = kwList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.INFORMATION, "提示", "请先选择要删除的关键字");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "确定要将该关键字从当前书目中移除吗？此操作不会删除该关键字与其他书目的关系。",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.setHeaderText("确认删除关键字");
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        BookAuthorKeywordDao relDao = new BookAuthorKeywordDao();
                        relDao.removeBookKeyword(book.getBookId(), selected.getKeywordId());
                        kwList.getItems().remove(selected);
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", "删除关键字失败：" + ex.getMessage());
                    }
                }
            });
        });

        HBox authorBtnBar = new HBox(10, addAuthorBtn, editAuthorBtn, deleteAuthorBtn);
        HBox kwBtnBar = new HBox(10, addKwBtn, editKwBtn, deleteKwBtn);

        authorBox.getChildren().addAll(authorLabel, authorList, authorBtnBar);
        keywordBox.getChildren().addAll(kwLabel, kwList, kwBtnBar);
        root.getChildren().addAll(authorBox, keywordBox);

        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    /**
     * 管理某本书的供货关系（供应商及其供货价/交期、主供货商）。
     */
    private void showBookSupplyDialog(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("供货关系 - " + book.getTitle());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(600, 400);

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        TableView<Supply> table = new TableView<>();
        table.setPrefHeight(280);

        TableColumn<Supply, Long> supplierCol = new TableColumn<>("供应商ID");
        supplierCol.setCellValueFactory(new PropertyValueFactory<>("supplierId"));

        TableColumn<Supply, BigDecimal> priceCol = new TableColumn<>("供货价");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("supplyPrice"));

        TableColumn<Supply, Integer> leadCol = new TableColumn<>("供货周期(天)");
        leadCol.setCellValueFactory(new PropertyValueFactory<>("leadTimeDays"));

        TableColumn<Supply, String> primaryCol = new TableColumn<>("主供");
        primaryCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isPrimary() ? "是" : ""));

        table.getColumns().addAll(supplierCol, priceCol, leadCol, primaryCol);

        HBox btnBar = new HBox(10);
        Button addBtn = new Button("新增供货");
        Button delBtn = new Button("删除选中");
        btnBar.getChildren().addAll(addBtn, delBtn);

        addBtn.setOnAction(e -> {
            Dialog<Void> ad = new Dialog<>();
            ad.setTitle("新增供货关系");

            GridPane g = new GridPane();
            g.setHgap(10);
            g.setVgap(10);
            g.setPadding(new Insets(15));

            TextField supplierField = new TextField();
            TextField priceField = new TextField();
            TextField leadField = new TextField();
            CheckBox primaryCheck = new CheckBox("设为主供货商");

            g.add(new Label("供应商ID:"), 0, 0);
            g.add(supplierField, 1, 0);
            g.add(new Label("供货价:"), 0, 1);
            g.add(priceField, 1, 1);
            g.add(new Label("供货周期(天):"), 0, 2);
            g.add(leadField, 1, 2);
            g.add(primaryCheck, 1, 3);

            ad.getDialogPane().setContent(g);
            ad.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            ad.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    try {
                        long supplierId = Long.parseLong(supplierField.getText().trim());
                        BigDecimal sp = new BigDecimal(priceField.getText().trim());
                        Integer lead = null;
                        if (!leadField.getText().trim().isEmpty()) {
                            lead = Integer.parseInt(leadField.getText().trim());
                        }
                        Supply s = new Supply();
                        s.setSupplierId(supplierId);
                        s.setBookId(book.getBookId());
                        s.setSupplyPrice(sp);
                        s.setLeadTimeDays(lead);
                        s.setPrimary(primaryCheck.isSelected());

                        SupplyDao dao = new SupplyDao();
                        dao.insert(s);
                        loadSupplyForBook(book, table);
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "错误", "新增供货关系失败：" + ex.getMessage());
                    }
                }
                return null;
            });

            ad.showAndWait();
        });

        delBtn.setOnAction(e -> {
            Supply selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "提示", "请先选择一条供货记录");
                return;
            }
            try {
                SupplyDao dao = new SupplyDao();
                dao.delete(selected.getSupplierId(), book.getBookId());
                loadSupplyForBook(book, table);
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "错误", "删除供货关系失败：" + ex.getMessage());
            }
        });

        try {
            loadSupplyForBook(book, table);
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "错误", "加载供货关系失败：" + ex.getMessage());
        }

        root.getChildren().addAll(table, btnBar);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    private void loadSupplyForBook(Book book, TableView<Supply> table) throws SQLException {
        SupplyDao dao = new SupplyDao();
        List<Supply> list = dao.findByBookId(book.getBookId());
        table.setItems(FXCollections.observableArrayList(list));
    }

    /**
     * 编辑书目的可选详情：目录与封面 URL。
     */
    private void showEditBookDetailDialog(Book book) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("编辑详情 - " + book.getTitle());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField coverField = new TextField(book.getCoverImageUrl() != null ? book.getCoverImageUrl() : "");
        TextArea catalogArea = new TextArea(book.getCatalog() != null ? book.getCatalog() : "");
        catalogArea.setPrefRowCount(6);

        grid.add(new Label("封面URL:"), 0, 0);
        grid.add(coverField, 1, 0);
        grid.add(new Label("目录:"), 0, 1);
        grid.add(catalogArea, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    book.setCoverImageUrl(coverField.getText().trim().isEmpty() ? null : coverField.getText().trim());
                    book.setCatalog(catalogArea.getText().trim().isEmpty() ? null : catalogArea.getText().trim());
                    BookDao dao = new BookDao();
                    dao.update(book);
                    showAlert(Alert.AlertType.INFORMATION, "成功", "书目详情已更新");
                    showBookManagement();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "错误", "更新书目详情失败：" + ex.getMessage());
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

    public BorderPane getView() {
        return root;
    }
}

