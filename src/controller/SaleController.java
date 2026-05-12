package controller;

import DAO.OrderDAO;
import DAO.OrderDetailDAO;
import DAO.ProductDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaleController {

    @FXML
    private FlowPane flowPaneProducts;

    @FXML
    private VBox vboxCartItems;

    @FXML
    private Label lblTamTinh;

    @FXML
    private TextField txtDiscount;

    @FXML
    private Label lblGiamGia;

    @FXML
    private Label lblTotal;

    @FXML
    private TextField txtSearch;

    @FXML
    private TextField txtCustomerName;

    @FXML
    private HBox hboxCategoryFilter;

    @FXML
    private VBox vboxCash;

    @FXML
    private VBox vboxTransfer;

    @FXML
    private VBox vboxCard;

    private ProductDAO productDAO = new ProductDAO();
    private OrderDAO orderDAO = new OrderDAO();
    private OrderDetailDAO orderDetailDAO = new OrderDetailDAO();
    private DAO.CategoryDAO categoryDAO = new DAO.CategoryDAO();
    private List<Product> productList = new ArrayList<>();
    private List<CartItem> cartList = new ArrayList<>();
    private String selectedPaymentMethod = "Tiền mặt";

    @FXML
    public void initialize() {
        loadData();
        setupSearch();
        setupDiscountListener();
        setupCategoryFilters();
    }

    private void setupCategoryFilters() {
        if (hboxCategoryFilter == null) return;
        hboxCategoryFilter.getChildren().clear();

        // Nút "Tất cả"
        Button btnAll = new Button("Tất cả");
        btnAll.getStyleClass().addAll("filter-chip", "filter-chip-active");
        btnAll.setOnAction(e -> {
            resetFilterChipStyles(btnAll);
            renderProducts(productList);
        });
        hboxCategoryFilter.getChildren().add(btnAll);

        // Lấy danh sách danh mục từ DB
        List<models.Category> categories = categoryDAO.getAllCategories();
        for (models.Category cat : categories) {
            Button btnCat = new Button(cat.getName());
            btnCat.getStyleClass().add("filter-chip");
            btnCat.setOnAction(e -> {
                resetFilterChipStyles(btnCat);
                filterByCategory(cat.getName());
            });
            hboxCategoryFilter.getChildren().add(btnCat);
        }
    }

    private void resetFilterChipStyles(Button activeBtn) {
        for (javafx.scene.Node node : hboxCategoryFilter.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.getStyleClass().remove("filter-chip-active");
                if (!btn.getStyleClass().contains("filter-chip")) {
                    btn.getStyleClass().add("filter-chip");
                }
            }
        }
        activeBtn.getStyleClass().add("filter-chip-active");
    }

    private void filterByCategory(String categoryName) {
        List<Product> filtered = productList.stream()
                .filter(p -> p.getCategoryName() != null && p.getCategoryName().equalsIgnoreCase(categoryName))
                .collect(Collectors.toList());
        renderProducts(filtered);
    }

    private void loadData() {
        try {
            productList = productDAO.getAll();
            renderProducts(productList);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi tải dữ liệu sản phẩm.");
        }
    }

    private void renderProducts(List<Product> products) {
        flowPaneProducts.getChildren().clear();
        for (Product p : products) {
            VBox card = new VBox();
            card.getStyleClass().add("product-card");
            card.setSpacing(10);
            card.setPrefWidth(180);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 15px; -fx-effect: dropshadow(three-pass-box, rgba(29, 0, 0, 0.05), 10, 0, 0, 4);");

            Label imgPlaceholder = new Label("🖼");
            imgPlaceholder.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8px; -fx-min-height: 150px; -fx-alignment: center; -fx-font-size: 40px;");
            imgPlaceholder.setMaxWidth(Double.MAX_VALUE);

            Label lblBrand = new Label(p.getCategoryName());
            lblBrand.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #6b7280; -fx-padding: 3 8; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-font-weight: bold;");

            Label lblName = new Label(p.getName());
            lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111827;");
            lblName.setWrapText(true);

            Label lblSize = new Label("Size: " + p.getSize() + " | Kho: " + p.getStock());
            lblSize.setStyle(p.getStock() <= 0
                    ? "-fx-text-fill: #ef4444; -fx-font-size: 12px;"
                    : "-fx-text-fill: #6b7280; -fx-font-size: 12px;");

            Label lblPrice = new Label(String.format("%,.0fđ", p.getPrice()));
            lblPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #111827;");

            card.getChildren().addAll(imgPlaceholder, lblBrand, lblName, lblSize, lblPrice);

            javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
            javafx.scene.control.MenuItem editItem = new javafx.scene.control.MenuItem("Chỉnh sửa");
            editItem.setOnAction(e -> {
                suaProduct(p);
            });
            contextMenu.getItems().add(editItem);

            card.setOnContextMenuRequested(event -> {
                contextMenu.show(card, event.getScreenX(), event.getScreenY());
            });

            if (p.getStock() > 0) {
                card.setOnMouseClicked(event -> {
                    if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                        addToCart(p);
                    }
                });
                card.setStyle(card.getStyle() + "; -fx-cursor: hand;");
            } else {
                card.setOpacity(0.5);
            }

            flowPaneProducts.getChildren().add(card);
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
            renderProducts(filtered);
        });
    }

    private void setupDiscountListener() {
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtDiscount.setText(newVal.replaceAll("[^\\d]", ""));
            }
            updateCartView();
        });
    }

    private void addToCart(Product product) {
        if (product == null) return;

        if (product.getStock() <= 0) {
            showWarning("Cảnh báo", "Sản phẩm đã hết hàng!");
            return;
        }

        CartItem existing = findCartItem(product);
        if (existing != null) {
            int nextQty = existing.getQuantity() + 1;
            if (nextQty > product.getStock()) {
                showWarning("Cảnh báo", "Không thể thêm! Số lượng vượt tồn kho (" + product.getStock() + ").");
                return;
            }
            existing.setQuantity(nextQty);
        } else {
            cartList.add(new CartItem(product, 1));
        }
        updateCartView();
    }

    private CartItem findCartItem(Product product) {
        for (CartItem item : cartList) {
            if (item.getProduct().getProductCode().equals(product.getProductCode()) &&
                item.getProduct().getSize().equals(product.getSize())) {
                return item;
            }
        }
        return null;
    }

    private void updateCartView() {
        if (vboxCartItems == null) return;
        
        vboxCartItems.getChildren().clear();
        double total = 0;
        
        for (CartItem item : cartList) {
            Product p = item.getProduct();
            HBox itemBox = new HBox();
            itemBox.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1px 0; -fx-padding: 0 0 15px 0;");
            itemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            itemBox.setSpacing(15);
            
            Label imgPlaceholder = new Label("🖼");
            imgPlaceholder.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 4px; -fx-min-width: 50px; -fx-min-height: 50px; -fx-alignment: center; -fx-font-size: 20px;");

            VBox details = new VBox();
            details.setSpacing(5);
            Label nameLabel = new Label(p.getName());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");
            Label sizeLabel = new Label("Size: " + p.getSize() + " | SKU: " + p.getProductCode());
            sizeLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");

            HBox qtyBox = new HBox();
            qtyBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            qtyBox.setSpacing(6);

            Button btnMinus = new Button("-");
            btnMinus.setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-radius: 4px; -fx-cursor: hand;");
            Label qtyLabel = new Label(String.valueOf(item.getQuantity()));
            qtyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 20px; -fx-alignment: center;");
            Button btnPlus = new Button("+");
            btnPlus.setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-radius: 4px; -fx-cursor: hand;");

            qtyBox.getChildren().addAll(btnMinus, qtyLabel, btnPlus);
            details.getChildren().addAll(nameLabel, sizeLabel, qtyBox);

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            double totalItemPrice = p.getPrice() * item.getQuantity();
            total += totalItemPrice;
            Label priceLabel = new Label(String.format("%,.0fđ", totalItemPrice));
            priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #111827;");

            btnPlus.setOnAction(e -> {
                int nextQty = item.getQuantity() + 1;
                if (nextQty > p.getStock()) {
                    showWarning("Cảnh báo", "Tồn kho tối đa cho biến thể này là " + p.getStock() + ".");
                    return;
                }
                item.setQuantity(nextQty);
                updateCartView();
            });

            btnMinus.setOnAction(e -> {
                int nextQty = item.getQuantity() - 1;
                if (nextQty <= 0) {
                    cartList.remove(item);
                } else {
                    item.setQuantity(nextQty);
                }
                updateCartView();
            });
            
            itemBox.getChildren().addAll(imgPlaceholder, details, spacer, priceLabel);
            vboxCartItems.getChildren().add(itemBox);
        }
        
        lblTamTinh.setText(String.format("%,.0fđ", total));

        double discountPercent = 0;
        try {
            discountPercent = Double.parseDouble(txtDiscount.getText());
        } catch (NumberFormatException e) {
            discountPercent = 0;
        }

        double discountAmount = total * (discountPercent / 100);
        lblGiamGia.setText(String.format("-%,.0fđ", discountAmount));

        double finalTotal = total - discountAmount;
        lblTotal.setText(String.format("%,.0fđ", finalTotal));
    }

    @FXML
    void selectPaymentCash(javafx.scene.input.MouseEvent event) {
        selectedPaymentMethod = "Tiền mặt";
        updatePaymentStyles(vboxCash);
    }

    @FXML
    void selectPaymentTransfer(javafx.scene.input.MouseEvent event) {
        selectedPaymentMethod = "Chuyển khoản";
        updatePaymentStyles(vboxTransfer);
    }

    @FXML
    void selectPaymentCard(javafx.scene.input.MouseEvent event) {
        selectedPaymentMethod = "Thẻ (Visa/MC)";
        updatePaymentStyles(vboxCard);
    }

    private void updatePaymentStyles(VBox selectedBox) {
        if (vboxCash != null) vboxCash.getStyleClass().remove("payment-method-selected");
        if (vboxTransfer != null) vboxTransfer.getStyleClass().remove("payment-method-selected");
        if (vboxCard != null) vboxCard.getStyleClass().remove("payment-method-selected");
        
        if (selectedBox != null) selectedBox.getStyleClass().add("payment-method-selected");
    }

    @FXML
    void xoaHetCart(ActionEvent event) {
        cartList.clear();
        updateCartView();
    }

    @FXML
    void xuLyThanhToan(ActionEvent event) {
        if (cartList.isEmpty()) {
            showWarning("Thông báo", "Giỏ hàng đang trống!");
            return;
        }

        // Lấy tên khách hàng (bắt buộc nhập)
        String customerName = txtCustomerName != null ? txtCustomerName.getText().trim() : "";
        if (customerName.isEmpty()) {
            showWarning("Thông báo", "Vui lòng nhập tên khách hàng!");
            if (txtCustomerName != null) txtCustomerName.requestFocus();
            return;
        }

        // Tính tổng tiền sau chiết khấu
        double total = 0;
        for (CartItem item : cartList) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        double discountPercent = 0;
        try {
            discountPercent = Double.parseDouble(txtDiscount.getText());
        } catch (NumberFormatException ignored) {}
        double finalTotal = total * (1 - discountPercent / 100);

        // Tạo đối tượng Order
        models.Order order = new models.Order();
        order.setCustomerId(0); // 0 = khách vãng lai, không có trong bảng customers
        order.setCustomerName(customerName);
        order.setTotalAmount(finalTotal);
        order.setOrderDate(new java.sql.Date(System.currentTimeMillis()));
        order.setStatus("Đã thanh toán");

        // Insert vào DB và lấy ID vừa tạo
        int newOrderId = orderDAO.insertOrderReturnId(order);
        if (newOrderId <= 0) {
            showWarning("Lỗi", "Không thể tạo đơn hàng. Vui lòng kiểm tra kết nối DB!");
            return;
        }

        // Insert từng dòng order_detail và giảm tồn kho
        boolean allSuccess = true;
        for (CartItem item : cartList) {
            Product p = item.getProduct();

            // Insert order_detail
            models.OrderDetail detail = new models.OrderDetail();
            detail.setOrderId(newOrderId);
            detail.setProductId(p.getId());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(p.getPrice());
            boolean detailOk = orderDetailDAO.insertOrderDetail(detail);

            // Giảm tồn kho
            boolean stockOk = productDAO.decreaseStock(p.getId(), item.getQuantity());

            if (!detailOk || !stockOk) {
                allSuccess = false;
                System.err.println("Lỗi khi lưu chi tiết hoặc giảm stock cho sản phẩm: " + p.getName());
            }
        }

        // Hiển thị kết quả & In hóa đơn
        if (allSuccess) {
            printInvoice(newOrderId, customerName, total, discountPercent, finalTotal);
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Thành công");
            success.setHeaderText(null);
            success.setContentText("Thanh toán thành công bằng " + selectedPaymentMethod + "!\nMã đơn hàng: #" + newOrderId + "\nHóa đơn đã được tạo.");
            success.showAndWait();
        } else {
            Alert warn = new Alert(Alert.AlertType.WARNING);
            warn.setTitle("Cảnh báo");
            warn.setHeaderText(null);
            warn.setContentText("Đơn hàng đã tạo (Mã #" + newOrderId + ") nhưng có lỗi khi lưu chi tiết.\nVui lòng kiểm tra lại!");
            warn.showAndWait();
        }

        // Reset
        cartList.clear();
        if (txtCustomerName != null) txtCustomerName.clear();
        updateCartView();

        // Reload danh sách sản phẩm để cập nhật tồn kho mới
        loadData();
    }

    private void printInvoice(int orderId, String customerName, double total, double discount, double finalTotal) {
        try {
            java.io.File receiptDir = new java.io.File("receipts");
            if (!receiptDir.exists()) receiptDir.mkdir();
            
            java.io.File file = new java.io.File(receiptDir, "hoadon_" + orderId + ".txt");
            try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                writer.println("=====================================");
                writer.println("          HÓA ĐƠN BÁN HÀNG           ");
                writer.println("=====================================");
                writer.println("Mã đơn hàng: #" + orderId);
                writer.println("Ngày: " + new java.util.Date().toString());
                writer.println("Khách hàng: " + customerName);
                writer.println("Phương thức TT: " + selectedPaymentMethod);
                writer.println("-------------------------------------");
                writer.println(String.format("%-20s %-5s %10s", "Sản phẩm", "SL", "Thành tiền"));
                for (CartItem item : cartList) {
                    Product p = item.getProduct();
                    double price = p.getPrice() * item.getQuantity();
                    String name = p.getName().length() > 18 ? p.getName().substring(0, 15) + "..." : p.getName();
                    writer.println(String.format("%-20s %-5d %,10.0f", name, item.getQuantity(), price));
                }
                writer.println("-------------------------------------");
                writer.println(String.format("Tạm tính:                       %,.0fđ", total));
                if (discount > 0) {
                    writer.println(String.format("Chiết khấu (%.0f%%):             -%,.0fđ", discount, total * discount / 100));
                }
                writer.println(String.format("TỔNG CỘNG:                      %,.0fđ", finalTotal));
                writer.println("=====================================");
                writer.println("     CẢM ƠN QUÝ KHÁCH HẸN GẶP LẠI    ");
                writer.println("=====================================");
            }
            System.out.println("Đã in hóa đơn ra file: " + file.getAbsolutePath());
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showWarning("Lỗi in hóa đơn", "Không thể tạo file hóa đơn: " + e.getMessage());
        }
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void themMoiProduct(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/AddProduct.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Thêm sản phẩm mới");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            loadData(); // Cập nhật lại list
            setupCategoryFilters(); // Cập nhật lại filter
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showWarning("Lỗi", "Không thể mở form thêm sản phẩm.");
        }
    }

    private void suaProduct(Product product) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/view/EditProduct.fxml"));
            javafx.scene.Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setProductData(product);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Chỉnh sửa sản phẩm");
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();

            loadData(); // Cập nhật lại list
            setupCategoryFilters(); // Cập nhật lại filter
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showWarning("Lỗi", "Không thể mở form sửa sản phẩm.");
        }
    }

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
    }
}
