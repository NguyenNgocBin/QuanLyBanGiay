package controller;

import DAO.DashboardDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label lblTodayRevenue;
    @FXML private Label lblNewOrders;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblLowStock;
    @FXML private VBox topProductsContainer;

    @FXML private AreaChart<String, Number> revenueChart;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colId;
    @FXML private TableColumn<Transaction, String> colCustomer;
    @FXML private TableColumn<Transaction, String> colProduct;
    @FXML private TableColumn<Transaction, String> colTotal;
    @FXML private TableColumn<Transaction, String> colStatus;

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @FXML
    public void initialize() {
        loadMetrics();
        setupChart();
        setupTable();
        loadTopProducts();
    }

    private void loadMetrics() {
        lblTodayRevenue.setText(formatCurrency(dashboardDAO.getTodayRevenue()));
        lblNewOrders.setText(String.valueOf(dashboardDAO.getNewOrdersCount()));
        lblTotalCustomers.setText(String.valueOf(dashboardDAO.getTotalCustomersCount()));
        lblLowStock.setText(String.valueOf(dashboardDAO.getLowStockCount()));
    }

    private void setupChart() {
        revenueChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu 7 ngày qua");
        
        List<DashboardDAO.RevenueByDay> data = dashboardDAO.getRevenueLast7Days();
        for (DashboardDAO.RevenueByDay r : data) {
            series.getData().add(new XYChart.Data<>(r.date(), r.revenue()));
        }
        
        revenueChart.getData().add(series);
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        List<DashboardDAO.RecentTransaction> recent = dashboardDAO.getRecentTransactions(5);
        ObservableList<Transaction> data = FXCollections.observableArrayList();
        for (DashboardDAO.RecentTransaction rt : recent) {
            data.add(new Transaction(rt.id(), rt.customer(), rt.product(), formatCurrency(rt.total()), rt.status()));
        }
        transactionTable.setItems(data);
    }

    private void loadTopProducts() {
        topProductsContainer.getChildren().clear();
        List<DashboardDAO.TopProduct> topProducts = dashboardDAO.getTopProducts(5);

        for (DashboardDAO.TopProduct tp : topProducts) {
            HBox hbox = new HBox();
            hbox.setAlignment(Pos.CENTER_LEFT);

            Label icon = new Label("👟");
            icon.setStyle("-fx-font-size: 20px;");

            VBox infoBox = new VBox();
            infoBox.setPadding(new Insets(0, 0, 0, 10));
            Label nameLabel = new Label(tp.name());
            nameLabel.setStyle("-fx-font-weight: bold;");
            Label soldLabel = new Label("Đã bán " + tp.totalSold() + " đôi");
            soldLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
            infoBox.getChildren().addAll(nameLabel, soldLabel);

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label revenueLabel = new Label(formatCompactCurrency(tp.totalRevenue()));
            revenueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");

            hbox.getChildren().addAll(icon, infoBox, spacer, revenueLabel);
            topProductsContainer.getChildren().add(hbox);
        }
        
        if (topProducts.isEmpty()) {
            Label emptyLbl = new Label("Chưa có dữ liệu bán hàng");
            emptyLbl.setStyle("-fx-text-fill: #718096;");
            topProductsContainer.getChildren().add(emptyLbl);
        }
    }

    @FXML
    private void goToProducts() {
        if (MainController.getInstance() != null) {
            MainController.getInstance().selectTab("products");
        }
    }

    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ";
    }

    private String formatCompactCurrency(double value) {
        if (value >= 1_000_000_000) {
            return String.format(Locale.US, "%.1fB", value / 1_000_000_000);
        }
        if (value >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", value / 1_000_000);
        }
        if (value >= 1_000) {
            return String.format(Locale.US, "%.1fK", value / 1_000);
        }
        return formatCurrency(value);
    }

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
