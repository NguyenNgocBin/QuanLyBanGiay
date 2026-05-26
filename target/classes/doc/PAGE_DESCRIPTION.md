# 🖥️ HƯỚNG DẪN CHI TIẾT CHỨC NĂNG CỦA TỪNG TRANG TRONG SOLEMANAGER
## 👟 HỆ THỐNG QUẢN LÝ CỬA HÀNG BÁN GIÀY - SOLEMANAGER

Tài liệu này giải thích chi tiết mục đích, vai trò sử dụng, giao diện hiển thị và logic nghiệp vụ hoạt động của **từng trang (màn hình)** trong ứng dụng **SoleManager**.

---

## 📂 DANH SÁCH CÁC TRANG CỦA HỆ THỐNG

### PHÂN NHÓM 1: CÁC TRANG XÁC THỰC (AUTHENTICATION)
1. [Trang Đăng Nhập (Login.fxml)](#1-trang-dang-nhap)
2. [Trang Tạo Tài Khoản (CreateAccount.fxml)](#2-trang-tao-tai-khoan)
3. [Trang Quên Mật Khẩu (ForgotPassword.fxml)](#3-trang-quen-mat-khau)

### PHÂN NHÓM 2: KHUNG GIAO DIỆN CHÍNH (SHELL & CONTAINER)
4. [Trang Giao Diện Khung (Main.fxml)](#4-trang-giao-dien-khung)

### PHÂN NHÓM 3: CÁC TRANG QUẢN TRỊ & NGHIỆP VỤ (ADMIN & STAFF WORKSPACES)
5. [Trang Tổng Quan (Dashboard.fxml)](#5-trang-tong-quan)
6. [Trang Bán Hàng POS (Sale.fxml)](#6-trang-ban-hang-pos)
7. [Trang Kho Hàng / Sản Phẩm (Product.fxml)](#7-trang-kho-hang)
8. [Cửa Sổ Thêm & Sửa Sản Phẩm (AddProduct.fxml / EditProduct.fxml)](#8-cua-so-them-sua-san-pham)
9. [Trang Quản Lý Danh Mục (Category.fxml)](#9-trang-quan-ly-danh-muc)
10. [Trang Quản Lý Nhà Cung Cấp (Supplier.fxml)](#10-trang-quan-ly-nha-cung-cap)
11. [Trang Quản Lý Nhân Viên (Staff.fxml)](#11-trang-quan-ly-nhan-vien)
12. [Trang Nhập Hàng Hóa (Import.fxml)](#12-trang-nhap-hang-hoa)
13. [Trang Lịch Sử Nhập Kho (ImportHistory.fxml)](#13-trang-lich-su-nhap-kho)
14. [Trang Lịch Sử Đơn Hàng (Order.fxml)](#14-trang-lich-su-don-hang)
15. [Trang Báo Cáo Tài Chính (Reports.fxml)](#15-trang-bao-cao-tai-chinh)
16. [Trang Cài Đặt Hệ Thống (Settings.fxml)](#16-trang-cai-dat-he-thong)

---

<a name="1-trang-dang-nhap"></a>
### 1. 🔑 Trang Đăng Nhập (`Login.fxml`)
*   **Mục đích:** Xác thực danh tính của người dùng để truy cập vào hệ thống.
*   **Vai trò truy cập:** Tất cả các tài khoản (Admin và Staff).
*   **Các thành phần UI chính:**
    *   Ô nhập tài khoản (`TextField` username).
    *   Ô nhập mật khẩu ẩn (`PasswordField` password).
    *   Nút bấm **Đăng nhập** (`Button`).
    *   Liên kết chuyển hướng: "Tạo tài khoản mới" và "Quên mật khẩu?".
*   **Logic xử lý & Nghiệp vụ:**
    *   **Bảo mật:** Hệ thống lấy mật khẩu người dùng nhập, tiến hành băm một chiều bằng thuật toán **SHA-256** và so sánh chuỗi băm này với mật khẩu đã lưu trong bảng `users` ở cơ sở dữ liệu.
    *   **Phân quyền ngay khi đăng nhập:** 
        *   Nếu tài khoản đăng nhập có quyền `ADMIN`, hệ thống mở khung giao diện đầy đủ với trang mặc định là **Tổng quan (Dashboard)**.
        *   Nếu tài khoản có quyền `STAFF`, hệ thống tự động ẩn các menu nhạy cảm trên Sidebar và chuyển hướng thẳng đến trang **Bán Hàng POS**.
    *   **Kiểm tra tính hợp lệ:** Hiển thị thông báo màu đỏ bên dưới Form nếu nhập sai thông tin tài khoản hoặc để trống các trường dữ liệu.

---

<a name="2-trang-tao-tai-khoan"></a>
### 2. 📝 Trang Tạo Tài Khoản (`CreateAccount.fxml`)
*   **Mục đích:** Cho phép nhân viên mới đăng ký tài khoản thành viên trong hệ thống.
*   **Vai trò truy cập:** Tự do (đăng ký từ màn hình ngoài).
*   **Các thành phần UI chính:**
    *   Form nhập thông tin: Họ tên, Tên đăng nhập, Địa chỉ Email, Mật khẩu.
    *   Nút bấm **Đăng ký** (`Button`).
    *   Đường dẫn quay lại màn hình Đăng nhập.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Kiểm tra tính duy nhất (Unique constraint):** Kiểm tra xem tên đăng nhập (`username`) và `email` đã tồn tại trong CSDL chưa. Nếu đã tồn tại, hiển thị cảnh báo lỗi tương ứng.
    *   **Bảo mật:** Băm mật khẩu bằng thuật toán **SHA-256** trước khi lưu vào CSDL.
    *   **Thiết lập quyền mặc định:** Tài khoản đăng ký tự động được cấp vai trò mặc định là `STAFF` (Nhân viên bán hàng) để đảm bảo tính an toàn cho hệ thống.

---

<a name="3-trang-quen-mat-khau"></a>
### 3. 📧 Trang Quên Mật Khẩu (`ForgotPassword.fxml`)
*   **Mục đích:** Giúp người dùng khôi phục lại mật khẩu tài khoản khi bị quên thông qua Email xác thực OTP.
*   **Vai trò truy cập:** Tự do.
*   **Quy trình 3 bước nghiệp vụ khép kín:**
    1.  **Bước 1 (Gửi mã OTP):** Người dùng nhập email đăng ký. Hệ thống kiểm tra tính tồn tại của email. Nếu email hợp lệ, hệ thống tự động sinh mã OTP ngẫu nhiên gồm 6 chữ số và sử dụng thư viện `JavaMail` (kết nối cổng SMTP của Gmail) để gửi thư chứa mã OTP đến email thực tế của người dùng.
    2.  **Bước 2 (Xác thực mã OTP):** Form nhập mã OTP xuất hiện. Người dùng nhập mã nhận được trong email. Hệ thống so khớp mã OTP tạm thời. Nếu khớp, cho phép chuyển sang bước thiết lập mật khẩu mới.
    3.  **Bước 3 (Đổi mật khẩu mới):** Người dùng nhập mật khẩu mới và xác nhận mật khẩu. Hệ thống tiến hành băm SHA-256 mật khẩu mới và cập nhật trực tiếp vào CSDL MySQL, sau đó tự động chuyển người dùng trở lại trang đăng nhập.

---

<a name="4-trang-giao-dien-khung"></a>
### 4. 🧭 Trang Giao Diện Khung (`Main.fxml`)
*   **Mục đích:** Đóng vai trò là Shell (khung chứa chính) chứa Sidebar điều hướng bên trái và vùng hiển thị nội dung động (Content Area) bên phải.
*   **Vai trò truy cập:** Admin / Staff.
*   **Các thành phần UI chính:**
    *   **Thanh điều hướng bên trái (Sidebar):** Logo thương hiệu, các nút bấm chuyển trang (Tổng quan, Kho hàng, Bán hàng, Danh mục, Đối tác, Nhân viên, Đơn hàng, Báo cáo, Cài đặt) và nút Đăng xuất.
    *   **Thanh công cụ trên cùng (Topbar):** Ô tìm kiếm nhanh toàn hệ thống, chuông báo thông báo khẩn cấp, tên người dùng đăng nhập và thẻ Avatar hiển thị chức vụ.
    *   **Vùng hiển thị nội dung (VBox/StackPane VBox.vgrow="ALWAYS"):** Nơi các tệp giao diện con (FXML con) được nạp động khi người dùng nhấn chuyển trang trên Sidebar.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Điều hướng linh hoạt:** Quản lý việc chuyển đổi giữa các view mà không cần tải lại toàn bộ ứng dụng (Single Page App style).
    *   **Phân quyền hiển thị (Security Guard):** Dựa vào thông tin tài khoản đăng nhập từ `SessionManager`. Nếu là nhân viên (`STAFF`), controller sẽ tự động loại bỏ các nút chuyển trang của quản lý ra khỏi Sidebar, đảm bảo nhân viên không thể nhìn thấy hay nhấp vào.

---

<a name="5-trang-tong-quan"></a>
### 5. 📊 Trang Tổng Quan (`Dashboard.fxml`)
*   **Mục đích:** Cung cấp cho người quản lý bức tranh tổng thể về tình hình tài chính và hoạt động bán hàng của cửa hàng trong ngày.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   **Thẻ số liệu KPI (Metric Cards):** 4 thẻ hiển thị lớn: Doanh thu hôm nay (VND), Số đơn hàng mới, Tổng số lượng khách hàng đã lưu, và Số lượng sản phẩm sắp hết hàng.
    *   **Biểu đồ miền (`AreaChart`):** Trực quan hóa biến động doanh số 7 ngày gần nhất.
    *   **Danh sách Top 5 sản phẩm bán chạy nhất:** Bảng thu gọn hiển thị sản phẩm kèm doanh thu thu về (được định dạng rút gọn như `15M`, `800K`).
*   **Logic xử lý & Nghiệp vụ:**
    *   **Tải dữ liệu real-time:** Mọi số liệu và biểu đồ được tính toán thông qua các câu lệnh SQL tối ưu (gom nhóm `GROUP BY`, tính tổng `SUM`, đếm `COUNT`) ngay khi mở trang.
    *   **Hành động nhanh:** Nút "Xem kho hàng" để chuyển hướng nhanh đến màn quản lý sản phẩm.

---

<a name="6-trang-ban-hang-pos"></a>
### 6. 🛒 Trang Bán Hàng POS (`Sale.fxml`)
*   **Mục đích:** Phục vụ thu ngân thao tác bán lẻ sản phẩm cho khách tại quầy nhanh nhất.
*   **Vai trò truy cập:** Staff / Admin.
*   **Các thành phần UI chính:**
    *   **Lưới sản phẩm (bên trái):** Hiển thị danh mục và bảng sản phẩm.
    *   **Giỏ hàng & Khách hàng (bên phải):** Bảng hiển thị các đôi giày khách đã chọn, số lượng, đơn giá. Ô tìm kiếm khách hàng bằng Tên/SĐT và nút bấm `+` thêm nhanh khách hàng.
    *   **Thanh toán (dưới cùng):** Chọn phương thức thanh toán (Tiền mặt, Chuyển khoản, Thẻ), hiển thị tổng tiền tạm tính, chiết khấu, và nút **Thanh toán**.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Tích hợp quét mã vạch:** Có trình lắng nghe sự kiện gõ phím siêu tốc từ Barcode Scanner vật lý để thêm nhanh giày vào giỏ.
    *   **Kiểm soát số lượng tồn kho:** Ngăn không cho nhân viên tăng số lượng trong giỏ hàng vượt quá số lượng giày thực tế đang có sẵn trong kho, hiển thị Toast cảnh báo đỏ.
    *   **Tìm kiếm & Thêm nhanh khách hàng:** 
        *   Khi gõ SĐT/Tên, hệ thống gợi ý danh sách khách hàng trên một ListView nổi.
        *   Nút thêm nhanh khách hàng mở Dialog, lưu dữ liệu khách mới vào DB và tự động chọn luôn khách hàng đó vào hóa đơn mà không cần tải lại trang.
    *   **Sinh hóa đơn PDF tự động:** Khi thanh toán thành công, hệ thống lưu đơn vào DB (tạo bản ghi `orders` và `order_details`), tự động cập nhật trừ tồn kho trong bảng `products`, sau đó tự động xuất tệp hóa đơn PDF lưu ở thư mục `invoices/` và mở trực tiếp lên màn hình máy tính để in.
    *   **Hệ thống phím tắt:** `F1` để thanh toán hóa đơn, `F2` để nhảy nhanh tới ô tìm kiếm khách hàng, `ESC` để xóa sạch giỏ hàng hiện tại.
    *   **Thông báo Toast:** Hiển thị thông báo nổi ở góc màn hình khi thêm sản phẩm, thanh toán thành công hoặc hủy giỏ hàng.

---

<a name="7-trang-kho-hang"></a>
### 7. 👟 Trang Kho Hàng / Sản Phẩm (`Product.fxml`)
*   **Mục đích:** Quản lý danh sách giày dép, định giá bán, quản lý số lượng hàng hóa và kiểm soát giá trị tồn kho.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   **Các chỉ số kho (KPI Widgets):** Tổng số dòng sản phẩm, Số sản phẩm sắp hết hàng, Tổng số lượng giày hiện có trong kho, và **Tổng giá trị tiền hàng tồn kho** ($Price \times Stock$).
    *   **Bộ lọc tìm kiếm:** Ô tìm kiếm sản phẩm theo Tên/SKU/Kích cỡ và ComboBox lọc theo trạng thái kho (Tất cả, Còn hàng, Sắp hết, Hết hàng).
    *   **Bảng danh mục sản phẩm (TableView):** Hiển thị ảnh thu nhỏ của giày, Tên giày (kèm mã SKU), Nhãn danh mục (màu sắc động theo nhóm), Kích cỡ, Giá bán, Tồn kho (báo đỏ nếu sắp hết hàng), Trạng thái và nút Hành động (Sửa, Xóa).
*   **Logic xử lý & Nghiệp vụ:**
    *   **Trạng thái màu sắc động:** Cột Tồn kho tự động hiển thị nhãn màu xanh lá (Còn hàng), màu cam (Sắp hết - dưới 5 đôi), màu đỏ (Hết hàng - 0 đôi).
    *   **Ảnh minh họa sản phẩm:** Đọc tệp ảnh từ ổ đĩa máy tính theo đường dẫn lưu trữ, nếu sản phẩm chưa có ảnh sẽ hiển thị biểu tượng giày mặc định phối màu sang trọng theo danh mục.
    *   **Ngăn ngừa lỗi ràng buộc toàn vẹn dữ liệu:** Khi xóa một sản phẩm, hệ thống kiểm tra xem mã sản phẩm đã từng phát sinh hóa đơn bán lẻ trong CSDL chưa. Nếu đã từng bán, hệ thống ngăn chặn hành động xóa và hiển thị cảnh báo để tránh xung đột khóa ngoại (`Foreign Key Constraint`).

---

<a name="8-cua-so-them-sua-san-pham"></a>
### 8. ➕ Cửa Sổ Thêm & Sửa Sản Phẩm (`AddProduct.fxml` / `EditProduct.fxml`)
*   **Mục đích:** Thêm sản phẩm mới vào kho hàng hoặc cập nhật thông tin sản phẩm có sẵn.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   Các ô nhập liệu: Tên giày, Mã SKU, Giá bán lẻ, Kích cỡ.
    *   ComboBox chọn Danh mục sản phẩm.
    *   Khu vực chọn ảnh đại diện (`Image View Picker` - click để chọn file ảnh trên máy tính).
    *   **Khu vực nhập kho tích hợp (chỉ có ở màn Thêm mới):** ComboBox chọn Nhà cung cấp, Ô nhập đơn giá nhập hàng, Ô nhập số lượng tồn kho ban đầu, và nhãn hiển thị Tổng tiền nhập hàng tạm tính.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Giao dịch an toàn (Database Transaction):** Khi bấm lưu ở màn hình Thêm mới, hệ thống thực thi giao dịch đồng thời thêm mới sản phẩm vào bảng `products`, tự động tạo phiếu nhập kho tương ứng trong bảng `import_orders`, và chèn chi tiết hàng nhập vào `import_details` để đảm bảo dữ liệu lịch sử kho luôn trùng khớp 100%.
    *   **Xử lý hình ảnh:** Lưu hình ảnh sản phẩm đã chọn vào thư mục tài nguyên của dự án và lưu đường dẫn tương đối vào cơ sở dữ liệu.
    *   **Kiểm tra tính hợp lệ dữ liệu:** Giá bán, giá nhập, số lượng tồn kho bắt buộc phải là số dương, không được để trống thông tin.

---

<a name="9-trang-quan-ly-danh-muc"></a>
### 9. 🗂️ Trang Quản Lý Danh Mục (`Category.fxml`)
*   **Mục đích:** Quản lý các nhóm danh mục sản phẩm của cửa hàng (VD: Giày Tây, Giày Thể Thao, Dép Sandal...).
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   Thanh tìm kiếm danh mục nhanh theo tên.
    *   Nút bấm **Thêm danh mục mới**.
    *   Bảng TableView danh sách danh mục: ID, Tên danh mục, **Số lượng sản phẩm thuộc danh mục** (tính toán động qua lệnh SQL JOIN), và cột Hành động (Sửa, Xóa).
*   **Logic xử lý & Nghiệp vụ:**
    *   **Tính toán số lượng sản phẩm động:** Khi tải trang, hệ thống truy vấn CSDL và đếm số lượng giày thực tế đang được phân loại vào từng danh mục.
    *   **Ràng buộc xóa danh mục:** Hệ thống sẽ không cho phép Admin xóa một danh mục nếu trong kho đang có ít nhất 1 sản phẩm thuộc danh mục đó, tránh lỗi mồ côi sản phẩm.

---

<a name="10-trang-quan-ly-nha-cung-cap"></a>
### 10. 🏢 Trang Quản Lý Nhà Cung Cấp (`Supplier.fxml`)
*   **Mục đích:** Quản lý danh sách các nhà phân phối cung cấp nguồn hàng đầu vào cho cửa hàng.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   Thanh tìm kiếm nhà cung cấp nhanh theo Mã/Tên/SĐT.
    *   Nút bấm **Thêm nhà cung cấp mới** mở Dialog.
    *   Bảng TableView hiển thị: Mã nhà cung cấp, Tên, Số điện thoại, Email, Địa chỉ, và **Số đơn đã nhập hàng** từ nhà cung cấp này.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Validation dữ liệu đầu vào cực kỳ chặt chẽ:**
        *   *Số điện thoại:* Bắt buộc phải là định dạng 10 số và bắt đầu bằng số `0` (sử dụng regex `^0[0-9]{9}$`).
        *   *Email:* Bắt buộc nhập đúng định dạng email chuẩn.
    *   **Kiểm tra trùng lặp khóa:** Hệ thống tự động kiểm tra trùng lặp mã nhà cung cấp, email hoặc số điện thoại trong DB trước khi lưu và hiển thị cảnh báo lỗi nếu bị trùng.

---

<a name="11-trang-quan-ly-nhan-vien"></a>
### 11. 👥 Trang Quản Lý Nhân Viên (`Staff.fxml`)
*   **Mục đích:** Quản lý danh sách tài khoản nhân viên trong cửa hàng, thực hiện phân quyền và kiểm soát nhân sự.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   Bảng TableView hiển thị: ID tài khoản, Tên nhân viên, Tên đăng nhập, Email liên lạc, Vai trò phân quyền (ADMIN / STAFF), Ngày tạo tài khoản và nút Hành động.
    *   Dialog thêm nhân viên mới hoặc chỉnh sửa phân quyền.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Phân quyền người dùng (Role Management):** Admin có thể thay đổi chức năng của một tài khoản từ nhân viên (`STAFF`) lên quản lý (`ADMIN`) và ngược lại. Quyền hạn này lập tức có hiệu lực ở lần đăng nhập tiếp theo của tài khoản đó.
    *   **Bảo mật:** Mã hóa băm SHA-256 đối với mật khẩu nhân viên khi được tạo mới từ trang quản lý này.

---

<a name="12-trang-nhap-hang-hoa"></a>
### 12. 📦 Trang Nhập Hàng Hóa (`Import.fxml`)
*   **Mục đích:** Thực hiện tạo phiếu nhập kho cho các sản phẩm đã có sẵn để bổ sung số lượng tồn kho.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   **Bảng chọn sản phẩm nhanh (bên trái):** Hiển thị danh sách giày dưới dạng lưới thẻ sản phẩm (Grid Cards) kèm ô tìm kiếm và lọc danh mục.
    *   **Thông tin phiếu nhập (bên phải):** ComboBox chọn Nhà cung cấp, Bảng chi tiết danh sách sản phẩm nhập (Editable TableView), Ô hiển thị tổng số tiền nhập hàng, và nút **Hoàn tất nhập kho**.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Bảng cho phép chỉnh sửa trực tiếp (Editable Table):** Người dùng có thể kích đúp chuột vào cột Số lượng nhập (`Quantity`) hoặc cột Đơn giá nhập (`Import Price`) của bảng bên phải để nhập số liệu thực tế ngay trên bảng. Hệ thống tự động tính toán tổng tiền lô hàng tương ứng theo thời gian thực.
    *   **Lưu phiếu nhập & cập nhật tồn kho:** Khi nhấn "Hoàn tất nhập kho", hệ thống tạo phiếu nhập mới, lưu chi tiết phiếu và tiến hành **cộng thêm** trực tiếp số lượng nhập vào số lượng tồn kho hiện tại của các sản phẩm tương ứng trong bảng `products`.

---

<a name="13-trang-lich-su-nhap-kho"></a>
### 13. 📜 Trang Lịch Sử Nhập Kho (`ImportHistory.fxml`)
*   **Mục đích:** Nơi lưu trữ, tra cứu và kiểm toán lại tất cả các đợt nhập hàng hóa đầu vào của cửa hàng.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   Bảng TableView hiển thị: Mã phiếu nhập (định dạng `HDNNxxxxx`), Tên nhà cung cấp, Ngày nhập hàng, Tổng giá trị tiền nhập lô hàng, Trạng thái (Đã nhập) và nút **Chi tiết**.
    *   **Popup Chi tiết Phiếu nhập:** Một Dialog xuất hiện hiển thị thông tin nhà cung cấp và bảng liệt kê chi tiết các sản phẩm của lô hàng đó (Tên sản phẩm, mã SKU, số lượng nhập, đơn giá nhập và thành tiền).
*   **Logic xử lý & Nghiệp vụ:**
    *   Cho phép tìm kiếm nhanh phiếu nhập theo mã phiếu hoặc tên nhà cung cấp đối tác.

---

<a name="14-trang-lich-su-don-hang"></a>
### 14. 📄 Trang Lịch Sử Đơn Hàng (`Order.fxml`)
*   **Mục đích:** Quản lý và tra cứu thông tin tất cả các hóa đơn bán lẻ đã giao dịch tại cửa hàng.
*   **Vai trò truy cập:** Admin / Staff.
*   **Các thành phần UI chính:**
    *   Thanh thống kê Doanh thu tích lũy mọi thời đại của cửa hàng.
    *   Bộ lọc tìm kiếm hóa đơn theo Mã hóa đơn, Tên/SĐT khách hàng và ComboBox lọc theo phương thức thanh toán.
    *   Bảng TableView hiển thị: Mã hóa đơn (định dạng `HDxxxxx`), Tên khách hàng (mặc định hiển thị "Khách lẻ" nếu không liên kết), Ngày bán, Phương thức thanh toán (gán nhãn màu sắc riêng biệt), Tổng tiền hóa đơn, Trạng thái đơn và nút Hành động (Chi tiết, Hủy đơn).
    *   **Popup Chi tiết Đơn hàng:** Bảng phụ hiển thị danh sách các món giày khách đã mua trong đơn hàng, số lượng và đơn giá thực tế lúc bán.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Logic Hủy đơn hàng và Hoàn trả kho (Chỉ dành cho Admin):** Khi Admin nhấn nút hủy một đơn hàng hợp lệ, hệ thống sẽ thực thi quy trình cập nhật đơn hàng thành trạng thái `'Da huy'` và lấy danh sách sản phẩm trong hóa đơn đó để **cộng ngược lại** vào số lượng tồn kho của các sản phẩm tương ứng, giúp số lượng hàng trong kho tự động được khôi phục chính xác.

---

<a name="15-trang-bao-cao-tai-chinh"></a>
### 15. 📊 Trang Báo Cáo Tài Chính (`Reports.fxml`)
*   **Mục đích:** Cung cấp các phân tích số liệu tài chính chuyên sâu và hỗ trợ xuất file Excel gửi cho ban giám đốc.
*   **Vai trò truy cập:** Chỉ dành cho Admin.
*   **Các thành phần UI chính:**
    *   **Bộ lọc thời gian:** Ô chọn ngày `Từ ngày` và `Đến ngày` (`DatePicker`).
    *   **Hộp chỉ số KPI:** Tổng doanh thu thực thu, Tổng lợi nhuận gộp ước tính, Tổng số sản phẩm giày đã bán trong khoảng thời gian được lọc.
    *   **Hệ thống Biểu đồ:**
        *   *Biểu đồ đường (LineChart):* Biến động doanh thu theo từng ngày.
        *   *Biểu đồ cột (BarChart):* Vinh danh Top 5 sản phẩm bán chạy nhất.
        *   *Biểu đồ tròn (PieChart):* Cơ cấu doanh thu theo từng nhóm danh mục giày.
    *   Nút bấm **Xuất Excel** báo cáo tài chính.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Truy vấn phân tích dữ liệu:** Sử dụng các câu lệnh SQL nâng cao để lấy và nhóm dữ liệu bán lẻ theo khoảng thời gian được chọn thời gian thực.
    *   **Ước tính lợi nhuận gộp:** Tính toán lợi nhuận dựa trên giá vốn nhập kho thực tế từ lịch sử nhập (đối với các giày có phiếu nhập) hoặc trừ đi 60% giá bán làm giá vốn ước định đối với các sản phẩm mặc định trong hệ thống để đưa ra bức tranh tài chính tương đối chính xác nhất.
    *   **Tích hợp Apache POI xuất Excel chuyên nghiệp:** Tạo file báo cáo `.xlsx` định dạng chuẩn hóa: Đổ màu nền Indigo cho dòng tiêu đề cột, định dạng số tiền tệ rõ ràng, tự động giãn độ rộng cột dựa trên dữ liệu và tự động chèn công thức tính tổng doanh thu cuối bảng.

---

<a name="16-trang-cai-dat-he-thong"></a>
### 16. ⚙️ Trang Cài Đặt Hệ Thống (`Settings.fxml`)
*   **Mục đích:** Cho phép người dùng chỉnh sửa thông tin cá nhân và thay đổi các cấu hình hệ thống cơ bản.
*   **Vai trò truy cập:** Admin / Staff.
*   **Các thành phần UI chính:**
    *   Form thông tin cá nhân: Họ tên, Email (cho phép cập nhật thông tin).
    *   Form đổi mật khẩu: Mật khẩu hiện tại, Mật khẩu mới, Xác nhận mật khẩu mới.
    *   Nút bấm **Lưu thay đổi** và **Cập nhật mật khẩu**.
*   **Logic xử lý & Nghiệp vụ:**
    *   **Đổi mật khẩu bảo mật:** Yêu cầu người dùng nhập mật khẩu hiện tại. Hệ thống băm SHA-256 mật khẩu hiện tại để so khớp trong DB. Nếu khớp, tiến hành băm mật khẩu mới và cập nhật vào CSDL.
    *   **Xác thực thông tin:** Kiểm tra định dạng Email hợp lệ khi cập nhật thông tin cá nhân.

---
*Tài liệu hướng dẫn chức năng các trang được biên soạn chi tiết cho dự án SoleManager.*
