package controller;

import DAO.ImportHistoryDAO;
import DAO.ImportHistoryDAO.ImportDetailRow;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.ImportOrder;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ImportHistoryController {

    @FXML private TextField txtSearch;
    @FXML private Label lblShowing;
    @FXML private TableView<ImportOrder> historyTable;
    @FXML private TableColumn<ImportOrder, Integer> colId;
    @FXML private TableColumn<ImportOrder, String> colSupplierName;
    @FXML private TableColumn<ImportOrder, String> colImportDate;
    @FXML private TableColumn<ImportOrder, String> colTotalAmount;
    @FXML private TableColumn<ImportOrder, Integer> colTotalItems;
    @FXML private TableColumn<ImportOrder, String> colStatus;
    @FXML private TableColumn<ImportOrder, ImportOrder> colAction;

    private final ImportHistoryDAO historyDAO = new ImportHistoryDAO();
    private final ObservableList<ImportOrder> allImports = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadHistory();

        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            searchHistory(newValue);
        });
    }

    private void setupTable() {
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        historyTable.setFixedCellSize(50);

        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colSupplierName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplierName()));
        colImportDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getImportDate()));
        colTotalAmount.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getTotalAmount())));
        colTotalItems.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTotalItems()).asObject());
        
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label label = new Label(status);
                label.getStyleClass().addAll("status-pill", "status-success");
                setGraphic(label);
            }
        });

        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ImportOrder order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setGraphic(null);
                    return;
                }

                Button btnDetails = new Button("Chi tiết 🔍");
                btnDetails.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #A855F7); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 4; -fx-cursor: hand;");
                btnDetails.setOnAction(event -> showDetailsDialog(order));
                
                HBox box = new HBox(btnDetails);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });
    }

    private void loadHistory() {
        List<ImportOrder> list = historyDAO.getAllImports();
        allImports.setAll(list);
        historyTable.setItems(allImports);
        lblShowing.setText("Hiển thị " + allImports.size() + " phiếu nhập");
    }

    private void searchHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadHistory();
            return;
        }
        List<ImportOrder> list = historyDAO.searchImports(keyword);
        historyTable.setItems(FXCollections.observableArrayList(list));
        lblShowing.setText("Hiển thị " + list.size() + " phiếu nhập");
    }

    private void showDetailsDialog(ImportOrder order) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Chi tiết phiếu nhập - Mã #" + order.getId());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #0b111e; -fx-text-fill: white;");

        Label title = new Label("CHI TIẾT PHIẾU NHẬP KHO #" + order.getId());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label supplier = new Label("Nhà cung cấp: " + order.getSupplierName());
        supplier.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
        Label date = new Label("Ngày nhập: " + order.getImportDate());
        date.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        HBox metaBox = new HBox(30, supplier, date);

        // Details Table
        TableView<ImportDetailRow> detailsTable = new TableView<>();
        detailsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        detailsTable.setFixedCellSize(40);
        detailsTable.getStyleClass().add("warehouse-table");

        TableColumn<ImportDetailRow, String> colProd = new TableColumn<>("TÊN SẢN PHẨM");
        colProd.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));

        TableColumn<ImportDetailRow, Integer> colQty = new TableColumn<>("SỐ LƯỢNG");
        colQty.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());

        TableColumn<ImportDetailRow, String> colPrice = new TableColumn<>("ĐƠN GIÁ NHẬP");
        colPrice.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getImportPrice())));

        TableColumn<ImportDetailRow, String> colTotal = new TableColumn<>("THÀNH TIỀN");
        colTotal.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getTotalPrice())));

        detailsTable.getColumns().addAll(colProd, colQty, colPrice, colTotal);

        List<ImportDetailRow> list = historyDAO.getImportDetails(order.getId());
        detailsTable.setItems(FXCollections.observableArrayList(list));

        Label total = new Label("TỔNG TIỀN PHIẾU NHẬP: " + formatCurrency(order.getTotalAmount()));
        total.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 15px;");
        
        Button btnClose = new Button("Đóng lại");
        btnClose.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        btnClose.setOnAction(e -> dialog.close());

        HBox bottomBox = new HBox(total);
        bottomBox.setAlignment(Pos.CENTER_LEFT);
        
        HBox actionBox = new HBox(btnClose);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        layout.getChildren().addAll(title, metaBox, detailsTable, bottomBox, actionBox);

        Scene scene = new Scene(layout, 600, 450);
        utils.ThemeManager.applyTheme(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ";
    }
}
