package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbDiagnostics {
    public static void main(String[] args) {
        System.out.println("=== DIAGNOSTIC START ===");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/testlogin", "root", "xuanduc2007");
                 Statement stmt = con.createStatement()) {

                System.out.println("\n--- ORDERS ---");
                try (ResultSet rs = stmt.executeQuery("SELECT id, total_amount, order_date, status FROM orders")) {
                    while (rs.next()) {
                        System.out.printf("Order ID: %d | Total: %.2f | Date: %s | Status: %s%n",
                                rs.getInt("id"), rs.getDouble("total_amount"), rs.getDate("order_date"), rs.getString("status"));
                    }
                }

                System.out.println("\n--- ORDER DETAILS ---");
                try (ResultSet rs = stmt.executeQuery("SELECT od.order_id, od.product_id, od.quantity, od.unit_price FROM order_details od")) {
                    while (rs.next()) {
                        System.out.printf("Order ID: %d | Product ID: %d | Qty: %d | Price: %.2f%n",
                                rs.getInt("order_id"), rs.getInt("product_id"),
                                rs.getInt("quantity"), rs.getDouble("unit_price"));
                    }
                }

                System.out.println("\n--- IMPORT DETAILS ---");
                try (ResultSet rs = stmt.executeQuery("SELECT id.import_id, id.product_id, id.quantity, id.import_price FROM import_details id")) {
                    while (rs.next()) {
                        System.out.printf("Import ID: %d | Product ID: %d | Qty: %d | Import Price: %.2f%n",
                                rs.getInt("import_id"), rs.getInt("product_id"),
                                rs.getInt("quantity"), rs.getDouble("import_price"));
                    }
                }

                System.out.println("\n--- USERS ---");
                try (ResultSet rs = stmt.executeQuery("SELECT id, name, username, role, last_login, session_revenue FROM users")) {
                    while (rs.next()) {
                        System.out.printf("User: %s | Role: %s | Last Login: %s | Session Rev: %.2f%n",
                                rs.getString("username"), rs.getString("role"), rs.getString("last_login"), rs.getDouble("session_revenue"));
                    }
                }

                System.out.println("\n--- PRODUCTS ---");
                try (ResultSet rs = stmt.executeQuery("SELECT id, name, price, stock FROM products")) {
                    while (rs.next()) {
                        System.out.printf("Product ID: %d | Name: %s | Price: %.2f | Stock: %d%n",
                                rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n=== DIAGNOSTIC END ===");
    }
}
