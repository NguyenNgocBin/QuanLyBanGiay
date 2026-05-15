package DAO;

import database.DBConnection;
import models.ImportDetail;
import models.ImportOrder;

import java.sql.*;
import java.util.List;

public class ImportDAO {

    public boolean createImportOrder(ImportOrder order, List<ImportDetail> details) {
        String insertOrderSql = "INSERT INTO import_orders (supplier_id, total_amount, import_date, status) VALUES (?, ?, ?, ?)";
        String insertDetailSql = "INSERT INTO import_details (import_id, product_id, quantity, import_price) VALUES (?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET stock = stock + ? WHERE id = ?";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false); // Start transaction

            // 1. Insert into import_orders
            int importId = 0;
            try (PreparedStatement psOrder = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, order.getSupplierId());
                psOrder.setDouble(2, order.getTotalAmount());
                psOrder.setString(3, order.getImportDate());
                psOrder.setString(4, "Hoàn thành");
                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        importId = rs.getInt(1);
                    } else {
                        throw new SQLException("Lỗi tạo phiếu nhập, không lấy được ID.");
                    }
                }
            }

            // 2. Insert details and update stock
            try (PreparedStatement psDetail = con.prepareStatement(insertDetailSql);
                 PreparedStatement psStock = con.prepareStatement(updateStockSql)) {
                
                for (ImportDetail detail : details) {
                    // Insert Detail
                    psDetail.setInt(1, importId);
                    psDetail.setInt(2, detail.getProductId());
                    psDetail.setInt(3, detail.getQuantity());
                    psDetail.setDouble(4, detail.getImportPrice());
                    psDetail.addBatch();

                    // Update Stock
                    psStock.setInt(1, detail.getQuantity());
                    psStock.setInt(2, detail.getProductId());
                    psStock.addBatch();
                }

                psDetail.executeBatch();
                psStock.executeBatch();
            }

            con.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback(); // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
