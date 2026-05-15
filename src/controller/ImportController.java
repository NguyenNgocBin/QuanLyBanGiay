package controller;

import DAO.CategoryDAO;
import DAO.ImportDAO;
import DAO.ProductDAO;
import DAO.SupplierDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import models.Category;
import models.ImportDetail;
import models.ImportOrder;
import models.Product;
import models.Supplier;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ImportController {

    @FXML private TextField txtSearchProduct;
    @FXML private ComboBox<Category> cmbCategory;
    @FXML private GridPane productGrid;

    @FXML private ComboBox<Supplier> cmbSupplier;
    @FXML private TableView<ImportItem> importTable;
    @FXML private TableColumn<ImportItem, String> colProductName;
    @FXML private TableColumn<ImportItem, Integer> colQuantity;
    @FXML private TableColumn<ImportItem, Double> colImportPrice;
    @FXML private Label lblTotalAmount;

    private ProductDAO productDAO = new ProductDAO();
    private CategoryDAO categoryDAO = new CategoryDAO();
    private SupplierDAO supplierDAO = new SupplierDAO();
    private ImportDAO importDAO = new ImportDAO();

    private ObservableList<ImportItem> importList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCategories();
        loadSuppliers();
        loadProducts(null, 0);
        setupTable();
    }

    private void loadCategories() {
        List<Category> cats = categoryDAO.getAllCategories();
        Category all = new Category(0, "Tất cả danh mục");
        cmbCategory.getItems().add(all);
        cmbCategory.getItems().addAll(cats);
        cmbCategory.getSelectionModel().selectFirst();

        cmbCategory.setOnAction(e -> handleSearchProduct());
    }

    private void loadSuppliers() {
        List<Supplier> suppliers = supplierDAO.getAll();
        cmbSupplier.getItems().setAll(suppliers);
    }

    private void setupTable() {
        importTable.setItems(importList);
        importTable.setEditable(true);

        colProductName.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
        
        colQuantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        colQuantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colQuantity.setOnEditCommit(event -> {
            ImportItem item = event.getRowValue();
            item.setQuantity(event.getNewValue() == null ? 1 : event.getNewValue());
            updateTotal();
        });

        colImportPrice.setCellValueFactory(cellData -> cellData.getValue().importPriceProperty().asObject());
        colImportPrice.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colImportPrice.setOnEditCommit(event -> {
            ImportItem item = event.getRowValue();
            item.setImportPrice(event.getNewValue() == null ? 0 : event.getNewValue());
            updateTotal();
        });
    }

    @FXML
    private void handleSearchProduct() {
        String keyword = txtSearchProduct.getText();
        Category selected = cmbCategory.getSelectionModel().getSelectedItem();
        int catId = (selected != null) ? selected.getId() : 0;
        loadProducts(keyword, catId);
    }

    private void loadProducts(String keyword, int categoryId) {
        productGrid.getChildren().clear();
        List<Product> list = productDAO.search(keyword);
        if (categoryId > 0) {
            list = list.stream().filter(p -> p.getCategoryId() == categoryId).toList();
        }

        int col = 0;
        int row = 0;
        for (Product p : list) {
            VBox card = createProductCard(p);
            productGrid.add(card, col, row);
            col++;
            if (col > 2) { // 3 columns
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-padding: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setSpacing(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);

        Label icon = new Label("📦");
        icon.setStyle("-fx-font-size: 30px;");

        Label name = new Label(p.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        name.setWrapText(true);

        Label info = new Label("Kho: " + p.getStock() + " | Size: " + p.getSize());
        info.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");

        Button btnAdd = new Button("Thêm vào phiếu");
        btnAdd.getStyleClass().add("btn-secondary");
        btnAdd.setOnAction(e -> addToImport(p));

        card.getChildren().addAll(icon, name, info, btnAdd);
        return card;
    }

    private void addToImport(Product p) {
        for (ImportItem item : importList) {
            if (item.getProduct().getId() == p.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                importTable.refresh();
                updateTotal();
                return;
            }
        }
        
        ImportItem newItem = new ImportItem(p, 1, 0.0); // Default import price 0
        importList.add(newItem);
        updateTotal();
    }

    @FXML
    private void handleClearList() {
        importList.clear();
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (ImportItem item : importList) {
            total += item.getQuantity() * item.getImportPrice();
        }
        lblTotalAmount.setText(NumberFormat.getNumberInstance(Locale.US).format(total).replace(",", ".") + "đ");
    }

    @FXML
    private void handleImport() {
        if (importList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Danh sách nhập trống!");
            return;
        }
        Supplier supplier = cmbSupplier.getSelectionModel().getSelectedItem();
        if (supplier == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn nhà cung cấp!");
            return;
        }

        // Validate import price
        for (ImportItem item : importList) {
            if (item.getImportPrice() <= 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Giá nhập của sản phẩm " + item.getProduct().getName() + " không hợp lệ!");
                return;
            }
        }

        // Create Order
        ImportOrder order = new ImportOrder();
        order.setSupplierId(supplier.getId());
        double total = importList.stream().mapToDouble(i -> i.getQuantity() * i.getImportPrice()).sum();
        order.setTotalAmount(total);
        order.setImportDate(LocalDate.now().toString());

        // Create Details
        List<ImportDetail> details = new ArrayList<>();
        for (ImportItem item : importList) {
            ImportDetail d = new ImportDetail();
            d.setProductId(item.getProduct().getId());
            d.setQuantity(item.getQuantity());
            d.setImportPrice(item.getImportPrice());
            details.add(d);
        }

        // Save to DB
        boolean success = importDAO.createImportOrder(order, details);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Nhập kho thành công! Tồn kho đã được cập nhật.");
            handleClearList();
            loadProducts(txtSearchProduct.getText(), 
                cmbCategory.getSelectionModel().getSelectedItem() != null ? cmbCategory.getSelectionModel().getSelectedItem().getId() : 0);
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi lưu phiếu nhập!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class cho TableView
    public static class ImportItem {
        private final Product product;
        private final SimpleStringProperty productName;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty importPrice;

        public ImportItem(Product product, int quantity, double importPrice) {
            this.product = product;
            this.productName = new SimpleStringProperty(product.getName());
            this.quantity = new SimpleIntegerProperty(quantity);
            this.importPrice = new SimpleDoubleProperty(importPrice);
        }

        public Product getProduct() { return product; }
        
        public String getProductName() { return productName.get(); }
        public SimpleStringProperty productNameProperty() { return productName; }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int q) { quantity.set(q); }
        public SimpleIntegerProperty quantityProperty() { return quantity; }

        public double getImportPrice() { return importPrice.get(); }
        public void setImportPrice(double p) { importPrice.set(p); }
        public SimpleDoubleProperty importPriceProperty() { return importPrice; }
    }
}
