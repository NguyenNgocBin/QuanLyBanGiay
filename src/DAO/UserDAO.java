package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBConnection;
import models.User;

// Cung cấp các phương thức thao tác với bảng User trong database
public class UserDAO {
    /**
     * Hàm thêm một người dùng mới vào cơ sở dữ liệu.
     * Hàm này được gọi khi người dùng thực hiện Đăng ký tài khoản.
     * 
     * @param user Đối tượng User chứa thông tin (name, username, email, password đã được băm)
     * @return true nếu thêm thành công, false nếu có lỗi (ví dụ: trùng username/email)
     */
    public boolean insertUser(User user) {
        String query = "INSERT INTO users (name, username, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, user.getName());
            pst.setString(2, user.getUserName());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getPassword());
            pst.setString(5, user.getRole() != null ? user.getRole() : "STAFF");

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hàm kiểm tra thông tin Đăng nhập của người dùng.
     * Hàm này đối chiếu email và mật khẩu (đã băm) xem có khớp trong Database hay không.
     * 
     * @param email Email người dùng nhập vào
     * @param password Mật khẩu đã được băm bằng SHA-256
     * @return Đối tượng User nếu thông tin chính xác, trả về null nếu sai email hoặc mật khẩu
     */
    public User login(String email, String password) {
        String Query = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(Query)) {
            pst.setString(1, email);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("role"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Hàm kiểm tra xem email đã tồn tại trong database chưa (dùng để tránh trùng lặp khi đăng ký)
    public boolean checkEmailExists(String email){
        String querty = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pst = connection.prepareStatement(querty)) {
            pst.setString(1, email);
            // Nếu có kết quả trả về, nghĩa là email đã tồn tại
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // Hàm cập nhật mật khẩu mới khi quên mật khẩu
    public boolean updatePassword(String email, String newHashedPassword) {
        String query = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, newHashedPassword);
            pst.setString(2, email);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
