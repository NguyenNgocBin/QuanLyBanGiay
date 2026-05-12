package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DashboardController {

    @FXML
    private AreaChart<String, Number> revenueChart;

    @FXML
    private TableView<Transaction> transactionTable;
    
    @FXML
    private TableColumn<Transaction, String> colId;
    @FXML
    private TableColumn<Transaction, String> colCustomer;
    @FXML
    private TableColumn<Transaction, String> colProduct;
    @FXML
    private TableColumn<Transaction, String> colTotal;
    @FXML
    private TableColumn<Transaction, String> colStatus;

    @FXML
    public void initialize() {
        setupChart();
        setupTable();
    }

    private void setupChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu thực tế");
        
        series.getData().add(new XYChart.Data<>("T2", 15000000));
        series.getData().add(new XYChart.Data<>("T3", 20000000));
        series.getData().add(new XYChart.Data<>("T4", 18000000));
        series.getData().add(new XYChart.Data<>("T5", 25000000));
        series.getData().add(new XYChart.Data<>("T6", 22000000));
        series.getData().add(new XYChart.Data<>("T7", 30000000));
        series.getData().add(new XYChart.Data<>("CN", 35000000));
        
        revenueChart.getData().add(series);
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        ObservableList<Transaction> data = FXCollections.observableArrayList(
            new Transaction("#DH-8492", "Nguyễn Văn Hùng", "Nike Air Force 1 (Trắng)", "2.850.000đ", "Đã giao"),
            new Transaction("#DH-8493", "Lê Thị Mai", "Adidas Superstar", "2.100.000đ", "Chờ xử lý"),
            new Transaction("#DH-8494", "Hoàng Văn Nam", "Puma RS-X", "2.500.000đ", "Đã hủy")
        );

        transactionTable.setItems(data);
    }

    // Inner class cho dữ liệu bảng
    public static class Transaction {
        private final String id;
        private final String customer;
        private final String product;
        private final String total;
        private final String status;

        public Transaction(String id, String customer, String product, String total, String status) {
            this.id = id;
            this.customer = customer;
            this.product = product;
            this.total = total;
            this.status = status;
        }

        public String getId() { return id; }
        public String getCustomer() { return customer; }
        public String getProduct() { return product; }
        public String getTotal() { return total; }
        public String getStatus() { return status; }
    }
}
