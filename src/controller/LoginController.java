package controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import DAO.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import models.User;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class LoginController {

    @FXML
    private Button buttonLogin;

    @FXML
    private PasswordField passWord;

    @FXML
    private TextField userName;

    @FXML
    private Text thongbaoloi;

    @FXML
    void chuyenSangDangKy(MouseEvent event) {
        try {

            Parent root = FXMLLoader.load(getClass().getResource("/view/createAccount.fxml"));

            // Lấy stage và set scene gộp dòng cho ngắn
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace(); // In lỗi nếu không tìm thấy file
        }
    }

    @FXML
    void chuyenSangTrangChu(ActionEvent event) {
        // kiem tra dang nhap
        String user = userName.getText();
        String pass = passWord.getText();
        // kiem tra null
        if (user.isEmpty() || pass.isEmpty()) {
            thongbaoloi.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        String hashedPass = hashPassword(pass);


        // 1. Gọi hàm login từ DAO
        UserDAO userDAO = new UserDAO();
        User userDangNhap = userDAO.login(user, hashedPass);

        // 2. Kiểm tra kết quả
        if (userDangNhap != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Main.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            thongbaoloi.setText("Tên đăng nhập hoặc mật khẩu không đúng!");
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());

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
            return password;
        }
    }

}
