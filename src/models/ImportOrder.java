package models;

public class ImportOrder {
    private int id;
    private int supplierId;
    private double totalAmount;
    private String importDate;
    private String status;

    public ImportOrder() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getImportDate() { return importDate; }
    public void setImportDate(String importDate) { this.importDate = importDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
