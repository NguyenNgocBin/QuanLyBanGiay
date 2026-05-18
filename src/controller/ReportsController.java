package controller;

import DAO.ReportsDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

public class ReportsController {

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Button btnFilter;
    @FXML private Button btnExport;
    @FXML private Label lblRevenue;
    @FXML private Label lblProfit;
    @FXML private Label lblItemsSold;
    
    @FXML private LineChart<String, Number> chartRevenue;
    @FXML private PieChart chartCategories;
    @FXML private BarChart<String, Number> chartTopProducts;

    private final ReportsDAO reportsDAO = new ReportsDAO();

    @FXML
    public void initialize() {
        // Set default filter date to the last 30 days
        dpEnd.setValue(LocalDate.now());
        dpStart.setValue(LocalDate.now().minusDays(30));

        loadReportData();
    }

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

        loadReportData();
    }

    private void loadReportData() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        // 1. Load KPI cards
        double revenue = reportsDAO.getTotalRevenue(start, end);
        double profit = reportsDAO.getTotalProfit(start, end);
        int sold = reportsDAO.getTotalItemsSold(start, end);

        lblRevenue.setText(formatCurrency(revenue));
        lblProfit.setText(formatCurrency(profit));
        lblItemsSold.setText(String.format("%,d sp", sold));

        // 2. Render Revenue Trend Line Chart
        chartRevenue.getData().clear();
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        Map<String, Double> trend = reportsDAO.getRevenueTrend(start, end);
        for (Map.Entry<String, Double> entry : trend.entrySet()) {
            revenueSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chartRevenue.getData().add(revenueSeries);

        // 3. Render Category Pie Chart
        chartCategories.getData().clear();
        Map<String, Double> catRev = reportsDAO.getCategoryRevenue(start, end);
        for (Map.Entry<String, Double> entry : catRev.entrySet()) {
            chartCategories.getData().add(new PieChart.Data(entry.getKey() + " (" + formatCompactCurrency(entry.getValue()) + ")", entry.getValue()));
        }

        // 4. Render Top Selling Products Bar Chart
        chartTopProducts.getData().clear();
        XYChart.Series<String, Number> topSeries = new XYChart.Series<>();
        Map<String, Integer> top = reportsDAO.getTopSellingProducts(start, end);
        for (Map.Entry<String, Integer> entry : top.entrySet()) {
            topSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chartTopProducts.getData().add(topSeries);
    }

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
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Báo cáo Doanh Thu");

            // Fonts & Styles
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            // Title block
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("BÁO CÁO THỐNG KÊ DOANH THU & PHÂN TÍCH KINH DOANH");
            
            org.apache.poi.ss.usermodel.Row dateRow = sheet.createRow(1);
            dateRow.createCell(0).setCellValue("Thời gian lọc: từ " + start + " đến " + end);

            // KPI stats row
            org.apache.poi.ss.usermodel.Row kpiRow = sheet.createRow(3);
            kpiRow.createCell(0).setCellValue("Tổng Doanh Thu:");
            kpiRow.createCell(1).setCellValue(lblRevenue.getText());
            kpiRow.createCell(3).setCellValue("Ước Tính Lợi Nhuận:");
            kpiRow.createCell(4).setCellValue(lblProfit.getText());

            // Table headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(5);
            String[] headers = {"STT", "Thời gian", "Doanh thu (VND)"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Write sales trend rows
            int rowIdx = 6;
            int stt = 1;
            Map<String, Double> trend = reportsDAO.getRevenueTrend(start, end);
            for (Map.Entry<String, Double> entry : trend.entrySet()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(entry.getKey());
                row.createCell(2).setCellValue(entry.getValue());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
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

    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ";
    }

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
}
