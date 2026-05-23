package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/testlogin";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "xuanduc2007";

    static {
        // Tự động nâng cấp cấu trúc cơ sở dữ liệu nếu thiếu cột user_id trong bảng orders
        try (Connection con = DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD)) {
            boolean hasUserId = false;
            try (ResultSet rs = con.getMetaData().getColumns(null, null, "orders", "user_id")) {
                if (rs.next()) {
                    hasUserId = true;
                }
            }
            if (!hasUserId) {
                try (Statement stmt = con.createStatement()) {
                    stmt.execute("ALTER TABLE orders ADD COLUMN user_id INT NULL");
                    stmt.execute("ALTER TABLE orders ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra/nâng cấp cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }
}
