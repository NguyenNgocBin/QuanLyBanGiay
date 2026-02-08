package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import database.DBConnection;
import models.Oder;

public class OderDAO {
    // Lấy toàn bộ danh sách đơn hàng
    public List<Oder> getAllOder() {
        List<Oder> list = new ArrayList<>();
        String sql = "SELECT * FROM testlogin.oder";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pst = connection.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Oder oder = new Oder();
                oder.setId(rs.getInt("id"));
                oder.setCustomerName(rs.getString("customer_name"));
                oder.setTotal(rs.getLong("total"));
                oder.setOrderDate(rs.getDate("order_date"));
                oder.setStatus(rs.getString("status"));
                list.add(oder);
            }
        } catch (Exception e) {
            System.out.println("LỖI KẾT NỐI CSDL: " + e.getMessage()); // Thêm dòng này
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateOder(Oder oder) {
        String sql = "UPDATE oder SET customer_name=?, total=?, status=? WHERE id=?";

        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, oder.getCustomerName());
            pstmt.setDouble(2, oder.getTotal());
            pstmt.setString(3, oder.getStatus());
            pstmt.setInt(4, oder.getId());

            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0; // Trả về true nếu sửa thành công

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteOder(int id) {
        String sql = "DELETE FROM oder WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, id);
            return pst.executeUpdate() > 0; // Trả về true nếu xóa thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
