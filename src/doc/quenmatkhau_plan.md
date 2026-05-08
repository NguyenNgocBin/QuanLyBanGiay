# Kế hoạch triển khai Chức năng Quên Mật Khẩu

Để hoàn thiện chức năng Quên mật khẩu với quy trình: *Nhập Email -> Nhận mã xác nhận qua Email -> Đổi mật khẩu mới*, chúng ta cần thực hiện các công việc sau:

## User Review Required

> [!IMPORTANT]
> **Cấu hình Email Gửi Mã (SMTP):** Để phần mềm có thể tự động gửi email chứa mã xác nhận cho người dùng, bạn cần cung cấp một tài khoản Gmail dùng làm "Tổng đài". 
> Do tính bảo mật, bạn sẽ cần tạo **Mật khẩu ứng dụng (App Password)** từ tài khoản Gmail đó thay vì dùng mật khẩu gốc.
> Tạm thời trong code, mình sẽ để một biến cấu hình `SENDER_EMAIL` và `SENDER_PASSWORD` trống. Bạn cần điền email và App password của bạn vào biến này sau khi code hoàn thành. Bạn có đồng ý với giải pháp này không?

## Proposed Changes

### 1. Database Access (DAO)
#### [MODIFY] `src/DAO/UserDAO.java`
- Thêm hàm `checkEmailExists(String email)`: Kiểm tra xem email nhập vào có tồn tại trong cơ sở dữ liệu hay không.
- Thêm hàm `updatePassword(String email, String newHashedPassword)`: Cập nhật mật khẩu mới (đã được băm SHA-256) cho tài khoản dựa trên email.

### 2. Giao diện (UI)
#### [NEW] `src/view/ForgotPassword.fxml`
- Tạo một màn hình mới (có thể dùng lại ảnh nền `unnamed.png` cho đồng bộ).
- Xây dựng một "Card" (Thẻ) ở giữa chứa 3 khối (VBox) tương ứng với 3 bước, được ẩn/hiện động:
  - **Bước 1 (Nhập Email)**: Ô nhập Email và nút "Gửi mã xác nhận".
  - **Bước 2 (Nhập Mã)**: Ô nhập mã 6 số và nút "Xác nhận".
  - **Bước 3 (Đổi Mật khẩu)**: Ô nhập mật khẩu mới, ô nhập lại mật khẩu và nút "Đổi mật khẩu".

### 3. Logic xử lý (Controller)
#### [NEW] `src/controller/ForgotPasswordController.java`
- Điều khiển việc ẩn/hiện 3 bước trong `ForgotPassword.fxml`.
- Chứa logic gửi Email bằng thư viện `javax.mail` (đã có sẵn trong `pom.xml` của bạn).
- Chứa logic tạo ngẫu nhiên mã xác nhận 6 chữ số (`Random`).
- Chứa logic mã hóa mật khẩu mới (dùng lại hàm băm SHA-256) và gọi `UserDAO` để lưu.
- Xử lý chuyển hướng người dùng về lại trang `Login.fxml` khi đổi mật khẩu thành công.

#### [MODIFY] `src/controller/LoginController.java`
- Cập nhật hàm `quenMatKhau(MouseEvent event)` để chuyển cảnh (load scene) sang màn hình `ForgotPassword.fxml` thay vì chỉ in ra console như hiện tại.

## Verification Plan
1. Chạy thử phần mềm, tại trang Đăng nhập bấm vào "Quên mật khẩu?".
2. Màn hình phải chuyển sang trang Nhập Email.
3. Nhập một email không tồn tại -> App phải báo lỗi "Email không tồn tại".
4. Nhập email đúng trong DB -> App báo thành công và tự động hiển thị ô Nhập Mã. Đồng thời, kiểm tra hòm thư thật xem có nhận được mã 6 số không.
5. Nhập mã sai -> App báo lỗi mã không hợp lệ.
6. Nhập mã đúng -> App tự động hiển thị ô Nhập Mật Khẩu Mới.
7. Đổi mật khẩu thành công -> App quay về trang Đăng nhập. Dùng mật khẩu mới để đăng nhập thử.
