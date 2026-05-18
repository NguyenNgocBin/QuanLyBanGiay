# 🏆 HỆ THỐNG QUẢN LÝ BÁN GIÀY - SOLEMANAGER
## DANH SÁCH CÁC CHỨC NĂNG & CẢI TIẾN HỆ THỐNG MỚI NÂNG CẤP

Tài liệu này tổng hợp toàn bộ các tính năng, cải tiến công nghệ, thiết kế giao diện Glassmorphism và các bản sửa lỗi logic nghiệp vụ vừa được triển khai hoàn tất cho dự án **SoleManager**.

---

## 📂 MỤC LỤC
1. [Phân Quyền Người Dùng (Role System)](#1-role-system)
2. [Quản Lý Danh Mục Sản Phẩm (Category Module)](#2-category-module)
3. [Quản Lý Nhà Cung Cấp (Supplier Module)](#3-supplier-module)
4. [Báo Cáo Tài Chính & Biểu Đồ Phân Tích (Reports Module)](#4-reports-module)
5. [Lịch Sử Nhập Kho Chi Tiết (Import History)](#5-import-history)
6. [Hệ Thống POS Bán Hàng Nâng Cao & Phím Tắt](#6-pos-system)
7. [Tối Giản Hóa Quy Trình Nhập Kho Vào Dialog Thêm Sản Phẩm](#7-import-workflow)
8. [Các Lỗi Logic & Giao Diện Quan Trọng Đã Được Khắc Phục](#8-bug-fixes)

---

<a name="1-role-system"></a>
### 1. 🔑 PHÂN QUYỀN NGƯỜI DÙNG (ROLE SYSTEM)
Hệ thống phân quyền thông minh giúp phân tách rõ ràng trách nhiệm giữa người quản trị hệ thống và nhân viên bán hàng trực tiếp.

*   **Bảng dữ liệu `users`**: Bổ sung trường `role` dạng `ENUM('ADMIN', 'STAFF')` mặc định là `STAFF`.
*   **Lớp Session Tiện ích ([SessionManager.java](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/utils/SessionManager.java))**: Lưu trữ thông tin tài khoản đang đăng nhập trong suốt phiên làm việc, cung cấp các kiểm tra quyền hạn nhanh (`isAdmin()`, `isLoggedIn()`).
*   **Giao diện Động theo Vai Trò**:
    *   **Khi ADMIN Đăng Nhập**: Có toàn quyền truy cập toàn bộ các phân hệ từ Dashboard, Sản phẩm, Khách hàng, Danh mục, Nhà cung cấp, Lịch sử nhập, Báo cáo tài chính đến cài đặt hệ thống.
    *   **Khi STAFF Đăng Nhập**: 
        *   Sidebar sẽ tự động **ẩn hoàn toàn** các menu không được phép truy cập (Dashboard, Sản phẩm, Danh mục, Nhà cung cấp, Lịch sử nhập, Báo cáo).
        *   Hệ thống tự động chuyển hướng màn hình chính sang thẳng giao diện **Bán Hàng POS** để nhân viên làm việc ngay lập tức.
        *   Tích hợp bộ chặn (guard) trong bộ điều khiển tab ngăn cản nhân viên cố ý chuyển sang các màn hình quản trị bằng bất kỳ thủ thuật nào.

---

<a name="2-category-module"></a>
### 2. 🗂️ QUẢN LÝ DANH MỤC SẢN PHẨM (CATEGORY MODULE)
Hỗ trợ quản lý và phân nhóm các loại giày bán trong cửa hàng (Giày Thể Thao, Giày Tây, Giày Sandal, Phụ Kiện,...).

*   **Tính năng chính**:
    *   Thực hiện đầy đủ nghiệp vụ CRUD (Thêm, Đọc, Sửa, Xóa) danh mục sản phẩm thông qua Dialog popup hiện đại.
    *   Tích hợp thanh tìm kiếm thời gian thực (Live Search) lọc danh mục cực nhanh theo tên.
    *   **Thống kê động số lượng**: Cột **Số sản phẩm** trên TableView hiển thị số lượng giày thực tế đang thuộc về danh mục đó bằng truy vấn `LEFT JOIN` và `COUNT(p.id)` tối ưu.
*   **Validation & Bảo mật dữ liệu**:
    *   Ngăn chặn hoàn toàn việc xóa một Danh mục nếu như trong hệ thống đang có sản phẩm thuộc danh mục đó, tránh lỗi mồ côi khóa ngoại trong cơ sở dữ liệu.

---

<a name="3-supplier-module"></a>
### 3. 🏢 QUẢN LÝ NHÀ CUNG CẤP (SUPPLIER MODULE)
Giúp quản lý nguồn hàng và các nhà phân phối sản phẩm đầu vào cho cửa hàng.

*   **Tính năng chính**:
    *   CRUD nhà cung cấp với các thông tin chi tiết: Mã nhà cung cấp, Tên, Số điện thoại, Email, Địa chỉ.
    *   TableView hiện đại tích hợp thống kê **Số đơn nhập hàng** từ nhà cung cấp đó.
*   **Validation dữ liệu đầu vào cực kỳ chặt chẽ**:
    *   **Kiểm tra Số điện thoại**: Phải bắt đầu bằng số `0` và có độ dài chính xác là `10` chữ số (`^0[0-9]{9}$`).
    *   **Kiểm tra Email**: Định dạng email chuẩn khoa học, ngăn ngừa các ký tự lạ hoặc định dạng sai.
    *   **Trùng lặp khóa**: Tự động thông báo nếu trùng Mã nhà cung cấp, Email hoặc Số điện thoại.

---

<a name="4-reports-module"></a>
### 4. 📊 BÁO CÁO TÀI CHÍNH & BIỂU ĐỒ PHÂN TÍCH (REPORTS MODULE)
Cung cấp cho Nhà quản lý cái nhìn toàn cảnh về tình hình kinh doanh của cửa hàng thông qua số liệu trực quan.

*   **Thống kê KPI nhanh**:
    *   **Tổng Doanh Thu**: Tổng số tiền thực thu từ tất cả các đơn hàng thành công (không tính đơn đã hủy).
    *   **Ước Tính Lợi Nhuận**: Tính toán bằng cách lấy doanh thu trừ đi giá vốn nhập hàng thực tế từ lịch sử kho (hoặc trừ đi 60% giá bán làm giá vốn ước định đối với các sản phẩm mặc định chưa có phiếu nhập).
    *   **Sản Phẩm Đã Bán**: Tổng số lượng giày thực tế đã bán đến tay khách hàng.
*   **Hệ thống Biểu đồ Phân tích Trực quan**:
    *   📈 **Biểu đồ Đường (LineChart)**: Thể hiện xu hướng biến động doanh thu theo từng ngày trong khoảng thời gian được lọc.
    *   🍩 **Biểu đồ Tròn (PieChart)**: Phân tích cơ cấu doanh thu theo từng Danh mục sản phẩm (hiển thị rõ tỷ lệ phần trăm và doanh thu thu về của từng nhóm).
    *   📊 **Biểu đồ Cột (BarChart)**: Liệt kê danh sách **Top 5 Sản phẩm bán chạy nhất** của cửa hàng.
*   **Lọc Dữ Liệu Thời Gian**:
    *   Tích hợp hai ô chọn ngày `Từ ngày` và `Đến ngày` (`DatePicker`) giúp quản lý lọc báo cáo theo bất kỳ khoảng thời gian mong muốn nào.
*   **📥 Xuất Báo Cáo Excel Chuyên Nghiệp**:
    *   Tích hợp thư viện **Apache POI** cho phép xuất dữ liệu thống kê doanh thu sang file `.xlsx` chỉ với 1 click.
    *   Định dạng file Excel chuyên nghiệp với tiêu đề in đậm, hàng tiêu đề đổ nền màu Indigo sang trọng, số thứ tự tự động và tính năng tự động giãn rộng cột (Autofit columns).

---

<a name="5-import-history"></a>
### 5. 📜 LỊCH SỬ NHẬP KHO CHI TIẾT (IMPORT HISTORY)
Nơi lưu trữ và tra cứu toàn bộ các đợt nhập hàng hóa đầu vào của cửa hàng.

*   **Tính năng chính**:
    *   Hiển thị danh sách các phiếu nhập kho bao gồm: Mã phiếu nhập (định dạng HDNNxxxxx), Nhà cung cấp, Tổng tiền nhập, Ngày nhập kho và trạng thái.
    *   **Popup Chi tiết Phiếu nhập**: Khi bấm vào nút **Chi tiết** của một phiếu nhập, một cửa sổ phụ sẽ xuất hiện với TableView liệt kê rõ ràng:
        *   Tên sản phẩm nhập.
        *   Mã SKU sản phẩm.
        *   Số lượng nhập thực tế.
        *   Đơn giá nhập hàng.
        *   Thành tiền của từng dòng sản phẩm.

---

<a name="6-pos-system"></a>
### 6. 🛒 HỆ THỐNG POS BÁN HÀNG NÂNG CAO & PHÍM TẮT
Cải tiến trải nghiệm thanh toán tại quầy của nhân viên bán hàng, tăng tốc quy trình xử lý đơn hàng lên gấp nhiều lần.

*   **🔔 Thông báo Toast Nổi (Toast Notification)**:
    *   Tạo lớp tiện ích [Toast.java](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/utils/Toast.java) để hiển thị thông báo trôi mượt mà (Fade-out sau 1.5 giây) ở góc màn hình khi thêm sản phẩm, thanh toán thành công hoặc hủy giỏ hàng, mang lại trải nghiệm giống ứng dụng web hiện đại.
*   **⌨️ Hệ Thống Phím Tắt Tiện Lợi**:
    *   `F1`: Thanh toán hóa đơn ngay lập tức (mở popup xác nhận thanh toán).
    *   `F2`: Chuyển nhanh con trỏ đến ô tìm kiếm / thêm nhanh khách hàng mới.
    *   `ESC`: Hủy và dọn sạch toàn bộ giỏ hàng hiện tại kèm âm báo và Toast thông báo.
*   **🔌 Quét Mã Vạch Bằng Máy Quét Vật Lý (Barcode Scanner Integration)**:
    *   Tích hợp trình lắng nghe bàn phím siêu tốc (Keyboard Scanner Listener). Khi máy quét mã vạch vật lý quét mã SKU dán trên hộp giày, hệ thống sẽ bắt luồng phím gõ nhanh kết thúc bằng phím `Enter`.
    *   Hệ thống tự động tra cứu mã SKU trong CSDL và thêm ngay sản phẩm đó vào giỏ hàng với số lượng tăng thêm `1` mà nhân viên không cần dùng chuột hay bàn phím, cực kỳ nhanh chóng và chuyên nghiệp!

---

<a name="7-import-workflow"></a>
### 7. ⚡ TỐI GIẢN HÓA QUY TRÌNH NHẬP KHO VÀO DIALOG THÊM SẢN PHẨM
Loại bỏ các bước thủ công rườm rà trong quy trình kiểm kho và nhập hàng.

*   **Xóa bỏ nút "Nhập kho" cũ**: Không còn nút Nhập kho riêng lẻ, mọi thao tác nhập kho ban đầu được hợp nhất làm một.
*   **Tích hợp trường dữ liệu nhập hàng vào Dialog "Thêm sản phẩm mới"**:
    *   Khi bấm **Thêm sản phẩm mới** ở trang Kho hàng, giao diện mới hiển thị thêm:
        *   **Chọn nhà cung cấp** (`ComboBox<Supplier>`).
        *   **Giá nhập** (`txtImportPrice`).
        *   **Tổng tiền nhập hàng** (`lblTotalAmount`) tự động tính toán bằng: `Tồn kho ban đầu * Giá nhập` ngay lập tức khi người dùng đang nhập dữ liệu.
*   **Giao dịch an toàn (Database Transaction)**:
    *   Khi lưu sản phẩm mới, phương thức `addProductWithImport(...)` trong [ProductDAO.java](file:///d:/VKU/doancoso1/QuanLyBanGiay/src/DAO/ProductDAO.java) sẽ thực thi một giao dịch an toàn:
        1. Thêm sản phẩm mới vào bảng `products`.
        2. Tự động chèn một phiếu nhập hàng mới vào bảng `import_orders` với Nhà cung cấp đã chọn.
        3. Tự động chèn chi tiết phiếu nhập vào `import_details`.
    *   Đảm bảo tính nhất quán của dữ liệu. Triggers tự động trong CSDL sẽ ghi nhận log lịch sử thay đổi tồn kho chính xác 100%.

---

<a name="8-bug-fixes"></a>
### 8. 🛠️ CÁC LỖI LOGIC & GIAO DIỆN QUAN TRỌNG ĐÃ ĐƯỢC KHẮC PHỤC

#### A. Sửa lỗi hiển thị sai số lượng sản phẩm bán ra ở trang Đơn hàng
*   **Mô tả lỗi**: Khi khách hàng mua 1 sản phẩm với số lượng `5` đôi hoặc `10` đôi, bên trang quản lý **Đơn hàng** vẫn chỉ hiển thị cột số lượng sản phẩm đã bán là `1`.
*   **Nguyên nhân**: Truy vấn SQL của `OrderDAO` sử dụng `COUNT(*)` đếm số dòng chi tiết thay vì tính tổng số lượng giày bán ra.
*   **Cách khắc phục**: Thay thế `COUNT(*)` bằng `COALESCE(SUM(od.quantity), 0)` trong câu truy vấn lấy danh sách đơn hàng. Hệ thống giờ đây hiển thị chính xác tổng số lượng sản phẩm thực tế đã bán trong đơn hàng đó.

#### B. Sửa lỗi biến trắng màn hình Báo Cáo Doanh Thu
*   **Mô tả lỗi**: Trang doanh thu bị trống trơn một khoảng trắng rất lớn và các thẻ KPI bị đẩy xuống dưới cùng khuất khỏi tầm mắt.
*   **Nguyên nhân**: Tấm bảng bộ lọc ngày sử dụng lớp `.warehouse-panel` có chiều cao tối thiểu mặc định là `380px`, đẩy toàn bộ nội dung khác xuống dưới. Nhãn chữ "Từ ngày" và "Đến ngày" có màu trắng hiển thị trên nền trắng nên bị tàng hình.
*   **Cách khắc phục**: Gỡ bỏ lớp `.warehouse-panel` khỏi bộ lọc, cấu hình inline tự động co giãn theo nội dung. Đổi màu chữ của nhãn bộ lọc sang màu Slate `#475569` rõ nét.

#### C. Sửa lỗi ẩn tên sản phẩm dưới trục biểu đồ Top 5 bán chạy
*   **Mô tả lỗi**: Trục hoành của biểu đồ cột Top 5 sản phẩm bán chạy nhất bị trống trơn, không nhìn thấy tên các đôi giày bán chạy.
*   **Nguyên nhân**: Trục CategoryAxis của biểu đồ đang gán màu chữ nhãn là Trắng (`tickLabelFill="WHITE"`) trên nền biểu đồ màu trắng dẫn đến chữ bị ẩn. Tiêu đề biểu đồ cũng gặp lỗi tương tự.
*   **Cách khắc phục**: Cập nhật `tickLabelFill` sang màu xám Slate `#475569` và tiêu đề sang màu xanh đen đậm `#1E293B` giúp mọi thông tin hiển thị cực kỳ trực quan và bắt mắt.

---

## 📈 TÓM TẮT CÔNG NGHỆ ÁP DỤNG
*   **Ngôn ngữ chính**: Java 17 + JavaFX 21 (Hỗ trợ FXML).
*   **Cơ sở dữ liệu**: MySQL 8.0 với các Triggers đồng bộ tồn kho tự động.
*   **Giao dịch nguyên tử (Transactions)**: Đảm bảo toàn vẹn dữ liệu nhập kho và POS.
*   **Thư viện tích hợp**: Apache POI (Xuất file Excel) & iText PDF (In hóa đơn).
*   **Phong cách thiết kế**: Glassmorphism hiện đại, phối màu Slate & Indigo cao cấp.
