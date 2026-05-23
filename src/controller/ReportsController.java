package controller;

import DAO.ReportsDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.List;

/**
 * Controller quản lý hiển thị các báo cáo thống kê, biểu đồ phân tích và xuất dữ liệu Excel.
 */
public class ReportsController {

    // Khai báo các điều khiển FXML của trang báo cáo
    @FXML private ComboBox<String> cbQuickFilter;
    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Button btnFilter;
    @FXML private Button btnExport;
    @FXML private Label lblRevenue;
    @FXML private Label lblProfit;
    @FXML private Label lblItemsSold;
    @FXML private Label lblOrdersCount;
    @FXML private Label lblAOV;
    
    // Khai báo các biểu đồ còn lại
    @FXML private PieChart chartCategories;
    @FXML private BarChart<String, Number> chartTopProducts;
    @FXML private PieChart chartPayments;

    // Khai báo các bảng dữ liệu thống kê doanh thu và chi tiết mới
    @FXML private TableView<RevenueReportRow> tblRevenueTrend;
    @FXML private TableColumn<RevenueReportRow, String> colRevDate;
    @FXML private TableColumn<RevenueReportRow, String> colRevAmount;
    @FXML private TableColumn<RevenueReportRow, Integer> colRevOrdersCount;
    @FXML private TableColumn<RevenueReportRow, String> colRevStaffName;

    @FXML private TableView<ProductReportRow> tblTopProducts;
    @FXML private TableColumn<ProductReportRow, String> colProdName;
    @FXML private TableColumn<ProductReportRow, Integer> colProdQty;

    @FXML private TableView<StaffReportRow> tblTopStaff;
    @FXML private TableColumn<StaffReportRow, String> colStaffName;
    @FXML private TableColumn<StaffReportRow, String> colStaffRev;

    private final ReportsDAO reportsDAO = new ReportsDAO();

