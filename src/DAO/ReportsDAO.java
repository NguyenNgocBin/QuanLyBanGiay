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
import java.util.List;
import java.util.ArrayList;

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

    public int getTotalOrdersCount(LocalDate start, LocalDate end) {
        String sql = "SELECT COUNT(*) FROM orders WHERE status NOT LIKE '%Huy%' AND order_date BETWEEN ? AND ?";
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

    /**
     * Lấy cơ cấu doanh thu theo phương thức thanh toán trong khoảng thời gian lọc.
     * Dữ liệu được trích xuất từ bảng payments liên kết với bảng orders để lọc ngày.
     *
     * @param start Ngày bắt đầu lọc
     * @param end   Ngày kết thúc lọc
     * @return Map liên kết phương thức thanh toán hiển thị bằng tiếng Việt và số tiền tương ứng
     */
    public Map<String, Double> getPaymentMethodDistribution(LocalDate start, LocalDate end) {
        Map<String, Double> dist = new LinkedHashMap<>();
        String sql = """
                SELECT p.payment_method, SUM(p.amount) AS total 
                FROM payments p 
                JOIN orders o ON p.order_id = o.id 
                WHERE o.status NOT LIKE '%Huy%' AND o.order_date BETWEEN ? AND ? 
                GROUP BY p.payment_method
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String method = rs.getString("payment_method");
                    String displayName = "CASH".equals(method) ? "Tiền mặt" :
                                          "BANKING".equals(method) ? "Chuyển khoản" :
                                          "MOMO".equals(method) ? "Ví MoMo" : method;
                    dist.put(displayName, rs.getDouble("total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dist;
    }

    /**
     * Lấy danh sách Top 5 nhân viên có doanh thu tích lũy trong ca làm việc hiện tại cao nhất.
     * Dữ liệu được sắp xếp giảm dần theo cột session_revenue trong bảng users.
     *
     * @return Map liên kết giữa tên nhân viên và doanh thu ca hiện tại của họ
     */
    public Map<String, Double> getTopStaffRevenue() {
        Map<String, Double> top = new LinkedHashMap<>();
        String sql = "SELECT name, session_revenue FROM users WHERE session_revenue > 0 ORDER BY session_revenue DESC LIMIT 5";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                top.put(rs.getString("name"), rs.getDouble("session_revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    /**
     * Lấy chi tiết xu hướng doanh thu theo thời gian bao gồm doanh thu, số lượng đơn hàng
     * và danh sách tên các nhân viên bán hàng phát sinh giao dịch trong ngày đó.
     *
     * @param start Ngày bắt đầu lọc
     * @param end   Ngày kết thúc lọc
     * @return Danh sách chứa thông tin chi tiết từng ngày
     */
    public List<RevenueTrendData> getRevenueTrendDetail(LocalDate start, LocalDate end) {
        List<RevenueTrendData> list = new ArrayList<>();
        String sql = """
                SELECT 
                    o.order_date, 
                    SUM(o.total_amount) AS total_revenue, 
                    COUNT(o.id) AS total_orders,
                    COALESCE(GROUP_CONCAT(DISTINCT u.name SEPARATOR ', '), 'Khác') AS staff_names
                FROM orders o 
                LEFT JOIN users u ON o.user_id = u.id 
                WHERE o.status NOT LIKE '%Huy%' AND o.order_date BETWEEN ? AND ? 
                GROUP BY o.order_date 
                ORDER BY o.order_date ASC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RevenueTrendData(
                            rs.getDate("order_date").toString(),
                            rs.getDouble("total_revenue"),
                            rs.getInt("total_orders"),
                            rs.getString("staff_names")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static record RevenueTrendData(
            String date,
            double revenue,
            int ordersCount,
            String staffNames
    ) {}
}
