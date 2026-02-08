package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.DBConnection;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Customer;

public class CustomerDAO {

    // Hàm lấy mã tiếp theo
    public String getNextMaKH() {
        String nextMa = "KH001";
        String sql = "SELECT MaKH FROM customer ORDER BY MaKH DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastMa = rs.getString("MaKH");
                int number = Integer.parseInt(lastMa.substring(2));
                nextMa = String.format("KH%03d", number + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextMa;
    }

    // Hàm thêm mới khách hàng
    public boolean insertCustomer(String maKH, String hoTen, String sdt, String email) {
        String sql = "INSERT INTO Customer (MaKH, HoTen, Sdt, Email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, maKH);
            pst.setString(2, hoTen);
            pst.setString(3, sdt);
            pst.setString(4, email);

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm lấy tất cả khách hàng
    public ObservableList<Customer> getAllCustomers() {
        ObservableList<Customer> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM customer"; // Lấy tất cả dữ liệu từ bảng

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(new Customer(
                        rs.getString("MaKH"),
                        rs.getString("HoTen"),
                        rs.getString("Sdt"),
                        rs.getString("Email"),
                        rs.getDouble("TongChiTieu")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteCustomer(String maKH) {
        String sql = "DELETE FROM customer WHERE MaKH = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, maKH);
            return pst.executeUpdate() > 0; // Trả về true nếu xóa thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customer SET HoTen = ?, Sdt = ?, Email = ? WHERE MaKH = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getHoTen());
            pstmt.setString(2, customer.getSdt());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getMaKH());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}