# Kế hoạch Thiết kế Ứng dụng Quản lý Bán Giày

Tài liệu này trình bày lộ trình đề xuất để hoàn thiện ứng dụng "QuanLyBanGiay", tập trung vào các chức năng còn thiếu, cải thiện logic và nâng cao kiến trúc hệ thống.

## 1. Dashboard Động & Thống kê
Hiện tại, Dashboard đang sử dụng dữ liệu giả. Cần kết nối với dữ liệu thực tế từ cơ sở dữ liệu.

### Các thay đổi đề xuất:
- **Biểu đồ doanh thu**: Truy xuất doanh thu hàng ngày/hàng tháng từ bảng `orders`.
- **Thẻ tóm tắt**:
    - **Tổng doanh thu**: Tổng số tiền `total_amount` trong tháng hiện tại.
    - **Tổng đơn hàng**: Số lượng đơn hàng được tạo trong ngày hôm nay.
    - **Cảnh báo tồn kho**: Đếm các sản phẩm có số lượng tồn kho thấp hơn ngưỡng quy định.
- **Hoạt động gần đây**: Hiển thị 5 đơn hàng thực tế mới nhất thay vì dữ liệu mẫu.

## 2. Quản lý Kho & Nhà cung cấp (Nhập hàng)
Hệ thống hiện đã có chức năng bán hàng, nhưng chưa có cách chính thức để ghi nhận hàng hóa *nhập vào* từ nhà cung cấp.

### Các thành phần mới:
- **[MỚI] Model & DAO Nhà cung cấp**: Theo dõi tên công ty, thông tin liên lạc và địa chỉ.
- **[MỚI] Màn hình Nhập hàng**:
    - Tạo form để ghi nhận "Đơn nhập hàng".
    - Tự động cập nhật số lượng tồn kho sản phẩm sau khi hoàn tất nhập hàng.
    - Theo dõi "Giá nhập" so với "Giá bán" để tính toán lợi nhuận chính xác.

## 3. Báo cáo & Phân tích
Doanh nghiệp cần xem các xu hướng hiệu suất để đưa ra quyết định.

### Các tính năng đề xuất:
- **Sản phẩm bán chạy**: Danh sách 5 sản phẩm có số lượng bán ra cao nhất.
- **Doanh thu theo danh mục**: Biểu đồ tròn hiển thị loại giày nào mang lại thu nhập cao nhất.
- **Chức năng Xuất dữ liệu**: Thêm các nút để xuất danh sách Đơn hàng hoặc Sản phẩm ra file **Excel** (sử dụng Apache POI) hoặc **PDF** (sử dụng iText).

## 4. Nâng cấp chức năng Bán hàng (POS)
- **In Hóa đơn**: Tạo file PDF hóa đơn chuyên nghiệp hoặc bố cục máy in nhiệt sau khi thực hiện `checkout()`.
- **Giảm giá/Khuyến mãi**:
    - Thêm trường `discount_amount` hoặc `discount_percent` vào bảng `orders`.
    - Cho phép nhân viên áp dụng giảm giá thủ công hoặc chọn mã khuyến mãi khi thanh toán.

## 5. Quản lý Người dùng & Phân quyền
Hiện tại, hệ thống mới chỉ có đăng nhập cơ bản và chưa phân biệt rõ vai trò.

### Các thay đổi đề xuất:
- **Phân quyền**: Triển khai vai trò `ADMIN` và `STAFF`.
    - **Admin**: Có thể xem báo cáo tài chính, quản lý nhân viên và xóa sản phẩm.
    - **Staff**: Chỉ có thể thực hiện bán hàng và quản lý khách hàng.
- **Trang hồ sơ**: Màn hình để người dùng đổi mật khẩu và cập nhật thông tin cá nhân (ảnh đại diện, số điện thoại).

## 6. Cải thiện Logic & Kiến trúc

### [Logic] Quản lý Phiên làm việc (Session)
- Tạo lớp `SessionManager` để lưu trữ đối tượng `User` hiện đang đăng nhập.
- Truy cập tên và vai trò của người dùng trên toàn hệ thống để hiển thị trên Sidebar/Topbar.

### [Logic] Tính toàn vẹn của Giao dịch
- Đảm bảo các thao tác trong `SaleDAO` và `OrderDAO` sử dụng SQL Transaction một cách nhất quán để tránh sai lệch dữ liệu.

### [Kiến trúc] Quản lý Hình ảnh
- Thay vì sử dụng đường dẫn tuyệt đối, hãy triển khai logic để sao chép các hình ảnh tải lên vào một thư mục `assets/` cố định trong dự án hoặc một đường dẫn cấu hình bên ngoài.

---

## Kế hoạch Xác minh

### Kiểm tra tự động
- Kiểm tra các phương thức `DAO` bằng database kiểm thử.
- Kiểm tra logic xác thực dữ liệu (ví dụ: đảm bảo tồn kho không bị âm).

### Xác minh thủ công
1. **Quy trình bán hàng**: Đăng nhập -> Thêm khách hàng -> Thêm sản phẩm -> Thanh toán -> Kiểm tra tồn kho giảm -> Kiểm tra đơn hàng xuất hiện trong lịch sử.
2. **Kiểm tra quyền**: Đăng nhập với quyền Staff và đảm bảo các nút "Xóa sản phẩm" hoặc "Báo cáo tài chính" bị ẩn hoặc vô hiệu hóa.