    /**
     * Phương thức khởi tạo tự động chạy khi View FXML được nạp.
     * Dùng để cài đặt bộ chọn nhanh, ngày mặc định và ánh xạ cột TableView.
     */
    @FXML
    public void initialize() {
        // Khởi tạo ComboBox chọn nhanh mốc thời gian phổ biến
        cbQuickFilter.setItems(FXCollections.observableArrayList(
            "30 ngày qua", "Hôm nay", "Hôm qua", "7 ngày qua", "Tháng này", "Tháng trước"
        ));
        
        // Gắn sự kiện lắng nghe khi người dùng chọn mốc thời gian nhanh
        cbQuickFilter.setOnAction(event -> handleQuickFilter());

        // Cấu hình liên kết dữ liệu cột cho bảng doanh thu theo thời gian
        colRevDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        colRevAmount.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getRevenue())));
        colRevOrdersCount.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getOrdersCount()));
        colRevStaffName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStaffName()));

        // Cấu hình liên kết dữ liệu cột cho bảng danh sách sản phẩm bán chạy
        colProdName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProdQty.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));

        // Cấu hình liên kết dữ liệu cột cho bảng danh sách doanh thu nhân viên (ca hiện tại)
        colStaffName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colStaffRev.setCellValueFactory(data -> new SimpleStringProperty(formatCurrency(data.getValue().getRevenue())));

        // Thiết lập thời gian mặc định là 30 ngày qua và tải dữ liệu báo cáo lần đầu
        dpEnd.setValue(LocalDate.now());
        dpStart.setValue(LocalDate.now().minusDays(30));
        cbQuickFilter.setValue("30 ngày qua");

        loadReportData();
    }

    /**
     * Sự kiện nút "Lọc" khi người dùng chọn thủ công Ngày bắt đầu và Ngày kết thúc.
     */
    @FXML
    void handleFilter(ActionEvent event) {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        if (start == null || end == null) {
            showError("Vui lòng chọn đầy đủ từ ngày và đến ngày!");
            return;
        }

        if (start.isAfter(end)) {
            showError("Từ ngày không thể lớn hơn đến ngày!");
            return;
        }

        // Xóa giá trị ComboBox chọn nhanh để tránh nhầm lẫn khi tự chọn ngày
        cbQuickFilter.setValue(null);

        loadReportData();
    }

    /**
     * Xử lý tính toán tự động ngày bắt đầu/kết thúc khi chọn mốc nhanh.
     */
    private void handleQuickFilter() {
        String option = cbQuickFilter.getValue();
        if (option == null) return;

        LocalDate today = LocalDate.now();
        switch (option) {
            case "Hôm nay" -> {
                dpStart.setValue(today);
                dpEnd.setValue(today);
            }
            case "Hôm qua" -> {
                dpStart.setValue(today.minusDays(1));
                dpEnd.setValue(today.minusDays(1));
            }
            case "7 ngày qua" -> {
                dpStart.setValue(today.minusDays(7));
                dpEnd.setValue(today);
            }
            case "30 ngày qua" -> {
                dpStart.setValue(today.minusDays(30));
                dpEnd.setValue(today);
            }
            case "Tháng này" -> {
                dpStart.setValue(today.withDayOfMonth(1));
                dpEnd.setValue(today);
            }
            case "Tháng trước" -> {
                LocalDate firstOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
                LocalDate lastOfLastMonth = today.minusMonths(1).with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                dpStart.setValue(firstOfLastMonth);
                dpEnd.setValue(lastOfLastMonth);
            }
        }
        loadReportData();
    }

    /**
     * Tải và làm mới toàn bộ số liệu KPI, biểu đồ và các bảng dữ liệu thống kê chi tiết.
     */
    private void loadReportData() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        // 1. Tải và hiển thị các số liệu trên thẻ KPI
        double revenue = reportsDAO.getTotalRevenue(start, end);
        double profit = reportsDAO.getTotalProfit(start, end);
        int sold = reportsDAO.getTotalItemsSold(start, end);
        int ordersCount = reportsDAO.getTotalOrdersCount(start, end);
        double aov = ordersCount > 0 ? revenue / ordersCount : 0.0;

        lblRevenue.setText(formatCurrency(revenue));
        lblProfit.setText(formatCurrency(profit));
        lblItemsSold.setText(String.format("%,d sp", sold));
        lblOrdersCount.setText(String.format("%,d đơn", ordersCount));
        lblAOV.setText(formatCurrency(aov));

        // 2. Nạp dữ liệu vào bảng doanh thu theo thời gian (thay cho biểu đồ đường)
        tblRevenueTrend.getItems().clear();
        List<ReportsDAO.RevenueTrendData> trend = reportsDAO.getRevenueTrendDetail(start, end);
        for (ReportsDAO.RevenueTrendData data : trend) {
            tblRevenueTrend.getItems().add(new RevenueReportRow(data.date(), data.revenue(), data.ordersCount(), data.staffNames()));
        }

        // 3. Vẽ biểu đồ cơ cấu danh mục (PieChart)
        chartCategories.getData().clear();
        Map<String, Double> catRev = reportsDAO.getCategoryRevenue(start, end);
        for (Map.Entry<String, Double> entry : catRev.entrySet()) {
            chartCategories.getData().add(new PieChart.Data(entry.getKey() + " (" + formatCompactCurrency(entry.getValue()) + ")", entry.getValue()));
        }

        // 4. Vẽ biểu đồ cột Top 5 sản phẩm bán chạy (BarChart)
        chartTopProducts.getData().clear();
        XYChart.Series<String, Number> topSeries = new XYChart.Series<>();
        Map<String, Integer> top = reportsDAO.getTopSellingProducts(start, end);
        for (Map.Entry<String, Integer> entry : top.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            // Gắn Tooltip hiển thị số lượng đôi bán được khi di chuột qua cột biểu đồ
            data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip tooltip = new Tooltip(String.format("%,d đôi", data.getYValue().intValue()));
                    Tooltip.install(newNode, tooltip);
                }
            });
            topSeries.getData().add(data);
        }
        chartTopProducts.getData().add(topSeries);

        // 5. Vẽ biểu đồ cơ cấu phương thức thanh toán (PieChart)
        chartPayments.getData().clear();
        Map<String, Double> paymentDist = reportsDAO.getPaymentMethodDistribution(start, end);
        for (Map.Entry<String, Double> entry : paymentDist.entrySet()) {
            chartPayments.getData().add(new PieChart.Data(entry.getKey() + " (" + formatCompactCurrency(entry.getValue()) + ")", entry.getValue()));
        }

        // 6. Nạp dữ liệu chi tiết vào bảng "Sản phẩm bán chạy"
        tblTopProducts.getItems().clear();
        for (Map.Entry<String, Integer> entry : top.entrySet()) {
            tblTopProducts.getItems().add(new ProductReportRow(entry.getKey(), entry.getValue()));
        }

        // 7. Nạp dữ liệu chi tiết vào bảng "Doanh thu Nhân viên" (Ca hiện tại)
        tblTopStaff.getItems().clear();
        Map<String, Double> staffRevMap = reportsDAO.getTopStaffRevenue();
        for (Map.Entry<String, Double> entry : staffRevMap.entrySet()) {
            tblTopStaff.getItems().add(new StaffReportRow(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Xuất báo cáo chi tiết nhiều Sheet ra định dạng Excel (XLSX).
     */
    @FXML
    void handleExportExcel(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu báo cáo Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("BaoCaoDoanhThu_SoleManager_" + LocalDate.now() + ".xlsx");
        
        File file = fileChooser.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) return;

        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            // Thiết lập phong cách cho tiêu đề bảng
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            // PAGE 1: DOANH THU & KPI CHUNG
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Doanh Thu Chung");

            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("BÁO CÁO THỐNG KÊ DOANH THU & PHÂN TÍCH KINH DOANH");
            
            org.apache.poi.ss.usermodel.Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Thời gian lọc: từ " + start + " đến " + end);

            // Ghi các chỉ số tài chính KPI chính
            org.apache.poi.ss.usermodel.Row kpiRow = sheet.createRow(3);
            kpiRow.createCell(0).setCellValue("Tổng Doanh Thu:");
            kpiRow.createCell(1).setCellValue(lblRevenue.getText());
            kpiRow.createCell(3).setCellValue("Ước Tính Lợi Nhuận:");
            kpiRow.createCell(4).setCellValue(lblProfit.getText());

            org.apache.poi.ss.usermodel.Row kpiRow2 = sheet.createRow(4);
            kpiRow2.createCell(0).setCellValue("Tổng Hóa Đơn:");
            kpiRow2.createCell(1).setCellValue(lblOrdersCount.getText());
            kpiRow2.createCell(3).setCellValue("Giá trị TB đơn (AOV):");
            kpiRow2.createCell(4).setCellValue(lblAOV.getText());

            // Thiết lập tiêu đề cho bảng doanh thu xu hướng
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(6);
            String[] headers = {"STT", "Thời gian", "Số lượng đơn hàng", "Doanh thu (VND)", "Nhân viên bán trong ca"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Điền dữ liệu doanh thu hàng ngày
            int rowIdx = 7;
            int stt = 1;
            List<ReportsDAO.RevenueTrendData> trend = reportsDAO.getRevenueTrendDetail(start, end);
            for (ReportsDAO.RevenueTrendData data : trend) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(data.date());
                row.createCell(2).setCellValue(data.ordersCount());
                row.createCell(3).setCellValue(data.revenue());
                row.createCell(4).setCellValue(data.staffNames());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // PAGE 2: DOANH THU THEO DANH MỤC
            org.apache.poi.ss.usermodel.Sheet sheetCat = workbook.createSheet("Doanh Thu Danh Mục");
            org.apache.poi.ss.usermodel.Row catTitleRow = sheetCat.createRow(0);
            catTitleRow.createCell(0).setCellValue("CƠ CẤU DOANH THU THEO DANH MỤC SẢN PHẨM");
            org.apache.poi.ss.usermodel.Row catDateRow = sheetCat.createRow(1);
            catDateRow.createCell(0).setCellValue("Thời gian: từ " + start + " đến " + end);

            org.apache.poi.ss.usermodel.Row catHeaderRow = sheetCat.createRow(3);
            String[] catHeaders = {"STT", "Tên danh mục", "Doanh thu (VND)"};
            for (int i = 0; i < catHeaders.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = catHeaderRow.createCell(i);
                cell.setCellValue(catHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            int catRowIdx = 4;
            int catStt = 1;
            Map<String, Double> catRev = reportsDAO.getCategoryRevenue(start, end);
            for (Map.Entry<String, Double> entry : catRev.entrySet()) {
                org.apache.poi.ss.usermodel.Row row = sheetCat.createRow(catRowIdx++);
                row.createCell(0).setCellValue(catStt++);
                row.createCell(1).setCellValue(entry.getKey());
                row.createCell(2).setCellValue(entry.getValue());
            }
            for (int i = 0; i < catHeaders.length; i++) {
                sheetCat.autoSizeColumn(i);
            }

            // PAGE 3: SẢN PHẨM BÁN CHẠY
            org.apache.poi.ss.usermodel.Sheet sheetTop = workbook.createSheet("Top Sản Phẩm Bán Chạy");
            org.apache.poi.ss.usermodel.Row topTitleRow = sheetTop.createRow(0);
            topTitleRow.createCell(0).setCellValue("TOP SẢN PHẨM BÁN CHẠY NHẤT");
            org.apache.poi.ss.usermodel.Row topDateRow = sheetTop.createRow(1);
            topDateRow.createCell(0).setCellValue("Thời gian: từ " + start + " đến " + end);

            org.apache.poi.ss.usermodel.Row topHeaderRow = sheetTop.createRow(3);
            String[] topHeaders = {"STT", "Tên sản phẩm", "Số lượng bán (Đôi)"};
            for (int i = 0; i < topHeaders.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = topHeaderRow.createCell(i);
                cell.setCellValue(topHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            int topRowIdx = 4;
            int topStt = 1;
            Map<String, Integer> topProd = reportsDAO.getTopSellingProducts(start, end);
            for (Map.Entry<String, Integer> entry : topProd.entrySet()) {
                org.apache.poi.ss.usermodel.Row row = sheetTop.createRow(topRowIdx++);
                row.createCell(0).setCellValue(topStt++);
                row.createCell(1).setCellValue(entry.getKey());
                row.createCell(2).setCellValue(entry.getValue());
            }
            for (int i = 0; i < topHeaders.length; i++) {
                sheetTop.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            showInfo("Xuất Excel thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Xuất Excel thất bại: " + e.getMessage());
        }
    }

    /**
     * Định dạng số tiền thành VND (ví dụ: 100.000đ).
     */
    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ";
    }

    /**
     * Định dạng tiền gọn hiển thị trên nhãn biểu đồ (ví dụ: 5.5M cho 5.500.000đ).
     */
    private String formatCompactCurrency(double value) {
        if (value >= 1_000_000_000) {
            return String.format(Locale.US, "%.1fB", value / 1_000_000_000);
        }
        if (value >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", value / 1_000_000);
        }
        return formatCurrency(value);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ====================================================
    // LỚP HỖ TRỢ ĐỊNH NGHĨA DÒNG DỮ LIỆU DOANH THU THEO THỜI GIAN
    // ====================================================
    public static class RevenueReportRow {
        private final String date;
        private final double revenue;
        private final int ordersCount;
        private final String staffName;

        public RevenueReportRow(String date, double revenue, int ordersCount, String staffName) {
            this.date = date;
            this.revenue = revenue;
            this.ordersCount = ordersCount;
            this.staffName = staffName;
        }

        public String getDate() { return date; }
        public double getRevenue() { return revenue; }
        public int getOrdersCount() { return ordersCount; }
        public String getStaffName() { return staffName; }
    }

    // ====================================================
    // LỚP HỖ TRỢ ĐỊNH NGHĨA DÒNG DỮ LIỆU BẢNG SẢN PHẨM BÁN CHẠY
    // ====================================================
    public static class ProductReportRow {
        private final String name;
        private final int quantity;

        public ProductReportRow(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
    }

    // ====================================================
    // LỚP HỖ TRỢ ĐỊNH NGHĨA DÒNG DỮ LIỆU BẢNG DOANH THU NHÂN VIÊN
    // ====================================================
    public static class StaffReportRow {
        private final String name;
        private final double revenue;

        public StaffReportRow(String name, double revenue) {
            this.name = name;
            this.revenue = revenue;
        }

        public String getName() { return name; }
        public double getRevenue() { return revenue; }
    }
}
