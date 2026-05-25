package controller;

import DAO.CategoryDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Category;

import java.util.List;

public class CategoryController {

    @FXML private TextField txtSearch;
    @FXML private Label lblShowing;
    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, Integer> colProductCount;
    @FXML private TableColumn<Category, Category> colAction;

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ObservableList<Category> allCategories = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadCategories();
        
        // Live search
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            searchCategories(newValue);
        });
    }

    private void setupTable() {
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        categoryTable.setFixedCellSize(50);

        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProductCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getProductCount()).asObject());
        
        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setGraphic(null);
                    return;
                }

                Button edit = new Button("✎");
                Button delete = new Button("⌫");
                edit.getStyleClass().add("icon-action");
                delete.getStyleClass().add("icon-action");
                
                edit.setOnAction(event -> openCategoryDialog(category));
                delete.setOnAction(event -> handleDeleteCategory(category));
                
                HBox actions = new HBox(8, edit, delete);
                actions.setAlignment(Pos.CENTER_LEFT);
                setGraphic(actions);
            }
        });
    }

    private void loadCategories() {
        List<Category> list = categoryDAO.getAllCategories();
        allCategories.setAll(list);
        categoryTable.setItems(allCategories);
        lblShowing.setText("Hiển thị " + allCategories.size() + " danh mục");
    }

    private void searchCategories(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadCategories();
            return;
        }
        List<Category> list = categoryDAO.searchCategories(keyword);
        categoryTable.setItems(FXCollections.observableArrayList(list));
        lblShowing.setText("Hiển thị " + list.size() + " danh mục");
    }

    @FXML
    void handleNewCategory(ActionEvent event) {
        openCategoryDialog(null);
    }

    private void openCategoryDialog(Category category) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(category == null ? "Thêm danh mục mới" : "Sửa danh mục");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #0b111e; -fx-text-fill: white;");

        Label label = new Label(category == null ? "TÊN DANH MỤC MỚI" : "TÊN DANH MỤC");
        label.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px;");

        TextField txtName = new TextField();
        txtName.setPromptText("Nhập tên danh mục...");
        txtName.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4; -fx-padding: 8;");
        if (category != null) {
            txtName.setText(category.getName());
        }

        Button btnSave = new Button("Lưu lại");
        btnSave.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #A855F7); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");
        
        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");

        HBox buttonBox = new HBox(10, btnSave, btnCancel);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        layout.getChildren().addAll(label, txtName, buttonBox);

        btnSave.setOnAction(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                showError("Tên danh mục không được để trống!");
                return;
            }

            if (category == null) {
                Category newCat = new Category(0, name);
                if (categoryDAO.addCategory(newCat)) {
                    dialog.close();
                    loadCategories();
                    showInfo("Thêm danh mục thành công!");
                } else {
                    showError("Thêm danh mục thất bại!");
                }
            } else {
                category.setName(name);
                if (categoryDAO.updateCategory(category)) {
                    dialog.close();
                    loadCategories();
                    showInfo("Cập nhật danh mục thành công!");
                } else {
                    showError("Cập nhật danh mục thất bại!");
                }
            }
        });

        btnCancel.setOnAction(e -> dialog.close());

        Scene scene = new Scene(layout, 350, 180);
        utils.ThemeManager.applyTheme(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void handleDeleteCategory(Category category) {
        // Validation: Không cho xóa nếu đang có sản phẩm thuộc danh mục
        if (categoryDAO.hasProducts(category.getId())) {
            showError("Không thể xóa danh mục này vì vẫn còn sản phẩm đang thuộc danh mục!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa danh mục");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn xóa danh mục \"" + category.getName() + "\"?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (categoryDAO.deleteCategory(category.getId())) {
                    loadCategories();
                    showInfo("Xóa danh mục thành công!");
                } else {
                    showError("Xóa danh mục thất bại!");
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
