# 👟 SoleManager - Hệ Thống Quản Lý Cửa Hàng Bán Giày

**SoleManager** là ứng dụng máy để bàn (Desktop App) chuyên nghiệp được phát triển để quản lý toàn diện hoạt động kinh doanh của cửa hàng bán giày dép. Ứng dụng tích hợp đầy đủ các nghiệp vụ từ bán hàng POS tại quầy, xuất hóa đơn PDF tự động, nhập kho thông minh, thống kê doanh số trực quan đến bảo mật tài khoản nâng cao bằng mã OTP gửi qua Gmail.

---

## 🚀 Các Tính Năng Nổi Bật

1. **Bán Hàng POS Siêu Tốc ([Sale.fxml](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/view/Sale.fxml))**:
   * Hỗ trợ tìm kiếm sản phẩm real-time theo SKU, Tên, lọc nhanh theo loại.
   * Quản lý giỏ hàng chặt chẽ, kiểm soát tồn kho tức thời (ngăn đặt quá lượng tồn).
   * Gợi ý tìm kiếm thông minh và thêm mới khách hàng trực tiếp qua Dialog nổi mà không cần chuyển trang.
   * Thanh toán đa dạng (Tiền mặt, Chuyển khoản, Thẻ) và **tự động sinh hóa đơn PDF** ([PDFGenerator.java](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/utils/PDFGenerator.java)) chuyên nghiệp mở ngay lập tức để in.
2. **Quản Lý Nhập Kho Tiện Lợi ([Import.fxml](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/view/Import.fxml))**:
   * Giao diện Grid Cards chọn sản phẩm trực quan.
   * **Bảng phiếu nhập cho phép chỉnh sửa trực tiếp (Editable Table)** số lượng và giá nhập.
   * Tự động tạo phiếu nhập và cộng dồn số lượng tồn kho sản phẩm trong CSDL MySQL.
3. **Thống Kê Trực Quan (Dashboard) ([Dashboard.fxml](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/view/Dashboard.fxml))**:
   * 4 Thẻ KPI: Doanh thu ngày, Đơn hàng mới, Khách hàng, Sản phẩm sắp hết.
   * Biểu đồ miền (`AreaChart`) trực quan hóa doanh thu 7 ngày qua.
   * Danh sách Top 5 sản phẩm bán chạy nhất.
4. **Quản Lý Sản Phẩm CRUD ([Product.fxml](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/view/Product.fxml))**:
   * Quản lý danh sách giày, size, giá, ảnh đại diện, tình trạng tồn kho.
   * Báo đỏ cảnh báo tồn kho thấp. Tự động tính toán tổng giá trị tiền tồn kho của cửa hàng.
   * Kiểm tra ràng buộc khóa ngoại trước khi xóa, tránh lỗi hệ thống.
5. **Duyệt Đơn Hàng & Khôi Phục Kho ([Order.fxml](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/view/Order.fxml))**:
   * Xem lịch sử đơn hàng, xem chi tiết hóa đơn (popup danh sách món).
   * **Tính năng hủy đơn thông minh**: Tự động hoàn trả số lượng giày đã đặt mua ngược lại vào kho tồn của sản phẩm tương ứng.
6. **Xác Thực OTP Quên Mật Khẩu ([ForgotPasswordController.java](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/controller/ForgotPasswordController.java))**:
   * Bảo mật tài khoản nhân viên với băm **SHA-256**.
   * Quy trình quên mật khẩu 3 bước gửi mã xác nhận OTP thực tế qua hòm thư Gmail bằng thư viện `JavaMail`.

---

## 🛠️ Công Nghệ Sử Dụng

* **Java 17** & **JavaFX 21.0.1** (Bố cục FXML + CSS tùy biến cao với `SoleManager.css`).
* **MySQL 8.0** (Cơ sở dữ liệu quan hệ đồng bộ).
* **iText PDF 5.5.13.3** (Thư viện tạo file PDF hóa đơn).
* **JavaMail 1.6.2** (Thư viện gửi OTP Gmail).
* **Maven** (Trình quản lý phụ thuộc).

---

## 📖 Hướng Dẫn Xem Chi Tiết Dự Án

Để có cái nhìn sâu sắc và chi tiết nhất về cấu trúc mã nguồn, sơ đồ cơ sở dữ liệu MySQL, các logic nghiệp vụ và lộ trình phát triển nâng cấp hệ thống, vui lòng tham khảo:

👉 **[TÀI LIỆU TỔNG QUAN DỰ ÁN CHI TIẾT (PROJECT_OVERVIEW.md)](PROJECT_OVERVIEW.md)**
👉 **[BÁO CÁO PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG CHI TIẾT (SYSTEM_ANALYSIS_AND_DESIGN.md)](SYSTEM_ANALYSIS_AND_DESIGN.md)**
👉 **[HƯỚNG DẪN CHI TIẾT CHỨC NĂNG TỪNG TRANG (PAGE_DESCRIPTION.md)](PAGE_DESCRIPTION.md)**

---

## 💻 Cách Khởi Chạy Dự Án Dưới Local

### 1. Yêu Cầu Hệ Thống
* Đã cài đặt **JDK 17** trở lên.
* Đã cài đặt **MySQL Server** và công cụ quản trị (như Navicat, MySQL Workbench).
* Đã cài đặt **Maven**.

### 2. Thiết Lập Cơ Sở Dữ Liệu
1. Mở MySQL của bạn và tạo một database mới tên là `testlogin`.
2. Import toàn bộ nội dung tệp [database_new.sql](file:///d:/VKU/doancoso1/QuanLyBanGiay/database_new.sql) nằm ở thư mục gốc của dự án để khởi tạo bảng và dữ liệu mẫu đầy đủ.

### 3. Cấu Hình Kết Nối
Kiểm tra và cập nhật thông tin tài khoản MySQL của bạn (username, password) trong tệp kết nối:
* [DBConnection.java](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/database/DBConnection.java)

### 4. Khởi Chạy Ứng Dụng
Sử dụng dòng lệnh (Terminal) tại thư mục gốc của dự án để khởi chạy:
```bash
mvn clean javafx:run
```

---
*Dự án được xây dựng và tối ưu bởi **NguyenNgocBin / QuanLyBanGiay**.*
