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
    private TextField txtEmail;

    @FXML
    private Text thongbaoloi;

    /**
     * Hàm xử lý sự kiện khi người dùng bấm vào nút/văn bản "Đăng ký ngay".
     * Chức năng: Chuyển đổi giao diện từ màn hình Đăng nhập sang màn hình Đăng ký tài khoản.
     * 
     * @param event Sự kiện click chuột từ JavaFX
     */
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
    void quenMatKhau(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/ForgotPassword.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hàm xử lý sự kiện khi người dùng bấm nút "Đăng nhập".
     * Chức năng: 
     * 1. Thu thập email và mật khẩu từ form.
     * 2. Kiểm tra tính hợp lệ (bỏ trống, định dạng email).
     * 3. Băm mật khẩu và đối chiếu với cơ sở dữ liệu qua UserDAO.
     * 4. Nếu thành công, chuyển sang màn hình chính (Main.fxml). Nếu thất bại, hiển thị thông báo lỗi.
     * 
     * @param event Sự kiện click chuột từ JavaFX
     */
    @FXML
    void chuyenSangTrangChu(ActionEvent event) {
        // kiem tra dang nhap
        String email = txtEmail.getText().trim();
        String pass = passWord.getText();
        // kiem tra null
        if (email.isEmpty() || pass.isEmpty()) {
            thongbaoloi.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            thongbaoloi.setText("Vui lòng nhập đúng định dạng email *****@gmail.com!");
            return;
        }

        String hashedPass = hashPassword(pass);


        // 1. Gọi hàm login từ DAO
        UserDAO userDAO = new UserDAO();
        User userDangNhap = userDAO.login(email, hashedPass);

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
            thongbaoloi.setText("Email hoặc mật khẩu không đúng!");
        }
    }

    /**
     * Hàm hỗ trợ băm (hash) mật khẩu bằng thuật toán SHA-256.
     * Chức năng: Chuyển đổi mật khẩu dạng văn bản thuần túy thành chuỗi mã hóa
     * để đối chiếu với mật khẩu đã lưu trong Database.
     * 
     * @param password Mật khẩu gốc người dùng nhập
     * @return Chuỗi mật khẩu đã được mã hóa Hex
     */
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
