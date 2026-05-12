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



-----------
 #### chuyển đổi giữa các giao diện trng một file fxml
 Luồng hoạt động chuyển đổi giữa các VBox này là một thủ thuật cực kỳ mượt mà và phổ biến trong JavaFX để làm giao diện "Single-Page Application" (ứng dụng một trang) mà không cần phải tạo nhiều file FXML hay load lại màn hình.

Mình sẽ giải thích chi tiết cơ chế hoạt động của nó cho bạn hiểu nhé:

1. Cấu trúc ban đầu (Trong file FXML)
Bên trong cái thẻ màu trắng ở giữa màn hình, chúng ta xếp sẵn 3 khối <VBox> (tương ứng với 3 Bước) nằm dọc từ trên xuống dưới. Tuy nhiên, trong code FXML, mình đã thiết lập:

Bước 1 (Nhập Email): visible="true" managed="true" (Bật)
Bước 2 (Nhập Mã): visible="false" managed="false" (Tắt)
Bước 3 (Đổi Pass): visible="false" managed="false" (Tắt)
Vì vậy, khi bạn vừa mở trang Quên Mật Khẩu lên, mắt bạn chỉ nhìn thấy duy nhất khối Nhập Email.

2. Sự kết hợp ma thuật giữa Visible và Managed
Trong JavaFX, để ẩn một thứ đi hoàn toàn, bạn phải dùng kết hợp 2 lệnh:

setVisible(false): Tàng hình. Cái khối đó sẽ trở nên vô hình, người dùng không nhìn thấy nữa. NHƯNG, nó vẫn chiếm một khoảng trống (vị trí vật lý) trên màn hình. Màn hình của bạn sẽ bị chừa ra một lỗ hổng rất vô duyên.
setManaged(false): Loại bỏ khỏi bố cục. Lệnh này báo với JavaFX rằng: "Hãy coi như cái khối này không tồn tại, thu hẹp diện tích của nó về mức bằng 0 và nhường chỗ cho các khối khác".
👉 Kết hợp lại: Khi gọi cả hai lệnh này mang ý nghĩa "Cất khối này đi, xóa luôn vị trí của nó để khung giao diện tự động co giãn lại".

3. Luồng hoạt động thực tế khi bấm nút
Giả sử bạn đang ở Bước 1 (Nhập Email). Khi bạn nhập đúng Email và bấm nút "Gửi mã xác nhận", Java sẽ chạy đoạn code này:

java
private void chuyenSangBuoc2() {
    // 1. Tắt Bước 1: Cho tàng hình và thu hẹp diện tích về 0
    step1Box.setVisible(false); 
    step1Box.setManaged(false);
    
    // 2. Bật Bước 2: Cho hiện hình và cấp lại diện tích để nó phình to ra
    step2Box.setVisible(true);  
    step2Box.setManaged(true);
    
    // 3. Đảm bảo Bước 3 vẫn đang tắt
    step3Box.setVisible(false); 
    step3Box.setManaged(false);
}
Điều gì xảy ra trên màn hình lúc này? Ngay trong tích tắc (1 phần nghìn giây), khối Bước 1 xẹp xuống biến mất, và ngay lập tức khối Bước 2 phình to ra thế vào đúng khoảng trống đó. Đối với mắt người dùng, nó giống như màn hình vừa trượt/thay đổi sang một nội dung mới, nhưng thực tế là chúng ta vẫn đang ở nguyên trên một giao diện cũ, chỉ là "cất" cái Vbox này đi và "lôi" cái Vbox kia ra mà thôi.

Cơ chế này tương tự khi bạn bấm nút "Quay lại", chúng ta chỉ việc gọi lệnh tắt Bước 2 và bật lại Bước 1 là xong! 