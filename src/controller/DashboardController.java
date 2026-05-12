package controller;

import DAO.DashbroardDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DashboardController {

    @FXML
    private Label lblDoanhThu;

    @FXML
    private Label lblSoDonHang;

    @FXML
    private BarChart<String, Number> revenueChart;

    @FXML
    private TableView<OrderMock> tblRecentOrders;

    private DashbroardDAO dashbroardDAO = new DashbroardDAO();

    @FXML
    public void initialize() {
        loadThongKeHomNay();
        loadBieuDoDoanhThu();
        setupRecentOrdersTable();
    }

    private void loadThongKeHomNay() {
        double tongTien = dashbroardDAO.getDoanhThuHomNay();
        lblDoanhThu.setText(String.format("%,.0f VNĐ", tongTien));

        int soDon = dashbroardDAO.getSoDonHangHomNay();
        lblSoDonHang.setText(soDon + " Đơn");
    }

    private void loadBieuDoDoanhThu() {
        revenueChart.getData().clear();
        double[] doanhThu12Thang = dashbroardDAO.getDoanhThuTheoThang();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Năm " + java.time.Year.now().getValue());

        for (int i = 0; i < 12; i++) {
            String thang = "T" + (i + 1); // Rút gọn nhãn thành T1, T2... để vừa biểu đồ cột
            double doanhThu = doanhThu12Thang[i];
            series.getData().add(new XYChart.Data<>(thang, doanhThu));
        }
        revenueChart.getData().add(series);
    }

    private void setupRecentOrdersTable() {
        // Lấy các cột từ TableView (đã định nghĩa trong FXML)
        ObservableList<TableColumn<OrderMock, ?>> columns = tblRecentOrders.getColumns();
        
        if (columns.size() >= 5) {
            columns.get(0).setCellValueFactory(new PropertyValueFactory<>("maDon"));
            columns.get(1).setCellValueFactory(new PropertyValueFactory<>("khachHang"));
            columns.get(2).setCellValueFactory(new PropertyValueFactory<>("sanPham"));
            columns.get(3).setCellValueFactory(new PropertyValueFactory<>("tongTien"));
            columns.get(4).setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        }

        // Thêm dữ liệu giả lập
        ObservableList<OrderMock> data = FXCollections.observableArrayList(
            new OrderMock("#ORD-9402", "Nguyễn Văn A", "Air Max Premium x1", "3.450.000 đ", "Đang xử lý"),
            new OrderMock("#ORD-9401", "Trần Thị B", "Jordan Retro x1, Socks x2", "4.550.000 đ", "Đã giao"),
            new OrderMock("#ORD-9400", "Lê Văn C", "Ultraboost x1", "3.900.000 đ", "Đang xử lý"),
            new OrderMock("#ORD-9399", "Phạm Thị D", "Nike Air Force 1 x1", "2.500.000 đ", "Đã hủy"),
            new OrderMock("#ORD-9398", "Hoàng Văn E", "Vans Old Skool x2", "1.800.000 đ", "Đã giao")
        );

        tblRecentOrders.setItems(data);
    }

    // Class tĩnh hỗ trợ hiển thị dữ liệu giả lập
    public static class OrderMock {
        private String maDon;
        private String khachHang;
        private String sanPham;
        private String tongTien;
        private String trangThai;

        public OrderMock(String maDon, String khachHang, String sanPham, String tongTien, String trangThai) {
            this.maDon = maDon;
            this.khachHang = khachHang;
            this.sanPham = sanPham;
            this.tongTien = tongTien;
            this.trangThai = trangThai;
        }

        public String getMaDon() { return maDon; }
        public String getKhachHang() { return khachHang; }
        public String getSanPham() { return sanPham; }
        public String getTongTien() { return tongTien; }
        public String getTrangThai() { return trangThai; }
    }
}