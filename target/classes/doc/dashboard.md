# Hướng dẫn: Dashboard Động (Dynamic Dashboard)

Chức năng Dashboard hiện đã được kết nối với cơ sở dữ liệu thực, thay vì sử dụng dữ liệu mẫu (hardcoded) như trước. 

## Các thay đổi chính

### 1. `DashboardDAO.java`
Đã tạo mới file DAO này chuyên xử lý các truy vấn lấy dữ liệu thống kê cho Dashboard:
- `getTodayRevenue()`: Lấy tổng doanh thu trong ngày (loại trừ các đơn bị hủy).
- `getNewOrdersCount()`: Đếm số đơn hàng mới tạo trong ngày.
- `getTotalCustomersCount()`: Đếm tổng số khách hàng trên hệ thống.
- `getLowStockCount()`: Đếm số lượng sản phẩm sắp hết (tồn kho từ 1 đến 10).
- `getRevenueLast7Days()`: Lấy dữ liệu doanh thu của 7 ngày gần nhất để vẽ biểu đồ.
- `getTopProducts()`: Lấy top các sản phẩm bán chạy nhất (dựa trên số lượng đã bán).
- `getRecentTransactions()`: Lấy danh sách các giao dịch (đơn hàng) mới nhất.

### 2. `Dashboard.fxml`
- Đã gắn các `fx:id` (`lblTodayRevenue`, `lblNewOrders`, `lblTotalCustomers`, `lblLowStock`, `topProductsContainer`) vào các nhãn hiển thị để Controller có thể truy xuất và cập nhật dữ liệu.
- Xóa các thẻ giao diện bị "cứng hóa" (hardcoded) trong phần **Top Products**, thay bằng một `VBox` rỗng để nạp dữ liệu động từ Java.

### 3. `DashboardController.java`
- Khởi tạo đối tượng `dashboardDAO` và gọi các phương thức tương ứng.
- **`loadMetrics()`**: Cập nhật 4 thẻ thông số ở phía trên.
- **`setupChart()`**: Nạp dữ liệu doanh thu 7 ngày qua vào biểu đồ `AreaChart`.
- **`setupTable()`**: Nạp 5 giao dịch gần đây nhất vào bảng `transactionTable`.
- **`loadTopProducts()`**: Tự động tạo mã giao diện (UI Code) nạp danh sách 5 sản phẩm bán chạy nhất vào `topProductsContainer`.
- Bổ sung các phương thức format hiển thị tiền tệ (kể cả định dạng rút gọn như `3.2M`).

## Đã kiểm tra
- Các thành phần động đã kết nối với các biến đúng cấu trúc. 
- Formatter tiền tệ hiển thị dưới chuẩn Việt Nam.
- Layout không bị lỗi hiển thị khi danh sách Top Products rỗng.

> [!TIP]
> Bạn cần chạy lại ứng dụng bằng IDE hoặc Maven. Dashboard giờ đây sẽ hiển thị số liệu dựa trên các dữ liệu đang có trong database (các đơn hàng đã tạo, sản phẩm hiện tại, ...).
