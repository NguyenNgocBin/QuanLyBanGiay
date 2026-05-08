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
    private TextField txtUserName;
    @FXML
    private PasswordField txtPassWord;
    @FXML
    private TextField txtEmail;
    @FXML
    private PasswordField txtConfirmPass;

    private UserDAO userDAO = new UserDAO(); // Khởi tạo DAO

    /**
     * Hàm xử lý sự kiện khi người dùng bấm nút "ĐĂNG KÝ".
     * Chức năng: 
     * 1. Thu thập dữ liệu từ các ô nhập liệu.
     * 2. Kiểm tra tính hợp lệ của dữ liệu (bỏ trống, định dạng email, mật khẩu khớp nhau, độ dài mật khẩu).
     * 3. Băm mật khẩu để bảo mật.
     * 4. Gọi DAO để lưu thông tin xuống Database.
     * 5. Xóa form nếu thành công, hiển thị lỗi nếu thất bại.
     * 
     * @param event Sự kiện click chuột từ JavaFX
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // 1. Thu thập dữ liệu từ UI
        String username = txtUserName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassWord.getText();
        String confirmPass = txtConfirmPass.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            baoloi.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) { // Chỉ cho phép email kết thúc bằng @gmail.com
            baoloi.setText("Vui lòng nhập đúng định dạng email *****@gmail.com!");
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
        // Lưu ý: dùng username cho trường name luôn vì đã bỏ trường name
        User newUser = new User(0, username, username, email, hashedPassword);
        boolean success = userDAO.insertUser(newUser);
        if (success) {
            baoloi.setFill(Color.GREEN);
            baoloi.setText("Đăng ký thành công!");
            quayVeTrangLogin(event);



            // Clear form
            txtUserName.clear();
            txtEmail.clear();
            txtPassWord.clear();
            txtConfirmPass.clear(); 
        } else {
            baoloi.setFill(Color.RED);
            baoloi.setText("Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác!");

            // Clear form
            txtUserName.clear();
            txtEmail.clear();
            txtPassWord.clear();
            txtConfirmPass.clear();
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

    /**
     * Hàm hỗ trợ băm (hash) mật khẩu bằng thuật toán SHA-256.
     * Chức năng: Chuyển đổi mật khẩu dạng văn bản thuần túy (plaintext) thành một chuỗi mã hóa
     * không thể dịch ngược, giúp bảo vệ mật khẩu người dùng trong cơ sở dữ liệu.
     * @param password Mật khẩu gốc người dùng nhập
     * @return Chuỗi mật khẩu đã được mã hóa Hex, hoặc mật khẩu gốc nếu thuật toán bị lỗi
     */
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


    // Hàm chuyển cảnh về Đăng nhập
    public void quayVeTrangLogin(ActionEvent event) {
        try {
            // Sửa lại đường dẫn tới file Đăng nhập của bạn (hello-view.fxml hoặc login-view.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent root = loader.load(); // chay  giao dien
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}