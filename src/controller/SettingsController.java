package controller;

import DAO.UserDAO;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import models.User;
import utils.SessionManager;
import utils.ThemeManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SettingsController {

    @FXML private Button btnLightTheme;
    @FXML private Button btnDarkTheme;
    
    @FXML private TextField txtUsername;
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private Button btnSaveProfile;
    @FXML private Label lblProfileMessage;
    
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnChangePassword;
    @FXML private Label lblPasswordMessage;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        // Tải thông tin tài khoản đang đăng nhập
        if (SessionManager.isLoggedIn()) {
            User user = SessionManager.getCurrentUser();
            txtUsername.setText(user.getUserName());
            txtName.setText(user.getName());
            txtEmail.setText(user.getEmail());
        }
        
        updateThemeButtonsStyle();
    }

    @FXML
    private void handleLightTheme(ActionEvent event) {
        if (ThemeManager.isDarkMode()) {
            ThemeManager.setDarkMode(false);
            ThemeManager.applyTheme(btnLightTheme.getScene());
            playThemeTransition();
            updateThemeButtonsStyle();
        }
    }

    @FXML
    private void handleDarkTheme(ActionEvent event) {
        if (!ThemeManager.isDarkMode()) {
            ThemeManager.setDarkMode(true);
            ThemeManager.applyTheme(btnDarkTheme.getScene());
            playThemeTransition();
            updateThemeButtonsStyle();
        }
    }


    private void updateThemeButtonsStyle() {
        if (ThemeManager.isDarkMode()) {
            btnDarkTheme.setStyle("-fx-background-color: #6366F1; -fx-text-fill: #FFFFFF; -fx-border-color: #A855F7; -fx-border-width: 2; -fx-border-radius: 4; -fx-font-weight: bold;");
            btnLightTheme.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #94A3B8; -fx-border-color: transparent; -fx-font-weight: normal;");
        } else {
            btnLightTheme.setStyle("-fx-background-color: #6366F1; -fx-text-fill: #FFFFFF; -fx-border-color: #A855F7; -fx-border-width: 2; -fx-border-radius: 4; -fx-font-weight: bold;");
            btnDarkTheme.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #475569; -fx-border-color: transparent; -fx-font-weight: normal;");
        }
    }

    private void playThemeTransition() {
        try {
            var scene = btnLightTheme.getScene();
            if (scene != null && scene.getRoot() != null) {
                FadeTransition fade = new FadeTransition(Duration.millis(350), scene.getRoot());
                fade.setFromValue(0.85);
                fade.setToValue(1.0);
                fade.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        if (!SessionManager.isLoggedIn()) {
            lblProfileMessage.setTextFill(Color.RED);
            lblProfileMessage.setText("Chưa đăng nhập hệ thống.");
            return;
        }

        User user = SessionManager.getCurrentUser();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            lblProfileMessage.setTextFill(Color.RED);
            lblProfileMessage.setText("Họ tên và Email không được để trống!");
            return;
        }

        // Kiểm tra định dạng email
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            lblProfileMessage.setTextFill(Color.RED);
            lblProfileMessage.setText("Định dạng Email không hợp lệ!");
            return;
        }

        // Kiểm tra trùng lặp email với người khác
        if (userDAO.checkEmailExistsForOther(email, user.getId())) {
            lblProfileMessage.setTextFill(Color.RED);
            lblProfileMessage.setText("Email này đã có người sử dụng!");
            return;
        }

        // Thực hiện cập nhật CSDL
        if (userDAO.updateProfile(user.getId(), name, email)) {
            user.setName(name);
            user.setEmail(email);
            SessionManager.setCurrentUser(user);
            
            // Cập nhật lại thông tin hiển thị trên Topbar
            if (MainController.getInstance() != null) {
                MainController.getInstance().updateSessionInfo();
            }

            lblProfileMessage.setTextFill(Color.GREEN);
            lblProfileMessage.setText("Cập nhật thông tin thành công!");
        } else {
            lblProfileMessage.setTextFill(Color.RED);
            lblProfileMessage.setText("Cập nhật thất bại. Lỗi kết nối CSDL.");
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        if (!SessionManager.isLoggedIn()) {
            lblPasswordMessage.setTextFill(Color.RED);
            lblPasswordMessage.setText("Chưa đăng nhập hệ thống.");
            return;
        }

        User user = SessionManager.getCurrentUser();
        String oldPassword = txtOldPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            lblPasswordMessage.setTextFill(Color.RED);
            lblPasswordMessage.setText("Vui lòng nhập đầy đủ các trường mật khẩu!");
            return;
        }

        if (newPassword.length() < 6) {
            lblPasswordMessage.setTextFill(Color.RED);
            lblPasswordMessage.setText("Mật khẩu mới phải có độ dài ít nhất 6 ký tự!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            lblPasswordMessage.setTextFill(Color.RED);
            lblPasswordMessage.setText("Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return;
        }

        // Xác thực mật khẩu cũ
        String oldPasswordHashed = hashPassword(oldPassword);
        if (!oldPasswordHashed.equals(user.getPassword())) {
            lblPasswordMessage.setTextFill(Color.RED);
            lblPasswordMessage.setText("Mật khẩu hiện tại không chính xác!");
            return;
        }

        // Lưu mật khẩu mới đã băm
        String newPasswordHashed = hashPassword(newPassword);
        if (userDAO.updatePasswordById(user.getId(), newPasswordHashed)) {
            user.setPassword(newPasswordHashed);
            SessionManager.setCurrentUser(user);

            txtOldPassword.clear();
            txtNewPassword.clear();
            txtConfirmPassword.clear();

            lblPasswordMessage.setTextFill(Color.GREEN);
            lblPasswordMessage.setText("Đổi mật khẩu thành công!");
        } else {
            lblPasswordMessage.setTextFill(Color.RED);
            lblPasswordMessage.setText("Đổi mật khẩu thất bại. Lỗi kết nối CSDL.");
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
