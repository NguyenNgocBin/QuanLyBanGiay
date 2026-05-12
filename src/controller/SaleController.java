package controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.text.NumberFormat;
import java.util.Locale;

public class SaleController {

    @FXML private Button btnAll;
    @FXML private Button btnRunning;
    @FXML private Button btnBasketball;
    @FXML private Button btnCash;
    @FXML private Button btnBank;
    @FXML private Button btnCard;
    @FXML private Label lblProductCount;
    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotal;
    @FXML private VBox cartItemsBox;
    @FXML private TableView<SaleProduct> tblProducts;
    @FXML private TableColumn<SaleProduct, SaleProduct> colImage;
    @FXML private TableColumn<SaleProduct, SaleProduct> colProduct;
    @FXML private TableColumn<SaleProduct, String> colSku;
    @FXML private TableColumn<SaleProduct, String> colBrand;
    @FXML private TableColumn<SaleProduct, String> colSize;
    @FXML private TableColumn<SaleProduct, String> colColor;
    @FXML private TableColumn<SaleProduct, Integer> colStock;
    @FXML private TableColumn<SaleProduct, String> colPrice;
    @FXML private TableColumn<SaleProduct, SaleProduct> colAction;

    private final ObservableList<SaleProduct> products = FXCollections.observableArrayList(
            new SaleProduct("Air Zoom Pegasus 38", "Chạy bộ", "NK-P38-R", "NIKE", "42, 43, 44", "#B91C1C", "Đỏ đen", 15, 2450000, "thumb-sale-red"),
            new SaleProduct("Ultraboost 21 Cloud", "Chạy bộ", "AD-U21-W", "ADIDAS", "39, 40, 41", "#F8FAFC", "Trắng", 8, 3120000, "thumb-sale-light"),
            new SaleProduct("NB 574 Heritage", "Sneakers", "NB-574-N", "NEW BALANCE", "40, 42", "#1D4ED8", "Navy", 22, 1890000, "thumb-sale-dark"),
            new SaleProduct("Clyde All-Pro", "Bóng rổ", "PU-CAP-Y", "PUMA", "41, 43, 44, 45", "#EAB308", "Vàng neon", 5, 2100000, "thumb-sale-blue")
    );

    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();
    private String selectedCategory = "Tất cả";

    @FXML
    public void initialize() {
        setupTable();
        cart.add(new CartItem(products.get(0), 1));
        cart.add(new CartItem(products.get(1), 2));
        applyCategory();
        renderCart();
    }

