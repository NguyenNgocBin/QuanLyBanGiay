package controller;

import DAO.CategoryDAO;
import DAO.ProductDAO;
import DAO.SupplierDAO;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import models.Category;
import models.Product;
import models.Supplier;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class AddProductController {

    @FXML private TextField txtName;
    @FXML private TextField txtProductCode;
    @FXML private ComboBox<Category> cbCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtSize;
    @FXML private TextField txtStock;
    @FXML private ComboBox<Supplier> cbSupplier;
    @FXML private TextField txtImportPrice;
    @FXML private Label lblTotalAmount;
    @FXML private ImageView imgPreview;
    @FXML private Label lblMessage;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private File selectedImage;

    @FXML
    public void initialize() {
        // Load Categories
        cbCategory.getItems().setAll(categoryDAO.getAllCategories());
        cbCategory.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        cbCategory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category object) {
                return object == null ? "" : object.getName();
            }
            @Override
            public Category fromString(String string) {
                return null;
            }
        });
        if (!cbCategory.getItems().isEmpty()) {
            cbCategory.getSelectionModel().selectFirst();
        }

        // Load Suppliers
        cbSupplier.getItems().setAll(supplierDAO.getAll());
        cbSupplier.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        cbSupplier.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier object) {
                return object == null ? "" : object.getName();
            }
            @Override
            public Supplier fromString(String string) {
                return null;
            }
        });
        if (!cbSupplier.getItems().isEmpty()) {
            cbSupplier.getSelectionModel().selectFirst();
        }

        // Dynamic Total Amount Calculation
        ChangeListener<String> listener = (obs, oldVal, newVal) -> calculateTotalAmount();
        txtStock.textProperty().addListener(listener);
        txtImportPrice.textProperty().addListener(listener);
    }

    private void calculateTotalAmount() {
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            double importPrice = Double.parseDouble(txtImportPrice.getText().trim());
            double total = stock * importPrice;
            lblTotalAmount.setText(NumberFormat.getNumberInstance(Locale.US).format(total).replace(",", ".") + "đ");
        } catch (NumberFormatException e) {
            lblTotalAmount.setText("0đ");
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

        Supplier supplier = cbSupplier.getValue();
        if (supplier == null) {
            lblMessage.setText("Vui lòng chọn nhà cung cấp.");
            return;
        }

        double importPrice;
        try {
            importPrice = Double.parseDouble(txtImportPrice.getText().trim());
            if (importPrice < 0) {
                lblMessage.setText("Giá nhập không được âm.");
                return;
            }
        } catch (NumberFormatException e) {
            lblMessage.setText("Giá nhập phải là số hợp lệ.");
            return;
        }

        if (productDAO.addProductWithImport(product, supplier.getId(), importPrice)) {
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
