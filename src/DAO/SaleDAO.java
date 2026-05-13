package DAO;

import database.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

// DUNG DE XU LY THANH TOAN VA QUAN LY DON HANG
public class SaleDAO {

    /**
     * Thực hiện checkout: kiểm tra tồn kho, tạo đơn hàng, cập nhật chi tiết & tồn kho.
     *
     * @param lines         danh sách sản phẩm trong giỏ
     * @param totalAmount   tổng tiền thanh toán
     * @param paymentMethod phương thức thanh toán
     * @param customerId    ID khách hàng (0 = khách lẻ / không có KH)
     */
    public CheckoutResult checkout(List<CheckoutLine> lines, double totalAmount,
                                   String paymentMethod, int customerId) {
        if (lines == null || lines.isEmpty()) {
            return CheckoutResult.failed("Gio hang dang trong.");
        }

        String insertOrderSql = """
                INSERT INTO orders (customer_id, total_amount, order_date, status)
                VALUES (?, ?, ?, ?)
                """;
        String insertDetailSql = """
                INSERT INTO order_details (order_id, product_id, quantity, unit_price)
                VALUES (?, ?, ?, ?)
                """;
        // Khóa dòng sản phẩm để tránh tranh chấp khi cập nhật tồn kho
        String lockProductSql = "SELECT stock FROM products WHERE id = ? FOR UPDATE";
        // Cập nhật tồn kho, kiểm tra lại để tránh stock bị thay đổi bởi giao dịch khác
        String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // Kiểm tra tồn kho từng sản phẩm
                for (CheckoutLine line : lines) {
                    try (PreparedStatement statement = connection.prepareStatement(lockProductSql)) {
                        statement.setInt(1, line.productId());
                        try (ResultSet resultSet = statement.executeQuery()) {
                            if (!resultSet.next()) {
                                connection.rollback();
                                return CheckoutResult.failed("San pham khong ton tai: " + line.productName());
                            }
                            int stock = resultSet.getInt("stock");
                            if (stock < line.quantity()) {
                                connection.rollback();
                                return CheckoutResult.failed("Khong du ton kho cho " + line.productName()
                                        + ". Con lai: " + stock);
                            }
                        }
                    }
                }

                // Tạo đơn hàng
                int orderId;
                try (PreparedStatement statement = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                    if (customerId > 0) {
                        statement.setInt(1, customerId);
                    } else {
                        statement.setNull(1, Types.INTEGER);
                    }
                    statement.setDouble(2, totalAmount);
                    statement.setDate(3, Date.valueOf(LocalDate.now()));
                    statement.setString(4, "Da thanh toan - " + paymentMethod);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            connection.rollback();
                            return CheckoutResult.failed("Khong tao duoc hoa don.");
                        }
                        orderId = keys.getInt(1);
                    }
                }

                // Chèn chi tiết đơn hàng & cập nhật tồn kho trong cùng 1 batch
                try (PreparedStatement detailStatement = connection.prepareStatement(insertDetailSql);
                     PreparedStatement stockStatement = connection.prepareStatement(updateStockSql)) {

                    for (CheckoutLine line : lines) {
                        detailStatement.setInt(1, orderId);
                        detailStatement.setInt(2, line.productId());
                        detailStatement.setInt(3, line.quantity());
                        detailStatement.setDouble(4, line.unitPrice());
                        detailStatement.addBatch();

                        stockStatement.setInt(1, line.quantity());
                        stockStatement.setInt(2, line.productId());
                        stockStatement.setInt(3, line.quantity());
                        stockStatement.addBatch();
                    }

                    detailStatement.executeBatch();
                    int[] stockUpdates = stockStatement.executeBatch();
                    for (int updated : stockUpdates) {
                        if (updated <= 0) {
                            connection.rollback();
                            return CheckoutResult.failed("Cap nhat ton kho that bai.");
                        }
                    }
                }

                connection.commit();
                return CheckoutResult.success(orderId);

            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                return CheckoutResult.failed("Thanh toan that bai. Vui long thu lai.");
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return CheckoutResult.failed("Khong ket noi duoc co so du lieu.");
        }
    }

    public record CheckoutLine(int productId, String productName, int quantity, double unitPrice) {
    }

    public static class CheckoutResult {
        private final boolean success;
        private final int orderId;
        private final String message;

        private CheckoutResult(boolean success, int orderId, String message) {
            this.success = success;
            this.orderId = orderId;
            this.message = message;
        }

        public static CheckoutResult success(int orderId) {
            return new CheckoutResult(true, orderId, "Thanh toan thanh cong.");
        }

        public static CheckoutResult failed(String message) {
            return new CheckoutResult(false, 0, message);
        }

        public boolean isSuccess() { return success; }
        public int getOrderId() { return orderId; }
        public String getMessage() { return message; }
    }
}
