package controller;

import DAO.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class ForgotPasswordController {

    // --- CÁC KHỐI GIAO DIỆN CHÍNH ---
    @FXML
    private VBox step1Box; // Nhập Email
    @FXML
    private VBox step2Box; // Nhập Mã Code
    @FXML
    private VBox step3Box; // Nhập Mật khẩu mới

    // --- BƯỚC 1 ---
    @FXML
    private TextField txtEmail;
    @FXML
    private Button btnSendCode;

    // --- BƯỚC 2 ---
    @FXML
    private TextField txtCode;
    @FXML
    private Button btnVerifyCode;

    // --- BƯỚC 3 ---
    @FXML
    private PasswordField txtNewPassword;
    @FXML
    private PasswordField txtConfirmPassword;
    @FXML
    private Button btnResetPassword;

    // --- THÔNG BÁO ---
    @FXML
    private Text txtMessage;

    // Biến lưu trữ tạm
    private String emailCanDoi;                 // Lưu email người dùng vừa nhập
    private String maXacNhanSinhRa;             // Lưu mã xác nhận đã gửi để kiểm tra

    // ========================================================
    // CÁC HÀM XỬ LÝ SỰ KIỆN (BẠN SẼ VIẾT LOGIC VÀO ĐÂY)
    // ========================================================

    UserDAO userDAO = new UserDAO(); // Khởi tạo DAO để gọi hàm kiểm tra email tồn tại

    /**
     *  Xử lý khi người dùng bấm "Gửi mã xác nhận"
     */
    @FXML
    void handleSendCode(ActionEvent event) {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            hienThiLoi("Vui lòng nhập email!");
            return;
        }
        // TODO 1: Kiểm tra email có tồn tại trong hệ thống không
        boolean emailExists = userDAO.checkEmailExists(email);
        if (!emailExists) {
            hienThiLoi("Email không tồn tại trong hệ thống!");
            return;
        }
        
        // TODO 2: Tạo ngẫu nhiên một mã xác nhận 6 số và gán vào biến maXacNhanSinhRa
        Random random = new Random();
        // Sinh ra một số ngẫu nhiên từ 100000 đến 999999
        int code = 100000 + random.nextInt(900000);  // Chuyển số thành chuỗi để dễ so sánh sau này
        maXacNhanSinhRa = String.valueOf(code);//
        
        // (Tạm thời in ra console để bạn dễ test khi chưa làm gửi Email)
        System.out.println("MÃ XÁC NHẬN CỦA BẠN LÀ: " + maXacNhanSinhRa);
        
        // TODO 3: Gửi email bằng JavaMail
        final String fromEmail = "appbangiay.vku@gmail.com"; // <-- EMAIL CỦA app VÀO ĐÂY
        final String password = "ufmp etwf xhwb keak";        // <--  APP PASSWORD

        //================ Cấu hình JavaMail===================
        java.util.Properties props = new java.util.Properties();// Cấu hình cho Gmail SMTP
        props.put("mail.smtp.host", "smtp.gmail.com");// Máy chủ SMTP của Gmail
        props.put("mail.smtp.port", "587");// Cổng TLS
        props.put("mail.smtp.auth", "true");// Yêu cầu xác thực
        props.put("mail.smtp.starttls.enable", "true");// Bật TLS

        // Tạo phiên làm việc với xác thực
        javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() { // Cung cấp thông tin xác thực
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() { // Trả về thông tin đăng nhập của email gửi
                return new javax.mail.PasswordAuthentication(fromEmail, password); // Sử dụng email và mật khẩu đã cung cấp
            }
        });

        try {
            javax.mail.Message message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress(fromEmail));
            message.setRecipients(javax.mail.Message.RecipientType.TO, javax.mail.internet.InternetAddress.parse(email));
            message.setSubject("Mã xác nhận quên mật khẩu - QUẢN LÝ BÁN GIÀY");
            message.setText("Xin chào,\n\nMã xác nhận để lấy lại mật khẩu của bạn là: "
                    + maXacNhanSinhRa +
                    "\n\nVui lòng không chia sẻ mã này cho bất kỳ ai.");

            javax.mail.Transport.send(message);
        } catch (javax.mail.MessagingException e) {
            e.printStackTrace();
            hienThiLoi("Lỗi khi gửi mail. Kiểm tra lại cấu hình Email hoặc Mạng!");
            return;
        }
        // Giả sử thành công:
        emailCanDoi = email;
        chuyenSangBuoc2();
        hienThiThanhCong("Mã xác nhận đã được gửi đến email của bạn!");
    }

    /**
     * BƯỚC 2: Xử lý khi người dùng nhập mã và bấm "Xác nhận mã"
     */
    @FXML
    void handleVerifyCode(ActionEvent event) {
        String code = txtCode.getText().trim();
        if (code.isEmpty()) {
            hienThiLoi("Vui lòng nhập mã xác nhận!");
            return;
        }

        // TODO 4: Kiểm tra code người dùng nhập có khớp với maXacNhanSinhRa không
        if (!code.equals(maXacNhanSinhRa)) {
            hienThiLoi("Mã xác nhận không đúng!");
            return;
        }

        // Giả sử đúng:
        chuyenSangBuoc3();
        hienThiThanhCong("Xác nhận thành công! Vui lòng nhập mật khẩu mới.");
    }

    /**
     * BƯỚC 3: Xử lý khi người dùng nhập mật khẩu mới và bấm "Lưu mật khẩu"
     */
    @FXML
    void handleResetPassword(ActionEvent event) {
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmPassword.getText();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            hienThiLoi("Vui lòng điền đầy đủ mật khẩu!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            hienThiLoi("Mật khẩu không khớp!");
            return;
        }

        // TODO 5: Băm mật khẩu (SHA-256) giống như lúc đăng ký
        String hashedPass = hashPassword(newPass);

        // TODO 6: Viết hàm trong UserDAO để cập nhật mật khẩu mới cho emailCanDoi
        boolean success = userDAO.updatePassword(emailCanDoi, hashedPass);

        if (success) {
            hienThiThanhCong("Đổi mật khẩu thành công!");
            // Trở về trang Đăng nhập
            backToLogin(null);
        } else {
            hienThiLoi("Có lỗi xảy ra khi đổi mật khẩu!");
        }
    }

    /**
     * Hàm hỗ trợ băm (hash) mật khẩu bằng thuật toán SHA-256.
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

    // ========================================================
    // CÁC HÀM HỖ TRỢ GIAO DIỆN (ĐÃ VIẾT SẴN)
    // ========================================================

    // Các hàm này giúp chuyển đổi giữa các bước của quy trình quên mật khẩu bằng cách ẩn/hiện các VBox tương ứng
    private void chuyenSangBuoc2() {
        step1Box.setVisible(false); step1Box.setManaged(false);
        step2Box.setVisible(true);  step2Box.setManaged(true);
        step3Box.setVisible(false); step3Box.setManaged(false);
    }

    private void chuyenSangBuoc3() {
        step1Box.setVisible(false); step1Box.setManaged(false);
        step2Box.setVisible(false); step2Box.setManaged(false);
        step3Box.setVisible(true);  step3Box.setManaged(true);
    }
//
    @FXML
    void backToStep1(MouseEvent event) {
        step1Box.setVisible(true);  step1Box.setManaged(true);
        step2Box.setVisible(false); step2Box.setManaged(false);
        step3Box.setVisible(false); step3Box.setManaged(false);
        txtMessage.setText(""); // Xóa thông báo lỗi
    }

    @FXML
    void backToLogin(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
            Stage stage = (Stage) btnSendCode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hienThiLoi(String msg) {
        txtMessage.setFill(Color.RED);
        txtMessage.setText(msg);
    }

    private void hienThiThanhCong(String msg) {
        txtMessage.setFill(Color.GREEN);
        txtMessage.setText(msg);
    }
}
