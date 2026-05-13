package DAO;

import database.DBConnection;
import models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String SELECT_BASE = """
            SELECT id, customer_code, full_name, phone, email,
                   COALESCE(total_spent, 0) AS total_spent
            FROM customers
            """;

    // ─── Record: KH kèm thống kê đơn hàng ───────────────────────────────────

    /** Khách hàng kèm số lần mua & tổng tiền thực tế từ bảng orders */
    public record CustomerStat(
            int    id,
            String customerCode,
            String fullName,
            String phone,
            String email,
            int    orderCount,
            double totalSpent,
            String lastOrderDate
    ) {}

    /** Một đơn hàng gắn với khách hàng */
    public record CustomerOrder(
            String orderCode,
            String orderDate,
            int    itemCount,
            double totalAmount,
            String paymentMethod,
            String status
    ) {}

    // ─── Lấy tất cả KH kèm thống kê ─────────────────────────────────────────

    public List<CustomerStat> getAllWithStats() {
        List<CustomerStat> list = new ArrayList<>();
        String sql = """
                SELECT
                    c.id,
                    c.customer_code,
                    c.full_name,
                    c.phone,
                    c.email,
                    COUNT(o.id)                                  AS order_count,
                    COALESCE(SUM(o.total_amount), 0)            AS total_spent,
                    MAX(DATE_FORMAT(o.order_date,'%d/%m/%Y'))   AS last_order_date
                FROM customers c
                LEFT JOIN orders o ON o.customer_id = c.id AND o.status != 'Da huy'
                GROUP BY c.id, c.customer_code, c.full_name, c.phone, c.email
                ORDER BY total_spent DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CustomerStat(
                        rs.getInt("id"),
                        rs.getString("customer_code"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("order_count"),
                        rs.getDouble("total_spent"),
                        rs.getString("last_order_date") == null ? "—" : rs.getString("last_order_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Tìm kiếm KH kèm thống kê theo tên hoặc SĐT */
    public List<CustomerStat> searchWithStats(String keyword) {
        List<CustomerStat> all = getAllWithStats();
        if (keyword == null || keyword.isBlank()) return all;
        String kw = keyword.trim().toLowerCase();
        return all.stream().filter(c ->
                c.fullName().toLowerCase().contains(kw)
                || c.phone().toLowerCase().contains(kw)
                || c.customerCode().toLowerCase().contains(kw)
        ).toList();
    }

    /** Lấy danh sách đơn hàng của một khách hàng */
    public List<CustomerOrder> getOrdersByCustomer(int customerId) {
        List<CustomerOrder> list = new ArrayList<>();
        String sql = """
                SELECT
                    CONCAT('HD', LPAD(o.id, 5, '0'))            AS order_code,
                    DATE_FORMAT(o.order_date, '%d/%m/%Y')        AS order_date,
                    (SELECT COUNT(*) FROM order_details od WHERE od.order_id = o.id) AS item_count,
                    o.total_amount,
                    CASE
                        WHEN o.status LIKE '%Chuyen khoan%' THEN 'Chuyển khoản'
                        WHEN o.status LIKE '%The Visa%'     THEN 'Thẻ Visa/MC'
                        ELSE 'Tiền mặt'
                    END AS payment_method,
                    o.status
                FROM orders o
                WHERE o.customer_id = ?
                ORDER BY o.id DESC
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CustomerOrder(
                            rs.getString("order_code"),
                            rs.getString("order_date"),
                            rs.getInt("item_count"),
                            rs.getDouble("total_amount"),
                            rs.getString("payment_method"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Thống kê tổng ───────────────────────────────────────────────────────

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM customers");
    }

    public int countNew() {
        // KH có đơn hàng trong tháng này
        return queryInt("""
                SELECT COUNT(DISTINCT customer_id) FROM orders
                WHERE MONTH(order_date) = MONTH(CURDATE())
                  AND YEAR(order_date)  = YEAR(CURDATE())
                  AND customer_id IS NOT NULL
                  AND status != 'Da huy'
                """);
    }

    public double avgSpent() {
        return queryDouble("""
                SELECT COALESCE(AVG(total), 0) FROM (
                    SELECT SUM(total_amount) AS total
                    FROM orders
                    WHERE customer_id IS NOT NULL AND status != 'Da huy'
                    GROUP BY customer_id
                ) t
                """);
    }

    // ─── CRUD cơ bản ─────────────────────────────────────────────────────────

    public List<Customer> getAll() {
        List<Customer> list = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY full_name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Customer> search(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = SELECT_BASE + """
                WHERE LOWER(full_name) LIKE ?
                   OR phone LIKE ?
                ORDER BY full_name
                LIMIT 8
                """;
        String p = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p); ps.setString(2, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Customer add(String fullName, String phone, String email) {
        String code = "KH" + System.currentTimeMillis() % 100000;
        String sql = """
                INSERT INTO customers (customer_code, full_name, phone, email, total_spent)
                VALUES (?, ?, ?, ?, 0)
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, fullName.trim());
            ps.setString(3, phone == null ? "" : phone.trim());
            ps.setString(4, email == null ? "" : email.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Customer(keys.getInt(1), code, fullName.trim(),
                            phone == null ? "" : phone.trim(),
                            email == null ? "" : email.trim(), 0);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean update(int id, String fullName, String phone, String email) {
        String sql = "UPDATE customers SET full_name=?, phone=?, email=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, phone == null ? "" : phone.trim());
            ps.setString(3, email == null ? "" : email.trim());
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        // Chỉ xóa nếu KH không có đơn hàng nào
        String sql = "DELETE FROM customers WHERE id=? AND NOT EXISTS (SELECT 1 FROM orders WHERE customer_id=?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private int queryInt(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private double queryDouble(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("customer_code"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getDouble("total_spent")
        );
    }
}
