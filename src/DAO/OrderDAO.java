package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Order;
import database.DBConnection;

public class OrderDAO {

    public ObservableList<Order> getAllOrders() {
        ObservableList<Order> list = FXCollections.observableArrayList();
        String sql = "SELECT o.*, c.full_name as customer_name FROM orders o LEFT JOIN customers c ON o.customer_id = c.id";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getDouble("total_amount"),
                        rs.getDate("order_date"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertOrder(Order order) {
        String sql = "INSERT INTO orders (customer_id, total_amount, order_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, order.getCustomerId());
            pst.setDouble(2, order.getTotalAmount());
            pst.setDate(3, order.getOrderDate());
            pst.setString(4, order.getStatus());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrder(Order order) {
        String sql = "UPDATE orders SET customer_id = ?, total_amount = ?, order_date = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, order.getCustomerId());
            pstmt.setDouble(2, order.getTotalAmount());
            pstmt.setDate(3, order.getOrderDate());
            pstmt.setString(4, order.getStatus());
            pstmt.setInt(5, order.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOrder(int id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int insertOrderReturnId(Order order) {
        String sql = "INSERT INTO orders (customer_id, total_amount, order_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            if (order.getCustomerId() > 0) {
                pst.setInt(1, order.getCustomerId());
            } else {
                pst.setNull(1, java.sql.Types.INTEGER);
            }
            pst.setDouble(2, order.getTotalAmount());
            pst.setDate(3, order.getOrderDate());
            pst.setString(4, order.getStatus());

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
