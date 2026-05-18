package DAO;

import database.DBConnection;
import models.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private static final String SELECT_WITH_CATEGORY = """
            SELECT p.id, p.product_code, p.name, p.category_id, c.name AS category_name,
                   p.price, p.stock, p.size, p.image_path
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            """;

    public List<Product> getAll() {
        List<Product> products = new ArrayList<>();
        String sql = SELECT_WITH_CATEGORY + " ORDER BY p.id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapResultSetToProduct(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public Product getByProductCode(String productCode) {
        String sql = SELECT_WITH_CATEGORY + " WHERE p.product_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToProduct(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addProduct(Product product) {
        String sql = """
                INSERT INTO products (product_code, name, category_id, price, stock, size, image_path)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            fillInsertStatement(statement, product);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addProductWithImport(Product product, int supplierId, double importPrice) {
        String sqlProduct = """
                INSERT INTO products (product_code, name, category_id, price, stock, size, image_path)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String sqlOrder = "INSERT INTO import_orders (supplier_id, total_amount, import_date, status) VALUES (?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO import_details (import_id, product_id, quantity, import_price) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int productId = 0;
            // 1. Insert product
            try (PreparedStatement ps = con.prepareStatement(sqlProduct, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                fillInsertStatement(ps, product);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        productId = rs.getInt(1);
                    } else {
                        throw new SQLException("Lỗi lấy ID sản phẩm mới.");
                    }
                }
            }

            // 2. Insert import order if stock > 0 and supplierId is provided
            if (product.getStock() > 0 && supplierId > 0) {
                double totalAmount = product.getStock() * importPrice;
                int importId = 0;
                try (PreparedStatement psOrder = con.prepareStatement(sqlOrder, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    psOrder.setInt(1, supplierId);
                    psOrder.setDouble(2, totalAmount);
                    psOrder.setString(3, java.time.LocalDate.now().toString());
                    psOrder.setString(4, "Hoàn thành");
                    psOrder.executeUpdate();
                    try (ResultSet rs = psOrder.getGeneratedKeys()) {
                        if (rs.next()) {
                            importId = rs.getInt(1);
                        } else {
                            throw new SQLException("Lỗi lấy ID phiếu nhập.");
                        }
                    }
                }

                // 3. Insert import detail
                try (PreparedStatement psDetail = con.prepareStatement(sqlDetail)) {
                    psDetail.setInt(1, importId);
                    psDetail.setInt(2, productId);
                    psDetail.setInt(3, product.getStock());
                    psDetail.setDouble(4, importPrice);
                    psDetail.executeUpdate();
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public boolean insertProduct(String productCode, String name, int categoryId, double price, int stock, String size, String imagePath) {
        return addProduct(new Product(0, productCode, name, categoryId, price, stock, size, imagePath));
    }

    public boolean updateProduct(Product product) {
        String sql = """
                UPDATE products
                SET name = ?, category_id = ?, price = ?, stock = ?, size = ?, image_path = ?
                WHERE product_code = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            setNullableCategory(statement, 2, product.getCategoryId());
            statement.setDouble(3, product.getPrice());
            statement.setInt(4, product.getStock());
            statement.setString(5, product.getSize());
            statement.setString(6, product.getImagePath());
            statement.setString(7, product.getProductCode());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(String productCode) {
        String sql = "DELETE FROM products WHERE product_code = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, productCode);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> search(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = SELECT_WITH_CATEGORY + """
                WHERE LOWER(p.product_code) LIKE ?
                   OR LOWER(p.name) LIKE ?
                   OR LOWER(COALESCE(c.name, '')) LIKE ?
                ORDER BY p.id DESC
                """;

        String searchText = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, searchText);
            statement.setString(2, searchText);
            statement.setString(3, searchText);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapResultSetToProduct(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getByStockStatus(String status) {
        String condition = switch (status) {
            case "Còn hàng" -> "p.stock > 10";
            case "Sắp hết" -> "p.stock > 0 AND p.stock <= 10";
            case "Hết hàng" -> "p.stock <= 0";
            default -> "1 = 1";
        };

        List<Product> products = new ArrayList<>();
        String sql = SELECT_WITH_CATEGORY + " WHERE " + condition + " ORDER BY p.id DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapResultSetToProduct(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return products;
    }

    public int countAll() {
        return queryInt("SELECT COUNT(*) FROM products");
    }

    public int countLowStock() {
        return queryInt("SELECT COUNT(*) FROM products WHERE stock > 0 AND stock <= 10");
    }

    public int countCategories() {
        return queryInt("SELECT COUNT(DISTINCT category_id) FROM products WHERE category_id IS NOT NULL");
    }

    public double getInventoryValue() {
        String sql = "SELECT COALESCE(SUM(price * stock), 0) FROM products";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private int queryInt(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void fillInsertStatement(PreparedStatement statement, Product product) throws SQLException {
        statement.setString(1, product.getProductCode());
        statement.setString(2, product.getName());
        setNullableCategory(statement, 3, product.getCategoryId());
        statement.setDouble(4, product.getPrice());
        statement.setInt(5, product.getStock());
        statement.setString(6, product.getSize());
        statement.setString(7, product.getImagePath());
    }

    private void setNullableCategory(PreparedStatement statement, int index, int categoryId) throws SQLException {
        if (categoryId <= 0) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, categoryId);
        }
    }

    private Product mapResultSetToProduct(ResultSet resultSet) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("product_code"),
                resultSet.getString("name"),
                resultSet.getInt("category_id"),
                resultSet.getString("category_name"),
                resultSet.getDouble("price"),
                resultSet.getInt("stock"),
                resultSet.getString("size"),
                resultSet.getString("image_path")
        );
    }
}
