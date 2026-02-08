package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;
import models.User;

// Cung cấp các phương thức thao tác với bảng User trong database
public class UserDAO {
    // Thêm người dùng mới vào
    public boolean insertUser(User user) {
        String query = "INSERT INTO users (name, username, password) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, user.getName());
            pst.setString(2, user.getUserName());
            pst.setString(3, user.getPassword());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Đăng Nhập
    public User login(String username, String password) {
        String Query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(Query)) {
            pst.setString(1, username);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("password"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
