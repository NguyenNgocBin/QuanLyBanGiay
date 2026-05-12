package controller;

import DAO.CategoryDAO;
import DAO.ProductDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Category;
import models.Product;

import java.io.File;

public class EditProductController {

    @FXML private TextField txtName;
    @FXML private TextField txtProductCode;
    @FXML private ComboBox<Category> cbCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtSize;
    @FXML private TextField txtStock;
    @FXML private ImageView imgPreview;
    @FXML private Label lblMessage;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private Product product;
    private File selectedImage;

    @FXML
    public void initialize() {
        cbCategory.getItems().setAll(categoryDAO.getAllCategories());
    }

    public void setProductData(Product product) {
        this.product = product;
        txtName.setText(product.getName());
        txtProductCode.setText(product.getProductCode());
        txtPrice.setText(String.valueOf(product.getPrice()));
        txtSize.setText(product.getSize());
        txtStock.setText(String.valueOf(product.getStock()));
        selectCategory(product.getCategoryId());
        showCurrentImage(product.getImagePath());
    }

    @FXML
    private void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh sản phẩm");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(currentStage());
        if (file != null) {
            selectedImage = file;
            imgPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void save() {
        if (product == null) {
            lblMessage.setText("Không tìm thấy sản phẩm cần sửa.");
            return;
        }

        Category category = cbCategory.getValue();
        if (txtName.getText().trim().isBlank() || category == null) {
            lblMessage.setText("Vui lòng nhập tên sản phẩm và danh mục.");
            return;
        }

        try {
            product.setName(txtName.getText().trim());
            product.setCategoryId(category.getId());
            product.setCategoryName(category.getName());
            product.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            product.setStock(Integer.parseInt(txtStock.getText().trim()));
            product.setSize(txtSize.getText().trim());
            if (selectedImage != null) {
                product.setImagePath(selectedImage.getAbsolutePath());
            }

            if (productDAO.updateProduct(product)) {
                currentStage().close();
            } else {
                lblMessage.setText("Không thể cập nhật sản phẩm.");
            }
        } catch (NumberFormatException e) {
            lblMessage.setText("Giá bán và tồn kho phải là số hợp lệ.");
        }
    }

    @FXML
    private void cancel() {
        currentStage().close();
    }

    private void selectCategory(int categoryId) {
        for (Category category : cbCategory.getItems()) {
            if (category.getId() == categoryId) {
                cbCategory.getSelectionModel().select(category);
                return;
            }
        }
    }

    private void showCurrentImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        File file = new File(imagePath);
        if (file.isFile()) {
            imgPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    private Stage currentStage() {
        return (Stage) txtName.getScene().getWindow();
    }
}
