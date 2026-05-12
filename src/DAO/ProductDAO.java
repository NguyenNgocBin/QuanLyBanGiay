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
