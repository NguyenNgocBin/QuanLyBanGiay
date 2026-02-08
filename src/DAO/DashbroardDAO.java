package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;

public class DashbroardDAO {
    @FXML
    private LineChart<?, ?> revenueChart;

    // Hàm trả về tổng doanh thu ngày hôm nay
    public double getDoanhThuHomNay() {
        double doanhThu = 0;
        // SQL: Tính tổng tiền các đơn có ngày tạo là hôm nay VÀ đã thanh toán
        String sql = "SELECT SUM(total) FROM oder " +
                "WHERE DATE(order_date) = CURRENT_DATE() " +
                "AND status = N'Đã thanh toán'";
        try (Connection connection = DBConnection.getConnection(); // Gọi hàm kết nối
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                doanhThu = rs.getDouble(1); // Lấy cột đầu tiên (kết quả SUM)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    public int getSoDonHangHomNay() {
        int soDon = 0;
        String sql = "SELECT COUNT(*) FROM oder WHERE order_date = CURRENT_DATE() AND status != N'Đã hủy'";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                soDon = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return soDon;
    }

    // Hàm lấy doanh thu 12 tháng của NĂM HIỆN TẠI
    public double[] getDoanhThuTheoThang() {
        double[] data = new double[12];

        String sql = "SELECT MONTH(order_date) as thang, SUM(total) as tong_tien " +
                "FROM oder " +
                "WHERE YEAR(order_date) = YEAR(CURRENT_DATE()) " +
                "AND status = N'Đã thanh toán' " +
                "GROUP BY MONTH(order_date)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int thang = rs.getInt("thang"); // Lấy tháng (1-12)
                double tien = rs.getDouble("tong_tien"); // Lấy tổng tiền

                // Lưu vào mảng (Tháng 1 thì lưu vào index 0)
                if (thang >= 1 && thang <= 12) {
                    data[thang - 1] = tien;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
