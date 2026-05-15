package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import models.Customer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    public static class InvoiceItem {
        public String productName;
        public int quantity;
        public double unitPrice;
        public double lineTotal;

        public InvoiceItem(String productName, int quantity, double unitPrice, double lineTotal) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }
    }

    public static File generateInvoice(int orderId, Customer customer, List<InvoiceItem> items, double totalAmount, String paymentMethod) {
        // Ensure invoices directory exists
        File outDir = new File("invoices");
        if (!outDir.exists()) {
            outDir.mkdir();
        }

        String fileName = "invoices/HoaDon_HD" + String.format("%05d", orderId) + ".pdf";
        File file = new File(fileName);

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            // Header
            Paragraph title = new Paragraph("DUCBINZ SNEAKER", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subTitle = new Paragraph("HOA DON BAN HANG\n\n", boldFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            // Info
            document.add(new Paragraph("Ma hoa don: HD" + String.format("%05d", orderId), normalFont));
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            document.add(new Paragraph("Ngay ban: " + dtf.format(LocalDateTime.now()), normalFont));
            
            if (customer != null) {
                // Converting to basic ascii for safety without custom TTF
                String cName = removeAccents(customer.getFullName());
                document.add(new Paragraph("Khach hang: " + cName, normalFont));
                document.add(new Paragraph("Dien thoai: " + customer.getPhone(), normalFont));
            } else {
                document.add(new Paragraph("Khach hang: Khach le", normalFont));
            }
            document.add(new Paragraph("Hinh thuc thanh toan: " + removeAccents(paymentMethod) + "\n\n", normalFont));

            // Table
            PdfPTable table = new PdfPTable(4); // 4 columns
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1f, 2f, 2f});

            // Table Headers
            table.addCell(new PdfPCell(new Phrase("Ten San Pham", boldFont)));
            table.addCell(new PdfPCell(new Phrase("SL", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Don Gia", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Thanh Tien", boldFont)));

            // Table Rows
            for (InvoiceItem item : items) {
                table.addCell(new PdfPCell(new Phrase(removeAccents(item.productName), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(item.quantity), normalFont)));
                table.addCell(new PdfPCell(new Phrase(formatCurrency(item.unitPrice), normalFont)));
                table.addCell(new PdfPCell(new Phrase(formatCurrency(item.lineTotal), normalFont)));
            }

            document.add(table);

            // Total
            Paragraph total = new Paragraph("\nTong cong: " + formatCurrency(totalAmount), boldFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            // Footer
            Paragraph footer = new Paragraph("\nXin cam on va hen gap lai!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + " VND";
    }

    private static String removeAccents(String str) {
        if (str == null) return "";
        String nfdNormalizedString = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD); 
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replace("Đ", "D").replace("đ", "d");
    }
}
