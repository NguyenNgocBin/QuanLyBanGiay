package DAO;

import database.DBConnection;
import models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý đơn hàng: truy vấn, lọc, hủy đơn, tìm kiếm chi tiết.
 */
public class OrderDAO {

    // ─── Inner record ─────────────────────────────────────────────────────────

    /** Đơn hàng với thông tin khách hàng (JOIN) để hiển thị trên bảng */
    public record OrderRow(
            int    id,
            String orderCode,
            String customerName,
            String customerPhone,
            String orderDate,
            String paymentMethod,
            double totalAmount,
            String status,
            int    itemCount
    ) {}

    /** Một dòng sản phẩm trong chi tiết đơn hàng */
    public record DetailLine(
            String productName,
            String productCode,
            int    quantity,
            double unitPrice,
            double lineTotal
    ) {}

    // ─── SQL ──────────────────────────────────────────────────────────────────

    private static final String SELECT_ORDERS = """
            SELECT
                o.id,
                CONCAT('HD', LPAD(o.id, 5, '0')) AS order_code,
                COALESCE(c.full_name, 'Khách lẻ')  AS customer_name,
                COALESCE(c.phone, '—')              AS customer_phone,
                DATE_FORMAT(o.order_date, '%d/%m/%Y') AS order_date,
                CASE
                    WHEN o.status LIKE '%Chuyen khoan%' THEN 'Chuyển khoản'
                    WHEN o.status LIKE '%The Visa%'     THEN 'Thẻ Visa/MC'
                    ELSE 'Tiền mặt'
                END AS payment_method,
                o.total_amount,
                o.status,
                (SELECT COALESCE(SUM(od.quantity), 0) FROM order_details od WHERE od.order_id = o.id) AS item_count
            FROM orders o
            LEFT JOIN customers c ON o.customer_id = c.id
            """;

    // ─── Truy vấn chính ───────────────────────────────────────────────────────

    /** Lấy tất cả đơn hàng, mới nhất trước */
    public List<OrderRow> getAll() {
        return query(SELECT_ORDERS + " ORDER BY o.id DESC", ps -> {});
    }

    /** Tìm kiếm theo mã HD, tên KH, SĐT */
    public List<OrderRow> search(String keyword) {
        String sql = SELECT_ORDERS + """
                WHERE LOWER(CONCAT('HD', LPAD(o.id, 5, '0'))) LIKE ?
                   OR LOWER(COALESCE(c.full_name, ''))  LIKE ?
                   OR LOWER(COALESCE(c.phone, ''))       LIKE ?
                ORDER BY o.id DESC
                """;
        String p = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return query(sql, ps -> { ps.setString(1, p); ps.setString(2, p); ps.setString(3, p); });
    }

    /** Lọc theo phương thức thanh toán */
    public List<OrderRow> filterByPayment(String method) {
        String sql = SELECT_ORDERS + " WHERE o.status LIKE ? ORDER BY o.id DESC";
        String pattern = switch (method) {
            case "Chuyển khoản" -> "%Chuyen khoan%";
            case "Thẻ Visa/MC"  -> "%The Visa%";
            case "Tiền mặt"     -> "%Tien mat%";
            default             -> "%";
        };
        return query(sql, ps -> ps.setString(1, pattern));
    }

    /** Lọc theo khoảng ngày */
    public List<OrderRow> filterByDate(String fromDate, String toDate) {
        String sql = SELECT_ORDERS
                + " WHERE o.order_date BETWEEN ? AND ? ORDER BY o.id DESC";
        return query(sql, ps -> { ps.setString(1, fromDate); ps.setString(2, toDate); });
    }

    // ─── Chi tiết đơn hàng ────────────────────────────────────────────────────

    /** Lấy danh sách sản phẩm trong một đơn hàng */
    public List<DetailLine> getDetails(int orderId) {
        List<DetailLine> list = new ArrayList<>();
        String sql = """
                SELECT
                    p.name          AS product_name,
                    p.product_code,
                    od.quantity,
                    od.unit_price,
                    od.quantity * od.unit_price AS line_total
                FROM order_details od
                JOIN products p ON od.product_id = p.id
                WHERE od.order_id = ?
                ORDER BY od.id
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DetailLine(
                            rs.getString("product_name"),
                            rs.getString("product_code"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("line_total")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Hủy đơn hàng (hoàn tồn kho) ─────────────────────────────────────────

    public boolean cancelOrder(int orderId) {
        String restoreStock = """
                UPDATE products p
                JOIN order_details od ON p.id = od.product_id
                SET p.stock = p.stock + od.quantity
                WHERE od.order_id = ?
                """;
        String updateStatus = "UPDATE orders SET status = 'Da huy' WHERE id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(restoreStock)) {
                    ps.setInt(1, orderId); ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(updateStatus)) {
                    ps.setInt(1, orderId); ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Thống kê nhanh ───────────────────────────────────────────────────────

    public long countToday() {
        return queryLong(
            "SELECT COUNT(*) FROM orders WHERE order_date = CURDATE() AND status != 'Da huy'");
    }

    public double revenueToday() {
        return queryDouble(
            "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE order_date = CURDATE() AND status != 'Da huy'");
    }

    public double revenueTotal() {
        return queryDouble(
            "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE status != 'Da huy'");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    @FunctionalInterface
    interface ParamSetter { void set(PreparedStatement ps) throws SQLException; }

    private List<OrderRow> query(String sql, ParamSetter setter) {
        List<OrderRow> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OrderRow(
                            rs.getInt("id"),
                            rs.getString("order_code"),
                            rs.getString("customer_name"),
                            rs.getString("customer_phone"),
                            rs.getString("order_date"),
                            rs.getString("payment_method"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"),
                            rs.getInt("item_count")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private long queryLong(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private double queryDouble(String sql) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }
}
