package models;

import java.sql.Date;

public class Oder {
    private int id;
    private String customerName;
    private Long total;
    private Date orderDate;
    private String status;

    // tránh lỗi mặc định
    public Oder() {
    }

    public Oder(int id, String customerName, Long total, Date orderDate, String status) {
        this.id = id;
        this.customerName = customerName;
        this.total = total;
        this.orderDate = orderDate;
        this.status = status;
    }

    public Oder(String customerName, Long total, Date orderDate, String status) {
        this.customerName = customerName;
        this.total = total;
        this.orderDate = orderDate;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}