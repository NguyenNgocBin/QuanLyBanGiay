package models;

public class Product {
    private String Id;
    private String Name;
    private String Category;
    private long Price;
    private int Stock;
    private String Size;
    private String Image_path;

    public Product() {
    }

    public Product(String Id, String Name, String Category, long Price, int Stock, String Size, String Image_path) {
        this.Id = Id;
        this.Name = Name;
        this.Category = Category;
        this.Price = Price;
        this.Stock = Stock;
        this.Size = Size;
        this.Image_path = Image_path;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String Category) {
        this.Category = Category;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(long Price) {
        this.Price = Price;
    }

    public int getStock() {
        return Stock;
    }

    public void setStock(int Stock) {
        this.Stock = Stock;
    }

    public String getSize() {
        return Size;
    }

    public void setSize(String Size) {
        this.Size = Size;
    }

    public String getImage_path() {
        return Image_path;
    }

    public void setImage_path(String Image_path) {
        this.Image_path = Image_path;
    }
}
