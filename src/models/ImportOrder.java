package models;

public class ImportOrder {
    private int id;
    private int supplierId;
    private double totalAmount;
    private String importDate;
    private String status;
    private String supplierName;
    private int totalItems;

    public ImportOrder() {
    }

    public ImportOrder(int id, int supplierId, double totalAmount, String importDate, String status, String supplierName, int totalItems) {
        this.id = id;
        this.supplierId = supplierId;
        this.totalAmount = totalAmount;
        this.importDate = importDate;
        this.status = status;
        this.supplierName = supplierName;
        this.totalItems = totalItems;
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

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getTotalItems() { return totalItems; }
    public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
}
