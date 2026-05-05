package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/testlogin";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "xuanduc2007";

    public static Connection getConnection() throws SQLException {
        return (Connection) DriverManager.getConnection(DATABASE_URL, USERNAME, PASSWORD);
    }
}
