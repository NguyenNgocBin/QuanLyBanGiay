package models;

public class Customer {
    private int id;
    private String customerCode;
    private String fullName;
    private String phone;
    private String email;
    private double totalSpent;

    public Customer(int id, String customerCode, String fullName, String phone, String email, double totalSpent) {
        this.id = id;
        this.customerCode = customerCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.totalSpent = totalSpent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }
}
