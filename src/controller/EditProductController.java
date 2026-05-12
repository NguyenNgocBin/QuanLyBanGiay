package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Product;
import models.Category;
import DAO.ProductDAO;

import java.util.List;

import DAO.CategoryDAO;

public class EditProductController {

    @FXML
    private ComboBox<Category> cbCategory;
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
        List<Category> categories = categoryDAO.getAllCategories();
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll(categories);
    }

    public void setProductData(Product product) {
        this.product = product;
        txtName.setText(product.getName());
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtSize.setText(product.getSize());
        txtStock.setText(String.valueOf(product.getStock()));
        // Optionally select the category in combobox
        for (Category c : cbCategory.getItems()) {
            if (c.getId() == product.getCategoryId()) {
                cbCategory.getSelectionModel().select(c);
                break;
            }
        }
    }

    private void closeStage() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    @FXML
    void buttonSave(ActionEvent event) {
        try {
            product.setName(txtName.getText());
            product.setSize(txtSize.getText());
            product.setPrice(Double.parseDouble(txtPrice.getText()));
            product.setStock(Integer.parseInt(txtStock.getText()));
            if (cbCategory.getValue() != null) {
                product.setCategoryId(cbCategory.getValue().getId());
            }
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
