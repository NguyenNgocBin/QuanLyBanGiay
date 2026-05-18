package models;

public class Category {
    private int id;
    private String name;
    private int productCount;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
        this.productCount = 0;
    }

    public Category(int id, String name, int productCount) {
        this.id = id;
        this.name = name;
        this.productCount = productCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    @Override
    public String toString() {
        return name;
    }
}
