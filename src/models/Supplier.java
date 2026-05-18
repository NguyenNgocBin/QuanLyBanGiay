package models;

public class Supplier {
    private int id;
    private String supplierCode;
    private String name;
    private String phone;
    private String email;
    private String address;
    private int importCount;

    public Supplier() {
    }

    public Supplier(int id, String supplierCode, String name, String phone, String email, String address) {
        this(id, supplierCode, name, phone, email, address, 0);
    }

    public Supplier(int id, String supplierCode, String name, String phone, String email, String address, int importCount) {
        this.id = id;
        this.supplierCode = supplierCode;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.importCount = importCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getImportCount() { return importCount; }
    public void setImportCount(int importCount) { this.importCount = importCount; }

    @Override
    public String toString() {
        return name; // Useful for ComboBox
    }
}
