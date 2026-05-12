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
                fallback.getStyleClass().addAll("shoe-thumb", "thumb-light");
            }

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
        colColor.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(colorForProduct(data.getValue())));
        colColor.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String colors, boolean empty) {
                super.updateItem(colors, empty);
                if (empty || colors == null) {
                    setGraphic(null);
                    return;
                }

                HBox colorBox = new HBox(5);
                colorBox.setAlignment(Pos.CENTER_LEFT);
                for (String color : colors.split(",")) {
                    Circle dot = new Circle(6);
                    dot.setStyle("-fx-fill: " + color + "; -fx-stroke: #cbd5e1; -fx-stroke-width: 1;");
                    colorBox.getChildren().add(dot);
                }
                setGraphic(colorBox);
            }
        });

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

    private void updateMetrics() {
        long lowStock = allProducts.stream().filter(product -> product.getStock() > 0 && product.getStock() <= 10).count();
        double inventoryValue = allProducts.stream().mapToDouble(product -> product.getPrice() * product.getStock()).sum();
        long categoryCount = allProducts.stream()
                .map(Product::getCategoryName)
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .count();

        lblTotalProducts.setText(String.format("%,d", allProducts.size()));
        lblLowStock.setText(String.valueOf(lowStock));
        lblInventoryValue.setText(formatCompactCurrency(inventoryValue));
        lblBrandCount.setText(String.valueOf(categoryCount));
    }

    @FXML
    private void handleAddProduct() {
        openAddProductForm();
    }

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

    private void openEditProductForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditProduct.fxml"));
            Parent root = loader.load();
            EditProductController controller = loader.getController();
            controller.setProductData(product);
            Stage stage = createDialogStage("Sửa sản phẩm - " + product.getProductCode(), root);
            stage.showAndWait();
            loadProducts();
        } catch (IOException e) {
            showError("Không thể mở form sửa sản phẩm.");
            e.printStackTrace();
        }
    }

    private Stage createDialogStage(String title, Parent root) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        return stage;
    }

    private void deleteProduct(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa sản phẩm");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa sản phẩm \"" + product.getName() + "\"?");

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

    private Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        File file = new File(imagePath);
        if (!file.isFile()) {
            return null;
        }

        Image image = new Image(file.toURI().toString(), 52, 44, true, true);
        return image.isError() ? null : image;
    }

    private String statusOf(Product product) {
        if (product.getStock() <= 0) return "Hết hàng";
        if (product.getStock() <= 10) return "Sắp hết";
        return "Còn hàng";
    }

    private String statusClass(String status) {
        if ("Sắp hết".equals(status)) return "status-warning";
        if ("Hết hàng".equals(status)) return "status-muted";
        return "status-success";
    }

    private String thumbClass(Product product) {
        String category = safe(product.getCategoryName()).toLowerCase(Locale.ROOT);
        if (category.contains("running") || category.contains("chạy")) return "thumb-dark";
        if (category.contains("basket") || category.contains("bóng")) return "thumb-black";
        if (category.contains("da") || category.contains("converse")) return "thumb-gray";
        return "thumb-light";
    }

    private String colorForProduct(Product product) {
        String category = safe(product.getCategoryName()).toLowerCase(Locale.ROOT);
        if (category.contains("running") || category.contains("chạy")) return "#D8DEE6,#F8FAFC";
        if (category.contains("basket") || category.contains("bóng")) return "#C51F2C,#F8FAFC";
        if (category.contains("da")) return "#4B5563";
        return "#D12424,#16181D";
    }

    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ";
    }

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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
