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
    private ComboBox<String> cbDanhMuc;

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
        System.out.println("Đang nạp dữ liệu danh mục..."); // test
        try {
            List<String> listDanhMuc = categoryDao.getAllCategoryNames();
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
    private File fileAnhDaChon; // Biến toàn cục lưu tệp ảnh đã chọn

    @FXML
    void handleChonAnh(ActionEvent event) {
        // Khởi tạo FileChooser(trình chọn tệp)
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn Ảnh Sản Phẩm");
        // 2. Thiết lập bộ lọc (chỉ cho phép chọn ảnh)
        fileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        // 3. Hiển thị hộp thoại chọn tệp
        Stage stage = (Stage) buttonChonAnh.getScene().getWindow();
        File selectFile = fileChooser.showOpenDialog(stage);
        // 4. Xử lý tệp được chọn
        if (selectFile != null) {
            // Hiển thị ảnh lên giao diện
            Image image = new Image(selectFile.toURI().toString());
            imageViewSanPham.setImage(image);
            // Thiết lập kích thước
            imageViewSanPham.setFitWidth(150);
            imageViewSanPham.setFitHeight(150);
            imageViewSanPham.setPreserveRatio(true);
            // Lưu file vào biến toàn cục để dùng cho nút "Lưu sản phẩm"
            this.fileAnhDaChon = selectFile;
        }
    }

    @FXML
    void buttonSave(ActionEvent event) {
        String id = txtId.getText();
        String name = txtTenSanPham.getText();
        String category = cbDanhMuc.getValue();
        String size = txtSize.getText();
        // Kiểm tra dữ liệu
        if (id.isEmpty() || name.isEmpty() || category == null) {
            thongBao.setText("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        // xử lý số liệu giá và tồn kho
        long price = 0;
        int stock = 0;
        try {
            // Chuyển chuỗi sang số
            if (!txtGia.getText().isEmpty()) {
                price = Long.parseLong(txtGia.getText());
            }
            if (!txtTonKho.getText().isEmpty()) {
                stock = Integer.parseInt(txtTonKho.getText());
            }
        } catch (Exception e) {
            thongBao.setText("Giá hoặc Tồn kho không hợp lệ!");
            return;
        }
        Product product = new Product(id, name, category, price, stock, size,
                fileAnhDaChon != null ? fileAnhDaChon.getAbsolutePath() : "");
        // Gọi DAO để lưu sản phẩm
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
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
