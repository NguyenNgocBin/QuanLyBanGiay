# Ý tưởng và Logic thiết kế Trang Sản phẩm Chuẩn

Để thiết kế một trang sản phẩm "chuẩn" và đầy đủ chức năng cho hệ thống quản lý bán giày (hoặc bất kỳ hệ thống bán lẻ nào), chúng ta cần phân định rõ **mục đích sử dụng** của trang đó. Thông thường sẽ có 2 loại trang sản phẩm chính:

1. **Trang Quản lý Sản phẩm (Dành cho Quản lý/Admin)**: Tập trung vào CRUD (Thêm, Sửa, Xóa), quản lý kho, giá cả.
2. **Trang Bán hàng / POS (Dành cho Thu ngân)**: Tập trung vào tìm kiếm nhanh, chọn size/màu, thêm vào giỏ và thanh toán.

Dưới đây là ý tưởng và logic chi tiết cho từng loại, tập trung vào mô hình **POS/Bán hàng** mà bạn đang hướng tới.

---

## I. Ý tưởng thiết kế Giao diện (Aesthetics & Layout)

### 1. Bố cục chia đôi (Split Layout) - Chuẩn POS
- **Bên trái (60-70%): Danh mục sản phẩm**. 
  - Hiển thị dạng **Lưới (Grid)** nếu sản phẩm có hình ảnh đẹp và thu ngân cần nhận diện bằng mắt.
  - Hiển thị dạng **Bảng (Table)** nếu danh mục quá nhiều, cần hiển thị nhiều thông tin text (mã vạch, size, kho) để nhìn nhanh.
- **Bên phải (30-40%): Giỏ hàng & Thanh toán**. Luôn cố định để thu ngân thấy được đơn hàng hiện tại mà không cần chuyển trang.

### 2. Các yếu tố trực quan (Visual Cues)
- **Màu sắc trạng thái**: Sử dụng màu Xanh (Còn hàng), Đỏ (Hết hàng), Vàng (Sắp hết hàng - < 5 sản phẩm) để thu ngân chủ động tư vấn.
- **Thẻ sản phẩm (nếu dùng Grid)**: Cần có ảnh, tên rút gọn, giá nổi bật, và một ComboBox chọn Size ngay trên thẻ để giảm bớt thao tác click.

---

## II. Logic xử lý chức năng (Core Logic)

Một trang sản phẩm chuẩn cần xử lý mượt mà các logic sau:

### 1. Logic Biến thể Sản phẩm (Product Variants)
- **Vấn đề**: Một đôi giày có thể có nhiều Size (39, 40, 41) và mỗi size có số lượng tồn kho khác nhau.
- **Giải pháp Logic**:
  - Trong DB: Mỗi SKU (Mã sản phẩm + Size) là 1 dòng riêng biệt.
  - Trên Giao diện: Gộp các sản phẩm cùng tên lại thành 1 thẻ/dòng. Khi người dùng chọn Size từ Dropdown, hệ thống tự động tra cứu số lượng tồn kho và giá (nếu có khác biệt) của SKU tương ứng để hiển thị.

### 2. Logic Tìm kiếm & Bộ lọc (Search & Filter)
- **Tìm kiếm đa năng**: Phải hỗ trợ tìm theo Tên, Mã ID, và đặc biệt là **Mã vạch (Barcode)**. Thu ngân dùng máy quét mã vạch sẽ bắn thẳng chuỗi số vào ô tìm kiếm.
- **Bộ lọc**: Lọc theo Thương hiệu (Nike, Adidas), Danh mục (Giày chạy, Giày tây), và Khoảng giá.

### 3. Logic Giỏ hàng (Cart Logic)
- **Thêm sản phẩm**: 
  - Nếu sản phẩm (cùng mã, cùng size) đã có trong giỏ -> Tăng số lượng lên 1.
  - Nếu chưa có -> Thêm dòng mới.
- **Kiểm tra tồn kho (Validation)**: Khi tăng số lượng trong giỏ, phải check xem `Số lượng trong giỏ <= Số lượng tồn kho`. Nếu vượt quá, báo lỗi không cho thêm.
- **Tự động tính toán**: Tổng tiền = Σ (Đơn giá * Số lượng). Tự động tính Thuế (VAT), Giảm giá (Discount) nếu có.

### 4. Logic Thanh toán & In hóa đơn
- **Phương thức**: Tiền mặt, Chuyển khoản (Quét mã QR động), Quẹt thẻ.
- **Xử lý DB**: Khi bấm Thanh toán thành công:
  - Trừ số lượng tồn kho trong bảng `Products`.
  - Lưu thông tin hóa đơn vào bảng `Orders` và `OrderDetails`.
- **In ấn**: Gọi lệnh in ra máy in nhiệt (khổ 58mm hoặc 80mm).

---

## III. Đề xuất Tính năng Nâng cao (To-Do cho tương lai)

Nếu muốn trang sản phẩm của bạn cực kỳ chuyên nghiệp, hãy bổ sung:
1. **Tích hợp Quét mã vạch**: Thu ngân chỉ cần cầm máy quét bắn vào sản phẩm, hệ thống tự động nhận diện và đưa vào giỏ hàng mà không cần bấm chuột.
2. **Xử lý Đơn hàng chờ**: Cho phép lưu tạm đơn hàng hiện tại (khi khách quên ví, đi lấy thêm đồ) để phục vụ khách tiếp theo, sau đó nạp lại đơn hàng cũ.
3. **Gợi ý sản phẩm**: Hiển thị các sản phẩm thường được mua cùng (ví dụ: mua giày gợi ý mua tất).
