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

public class AddProductController {

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
    private File selectedImage;

    @FXML
    public void initialize() {
        cbCategory.getItems().setAll(categoryDAO.getAllCategories());
        if (!cbCategory.getItems().isEmpty()) {
            cbCategory.getSelectionModel().selectFirst();
        }
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
        Product product = readProductFromForm();
        if (product == null) {
            return;
        }

        if (productDAO.addProduct(product)) {
            currentStage().close();
        } else {
            lblMessage.setText("Không thể thêm sản phẩm. Kiểm tra mã SKU có bị trùng không.");
        }
    }

    @FXML
    private void cancel() {
        currentStage().close();
    }

    private Product readProductFromForm() {
        String name = txtName.getText().trim();
        String code = txtProductCode.getText().trim();
        Category category = cbCategory.getValue();

        if (name.isBlank() || code.isBlank() || category == null) {
            lblMessage.setText("Vui lòng nhập tên, mã SKU và danh mục.");
            return null;
        }

        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            String imagePath = selectedImage == null ? "" : selectedImage.getAbsolutePath();
            return new Product(0, code, name, category.getId(), category.getName(), price, stock, txtSize.getText().trim(), imagePath);
        } catch (NumberFormatException e) {
            lblMessage.setText("Giá bán và tồn kho phải là số hợp lệ.");
            return null;
        }
    }

    private Stage currentStage() {
        return (Stage) txtName.getScene().getWindow();
    }
}
