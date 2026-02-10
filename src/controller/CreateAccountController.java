package controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import DAO.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.User;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class CreateAccountController {
    @FXML
    private Text baoloi;
    @FXML
    private TextField txtName;
    @FXML
    private PasswordField txtPassWord;
    @FXML
    private TextField txtUserName;
    @FXML
    private PasswordField txtConfirmPass;

    private UserDAO userDAO = new UserDAO(); // Khởi tạo DAO

    @FXML
    private void handleRegister(ActionEvent event) {
        // 1. Thu thập dữ liệu từ UI
        String name = txtName.getText().trim();
        String username = txtUserName.getText().trim();
        String password = txtPassWord.getText();
        String confirmPass = txtConfirmPass.getText();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            baoloi.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        if (!password.equals(confirmPass)) {
            baoloi.setText("Mật khẩu xác nhận không khớp!");
            return;
        }
        if (!Pattern.matches("^[a-zA-Z0-9_]+$", username)) {
            baoloi.setText("Tên đăng nhập không được chứa ký tự đặc biệt hoặc khoảng trắng!");
            return;
        }
        if (password.length() < 6) {
            baoloi.setText("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }
        // (HASHING) BĂM MẬT KHẨU
        String hashedPassword = hashPassword(password);
        // Tạo đối tượng User và gọi DAO để lưu
        // Lúc này lưu hashedPassword chứ không phải password thường
        User newUser = new User(0, name, username, hashedPassword);
        // CHECK LẠI BÊN DAO XỬ LÝ TRY-CATCH VỀ TRÙNG USERNAME
        boolean success = userDAO.insertUser(newUser);
        if (success) {
            baoloi.setFill(Color.GREEN);
            baoloi.setText("Đăng ký thành công!");

            // Clear form
            txtName.clear();
            txtUserName.clear();
            txtPassWord.clear();
            txtConfirmPass.clear(); // Clear cả ô confirm
        } else {
            baoloi.setText("Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác!");
        }
    }

    @FXML
    void chuyenSangDangNhap(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // HÀM HỖ TRỢ BĂM MẬT KHẨU
    // Lưu ý *****
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());

            // Chuyển byte sang hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback nếu lỗi (hiếm khi xảy ra)
        }
    }
}