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
        String sql = "SELECT customer_code FROM customers ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastMa = rs.getString("customer_code");
                if (lastMa != null && lastMa.startsWith("KH")) {
                    int number = Integer.parseInt(lastMa.substring(2));
                    nextMa = String.format("KH%03d", number + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextMa;
    }

    // Hàm thêm mới khách hàng
    public boolean insertCustomer(String customerCode, String fullName, String phone, String email) {
        String sql = "INSERT INTO customers (customer_code, full_name, phone, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerCode);
            pst.setString(2, fullName);
            pst.setString(3, phone);
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
        String sql = "SELECT * FROM customers";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("customer_code"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getDouble("total_spent")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteCustomer(String customerCode) {
        String sql = "DELETE FROM customers WHERE customer_code = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, customerCode);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, phone = ?, email = ? WHERE customer_code = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getCustomerCode());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}