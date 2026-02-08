package controller;

import DAO.DashbroardDAO; // Hãy đảm bảo tên file DAO của bạn viết đúng chính tả là DashboardDAO
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label lblDoanhThu;

    @FXML
    private Label lblSoDonHang;

    @FXML
    private AreaChart<String, Number> revenueChart;

    // 1. Khởi tạo đối tượng DAO
    // Lưu ý: Kiểm tra file DAO của bạn tên là "DashboardDAO" hay "DashbroardDAO" để
    // sửa lại cho khớp
    private DashbroardDAO dashbroardDAO = new DashbroardDAO();

    @FXML
    public void initialize() {
        loadThongKeHomNay();
        loadBieuDoDoanhThu();
    }

    private void loadThongKeHomNay() {
        // XỬ LÝ DOANH THU
        double tongTien = dashbroardDAO.getDoanhThuHomNay();
        // Định dạng tiền tệ (Ví dụ: 5.000.000 đ)
        lblDoanhThu.setText(String.format("%,.0f VNĐ", tongTien));

        // --- XỬ LÝ SỐ ĐƠN HÀNG ---
        // Sửa lỗi: Thay 'donHangDAO' (chưa có) bằng 'dashboardDAO'
        int soDon = dashbroardDAO.getSoDonHangHomNay();
        lblSoDonHang.setText(soDon + " Đơn");
    }

    private void loadBieuDoDoanhThu() {
        // Xóa dữ liệu cũ trên biểu đồ (nếu có)
        revenueChart.getData().clear();
        // Lấy dữ liệu thực tế từ Database
        double[] doanhThu12Thang = dashbroardDAO.getDoanhThuTheoThang();

        // tạo một Series dữ liệu mới
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Đặt tên cho đường kẻ
        series.setName("Năm " + java.time.Year.now().getValue());

        // Chạy vòng lặp để đưa số liệu 12 tháng vào biểu đồ
        for (int i = 0; i < 12; i++) {
            String thang = "Tháng " + (i + 1); // Tạo nhãn: Tháng 1, Tháng 2...
            double doanhThu = doanhThu12Thang[i]; // Lấy tiền tương ứng

            series.getData().add(new XYChart.Data<>(thang, doanhThu));
        }
        revenueChart.getData().add(series);
    }
}