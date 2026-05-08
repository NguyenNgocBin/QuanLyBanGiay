# Cập nhật luồng Đăng ký và Đăng nhập sử dụng Email

Để thực hiện yêu cầu của bạn (Đăng ký gồm: Tên đăng nhập, Email, Mật khẩu; Đăng nhập bằng Email và Mật khẩu; Kiểm tra định dạng đuôi `@gmail.com`), chúng ta cần thay đổi toàn diện từ Giao diện (UI), Code xử lý (Controller/DAO) cho đến Cơ sở dữ liệu (Database). 

Dưới đây là kế hoạch chi tiết:

## User Review Required

> [!WARNING]
> **Cập nhật Database:** Bảng `users` hiện tại của bạn chưa có cột `email`. Do đó, bạn sẽ phải tự chạy lệnh SQL này trong MySQL Workbench hoặc phpMyAdmin để cập nhật bảng trước khi phần mềm có thể chạy được:
> ```sqlx`
> ALTER TABLE users ADD COLUMN email VARCHAR(100) UNIQUE AFTER username;
> ```
> Những tài khoản cũ đang có trong DB chưa có email nên sẽ không thể đăng nhập được nữa. Bạn có đồng ý với điều này không?

## Proposed Changes

### Database
- Yêu cầu chạy thủ công lệnh `ALTER TABLE` như đã đề cập ở trên.

---

### Models & DAO

#### [MODIFY] models/User.java
- Thêm thuộc tính `private String email;`.
- Cập nhật lại Constructor để nhận thêm tham số `email`.
- Thêm các hàm `getEmail()` và `setEmail(String email)`.

#### [MODIFY] DAO/UserDAO.java
- Cập nhật hàm `insertUser(User user)`: Thêm `email` vào câu lệnh `INSERT INTO users (name, username, email, password) VALUES (?, ?, ?, ?)`. (Sẽ dùng name rỗng hoặc bằng username nếu giao diện chỉ còn 3 trường).
- Cập nhật hàm `login(String email, String password)`: Thay đổi câu lệnh SQL thành `SELECT * FROM users WHERE email = ? AND password = ?`.

---

### UI & Controllers

#### [MODIFY] src/view/CreateAccount.fxml
- Thay đổi các trường nhập liệu thành: **Tên đăng nhập**, **Email**, **Mật khẩu**, **Nhập lại mật khẩu**.
- Thêm trường `TextField fx:id="txtEmail"` với giao diện bo góc tương tự.

#### [MODIFY] src/controller/CreateAccountController.java
- Thêm biến `@FXML private TextField txtEmail;`.
- Thêm quy tắc kiểm tra (Regex): `if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) { baoloi.setText("Vui lòng nhập đúng định dạng email *****@gmail.com!"); return; }`.
- Cập nhật tạo đối tượng `User` với email để lưu xuống DB.

#### [MODIFY] src/view/Login.fxml
- Sửa tiêu đề ô nhập từ `TÀI KHOẢN` thành `EMAIL`.
- Sửa gợi ý (PromptText) thành `Nhập Email của bạn`.

#### [MODIFY] src/controller/LoginController.java
- Đổi tên biến (nếu cần) và lấy dữ liệu email.
- Thêm quy tắc kiểm tra email định dạng `@gmail.com` trước khi cho phép đăng nhập.
- Gọi hàm `userDAO.login(email, hashedPassword)` thay vì username.

## Verification Plan
1. Xác nhận rằng lệnh SQL thêm cột `email` có thể chạy thành công.
2. Thử đăng ký 1 tài khoản mới với email sai định dạng (ví dụ `test@yahoo.com` hoặc `testgmail.com`) -> App phải báo lỗi.
3. Thử đăng ký với email đúng định dạng `abc@gmail.com` -> Thành công.
4. Thử đăng nhập bằng username -> Thất bại.
5. Thử đăng nhập bằng email `abc@gmail.com` và mật khẩu -> Thành công và chuyển vào trang chủ.
