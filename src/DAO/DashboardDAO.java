package DAO;

import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public double getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE order_date = CURDATE() AND status != 'Da huy'";
        return queryDouble(sql);
    }

    public int getNewOrdersCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_date = CURDATE()";
        return queryInt(sql);
    }

    public int getTotalCustomersCount() {
        String sql = "SELECT COUNT(*) FROM customers";
        return queryInt(sql);
    }

    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM products WHERE stock > 0 AND stock <= 10";
        return queryInt(sql);
    }

    public List<RevenueByDay> getRevenueLast7Days() {
        List<RevenueByDay> list = new ArrayList<>();
        String sql = """
            SELECT DATE_FORMAT(order_date, '%d/%m') as date_str, SUM(total_amount) as revenue
            FROM orders
            WHERE order_date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
              AND status != 'Da huy'
            GROUP BY order_date
            ORDER BY order_date ASC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new RevenueByDay(rs.getString("date_str"), rs.getDouble("revenue")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<TopProduct> getTopProducts(int limit) {
        List<TopProduct> list = new ArrayList<>();
        String sql = """
            SELECT p.name, SUM(od.quantity) as total_sold, SUM(od.quantity * od.unit_price) as total_revenue
            FROM order_details od
            JOIN products p ON od.product_id = p.id
            JOIN orders o ON o.id = od.order_id
            WHERE o.status != 'Da huy'
            GROUP BY p.id, p.name
            ORDER BY total_sold DESC
            LIMIT ?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TopProduct(
                        rs.getString("name"),
                        rs.getInt("total_sold"),
                        rs.getDouble("total_revenue")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<RecentTransaction> getRecentTransactions(int limit) {
        List<RecentTransaction> list = new ArrayList<>();
        String sql = """
            SELECT 
                o.id, 
                CONCAT('HD', LPAD(o.id, 5, '0')) as order_code,
                COALESCE(c.full_name, 'Khách lẻ') as customer_name,
                o.total_amount, 
                o.status,
                (SELECT GROUP_CONCAT(p.name SEPARATOR ', ') 
                 FROM order_details od 
                 JOIN products p ON od.product_id = p.id 
                 WHERE od.order_id = o.id) as products
            FROM orders o
            LEFT JOIN customers c ON o.customer_id = c.id
            ORDER BY o.id DESC
            LIMIT ?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RecentTransaction(
                        rs.getString("order_code"),
                        rs.getString("customer_name"),
                        rs.getString("products") != null ? rs.getString("products") : "N/A",
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private double queryDouble(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private int queryInt(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public record RevenueByDay(String date, double revenue) {}
    public record TopProduct(String name, int totalSold, double totalRevenue) {}
    public record RecentTransaction(String id, String customer, String product, double total, String status) {}
}
