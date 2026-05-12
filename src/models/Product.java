package models;

public class Product {
    private int id;
    private String productCode;
    private String name;
    private int categoryId;
    private String categoryName; // Lấy từ bảng categories để hiển thị lên bảng (JOIN)
    private double price;
    private int stock;
    private String size;
    private String imagePath;

    public Product() {}

    public Product(int id, String productCode, String name, int categoryId, String categoryName, double price, int stock, String size, String imagePath) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.price = price;
        this.stock = stock;
        this.size = size;
        this.imagePath = imagePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