    private void setupTable() {
        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblProducts.setFixedCellSize(66);

        colImage.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        colImage.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SaleProduct item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label image = new Label("▰");
                image.getStyleClass().setAll("sale-thumb", item.getThumbClass());
                setGraphic(image);
            }
        });

        colProduct.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        colProduct.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SaleProduct item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label name = new Label(item.getName());
                name.getStyleClass().add("sale-product-name");
                Label type = new Label(item.getCategory());
                type.getStyleClass().add("sale-product-type");
                setGraphic(new VBox(2, name, type));
            }
        });

        colSku.setCellValueFactory(data -> data.getValue().skuProperty());
        colBrand.setCellValueFactory(data -> data.getValue().brandProperty());
        colBrand.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label brand = new Label(item);
                brand.getStyleClass().add("sale-brand-tag");
                setGraphic(brand);
            }
        });

        colSize.setCellValueFactory(data -> data.getValue().sizeProperty());
        colColor.setCellValueFactory(data -> data.getValue().colorNameProperty());
        colColor.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                SaleProduct product = getTableView().getItems().get(getIndex());
                Circle dot = new Circle(5);
                dot.setStyle("-fx-fill: " + product.getColorHex() + "; -fx-stroke: #cbd5e1;");
                Label label = new Label(item);
                label.getStyleClass().add("sale-color-name");
                setGraphic(new HBox(6, dot, label));
            }
        });

        colStock.setCellValueFactory(data -> data.getValue().stockProperty().asObject());
        colPrice.setCellValueFactory(data -> data.getValue().priceTextProperty());
        colAction.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SaleProduct item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Button add = new Button("🛒");
                add.getStyleClass().add("sale-add-button");
                add.setOnAction(event -> addToCart(item));
                setGraphic(add);
                setAlignment(Pos.CENTER);
            }
        });
    }

    @FXML
    private void filterCategory(javafx.event.ActionEvent event) {
        Button clicked = (Button) event.getSource();
        selectedCategory = clicked.getText();
        btnAll.getStyleClass().remove("sale-chip-active");
        btnRunning.getStyleClass().remove("sale-chip-active");
        btnBasketball.getStyleClass().remove("sale-chip-active");
        clicked.getStyleClass().add("sale-chip-active");
        applyCategory();
    }

    private void applyCategory() {
        ObservableList<SaleProduct> filtered = products.filtered(product ->
                "Tất cả".equals(selectedCategory) || product.getCategory().equals(selectedCategory));
        tblProducts.setItems(filtered);
        lblProductCount.setText("Hiển thị " + filtered.size() + " sản phẩm");
    }

    private void addToCart(SaleProduct product) {
        for (CartItem item : cart) {
            if (item.product == product) {
                if (item.quantity.get() < product.getStock()) {
                    item.quantity.set(item.quantity.get() + 1);
                    renderCart();
                }
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
    }

    private HBox createCartRow(CartItem item) {
        Label image = new Label("▰");
        image.getStyleClass().setAll("cart-thumb", item.product.getThumbClass());

        Label name = new Label(item.product.getName());
        name.getStyleClass().add("cart-product-name");
        Label meta = new Label("Size: " + firstSize(item.product.getSize()) + " | SKU: " + item.product.getSku());
        meta.getStyleClass().add("cart-product-meta");

        Button minus = new Button("-");
        Button plus = new Button("+");
        Label quantity = new Label(String.valueOf(item.quantity.get()));
        minus.getStyleClass().add("qty-small");
        plus.getStyleClass().add("qty-small");
        quantity.getStyleClass().add("qty-number");
        minus.setOnAction(event -> {
            if (item.quantity.get() <= 1) {
                cart.remove(item);
            } else {
                item.quantity.set(item.quantity.get() - 1);
            }
            renderCart();
        });
        plus.setOnAction(event -> {
            if (item.quantity.get() < item.product.getStock()) {
                item.quantity.set(item.quantity.get() + 1);
                renderCart();
            }
        });

        HBox qty = new HBox(0, minus, quantity, plus);
        qty.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(3, name, meta, qty);
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label price = new Label(formatCurrency(item.getLineTotal()));
        price.getStyleClass().add("cart-line-price");

        HBox row = new HBox(10, image, info, spacer, price);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("cart-row");
        return row;
    }

    private void updateTotals() {
        double subtotal = cart.stream().mapToDouble(CartItem::getLineTotal).sum();
        double discount = subtotal * 0.05;
        lblSubtotal.setText(formatCurrency(subtotal));
        lblDiscount.setText("-" + formatCurrency(discount));
        lblTotal.setText(formatCurrency(subtotal - discount));
    }

    @FXML
    private void clearCart() {
        cart.clear();
        renderCart();
    }

    @FXML
    private void selectPayment(javafx.event.ActionEvent event) {
        btnCash.getStyleClass().remove("payment-card-active");
        btnBank.getStyleClass().remove("payment-card-active");
        btnCard.getStyleClass().remove("payment-card-active");
        ((Button) event.getSource()).getStyleClass().add("payment-card-active");
    }

    @FXML
    private void checkout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thanh toán");
        alert.setHeaderText(null);
        alert.setContentText(cart.isEmpty() ? "Đơn hàng đang trống." : "Thanh toán thành công. Hóa đơn đang được in.");
        alert.showAndWait();
        if (!cart.isEmpty()) {
            clearCart();
        }
    }

    private String firstSize(String sizes) {
        return sizes.split(",")[0].trim();
    }

    private String formatCurrency(double value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value).replace(",", ".") + "đ";
    }

    public static class SaleProduct {
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleStringProperty sku;
        private final SimpleStringProperty brand;
        private final SimpleStringProperty size;
        private final SimpleStringProperty colorHex;
        private final SimpleStringProperty colorName;
        private final SimpleIntegerProperty stock;
        private final SimpleStringProperty priceText;
        private final double price;
        private final String thumbClass;

        public SaleProduct(String name, String category, String sku, String brand, String size, String colorHex, String colorName, int stock, double price, String thumbClass) {
            this.name = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.sku = new SimpleStringProperty(sku);
            this.brand = new SimpleStringProperty(brand);
            this.size = new SimpleStringProperty(size);
            this.colorHex = new SimpleStringProperty(colorHex);
            this.colorName = new SimpleStringProperty(colorName);
            this.stock = new SimpleIntegerProperty(stock);
            this.price = price;
            this.priceText = new SimpleStringProperty(NumberFormat.getNumberInstance(Locale.US).format(price).replace(",", ".") + "đ");
            this.thumbClass = thumbClass;
        }

        public String getName() { return name.get(); }
        public String getCategory() { return category.get(); }
        public String getSku() { return sku.get(); }
        public String getSize() { return size.get(); }
        public String getColorHex() { return colorHex.get(); }
        public int getStock() { return stock.get(); }
        public double getPrice() { return price; }
        public String getThumbClass() { return thumbClass; }
        public SimpleStringProperty skuProperty() { return sku; }
        public SimpleStringProperty brandProperty() { return brand; }
        public SimpleStringProperty sizeProperty() { return size; }
        public SimpleStringProperty colorNameProperty() { return colorName; }
        public SimpleIntegerProperty stockProperty() { return stock; }
        public SimpleStringProperty priceTextProperty() { return priceText; }
    }

    private static class CartItem {
        private final SaleProduct product;
        private final SimpleIntegerProperty quantity = new SimpleIntegerProperty(1);

        private CartItem(SaleProduct product, int quantity) {
            this.product = product;
            this.quantity.set(quantity);
        }

        private double getLineTotal() {
            return product.getPrice() * quantity.get();
        }
    }
}
