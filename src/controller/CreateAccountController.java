package controller;

import java.io.IOException;
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

public class CreateAccountController {
    @FXML
    private Text baoloi;
    @FXML
    private TextField txtName;
    @FXML
    private PasswordField txtPassWord;
    @FXML
    private TextField txtUserName;

    private UserDAO userDAO = new UserDAO(); // Khởi tạo DAO

    @FXML
    private void handleRegister(ActionEvent event) {
        // 1. Thu thập dữ liệu từ UI
        String name = txtName.getText().trim();
        String username = txtUserName.getText().trim();
        String password = txtPassWord.getText();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            baoloi.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (username.contains(" ")) {
            baoloi.setText("Tên đăng nhập không được chứa khoảng trắng!");
            return;
        }
        // 3. Tạo đối tượng User và gọi DAO để lưu
        User newUser = new User(0, name, username, password);
        boolean success = userDAO.insertUser(newUser);

        if (success) {
            baoloi.setText("Tạo tài khoản thành công!");

            txtName.clear();
            txtUserName.clear();
            txtPassWord.clear();

        } else {
            baoloi.setText("Tài khoản đã tồn tại hoặc lỗi hệ thống!");
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
}