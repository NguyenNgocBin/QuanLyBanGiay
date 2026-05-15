package controller;

import DAO.CustomerDAO;
import DAO.ProductDAO;
import DAO.SaleDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import models.Customer;
import models.Product;
import utils.PDFGenerator;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleController {

    // ─── Bộ lọc sản phẩm ───────────────────────────────────────────────────────
    @FXML private TextField tfProductSearch;
    @FXML private Button btnAll;
    @FXML private Button btnRunning;
    @FXML private Button btnBasketball;
    @FXML private Label lblProductCount;

    // ─── Bảng sản phẩm ─────────────────────────────────────────────────────────
    @FXML private TableView<SaleProduct> tblProducts;
    @FXML private TableColumn<SaleProduct, SaleProduct> colImage;
    @FXML private TableColumn<SaleProduct, SaleProduct> colProduct;
    @FXML private TableColumn<SaleProduct, String>      colSku;
    @FXML private TableColumn<SaleProduct, String>      colBrand;
    @FXML private TableColumn<SaleProduct, String>      colSize;
    @FXML private TableColumn<SaleProduct, Integer>     colStock;
    @FXML private TableColumn<SaleProduct, String>      colPrice;
    @FXML private TableColumn<SaleProduct, SaleProduct> colAction;

    // ─── Khách hàng ─────────────────────────────────────────────────────────────
    @FXML private TextField          tfCustomerSearch;
    @FXML private ListView<Customer> lvCustomerSuggestions;
    @FXML private HBox               hbCustomerSelected;
    @FXML private Label              lblCustomerName;
    @FXML private Label              lblCustomerPhone;
    @FXML private Label              lblCustomerCode;

    // ─── Giỏ hàng & thanh toán ──────────────────────────────────────────────────
    @FXML private Label lblCartCount;
    @FXML private Label lblSubtotal;
    @FXML private Label lblTotal;
    @FXML private VBox  cartItemsBox;
    @FXML private Button btnCash;
    @FXML private Button btnBank;
    @FXML private Button btnCard;

    // ─── State ──────────────────────────────────────────────────────────────────
    private final ProductDAO  productDAO  = new ProductDAO();
    private final SaleDAO     saleDAO     = new SaleDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    private final ObservableList<SaleProduct> products = FXCollections.observableArrayList();
    private final ObservableList<CartItem>    cart     = FXCollections.observableArrayList();

    private String   selectedCategory = "";
    private String   selectedPayment  = "Tien mat";
    private Customer selectedCustomer = null;

    // ═══════════════════════════════════════════════════════════════════════════
    //  INITIALIZE
    // ═══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        setupTable();
        loadProducts();
        setupSearchField();
        setupCustomerSearch();
        renderCart();
    }

    // ─── Tìm kiếm sản phẩm ─────────────────────────────────────────────────────

    private void setupSearchField() {
        tfProductSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    // ─── Bảng sản phẩm ─────────────────────────────────────────────────────────

    private void setupTable() {
        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblProducts.setFixedCellSize(66);

        // Cột hình ảnh
        colImage.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            { iv.setFitWidth(42); iv.setFitHeight(34); iv.setPreserveRatio(true); iv.setSmooth(true); }

            @Override protected void updateItem(SaleProduct p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setGraphic(null); return; }
                Image img = loadImage(p.getImagePath());
                if (img != null) { iv.setImage(img); setGraphic(iv); }
                else {
                    Label lbl = new Label("👟");
                    lbl.getStyleClass().setAll("sale-thumb", p.getThumbClass());
                    setGraphic(lbl);
                }
            }
        });

        // Cột tên + loại
        colProduct.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        colProduct.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(SaleProduct p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setGraphic(null); return; }
                Label name = new Label(p.getName()); name.getStyleClass().add("sale-product-name");
                Label type = new Label(p.getCategory()); type.getStyleClass().add("sale-product-type");
                setGraphic(new VBox(2, name, type));
            }
        });

        colSku.setCellValueFactory(d -> d.getValue().skuProperty());

        // Cột brand
        colBrand.setCellValueFactory(d -> d.getValue().brandProperty());
        colBrand.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String brand, boolean empty) {
                super.updateItem(brand, empty);
                if (empty || brand == null || brand.isBlank()) { setGraphic(null); return; }
                Label lbl = new Label(brand); lbl.getStyleClass().add("sale-brand-tag");
                setGraphic(lbl);
            }
        });

        colSize.setCellValueFactory(d -> d.getValue().sizeProperty());

        // Cột tồn kho — đỏ khi sắp hết
        colStock.setCellValueFactory(d -> d.getValue().stockProperty().asObject());
        colStock.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(stock));
                setStyle(stock <= 5 ? "-fx-text-fill: #ef4444; -fx-font-weight: 900;" : "");
            }
        });

        colPrice.setCellValueFactory(d -> d.getValue().priceTextProperty());

        // Cột thêm vào giỏ
        colAction.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue()));
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(SaleProduct p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setGraphic(null); return; }
                Button btn = new Button("+");
                btn.getStyleClass().add("sale-add-button");
                btn.setDisable(p.getStock() <= cartQuantity(p));
                btn.setOnAction(e -> addToCart(p));
                setGraphic(btn);
                setAlignment(Pos.CENTER);
            }
        });
    }

    // ─── Load & lọc sản phẩm ───────────────────────────────────────────────────

    private void loadProducts() {
        products.setAll(productDAO.getAll().stream().map(SaleProduct::new).toList());
        setupCategoryButtons();
        applyFilter();
    }

    private void setupCategoryButtons() {
        List<String> categories = products.stream()
                .map(SaleProduct::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .limit(2)
                .toList();

        if (btnRunning != null) {
            if (!categories.isEmpty()) {
                btnRunning.setText(categories.get(0));
                btnRunning.setDisable(false);
                btnRunning.setVisible(true);
            } else {
                btnRunning.setVisible(false);
            }
        }
        if (btnBasketball != null) {
            if (categories.size() > 1) {
                btnBasketball.setText(categories.get(1));
                btnBasketball.setDisable(false);
                btnBasketball.setVisible(true);
            } else {
                btnBasketball.setVisible(false);
            }
        }
    }

    private void applyFilter() {
        String keyword = tfProductSearch == null ? "" : tfProductSearch.getText().trim().toLowerCase(Locale.ROOT);
        ObservableList<SaleProduct> filtered = products.filtered(p -> {
            boolean catMatch = selectedCategory.isBlank()
                    || normalizeCategory(p.getCategory()).equals(selectedCategory);
            boolean kwMatch  = keyword.isBlank()
                    || p.getName().toLowerCase(Locale.ROOT).contains(keyword)
                    || p.getSku().toLowerCase(Locale.ROOT).contains(keyword);
            return catMatch && kwMatch;
        });
        tblProducts.setItems(filtered);
        if (lblProductCount != null)
            lblProductCount.setText("Hiện thị " + filtered.size() + " sản phẩm");
    }

    @FXML
    private void filterCategory(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();
        selectedCategory = (clicked == btnAll) ? "" : normalizeCategory(clicked.getText());

        for (Button b : new Button[]{btnAll, btnRunning, btnBasketball}) {
            if (b != null) b.getStyleClass().remove("sale-chip-active");
        }
        clicked.getStyleClass().add("sale-chip-active");
        applyFilter();
    }

    // ─── Khách hàng ─────────────────────────────────────────────────────────────

    private void setupCustomerSearch() {
        lvCustomerSuggestions.setVisible(false);
        lvCustomerSuggestions.setManaged(false);
        hbCustomerSelected.setVisible(false);
        hbCustomerSelected.setManaged(false);

        // Custom cell cho danh sách gợi ý
        lvCustomerSuggestions.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Customer c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) { setText(null); return; }
                setText(c.getFullName() + "  •  " + c.getPhone());
            }
        });

        // Khi người dùng gõ → tìm kiếm
        tfCustomerSearch.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isBlank()) {
                lvCustomerSuggestions.setVisible(false);
                lvCustomerSuggestions.setManaged(false);
                return;
            }
            List<Customer> results = customerDAO.search(newVal.trim());
            lvCustomerSuggestions.setItems(FXCollections.observableArrayList(results));
            boolean show = !results.isEmpty();
            lvCustomerSuggestions.setVisible(show);
            lvCustomerSuggestions.setManaged(show);
        });

        // Khi chọn khách hàng từ danh sách
        lvCustomerSuggestions.setOnMouseClicked(e -> {
            Customer chosen = lvCustomerSuggestions.getSelectionModel().getSelectedItem();
            if (chosen != null) selectCustomer(chosen);
        });
    }

    private void selectCustomer(Customer c) {
        selectedCustomer = c;
        tfCustomerSearch.setText("");
        lvCustomerSuggestions.setVisible(false);
        lvCustomerSuggestions.setManaged(false);

        lblCustomerName.setText(c.getFullName());
        lblCustomerPhone.setText(c.getPhone());
        lblCustomerCode.setText(c.getCustomerCode());

        hbCustomerSelected.setVisible(true);
        hbCustomerSelected.setManaged(true);
    }

    @FXML
    private void clearCustomer() {
        selectedCustomer = null;
        hbCustomerSelected.setVisible(false);
        hbCustomerSelected.setManaged(false);
        tfCustomerSearch.setText("");
    }

    @FXML
    private void openAddCustomerDialog() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Thêm khách hàng mới");
        dialog.setHeaderText(null);

        // Nội dung form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField tfName  = new TextField(); tfName.setPromptText("Họ và tên *");
        TextField tfPhone = new TextField(); tfPhone.setPromptText("Số điện thoại");
        TextField tfEmail = new TextField(); tfEmail.setPromptText("Email");

        tfName.getStyleClass().add("form-input");
        tfPhone.getStyleClass().add("form-input");
        tfEmail.getStyleClass().add("form-input");
        tfName.setPrefWidth(260);

        grid.add(new Label("Họ và tên *"), 0, 0); grid.add(tfName,  1, 0);
        grid.add(new Label("Điện thoại"), 0, 1);  grid.add(tfPhone, 1, 1);
        grid.add(new Label("Email"),       0, 2);  grid.add(tfEmail, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Lưu");
        okBtn.setDisable(true);
        tfName.textProperty().addListener((o, v, nv) -> okBtn.setDisable(nv.trim().isBlank()));

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !tfName.getText().trim().isBlank()) {
                return customerDAO.add(tfName.getText(), tfPhone.getText(), tfEmail.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newCustomer -> {
            if (newCustomer != null) {
                selectCustomer(newCustomer);
            }
        });
    }

    // ─── Giỏ hàng ──────────────────────────────────────────────────────────────

    private void addToCart(SaleProduct product) {
        if (product.getStock() <= 0 || cartQuantity(product) >= product.getStock()) {
            showAlert(Alert.AlertType.WARNING, "Tồn kho", "Sản phẩm đã hết hàng hoặc không đủ số lượng.");
            return;
        }
        for (CartItem item : cart) {
            if (item.product.getId() == product.getId()) {
                item.quantity.set(item.quantity.get() + 1);
                renderCart();
                return;
            }
        }
        cart.add(new CartItem(product, 1));
        renderCart();
    }

    private void renderCart() {
        cartItemsBox.getChildren().clear();
        for (CartItem item : cart) {
            cartItemsBox.getChildren().add(createCartRow(item));
        }
        updateTotals();
        tblProducts.refresh();
    }

    private HBox createCartRow(CartItem item) {
        Label name = new Label(item.product.getName());
        name.getStyleClass().add("cart-product-name");
        Label meta = new Label("Size: " + firstSize(item.product.getSize())
                + "  |  SKU: " + item.product.getSku()
                + "  |  Tồn: " + item.product.getStock());
        meta.getStyleClass().add("cart-product-meta");

        Button minus = new Button("−");
        Button plus  = new Button("+");
        Label qty    = new Label(String.valueOf(item.quantity.get()));
        minus.getStyleClass().add("qty-small");
        plus.getStyleClass().add("qty-small");
        qty.getStyleClass().add("qty-number");

        minus.setOnAction(e -> {
            if (item.quantity.get() <= 1) cart.remove(item);
            else item.quantity.set(item.quantity.get() - 1);
            renderCart();
        });
        plus.setDisable(item.quantity.get() >= item.product.getStock());
        plus.setOnAction(e -> {
            if (item.quantity.get() < item.product.getStock()) {
                item.quantity.set(item.quantity.get() + 1);
                renderCart();
            } else {
                showAlert(Alert.AlertType.WARNING, "Tồn kho", "Số lượng trong giỏ đã bằng tồn kho hiện có.");
            }
        });

        HBox qtyBox = new HBox(0, minus, qty, plus);
        qtyBox.setAlignment(Pos.CENTER_LEFT);

        VBox info    = new VBox(3, name, meta, qtyBox);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Label price  = new Label(formatCurrency(item.getLineTotal()));
        price.getStyleClass().add("cart-line-price");

        HBox row = new HBox(10, info, spacer, price);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("cart-row");
        return row;
    }

    private void updateTotals() {
        double subtotal = cart.stream().mapToDouble(CartItem::getLineTotal).sum();
        if (lblSubtotal != null) lblSubtotal.setText(formatCurrency(subtotal));
        if (lblTotal    != null) lblTotal.setText(formatCurrency(subtotal));
        if (lblCartCount != null) {
            int n = cart.stream().mapToInt(i -> i.quantity.get()).sum();
            lblCartCount.setText(n + " sản phẩm");
        }
    }

    @FXML private void clearCart() { cart.clear(); renderCart(); }

    @FXML
    private void selectPayment(javafx.event.ActionEvent event) {
        for (Button b : new Button[]{btnCash, btnBank, btnCard})
            if (b != null) b.getStyleClass().remove("payment-card-active");
        Button sel = (Button) event.getSource();
        sel.getStyleClass().add("payment-card-active");
        selectedPayment = sel == btnBank  ? "Chuyen khoan"
                        : sel == btnCard  ? "The Visa/MC"
                        : "Tien mat";
    }

    @FXML
    private void checkout() {
        if (cart.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thanh toán", "Đơn hàng đang trống.");
            return;
        }

        List<SaleDAO.CheckoutLine> lines = new ArrayList<>();
        for (CartItem item : cart) {
            lines.add(new SaleDAO.CheckoutLine(
                    item.product.getId(),
                    item.product.getName(),
                    item.quantity.get(),
                    item.product.getPrice()
            ));
        }

        int custId = (selectedCustomer != null) ? selectedCustomer.getId() : 0;
        SaleDAO.CheckoutResult result = saleDAO.checkout(lines, calculateTotal(), selectedPayment, custId);

        if (!result.isSuccess()) {
            showAlert(Alert.AlertType.ERROR, "Thanh toán", result.getMessage());
            loadProducts();
            syncCartWithStock();
            renderCart();
            return;
        }

        String custInfo = (selectedCustomer != null)
                ? "\nKhách: " + selectedCustomer.getFullName()
                : "\nKhách lẻ";
                
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thanh toán thành công");
        alert.setHeaderText("Mã hóa đơn: #" + result.getOrderId() + custInfo);
        alert.setContentText("Bạn có muốn in hóa đơn không?");
        
        ButtonType btnYes = new ButtonType("In hóa đơn", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Không", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnYes, btnNo);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnYes) {
                printInvoice(result.getOrderId(), selectedCustomer, cart, calculateTotal(), selectedPayment);
            }
        });

        clearCart();
        clearCustomer();
        loadProducts();
    }

    private void printInvoice(int orderId, Customer customer, ObservableList<CartItem> cartList, double total, String paymentMethod) {
        List<PDFGenerator.InvoiceItem> invoiceItems = new ArrayList<>();
        for (CartItem item : cartList) {
            invoiceItems.add(new PDFGenerator.InvoiceItem(
                item.product.getName(),
                item.quantity.get(),
                item.product.getPrice(),
                item.getLineTotal()
            ));
        }
        
        File pdfFile = PDFGenerator.generateInvoice(orderId, customer, invoiceItems, total, paymentMethod);
        if (pdfFile != null && pdfFile.exists()) {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở file PDF: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi tạo hóa đơn PDF.");
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private double calculateTotal() {
        return cart.stream().mapToDouble(CartItem::getLineTotal).sum();
    }

    private void syncCartWithStock() {
        List<CartItem> dead = new ArrayList<>();
        for (CartItem item : cart) {
            products.stream()
                    .filter(p -> p.getId() == item.product.getId())
                    .findFirst()
                    .ifPresent(fresh -> {
                        item.product.setStock(fresh.getStock());
                        if (fresh.getStock() <= 0) dead.add(item);
                        else if (item.quantity.get() > fresh.getStock())
                            item.quantity.set(fresh.getStock());
                    });
        }
        cart.removeAll(dead);
    }

    private int cartQuantity(SaleProduct product) {
        return cart.stream()
                .filter(i -> i.product.getId() == product.getId())
                .mapToInt(i -> i.quantity.get())
                .sum();
    }

    private String firstSize(String sizes) {
        if (sizes == null || sizes.isBlank()) return "-";
        return sizes.split("[,\\-]")[0].trim();
    }

    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ";
    }

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) return null;
        File f = new File(path);
        if (!f.isFile()) return null;
        Image img = new Image(f.toURI().toString(), 42, 34, true, true);
        return img.isError() ? null : img;
    }

    private String normalizeCategory(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  INNER CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    public static class SaleProduct {
        private final int id;
        private final SimpleStringProperty  name, category, sku, brand, size,
                                             colorHex, colorName, priceText;
        private final SimpleIntegerProperty stock;
        private final double   price;
        private final String   thumbClass, imagePath;

        public SaleProduct(Product p) {
            this.id         = p.getId();
            this.name       = new SimpleStringProperty(safe(p.getName()));
            this.category   = new SimpleStringProperty(safe(p.getCategoryName()));
            this.sku        = new SimpleStringProperty(safe(p.getProductCode()));
            this.brand      = new SimpleStringProperty(safe(p.getCategoryName()));
            this.size       = new SimpleStringProperty(safe(p.getSize()));
            this.colorHex   = new SimpleStringProperty(colorHexFor(p));
            this.colorName  = new SimpleStringProperty(safe(p.getCategoryName()));
            this.stock      = new SimpleIntegerProperty(p.getStock());
            this.price      = p.getPrice();
            this.priceText  = new SimpleStringProperty(fmt(p.getPrice()));
            this.thumbClass = thumbClassFor(p);
            this.imagePath  = p.getImagePath();
        }

        public int    getId()        { return id; }
        public String getName()      { return name.get(); }
        public String getCategory()  { return category.get(); }
        public String getSku()       { return sku.get(); }
        public String getSize()      { return size.get(); }
        public String getColorHex()  { return colorHex.get(); }
        public int    getStock()     { return stock.get(); }
        public void   setStock(int s){ stock.set(s); }
        public double getPrice()     { return price; }
        public String getThumbClass(){ return thumbClass; }
        public String getImagePath() { return imagePath; }

        public SimpleStringProperty  skuProperty()       { return sku; }
        public SimpleStringProperty  brandProperty()     { return brand; }
        public SimpleStringProperty  sizeProperty()      { return size; }
        public SimpleStringProperty  colorNameProperty() { return colorName; }
        public SimpleIntegerProperty stockProperty()     { return stock; }
        public SimpleStringProperty  priceTextProperty() { return priceText; }

        private static String colorHexFor(Product p) {
            String c = safe(p.getCategoryName()).toLowerCase(Locale.ROOT);
            if (c.contains("sport") || c.contains("thao")) return "#1d4ed8";
            if (c.contains("classic") || c.contains("tay")) return "#7c2d12";
            if (c.contains("sandal")) return "#059669";
            if (c.contains("tre"))    return "#eab308";
            return "#b91c1c";
        }

        private static String thumbClassFor(Product p) {
            String c = safe(p.getCategoryName()).toLowerCase(Locale.ROOT);
            if (c.contains("sport") || c.contains("thao")) return "thumb-sale-blue";
            if (c.contains("classic") || c.contains("tay")) return "thumb-sale-dark";
            if (c.contains("sandal")) return "thumb-sale-light";
            return "thumb-sale-red";
        }

        private static String fmt(double v) {
            return NumberFormat.getNumberInstance(Locale.US).format(v).replace(",", ".") + "đ";
        }

        private static String safe(String s) { return s == null ? "" : s; }
    }

    private static class CartItem {
        final SaleProduct               product;
        final SimpleIntegerProperty     quantity = new SimpleIntegerProperty(1);

        CartItem(SaleProduct p, int qty) { this.product = p; this.quantity.set(qty); }

        double getLineTotal() { return product.getPrice() * quantity.get(); }
    }
}
