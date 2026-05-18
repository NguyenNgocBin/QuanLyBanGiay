package DAO;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportsDAO {

    public double getTotalRevenue(LocalDate start, LocalDate end) {
        String sql = "SELECT SUM(total_amount) FROM orders WHERE status NOT LIKE '%Huy%' AND order_date BETWEEN ? AND ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalProfit(LocalDate start, LocalDate end) {
        // Lợi nhuận = doanh thu - giá nhập (nếu không có giá nhập thì giả lập bằng 60% giá bán)
        String sql = """
                SELECT SUM(od.quantity * (od.unit_price - COALESCE(
                    (SELECT MIN(id.import_price) FROM import_details id WHERE id.product_id = od.product_id),
                    od.unit_price * 0.6
                ))) AS profit 
                FROM order_details od 
                JOIN orders o ON od.order_id = o.id 
                WHERE o.status NOT LIKE '%Huy%' AND o.order_date BETWEEN ? AND ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("profit");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTotalItemsSold(LocalDate start, LocalDate end) {
        String sql = "SELECT SUM(od.quantity) FROM order_details od JOIN orders o ON od.order_id = o.id WHERE o.status NOT LIKE '%Huy%' AND o.order_date BETWEEN ? AND ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Double> getRevenueTrend(LocalDate start, LocalDate end) {
        Map<String, Double> trend = new LinkedHashMap<>();
        String sql = "SELECT order_date, SUM(total_amount) FROM orders WHERE status NOT LIKE '%Huy%' AND order_date BETWEEN ? AND ? GROUP BY order_date ORDER BY order_date ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trend.put(rs.getDate("order_date").toString(), rs.getDouble(2));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trend;
    }

    public Map<String, Double> getCategoryRevenue(LocalDate start, LocalDate end) {
        Map<String, Double> dist = new LinkedHashMap<>();
        String sql = """
                SELECT c.name, SUM(od.quantity * od.unit_price) AS total_sales 
                FROM order_details od 
                JOIN products p ON od.product_id = p.id 
                JOIN categories c ON p.category_id = c.id 
                JOIN orders o ON od.order_id = o.id 
                WHERE o.status NOT LIKE '%Huy%' AND o.order_date BETWEEN ? AND ?
                GROUP BY c.name
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dist.put(rs.getString("name"), rs.getDouble("total_sales"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dist;
    }

    public Map<String, Integer> getTopSellingProducts(LocalDate start, LocalDate end) {
        Map<String, Integer> top = new LinkedHashMap<>();
        String sql = """
                SELECT p.name, SUM(od.quantity) AS qty 
                FROM order_details od 
                JOIN products p ON od.product_id = p.id 
                JOIN orders o ON od.order_id = o.id 
                WHERE o.status NOT LIKE '%Huy%' AND o.order_date BETWEEN ? AND ?
                GROUP BY p.id, p.name 
                ORDER BY qty DESC 
                LIMIT 5
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    top.put(rs.getString("name"), rs.getInt("qty"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }
}
