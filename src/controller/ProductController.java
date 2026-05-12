package controller;

import DAO.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductController {

    @FXML
    private TableView<Product> tableProducts;

    @FXML
    private TableColumn<Product, String> colId;

    @FXML
    private TableColumn<Product, String> colName;

    @FXML
    private TableColumn<Product, String> colCategory;

    @FXML
    private TableColumn<Product, String> colSize;

    @FXML
    private TableColumn<Product, Double> colPrice;

    @FXML
    private TableColumn<Product, Integer> colStock;

    @FXML
    private TableColumn<Product, Void> colAction;

    @FXML
    private TextField txtSearch;

    @FXML
    private VBox vboxCartItems;

    @FXML
    private Label lblTotal;

    private ProductDAO productDAO = new ProductDAO();
    private List<Product> productList = new ArrayList<>();
    private List<CartItem> cartList = new ArrayList<>();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadData();
        setupSearch();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("productCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        // Cột Thao tác
        colAction.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button btn = new Button("Thêm");
            {
                btn.getStyleClass().add("btn-add");
                btn.setStyle("-fx-padding: 5px 10px; -fx-font-size: 12px;");
                btn.setOnAction(event -> {
                    Product p = getTableView().getItems().get(getIndex());
                    addToCart(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product p = getTableView().getItems().get(getIndex());
                    if (p != null && p.getStock() <= 0) {
                        btn.setDisable(true);
                        btn.setText("Hết hàng");
                    } else {
                        btn.setDisable(false);
                        btn.setText("Thêm");
                    }
                    setGraphic(btn);
                }
            }
        });
    }

    private void loadData() {
        try {
            productList = productDAO.getAll();
            tableProducts.setItems(FXCollections.observableArrayList(productList));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi tải dữ liệu sản phẩm.");
        }
    }

    private void setupSearch() {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            String keyword = newValue != null ? newValue.toLowerCase() : "";
            List<Product> filtered = productList.stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword) || 
                                 p.getProductCode().toLowerCase().contains(keyword) ||
                                 p.getCategoryName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            tableProducts.setItems(FXCollections.observableArrayList(filtered));
        });
    }

    private void addToCart(Product product) {
        if (product == null) return;

        // Validation tồn kho (trước mọi hành động)
        if (product.getStock() <= 0) {
            showWarning("Cảnh báo", "Sản phẩm đã hết hàng!");
            return;
        }

        // Gộp dòng theo (ID biến thể + Size)
        CartItem existing = findCartItem(product);
        if (existing != null) {
            int nextQty = existing.getQuantity() + 1;
            if (nextQty > product.getStock()) {
                showWarning("Cảnh báo",
                        "Không thể thêm! Số lượng trong giỏ đã đạt giới hạn tồn kho (" + product.getStock() + ").");
                return;
            }
            existing.setQuantity(nextQty);
            updateCartView();
            return;
        }

        cartList.add(new CartItem(product, 1));
        updateCartView();
    }

    private CartItem findCartItem(Product product) {
        for (CartItem item : cartList) {
            if (item.matchesVariant(product)) return item;
        }
        return null;
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateCartView() {
        if (vboxCartItems == null) return;
        
        vboxCartItems.getChildren().clear();
        double total = 0;
        
        for (CartItem item : cartList) {
            Product p = item.getProduct();
            HBox itemBox = new HBox();
            itemBox.getStyleClass().add("cart-item");
            itemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            itemBox.setSpacing(10);
            
            VBox details = new VBox();
            Label nameLabel = new Label(p.getName());
            nameLabel.setStyle("-fx-font-weight: bold;");
            Label sizeLabel = new Label("Size: " + p.getSize());
            sizeLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
            details.getChildren().addAll(nameLabel, sizeLabel);
            
            // Qty controls
            HBox qtyBox = new HBox();
            qtyBox.setAlignment(javafx.geometry.Pos.CENTER);
            qtyBox.setSpacing(6);

            Button btnMinus = new Button("-");
            btnMinus.getStyleClass().add("qty-btn");
            Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
            qtyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 24px; -fx-alignment: center;");
            Button btnPlus = new Button("+");
            btnPlus.getStyleClass().add("qty-btn");

            Button btnRemove = new Button("x");
            btnRemove.getStyleClass().add("btn-remove");

            qtyBox.getChildren().addAll(btnMinus, qtyLabel, btnPlus, btnRemove);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            double totalItemPrice = p.getPrice() * item.getQuantity();
            total += totalItemPrice;
            Label priceLabel = new Label(String.format("%,.0fđ", totalItemPrice));
            priceLabel.setStyle("-fx-font-weight: bold;");

            // Actions
            btnPlus.setOnAction(e -> {
                int nextQty = item.getQuantity() + 1;
                if (nextQty > p.getStock()) {
                    showWarning("Cảnh báo",
                            "Không thể tăng! Tồn kho tối đa cho biến thể này là " + p.getStock() + ".");
                    return;
                }
                item.setQuantity(nextQty);
                updateCartView();
            });

            btnMinus.setOnAction(e -> {
                int nextQty = item.getQuantity() - 1;
                if (nextQty <= 0) {
                    cartList.remove(item);
                    updateCartView();
                    return;
                }
                item.setQuantity(nextQty);
                updateCartView();
            });

            btnRemove.setOnAction(e -> {
                cartList.remove(item);
                updateCartView();
            });
            
            itemBox.getChildren().addAll(details, qtyBox, spacer, priceLabel);
            vboxCartItems.getChildren().add(itemBox);
        }
        
        lblTotal.setText(String.format("%,.0fđ", total));
    }

    @FXML
    void xuLyThanhToan(ActionEvent event) {
        if (cartList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText("Giỏ hàng đang trống!");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Thanh toán");
        alert.setHeaderText("Chọn phương thức thanh toán");
        alert.setContentText("Vui lòng chọn phương thức thanh toán:");

        ButtonType btnTienMat = new ButtonType("Tiền mặt");
        ButtonType btnUngDung = new ButtonType("Ứng dụng / Chuyển khoản");
        ButtonType btnHuy = new ButtonType("Hủy", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnTienMat, btnUngDung, btnHuy);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnTienMat || type == btnUngDung) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Thành công");
                success.setHeaderText(null);
                success.setContentText("Thanh toán thành công bằng " + type.getText() + "!\nĐang in hóa đơn...");
                success.showAndWait();
                
                cartList.clear();
                updateCartView();
            }
        });
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



    // Class hỗ trợ giỏ hàng
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public boolean matchesVariant(Product other) {
            if (other == null) return false;
            // ID ở đây được coi là ID của biến thể (Product-Size).
            // Dù vậy vẫn so thêm Size để bám sát yêu cầu nghiệp vụ.
            String aId = product != null ? product.getProductCode() : null;
            String bId = other.getProductCode();
            String aSize = product != null ? product.getSize() : null;
            String bSize = other.getSize();
            return (aId != null && aId.equals(bId)) && (aSize != null && aSize.equals(bSize));
        }
    }
}
