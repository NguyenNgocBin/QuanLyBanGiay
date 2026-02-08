package controller;

import DAO.CategoryDAO;
import DAO.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Product;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.io.IOException;
import java.util.List;

public class ProductController {

    @FXML
    private TableView<Product> tableProducts;

    @FXML
    private TableColumn<Product, String> colId;

    @FXML
    private TableColumn<Product, String> colProduct;

    @FXML
    private TableColumn<Product, String> colCategory;

    @FXML
    private TableColumn<Product, Double> colPrice;

    @FXML
    private TableColumn<Product, String> colSize;

    @FXML
    private TableColumn<Product, Integer> colStock;

    @FXML
    private TextField txtSearch;

    @FXML
    private ComboBox<String> cbxDanhMuc;

    private ProductDAO productDAO = new ProductDAO();
    private ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Cấu hình cột
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // 2. Nạp dữ liệu ComboBox
        initComboBoxData();
        loadData();
        setupSearch();
        setUpContextMenu();
    }

    private void initComboBoxData() {
        CategoryDAO categoryDAO = new CategoryDAO();
        List<String> dataFromDB = categoryDAO.getAllCategoryNames();
        // Chuyển đổi List thường sang ObservableList của JavaFX
        ObservableList<String> listDanhMuc = FXCollections.observableArrayList(dataFromDB);
        listDanhMuc.add(0, "Tất cả");
        // Đưa dữ liệu vào ComboBox
        cbxDanhMuc.setItems(listDanhMuc);
        // Chọn giá trị mặc định là dòng đầu tiên ("Tất cả")
        cbxDanhMuc.getSelectionModel().selectFirst();
    }

    private void loadData() {
        try {
            List<Product> dataFromDB = productDAO.getAll();
            productList.clear();
            productList.addAll(dataFromDB);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi tải dữ liệu sản phẩm.");
        }
    }

    @FXML
    void themMoiProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddProduct.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Thêm sản phẩm mới");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        FilteredList<Product> filteredData = new FilteredList<>(productList, p -> true);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> updateFilter(filteredData));
        cbxDanhMuc.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter(filteredData));

        // 3. Gán vào bảng
        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableProducts.comparatorProperty());
        tableProducts.setItems(sortedData);
    }

    // Hàm lọc logic
    private void updateFilter(FilteredList<Product> filteredData) {
        filteredData.setPredicate(product -> {
            String keyword = txtSearch.getText() != null ? txtSearch.getText().toLowerCase() : "";
            String selectedCategory = cbxDanhMuc.getValue();

            boolean matchText = keyword.isEmpty() ||
                    product.getName().toLowerCase().contains(keyword) ||
                    product.getId().toLowerCase().contains(keyword);

            boolean matchCategory = (selectedCategory == null || selectedCategory.equals("Tất cả")) ||
                    product.getCategory().equalsIgnoreCase(selectedCategory);

            return matchText && matchCategory;
        });
    }

    private void setUpContextMenu() {
        // Tạo các item cho menu (Sửa, Xóa)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Chỉnh Sửa");
        MenuItem deleteItem = new MenuItem("Xóa");

        // Xử lý sự kiện cho item "Chỉnh sửa"
        editItem.setOnAction(event -> {
            Product selected = tableProducts.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEditProduct(selected);
            }
        });
        // Hiện thị xoá, sửa
        contextMenu.getItems().addAll(editItem, deleteItem);
        tableProducts.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        });
        // Xử lý sự kiện cho item "Xóa"
        deleteItem.setOnAction(event -> {
            Product selected = tableProducts.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteProduct(selected);
            }
        });
    }

    private void handleEditProduct(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProduct.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền dữ liệu
            EditProductController controller = loader.getController();
            controller.setProductData(product);

            // Hiển thị cửa sổ
            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa sản phẩm");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Cập nhật lại bảng sau khi đóng cửa sổ
            tableProducts.refresh();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Lỗi");
            alert.show();
        }
    }

    private void handleDeleteProduct(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa sản phẩm: " + product.getName() + "?");
        alert.setContentText("Hành động này không thể hoàn tác.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Bước 1: Gọi DAO để xóa trong Cơ sở dữ liệu
            boolean success = new ProductDAO().deleteProduct(product.getId());

            if (success) {
                productList.remove(product);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Thông báo");
                info.setHeaderText(null);
                info.setContentText("Đã xóa sản phẩm thành công!");
                info.showAndWait();
            }
        }
    }
}
