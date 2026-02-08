package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Product;
import DAO.ProductDAO;

import java.util.List;

import DAO.CategoryDAO;

public class EditProductController {

    @FXML
    private ComboBox<String> cbCategory;
    @FXML
    private TextField txtId;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPrice;
    @FXML
    private TextField txtSize;
    @FXML
    private TextField txtStock;

    private Product product;
    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();

    @FXML
    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        List<String> categories = categoryDAO.getAllCategoryNames();
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll(categories);
    }

    // Nhận dữ liệu từ bảng quyền qua
    public void setProductData(Product product) {
        this.product = product;
        txtName.setText(product.getName());
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtSize.setText(product.getSize());
        txtStock.setText(String.valueOf(product.getStock()));
    }

    private void closeStage() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    @FXML
    void buttonCancel(ActionEvent event) {

    }

    @FXML
    void buttonSave(ActionEvent event) {
        try {
            product.setName(txtName.getText());
            product.setSize(txtSize.getText());
            // Dùng Double.parseDouble cho giá tiền
            product.setPrice(Long.parseLong(txtPrice.getText()));
            // Dùng Integer.parseInt cho số lượng
            product.setStock(Integer.parseInt(txtStock.getText()));
            // Đóng cửa sổ sau khi lưu xong
            if (productDAO.updateProduct(product)) {
                closeStage();
            }

        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Giá và Số lượng phải là số!");
        }
    }

    @FXML
    private void buttonCancel() {
        closeStage();
    }

}
