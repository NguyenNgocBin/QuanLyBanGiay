package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBConnection;
import models.Product;

public class ProductDAO {

    public boolean insertProduct(String id, String name, String category, long price, int stock, String size,
            String imagePath) {
        String sql = "INSERT INTO products (id, name, category, price, stock, size, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(sql)) {

            pst.setString(1, id);
            pst.setString(2, name);
            pst.setString(3, category);
            pst.setDouble(4, price);
            pst.setInt(5, stock);
            pst.setString(6, size);
            pst.setString(7, imagePath);

            int rowsInserted = pst.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(sql);
                java.sql.ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Product p = new Product();

                p.setId(rs.getString("Id"));
                p.setName(rs.getString("Name"));
                p.setCategory(rs.getString("Category"));
                p.setPrice(rs.getLong("Price"));
                p.setStock(rs.getInt("Stock"));
                p.setSize(rs.getString("Size"));
                p.setImage_path(rs.getString("Image_path"));

                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (Id, Name, Category, Price, Stock, Size, Image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getId());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setInt(5, product.getStock());
            pstmt.setString(6, product.getSize());
            pstmt.setString(7, product.getImage_path());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Hàm tìm kiếm sản phẩm theo tên
    public List<Product> searchByName(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE Name LIKE ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setString(1, "%" + keyword + "%");

            try (java.sql.ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getString("Id"));
                    p.setName(rs.getString("Name"));
                    p.setCategory(rs.getString("Category"));
                    p.setPrice(rs.getLong("Price"));
                    p.setStock(rs.getInt("Stock"));
                    p.setSize(rs.getString("Size"));
                    p.setImage_path(rs.getString("Image_path"));

                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean deleteProduct(String Id) {
        String sql = "DELETE FROM products WHERE Id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, Id);
            return pst.executeUpdate() > 0; // Trả về true nếu xóa thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET Name = ?, Price = ?, Size = ?, Stock = ?, Category = ? WHERE Id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, product.getName());
            pst.setDouble(2, product.getPrice());
            pst.setString(3, product.getSize());
            pst.setInt(4, product.getStock());
            pst.setString(5, product.getCategory());
            pst.setString(5, product.getId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}