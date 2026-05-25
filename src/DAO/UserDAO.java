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
     * @param user Đối tượng User chứa thông tin (name, username, email, password đã
     *             được băm)
     * @return true nếu thêm thành công, false nếu có lỗi (ví dụ: trùng
     *         username/email)
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
     * Hàm này đối chiếu email và mật khẩu (đã băm) xem có khớp trong Database hay
     * không.
     * 
     * @param email    Email người dùng nhập vào
     * @param password Mật khẩu đã được băm bằng SHA-256
     * @return Đối tượng User nếu thông tin chính xác, trả về null nếu sai email
     *         hoặc mật khẩu
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

    // Hàm kiểm tra xem email đã tồn tại trong database chưa (dùng để tránh trùng
    // lặp khi đăng ký)
    public boolean checkEmailExists(String email) {
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

    // Hàm cập nhật thông tin cá nhân (Họ tên, Email)
    public boolean updateProfile(int userId, String name, String email) {
        String query = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setInt(3, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm cập nhật mật khẩu mới theo userId
    public boolean updatePasswordById(int userId, String newHashedPassword) {
        String query = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, newHashedPassword);
            pst.setInt(2, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kiểm tra xem email có bị trùng với người dùng khác hay không
    public boolean checkEmailExistsForOther(String email, int userId) {
        String query = "SELECT * FROM users WHERE email = ? AND id != ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Hàm lấy danh sách tất cả các nhân viên (cả ADMIN và STAFF)
    public java.util.List<User> getAllStaff() {
        java.util.List<User> list = new java.util.ArrayList<>();
        String query = "SELECT *, DATE_FORMAT(last_login, '%d/%m/%Y %H:%i') AS format_last_login FROM users ORDER BY id DESC";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"));
                user.setLastLogin(rs.getString("format_last_login"));
                user.setSessionRevenue(rs.getDouble("session_revenue"));
                list.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateLoginSession(int userId, java.time.LocalDateTime loginTime) {
        String query = "UPDATE users SET last_login = ?, session_revenue = 0.0 WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setTimestamp(1, java.sql.Timestamp.valueOf(loginTime));
            pst.setInt(2, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addSessionRevenue(int userId, double amount) {
        String query = "UPDATE users SET session_revenue = session_revenue + ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setDouble(1, amount);
            pst.setInt(2, userId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm cập nhật quyền (role) của người dùng
    public boolean updateUserRole(int id, String newRole) {
        String query = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, newRole);
            pst.setInt(2, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm xóa tài khoản nhân viên/quản lý
    public boolean deleteUser(int id) {
        String query = "DELETE FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
                PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}

