package controller;

import DAO.ProductDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Product;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblLowStock;
    @FXML private Label lblInventoryValue;
    @FXML private Label lblBrandCount;
    @FXML private Label lblShowing;
    @FXML private Label lblsumproduct;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Product> colImage;
    @FXML private TableColumn<Product, Product> colName;
    @FXML private TableColumn<Product, String> colBrand;
    @FXML private TableColumn<Product, String> colSize;
    @FXML private TableColumn<Product, String> colColor;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colStatus;
    @FXML private TableColumn<Product, Product> colAction;

    private final ProductDAO productDAO = new ProductDAO();
    private final ObservableList<Product> allProducts = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredProducts = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupFilters();
        setupTable();
        loadProducts();
    }

    private void setupFilters() {
        cbStatus.setItems(FXCollections.observableArrayList("Tất cả", "Còn hàng", "Sắp hết", "Hết hàng"));
        cbStatus.setValue("Tất cả");
        cbStatus.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
    }

    private void setupTable() {
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.setFixedCellSize(74);

        colImage.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colImage.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Label fallback = new Label("▰");

            {
                imageView.setFitWidth(52);
                imageView.setFitHeight(44);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                fallback.getStyleClass().addAll("shoe-thumb", "thumb-light");// Thiết lập kiểu dáng cho hình nền khi không có ảnh sản phẩm, sử dụng lớp CSS "shoe-thumb" và "thumb-light" để hiển thị một hình nền đơn giản thay thế.
            }

            //
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                    return;
                }

                Image image = loadImage(product.getImagePath());
                if (image != null) {
                    imageView.setImage(image);
                    setGraphic(imageView);
                } else {
                    fallback.getStyleClass().setAll("shoe-thumb", thumbClass(product));
                    setGraphic(fallback);
                }
                setAlignment(Pos.CENTER_LEFT);
            }
        });
        //
        colName.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colName.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                    return;
                }

                Label name = new Label(product.getName());
                name.getStyleClass().add("product-name");
                Label sku = new Label("SKU: " + safe(product.getProductCode()));
                sku.getStyleClass().add("product-sku");
                setGraphic(new VBox(3, name, sku));
            }
        });

        colBrand.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safe(data.getValue().getCategoryName())));
        colBrand.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null || category.isBlank()) {
                    setGraphic(null);
                    return;
                }
                Label tag = new Label(category);
                tag.getStyleClass().add("brand-tag");
                setGraphic(tag);
            }
        });

        colSize.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(safe(data.getValue().getSize())));
