package controller;

import DAO.OrderDAO;
import DAO.OrderDAO.DetailLine;
import DAO.OrderDAO.OrderRow;
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

public class OrderController {

    // ─── Thống kê ──────────────────────────────────────────────────────────────
    @FXML private Label lblTodayCount;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTotalRevenue;

    // ─── Bộ lọc ────────────────────────────────────────────────────────────────
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbPayment;

    // ─── Bảng đơn hàng ─────────────────────────────────────────────────────────
    @FXML private TableView<OrderRow>          tblOrders;
    @FXML private TableColumn<OrderRow, String>  colCode;
    @FXML private TableColumn<OrderRow, String>  colCustomer;
    @FXML private TableColumn<OrderRow, String>  colPhone;
    @FXML private TableColumn<OrderRow, String>  colDate;
    @FXML private TableColumn<OrderRow, String>  colPayment;
    @FXML private TableColumn<OrderRow, Integer> colItems;
    @FXML private TableColumn<OrderRow, String>  colTotal;
    @FXML private TableColumn<OrderRow, String>  colStatus;
    @FXML private TableColumn<OrderRow, OrderRow> colActions;

    @FXML private Label lblResultCount;

    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<OrderRow> allOrders = FXCollections.observableArrayList();

    // ═══════════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        setupFilters();
        setupTable();
        loadStats();
        loadOrders();
    }

    // ─── Thống kê ──────────────────────────────────────────────────────────────

    private void loadStats() {
        lblTodayCount.setText(String.valueOf(orderDAO.countToday()));
        lblTodayRevenue.setText(fmt(orderDAO.revenueToday()));
        lblTotalRevenue.setText(fmt(orderDAO.revenueTotal()));
    }

    // ─── Bộ lọc ────────────────────────────────────────────────────────────────

    private void setupFilters() {
        cbPayment.setItems(FXCollections.observableArrayList(
                "Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ Visa/MC"
        ));
        cbPayment.getSelectionModel().selectFirst();

        tfSearch.textProperty().addListener((obs, o, n) -> applyFilter());
        cbPayment.setOnAction(e -> applyFilter());
    }

    private void applyFilter() {
        String kw      = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase(Locale.ROOT);
        String payment = cbPayment.getValue();

        ObservableList<OrderRow> filtered = allOrders.filtered(row -> {
            boolean kwMatch = kw.isBlank()
                    || row.orderCode().toLowerCase().contains(kw)
                    || row.customerName().toLowerCase().contains(kw)
                    || row.customerPhone().toLowerCase().contains(kw);
            boolean payMatch = payment == null || payment.equals("Tất cả")
                    || row.paymentMethod().equals(payment);
            return kwMatch && payMatch;
        });

        tblOrders.setItems(filtered);
        lblResultCount.setText(filtered.size() + " đơn hàng");
    }

    // ─── Load dữ liệu ──────────────────────────────────────────────────────────

    private void loadOrders() {
        allOrders.setAll(orderDAO.getAll());
        applyFilter();
    }

    @FXML private void refresh() { loadStats(); loadOrders(); }

    // ─── Cài đặt bảng ──────────────────────────────────────────────────────────

    private void setupTable() {
        tblOrders.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblOrders.setFixedCellSize(52);

        colCode.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue().orderCode()));
        colCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                Label lbl = new Label(v); lbl.getStyleClass().add("order-code-label");
                setGraphic(lbl); setText(null);
            }
        });

        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().customerName()));
        colCustomer.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                boolean isGuest = v.equals("Khách lẻ");
                Label lbl = new Label(v);
                lbl.getStyleClass().add(isGuest ? "order-guest-label" : "order-customer-label");
                setGraphic(lbl); setText(null);
            }
        });

        colPhone.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue().customerPhone()));
        colDate.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue().orderDate()));
        colPayment.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().paymentMethod()));
        colPayment.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setGraphic(null); return; }
                Label lbl = new Label(v);
                lbl.getStyleClass().add("order-payment-tag");
                lbl.getStyleClass().add(switch (v) {
                    case "Chuyển khoản" -> "pay-bank";
                    case "Thẻ Visa/MC"  -> "pay-card";
                    default             -> "pay-cash";
                });
                setGraphic(lbl); setText(null);
            }
        });

        colItems.setCellValueFactory(d    -> new SimpleObjectProperty<>(d.getValue().itemCount()));
        colTotal.setCellValueFactory(d    -> new SimpleStringProperty(fmt(d.getValue().totalAmount())));
        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                Label lbl = new Label(v); lbl.getStyleClass().add("order-total-label");
                setGraphic(lbl); setText(null);
            }
        });

        colStatus.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue().status()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setGraphic(null); return; }
                boolean cancelled = v.equals("Da huy");
                Label lbl = new Label(cancelled ? "Đã hủy" : "Đã thanh toán");
                lbl.getStyleClass().add("order-status-pill");
                lbl.getStyleClass().add(cancelled ? "status-cancelled" : "status-paid");
                setGraphic(lbl); setText(null);
            }
        });

        // Cột thao tác
        colActions.setCellValueFactory(d  -> new SimpleObjectProperty<>(d.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(OrderRow row, boolean empty) {
                super.updateItem(row, empty);
                if (empty || row == null) { setGraphic(null); return; }

                Button btnDetail = new Button("Chi tiết");
                btnDetail.getStyleClass().add("order-action-detail");
                btnDetail.setOnAction(e -> showDetail(row));

                if (row.status().equals("Da huy")) {
                    setGraphic(btnDetail);
                    setAlignment(Pos.CENTER);
                    return;
                }

                Button btnCancel = new Button("Hủy đơn");
                btnCancel.getStyleClass().add("order-action-cancel");
                btnCancel.setOnAction(e -> cancelOrder(row));

                HBox box = new HBox(6, btnDetail, btnCancel);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
        });
    }

    // ─── Chi tiết đơn hàng ─────────────────────────────────────────────────────

    private void showDetail(OrderRow row) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Chi tiết đơn hàng " + row.orderCode());
        dlg.setHeaderText(null);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Thông tin đơn hàng
        VBox content = new VBox(14);
        content.setPadding(new Insets(18));
        content.setPrefWidth(560);

        // Card thông tin
        GridPane info = new GridPane();
        info.setHgap(16); info.setVgap(8);
        info.getStyleClass().add("order-detail-info");

        addInfoRow(info, 0, "Mã đơn:",      row.orderCode());
        addInfoRow(info, 1, "Khách hàng:",  row.customerName());
        addInfoRow(info, 2, "Điện thoại:",  row.customerPhone());
        addInfoRow(info, 3, "Ngày đặt:",    row.orderDate());
        addInfoRow(info, 4, "Thanh toán:",  row.paymentMethod());
        addInfoRow(info, 5, "Trạng thái:",
                row.status().equals("Da huy") ? "Đã hủy" : "Đã thanh toán");

        // Bảng sản phẩm
        Label tblTitle = new Label("SẢN PHẨM TRONG ĐƠN");
        tblTitle.getStyleClass().add("order-detail-section-title");

        TableView<DetailLine> tbl = new TableView<>();
        tbl.setFixedCellSize(42);
        tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbl.setPrefHeight(180);

        TableColumn<DetailLine, String>  cName  = new TableColumn<>("Sản phẩm");
        TableColumn<DetailLine, String>  cCode  = new TableColumn<>("SKU");
        TableColumn<DetailLine, Integer> cQty   = new TableColumn<>("SL");
        TableColumn<DetailLine, String>  cPrice = new TableColumn<>("Đơn giá");
        TableColumn<DetailLine, String>  cTotal = new TableColumn<>("Thành tiền");

        cName.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().productName()));
        cCode.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().productCode()));
        cQty.setCellValueFactory(d   -> new SimpleObjectProperty<>(d.getValue().quantity()));
        cPrice.setCellValueFactory(d -> new SimpleStringProperty(fmt(d.getValue().unitPrice())));
        cTotal.setCellValueFactory(d -> new SimpleStringProperty(fmt(d.getValue().lineTotal())));

        tbl.getColumns().addAll(List.of(cName, cCode, cQty, cPrice, cTotal));
        tbl.getStyleClass().add("order-detail-table");

        List<DetailLine> lines = orderDAO.getDetails(row.id());
        tbl.setItems(FXCollections.observableArrayList(lines));

        // Tổng tiền
        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_RIGHT);
        Label totalLabel = new Label("TỔNG CỘNG: " + fmt(row.totalAmount()));
        totalLabel.getStyleClass().add("order-detail-total");
        totalRow.getChildren().add(totalLabel);

        content.getChildren().addAll(info, tblTitle, tbl, totalRow);
        dlg.getDialogPane().setContent(content);
        dlg.showAndWait();
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label); lbl.getStyleClass().add("order-info-key");
        Label val = new Label(value); val.getStyleClass().add("order-info-val");
        grid.add(lbl, 0, row); grid.add(val, 1, row);
    }

    // ─── Hủy đơn ───────────────────────────────────────────────────────────────

    private void cancelOrder(OrderRow row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy đơn");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Hủy đơn " + row.orderCode() + " sẽ hoàn lại tồn kho tất cả sản phẩm.\n"
                + "Bạn có chắc muốn tiếp tục?");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean ok = orderDAO.cancelOrder(row.id());
            if (ok) {
                showInfo("Hủy đơn thành công", "Đơn " + row.orderCode() + " đã được hủy và tồn kho đã được hoàn.");
                loadStats();
                loadOrders();
            } else {
                showError("Không thể hủy đơn. Vui lòng thử lại.");
            }
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private String fmt(double v) {
        return NumberFormat.getNumberInstance(Locale.US).format(v).replace(",", ".") + "đ";
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Lỗi"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
