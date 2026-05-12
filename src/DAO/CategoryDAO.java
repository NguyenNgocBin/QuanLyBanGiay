package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import database.DBConnection;
import models.Category;

public class CategoryDAO {
    // Hàm lấy tất cả danh mục
    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name FROM categories";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name")));
            }

        } catch (Exception e) {
            System.out.println("Lỗi lấy danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // Hàm lấy danh sách tên danh mục (để tương thích ngược với code cũ nếu cần)
    public List<String> getAllCategoryNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM categories";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                list.add(rs.getString("name"));
            }

        } catch (Exception e) {
            System.out.println("Lỗi lấy danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
