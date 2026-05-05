package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import database.DBConnection;

public class    CategoryDAO {
    // Hàm lấy danh sách tên danh mục
    public List<String> getAllCategoryNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT TenDanhMuc FROM DanhMuc";
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            // Đọc dữ liệu
            while (rs.next()) {
                String ten = rs.getString("TenDanhMuc");
                list.add(ten);
            }
            // Đóng kết nối
            rs.close();
            ps.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Lỗi lấy danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
