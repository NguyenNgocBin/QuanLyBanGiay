package DAO;

import database.DBConnection;
import models.ImportOrder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ImportHistoryDAO {

    public static class ImportDetailRow {
        private final String productName;
        private final int quantity;
        private final double importPrice;
        private final double totalPrice;

        public ImportDetailRow(String productName, int quantity, double importPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.importPrice = importPrice;
            this.totalPrice = quantity * importPrice;
        }

        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getImportPrice() { return importPrice; }
        public double getTotalPrice() { return totalPrice; }
    }

    public List<ImportOrder> getAllImports() {
        List<ImportOrder> imports = new ArrayList<>();
        String sql = """
                SELECT io.id, io.supplier_id, io.total_amount, io.import_date, io.status, s.name AS supplier_name, SUM(COALESCE(id.quantity, 0)) AS total_items 
                FROM import_orders io 
                JOIN suppliers s ON io.supplier_id = s.id 
                LEFT JOIN import_details id ON io.id = id.import_id 
                GROUP BY io.id, io.supplier_id, io.total_amount, io.import_date, io.status, s.name 
                ORDER BY io.id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                imports.add(new ImportOrder(
                        rs.getInt("id"),
                        rs.getInt("supplier_id"),
                        rs.getDouble("total_amount"),
                        rs.getString("import_date"),
                        rs.getString("status"),
                        rs.getString("supplier_name"),
                        rs.getInt("total_items")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return imports;
    }

    public List<ImportOrder> searchImports(String keyword) {
        List<ImportOrder> imports = new ArrayList<>();
        String sql = """
                SELECT io.id, io.supplier_id, io.total_amount, io.import_date, io.status, s.name AS supplier_name, SUM(COALESCE(id.quantity, 0)) AS total_items 
                FROM import_orders io 
                JOIN suppliers s ON io.supplier_id = s.id 
                LEFT JOIN import_details id ON io.id = id.import_id 
                WHERE CAST(io.id AS CHAR) LIKE ? OR LOWER(s.name) LIKE ?
                GROUP BY io.id, io.supplier_id, io.total_amount, io.import_date, io.status, s.name 
                ORDER BY io.id DESC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pattern = "%" + keyword.toLowerCase().trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    imports.add(new ImportOrder(
                            rs.getInt("id"),
                            rs.getInt("supplier_id"),
                            rs.getDouble("total_amount"),
                            rs.getString("import_date"),
                            rs.getString("status"),
                            rs.getString("supplier_name"),
                            rs.getInt("total_items")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return imports;
    }

    public List<ImportDetailRow> getImportDetails(int importId) {
        List<ImportDetailRow> details = new ArrayList<>();
        String sql = """
                SELECT p.name AS product_name, id.quantity, id.import_price 
                FROM import_details id 
                JOIN products p ON id.product_id = p.id 
                WHERE id.import_id = ?
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, importId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(new ImportDetailRow(
                            rs.getString("product_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("import_price")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
}
