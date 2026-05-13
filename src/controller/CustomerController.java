package controller;

import DAO.CustomerDAO;
import DAO.CustomerDAO.CustomerOrder;
import DAO.CustomerDAO.CustomerStat;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CustomerController {

    // ─── Thống kê ──────────────────────────────────────────────────────────────
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblNewThisMonth;
    @FXML private Label lblAvgSpent;

    // ─── Bộ lọc ────────────────────────────────────────────────────────────────
    @FXML private TextField tfSearch;

    // ─── Bảng khách hàng ───────────────────────────────────────────────────────
    @FXML private TableView<CustomerStat>             tblCustomers;
    @FXML private TableColumn<CustomerStat, String>   colCode;
    @FXML private TableColumn<CustomerStat, String>   colName;
    @FXML private TableColumn<CustomerStat, String>   colPhone;
    @FXML private TableColumn<CustomerStat, String>   colEmail;
    @FXML private TableColumn<CustomerStat, Integer>  colOrders;
    @FXML private TableColumn<CustomerStat, String>   colSpent;
    @FXML private TableColumn<CustomerStat, String>   colLastOrder;
    @FXML private TableColumn<CustomerStat, String>   colRank;
    @FXML private TableColumn<CustomerStat, CustomerStat> colActions;

    @FXML private Label lblResultCount;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final ObservableList<CustomerStat> allCustomers = FXCollections.observableArrayList();

    // ═══════════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        setupTable();
        setupSearch();
        loadStats();
        loadCustomers();
    }

    // ─── Thống kê ──────────────────────────────────────────────────────────────

    private void loadStats() {
        lblTotalCustomers.setText(String.valueOf(customerDAO.countAll()));
        lblNewThisMonth.setText(String.valueOf(customerDAO.countNew()));
        lblAvgSpent.setText(fmt(customerDAO.avgSpent()));
    }

    // ─── Bộ lọc ────────────────────────────────────────────────────────────────

    private void setupSearch() {
        tfSearch.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void applyFilter() {
        String kw = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase(Locale.ROOT);
        ObservableList<CustomerStat> filtered = allCustomers.filtered(c ->
                kw.isBlank()
                || c.fullName().toLowerCase().contains(kw)
                || c.phone().toLowerCase().contains(kw)
                || c.customerCode().toLowerCase().contains(kw)
                || c.email().toLowerCase().contains(kw)
        );
        tblCustomers.setItems(filtered);
        lblResultCount.setText(filtered.size() + " khách hàng");
    }

    // ─── Load dữ liệu ──────────────────────────────────────────────────────────

    private void loadCustomers() {
        allCustomers.setAll(customerDAO.getAllWithStats());
        applyFilter();
    }

    @FXML private void refresh() { loadStats(); loadCustomers(); }

    // ─── Thêm khách hàng ───────────────────────────────────────────────────────

    @FXML
    private void openAddDialog() {
        showEditDialog(null);
    }

    // ─── Cài đặt bảng ──────────────────────────────────────────────────────────

    private void setupTable() {
        tblCustomers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblCustomers.setFixedCellSize(54);

        // Mã KH
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().customerCode()));
        colCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label lbl = new Label(v); lbl.getStyleClass().add("cust-code-label");
                setGraphic(lbl); setText(null);
            }
        });

        // Tên KH — avatar chữ cái đầu
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().fullName()));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String initial = v.isBlank() ? "?" : String.valueOf(v.charAt(0)).toUpperCase();
                Label avatar = new Label(initial); avatar.getStyleClass().add("cust-avatar");
                Label name   = new Label(v);       name.getStyleClass().add("cust-name-label");
                HBox box = new HBox(8, avatar, name); box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box); setText(null);
            }
        });

        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().phone()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().email()));

        // Số đơn hàng — hiển thị badge
        colOrders.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().orderCount()));
        colOrders.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label lbl = new Label(String.valueOf(v));
                lbl.getStyleClass().add(v > 0 ? "cust-order-badge" : "cust-order-badge-zero");
                setGraphic(lbl); setText(null); setAlignment(Pos.CENTER);
            }
        });

        // Tổng chi tiêu
        colSpent.setCellValueFactory(d -> new SimpleStringProperty(fmt(d.getValue().totalSpent())));
        colSpent.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                Label lbl = new Label(v); lbl.getStyleClass().add("cust-spent-label");
                setGraphic(lbl); setText(null);
            }
        });

        colLastOrder.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lastOrderDate()));

        // Hạng KH dựa theo tổng chi tiêu
        colRank.setCellValueFactory(d -> new SimpleStringProperty(rankOf(d.getValue().totalSpent())));
        colRank.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label lbl = new Label(v); lbl.getStyleClass().add("cust-rank-" + v.toLowerCase().replace(" ", ""));
                setGraphic(lbl); setText(null); setAlignment(Pos.CENTER);
            }
        });

        // Cột thao tác
        colActions.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(CustomerStat c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setGraphic(null); return; }

                Button btnOrders = new Button("Đơn hàng");
                btnOrders.getStyleClass().add("cust-action-orders");
                btnOrders.setOnAction(e -> showOrderHistory(c));

                Button btnEdit = new Button("Sửa");
                btnEdit.getStyleClass().add("cust-action-edit");
                btnEdit.setOnAction(e -> showEditDialog(c));

                Button btnDel = new Button("Xóa");
                btnDel.getStyleClass().add("cust-action-delete");
                btnDel.setDisable(c.orderCount() > 0); // không xóa KH đã có đơn
                btnDel.setOnAction(e -> deleteCustomer(c));

                HBox box = new HBox(5, btnOrders, btnEdit, btnDel);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    // ─── Dialog lịch sử đơn hàng ───────────────────────────────────────────────

    private void showOrderHistory(CustomerStat c) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Lịch sử mua hàng — " + c.fullName());
        dlg.setHeaderText(null);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setPrefWidth(640);

        VBox content = new VBox(14);
        content.setPadding(new Insets(18));

        // Thẻ thông tin KH
        GridPane info = new GridPane();
        info.setHgap(14); info.setVgap(8);
        info.getStyleClass().add("cust-detail-card");

        addInfoRow(info, 0, "Mã KH:",        c.customerCode());
        addInfoRow(info, 1, "Họ tên:",        c.fullName());
        addInfoRow(info, 2, "Điện thoại:",    c.phone());
        addInfoRow(info, 3, "Email:",          c.email());
        addInfoRow(info, 4, "Số lần mua:",    c.orderCount() + " đơn hàng");
        addInfoRow(info, 5, "Tổng chi tiêu:", fmt(c.totalSpent()));
        addInfoRow(info, 6, "Lần mua gần nhất:", c.lastOrderDate());
        addInfoRow(info, 7, "Hạng KH:",       rankOf(c.totalSpent()));

        // Tiêu đề bảng đơn hàng
        Label tblTitle = new Label("DANH SÁCH ĐƠN HÀNG");
        tblTitle.getStyleClass().add("cust-detail-section-title");

        // Bảng đơn hàng
        TableView<CustomerOrder> tbl = new TableView<>();
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setFixedCellSize(42);
        tbl.setPrefHeight(210);
        tbl.getStyleClass().add("cust-order-table");

        TableColumn<CustomerOrder, String>  cCode    = new TableColumn<>("Mã đơn");
        TableColumn<CustomerOrder, String>  cDate    = new TableColumn<>("Ngày");
        TableColumn<CustomerOrder, Integer> cItems   = new TableColumn<>("SL SP");
        TableColumn<CustomerOrder, String>  cPay     = new TableColumn<>("Thanh toán");
        TableColumn<CustomerOrder, String>  cTotal   = new TableColumn<>("Tổng tiền");
        TableColumn<CustomerOrder, String>  cStatus  = new TableColumn<>("Trạng thái");

        cCode.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().orderCode()));
        cCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                Label lbl = new Label(v); lbl.getStyleClass().add("order-code-label");
                setGraphic(lbl); setText(null);
            }
        });
        cDate.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().orderDate()));
        cItems.setCellValueFactory(d  -> new SimpleObjectProperty<>(d.getValue().itemCount()));
        cPay.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue().paymentMethod()));
        cTotal.setCellValueFactory(d  -> new SimpleStringProperty(fmt(d.getValue().totalAmount())));
        cTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                Label lbl = new Label(v); lbl.getStyleClass().add("order-total-label");
                setGraphic(lbl); setText(null);
            }
        });
        cStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().status().equals("Da huy") ? "Đã hủy" : "Đã thanh toán"));
        cStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setGraphic(null); return; }
                Label lbl = new Label(v);
                lbl.getStyleClass().add("order-status-pill");
                lbl.getStyleClass().add(v.equals("Đã hủy") ? "status-cancelled" : "status-paid");
                setGraphic(lbl); setText(null);
            }
        });

        tbl.getColumns().addAll(List.of(cCode, cDate, cItems, cPay, cTotal, cStatus));
        tbl.setItems(FXCollections.observableArrayList(customerDAO.getOrdersByCustomer(c.id())));

        Label placeholder = new Label("Chưa có đơn hàng nào");
        placeholder.getStyleClass().add("order-empty-label");
        tbl.setPlaceholder(placeholder);

        content.getChildren().addAll(info, tblTitle, tbl);
        dlg.getDialogPane().setContent(content);
        dlg.showAndWait();
    }

    // ─── Dialog thêm / sửa KH ──────────────────────────────────────────────────

    private void showEditDialog(CustomerStat existing) {
        boolean isEdit = existing != null;
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "Sửa thông tin khách hàng" : "Thêm khách hàng mới");
        dlg.setHeaderText(null);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(18));

        TextField tfName  = new TextField(isEdit ? existing.fullName() : "");
        TextField tfPhone = new TextField(isEdit ? existing.phone()    : "");
        TextField tfEmail = new TextField(isEdit ? existing.email()    : "");

        tfName.setPromptText("Họ và tên *");
        tfPhone.setPromptText("Số điện thoại");
        tfEmail.setPromptText("Email");
        for (TextField f : new TextField[]{tfName, tfPhone, tfEmail})
            f.getStyleClass().add("form-input");
        tfName.setPrefWidth(270);

        grid.add(labelForm("Họ và tên *"), 0, 0); grid.add(tfName,  1, 0);
        grid.add(labelForm("Điện thoại"),  0, 1); grid.add(tfPhone, 1, 1);
        grid.add(labelForm("Email"),        0, 2); grid.add(tfEmail, 1, 2);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(isEdit ? "Cập nhật" : "Lưu");
        okBtn.setDisable(tfName.getText().trim().isBlank());
        tfName.textProperty().addListener((o, v, n) -> okBtn.setDisable(n.trim().isBlank()));

        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !tfName.getText().trim().isBlank()) {
                if (isEdit) {
                    boolean ok = customerDAO.update(existing.id(),
                            tfName.getText(), tfPhone.getText(), tfEmail.getText());
                    if (!ok) showError("Không thể cập nhật. Vui lòng thử lại.");
                } else {
                    var newC = customerDAO.add(tfName.getText(), tfPhone.getText(), tfEmail.getText());
                    if (newC == null) showError("Không thể thêm khách hàng. Vui lòng thử lại.");
                }
                loadStats(); loadCustomers();
            }
            return null;
        });
        dlg.showAndWait();
    }

    // ─── Xóa KH ────────────────────────────────────────────────────────────────

    private void deleteCustomer(CustomerStat c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Xóa khách hàng \"" + c.fullName() + "\"?\nHành động này không thể hoàn tác.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> res = confirm.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.YES) {
            boolean ok = customerDAO.delete(c.id());
            if (ok) { loadStats(); loadCustomers(); }
            else showError("Không thể xóa. Khách hàng này đã có đơn hàng.");
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String rankOf(double spent) {
        if (spent >= 10_000_000) return "VIP";
        if (spent >= 3_000_000)  return "Vàng";
        if (spent >= 1_000_000)  return "Bạc";
        return "Thân thiết";
    }

    private String fmt(double v) {
        return NumberFormat.getNumberInstance(Locale.US).format(v).replace(",", ".") + "đ";
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label); lbl.getStyleClass().add("cust-info-key");
        Label val = new Label(value == null ? "—" : value); val.getStyleClass().add("cust-info-val");
        grid.add(lbl, 0, row); grid.add(val, 1, row);
    }

    private Label labelForm(String text) {
        Label l = new Label(text); l.getStyleClass().add("form-label"); return l;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