//        colColor.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(colorForProduct(data.getValue()))); //
//        colColor.setCellFactory(column -> new TableCell<>() {
//            @Override
//            protected void updateItem(String colors, boolean empty) {//
//                super.updateItem(colors, empty);
//                if (empty || colors == null) {
//                    setGraphic(null);
//                    return;
//                }
//
//                HBox colorBox = new HBox(5);
//                colorBox.setAlignment(Pos.CENTER_LEFT);
//                for (String color : colors.split(",")) {
//                    Circle dot = new Circle(6);
//                    dot.setStyle("-fx-fill: " + color + "; -fx-stroke: #cbd5e1; -fx-stroke-width: 1;");
//                    colorBox.getChildren().add(dot);
//                }
//                setGraphic(colorBox);
//            }
//        });

        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(formatCurrency(data.getValue().getPrice())));
        colStock.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colStock.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                setText(empty || stock == null ? null : stock.toString());
                getStyleClass().remove("low-stock-text");
                if (!empty && stock != null && stock > 0 && stock <= 10) {
                    getStyleClass().add("low-stock-text");
                }
            }
        });

        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(statusOf(data.getValue())));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label tag = new Label(status);
                tag.getStyleClass().addAll("status-pill", statusClass(status));
                setGraphic(tag);
            }
        });

        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                    return;
                }

                Button edit = new Button("✎");
                Button delete = new Button("⌫");
                edit.getStyleClass().add("icon-action");
                delete.getStyleClass().add("icon-action");
                edit.setOnAction(event -> openEditProductForm(product));
                delete.setOnAction(event -> deleteProduct(product));
                HBox actions = new HBox(8, edit, delete);
                actions.setAlignment(Pos.CENTER_LEFT);
                setGraphic(actions);
            }
        });
    }

    private void loadProducts() {
        allProducts.setAll(productDAO.getAll());
        applyFilter();
        updateMetrics();
    }

    // Hàm này áp dụng bộ lọc tìm kiếm và trạng thái lên danh sách sản phẩm. Nó sẽ lọc danh sách tất cả sản phẩm dựa trên từ khóa tìm kiếm và trạng thái được chọn, sau đó cập nhật TableView và nhãn hiển thị số lượng sản phẩm đang hiển thị so với tổng số sản phẩm.
    private void applyFilter() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = cbStatus.getValue() == null ? "Tất cả" : cbStatus.getValue();

        filteredProducts.setAll(allProducts.filtered(product -> {
            boolean matchesKeyword = keyword.isBlank()
                    || safe(product.getName()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(product.getProductCode()).toLowerCase(Locale.ROOT).contains(keyword)
                    || safe(product.getCategoryName()).toLowerCase(Locale.ROOT).contains(keyword);
            boolean matchesStatus = "Tất cả".equals(selectedStatus) || statusOf(product).equals(selectedStatus);
            return matchesKeyword && matchesStatus;
        }));

        productTable.setItems(filteredProducts);
        int end = Math.min(filteredProducts.size(), 10);
        lblShowing.setText("Hiển thị " + (filteredProducts.isEmpty() ? 0 : 1) + " - " + end + " trong " + allProducts.size() + " sản phẩm");
    }

    // Hàm này cập nhật các chỉ số tổng quan về sản phẩm, bao gồm số lượng sản phẩm sắp hết hàng (tồn kho từ 1 đến 10), tổng giá trị tồn kho (tổng giá trị của tất cả sản phẩm dựa trên giá và số lượng), và số lượng danh mục sản phẩm khác nhau. Các chỉ số này được hiển thị trên giao diện người dùng thông qua các nhãn tương ứng.
    private void updateMetrics() {
        long lowStock = allProducts.stream().filter(product -> product.getStock() > 0 && product.getStock() <= 10).count();

        double inventoryValue = allProducts.stream().mapToDouble(product -> product.getPrice() * product.getStock()).sum();

        long totalInventoryQuantity = allProducts.stream()
                .mapToLong(Product::getStock)
                .sum();


        long categoryCount = allProducts.stream()
                .map(Product::getCategoryName)
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .count();

        // Cập nhật các nhãn hiển thị số liệu tổng quan về sản phẩm
        lblTotalProducts.setText(String.format("%,d", allProducts.size()));//
        lblLowStock.setText(String.valueOf(lowStock));// Hiển thị số lượng sản phẩm sắp hết hàng
        lblInventoryValue.setText(formatCompactCurrency(inventoryValue));// Hiển thị tổng giá trị tồn kho dưới dạng rút gọn
        lblBrandCount.setText(String.valueOf(categoryCount));
            lblsumproduct.setText(String.format("%,d", totalInventoryQuantity));// Hiển thị tổng số lượng sản phẩm trong tồn kho với dấu phân cách hàng nghìn

    }

    @FXML
    private void handleAddProduct() {
        openAddProductForm();
    }


    // MỞ FORM THÊM SẢN PHẨM VÀ FORM SỬA SẢN PHẨM
    private void openAddProductForm() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AddProduct.fxml"));
            Stage stage = createDialogStage("Thêm sản phẩm mới", root);
            stage.showAndWait();
            loadProducts();
        } catch (IOException e) {
            showError("Không thể mở form thêm sản phẩm.");
            e.printStackTrace();
        }
    }

    // Hàm này sẽ được gọi khi người dùng bấm nút sửa trên một sản phẩm cụ thể. Nó sẽ mở form EditProduct.fxml và truyền dữ liệu sản phẩm cần sửa sang form đó.
    private void openEditProductForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProduct.fxml"));
            Parent root = loader.load();
            EditProductController controller = loader.getController(); // Lấy controller của form sửa sản phẩm
            controller.setProductData(product);// Truyền dữ liệu sản phẩm cần sửa sang form sửa sản phẩm
            Stage stage = createDialogStage("Sửa sản phẩm - " + product.getProductCode(), root); //
            stage.showAndWait();
            loadProducts(); // Sau khi form sửa sản phẩm đóng lại, tải lại danh sách sản phẩm để cập nhật thông tin mới nhất
        } catch (IOException e) {
            showError("Không thể mở form sửa sản phẩm.");
            e.printStackTrace();
        }
    }

    // Hàm hỗ trợ tạo một cửa sổ dialog mới với tiêu đề và nội dung được cung cấp. Cửa sổ này sẽ được sử dụng cho cả form thêm sản phẩm và sửa sản phẩm.
    private Stage createDialogStage(String title, Parent root) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);// Đảm bảo người dùng phải tương tác với cửa sổ này trước khi quay lại cửa sổ chính
        stage.setScene(new Scene(root));
        return stage;
    }

    private void deleteProduct(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa sản phẩm");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa sản phẩm \"" + product.getName() + "\"?");

        // Nếu người dùng xác nhận xóa, gọi DAO để xóa sản phẩm và tải lại danh sách sản phẩm. Nếu có lỗi (ví dụ: sản phẩm đang được tham chiếu trong đơn hàng), hiển thị thông báo lỗi.
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (productDAO.deleteProduct(product.getProductCode())) {
                    loadProducts();
                } else {
                    showError("Không thể xóa sản phẩm. Sản phẩm có thể đang được tham chiếu trong đơn hàng.");
                }
            }
        });
    }

    // Hàm hỗ trợ tải ảnh sản phẩm từ đường dẫn lưu trong cơ sở dữ liệu. Nếu đường dẫn hợp lệ và ảnh tồn tại, trả về đối tượng Image để hiển thị. Nếu không, trả về null để sử dụng hình nền mặc định.
    private Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {//
            return null;
        }

        File file = new File(imagePath);
        if (!file.isFile()) {
            return null;
        }

        Image image = new Image(file.toURI().toString(), 52, 44, true, true);
        return image.isError() ? null : image;
    }

    // Hàm này xác định trạng thái tồn kho của sản phẩm dựa trên số lượng trong kho. Nếu số lượng bằng 0, trả về "Hết hàng". Nếu số lượng từ 1 đến 10, trả về "Sắp hết". Nếu số lượng lớn hơn 10, trả về "Còn hàng".
    private String statusOf(Product product) {
        if (product.getStock() <= 0) return "Hết hàng";
        if (product.getStock() <= 5) return "Sắp hết";

        return "Còn hàng";
    }

    // Hàm này xác định lớp CSS cho trạng thái tồn kho của sản phẩm dựa trên giá trị trạng thái./
    private String statusClass(String status) {
        if ("Sắp hết".equals(status)) return "status-warning";// Nếu trạng thái là "Sắp hết", trả về lớp CSS "status-warning" để hiển thị màu vàng.
        if ("Hết hàng".equals(status)) return "status-muted";
        return "status-success";
    }

    // Hàm này xác định lớp CSS cho hình nền của ảnh sản phẩm dựa trên danh mục sản phẩm. Nếu danh mục chứa từ khóa liên quan đến chạy bộ, bóng rổ hoặc da, trả về lớp CSS tương ứng để hiển thị màu nền phù hợp. Nếu không, trả về lớp CSS mặc định.
    private String thumbClass(Product product) {
        String category = safe(product.getCategoryName()).toLowerCase(Locale.ROOT);
        if (category.contains("running") || category.contains("chạy")) return "thumb-dark";
        if (category.contains("basket") || category.contains("bóng")) return "thumb-black";
        if (category.contains("da") || category.contains("converse")) return "thumb-gray";
        return "thumb-light";
    }

    //
    private String colorForProduct(Product product) {
        String category = safe(product.getCategoryName()).toLowerCase(Locale.ROOT);
        if (category.contains("running") || category.contains("chạy")) return "#D8DEE6,#F8FAFC";
        if (category.contains("basket") || category.contains("bóng")) return "#C51F2C,#F8FAFC";
        if (category.contains("da")) return "#4B5563";
        return "#D12424,#16181D";
    }


    // Hàm này định dạng giá sản phẩm thành chuỗi có dấu phân cách hàng nghìn và thêm ký hiệu "đ" ở cuối. Ví dụ: 1500000 sẽ được định dạng thành "1.500.000đ".
    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ"; // Sử dụng Locale.US để có dấu phân cách hàng nghìn là dấu phẩy, sau đó thay thế dấu phẩy bằng dấu chấm để phù hợp với định dạng tiền tệ Việt Nam, và thêm "đ" vào cuối chuỗi.

    }

    // Hàm này định dạng giá trị tiền tệ thành dạng rút gọn với hậu tố "K" cho nghìn, "M" cho triệu và "B" cho tỷ. Ví dụ: 1500000 sẽ được định dạng thành "1.5M".
    private String formatCompactCurrency(double value) {
        if (value >= 1_000_000_000) {
            return String.format(Locale.US, "%.1fB", value / 1_000_000_000);
        }
        if (value >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", value / 1_000_000);
        }
        return formatCurrency(value);
    }

    private String safe(String text) {
        return text == null ? "" : text;
    }

    // Hàm này hiển thị một hộp thoại lỗi với thông điệp được cung cấp. Hộp thoại sẽ có tiêu đề "Lỗi" và chỉ có một nút "OK" để người dùng đóng hộp thoại.
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
