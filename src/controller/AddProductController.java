package controller;

import DAO.CategoryDAO;
import DAO.ProductDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Product;
import models.Category;
import javafx.scene.Node;

import java.io.File;
import java.util.List;
import javafx.scene.text.Text;

public class AddProductController {
    @FXML
    private Button buttonChonAnh;

    @FXML
    private Text thongBao;

    @FXML
    private ComboBox<Category> cbDanhMuc;

    @FXML
    private ImageView imageViewSanPham;

    @FXML
    private TextField txtGia;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtSize;

    @FXML
    private TextField txtTenSanPham;

    @FXML
    private TextField txtTonKho;

    private CategoryDAO categoryDao = new CategoryDAO();

    @FXML
    public void initialize() {
        System.out.println("Đang nạp dữ liệu danh mục...");
        try {
            List<Category> listDanhMuc = categoryDao.getAllCategories();
            cbDanhMuc.getItems().clear();
            cbDanhMuc.getItems().addAll(listDanhMuc);
            if (!listDanhMuc.isEmpty()) {
                cbDanhMuc.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.out.println("Lỗi nạp danh mục: " + e.getMessage());
        }
    }

    @FXML
    private File fileAnhDaChon;

    @FXML
    void handleChonAnh(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Ảnh Sản Phẩm");
        fileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) buttonChonAnh.getScene().getWindow();
        File selectFile = fileChooser.showOpenDialog(stage);
        if (selectFile != null) {
            Image image = new Image(selectFile.toURI().toString());
            imageViewSanPham.setImage(image);
            imageViewSanPham.setFitWidth(150);
            imageViewSanPham.setFitHeight(150);
            imageViewSanPham.setPreserveRatio(true);
            this.fileAnhDaChon = selectFile;
        }
    }

    @FXML
    void buttonSave(ActionEvent event) {
        String id = txtId.getText(); // This is productCode now
        String name = txtTenSanPham.getText();
        Category category = cbDanhMuc.getValue();
        String size = txtSize.getText();

        if (id.isEmpty() || name.isEmpty() || category == null) {
            thongBao.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        double price = 0;
        int stock = 0;
        try {
            if (!txtGia.getText().isEmpty()) {
                price = Double.parseDouble(txtGia.getText());
            }
            if (!txtTonKho.getText().isEmpty()) {
                stock = Integer.parseInt(txtTonKho.getText());
            }
        } catch (Exception e) {
            thongBao.setText("Giá hoặc Tồn kho không hợp lệ!");
            return;
        }

        Product product = new Product();
        product.setProductCode(id);
        product.setName(name);
        product.setCategoryId(category.getId());
        product.setPrice(price);
        product.setStock(stock);
        product.setSize(size);
        product.setImagePath(fileAnhDaChon != null ? fileAnhDaChon.getAbsolutePath() : "");

        ProductDAO productDAO = new ProductDAO();
        if (productDAO.addProduct(product)) {
            thongBao.setText("Thêm sản phẩm thành công!");
            closeWindow(event);
        } else {
            thongBao.setText("Thêm sản phẩm thất bại!");
        }
    }

    private void closeWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    void buttonCancel(ActionEvent event) {
        closeWindow(event);
    }
}
