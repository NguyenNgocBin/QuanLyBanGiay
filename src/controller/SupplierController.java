package controller;

import DAO.SupplierDAO;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Supplier;

import java.util.List;

public class SupplierController {

    @FXML private TextField txtSearch;
    @FXML private Label lblShowing;
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, String> colCode;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colAddress;
    @FXML private TableColumn<Supplier, Integer> colImportCount;
    @FXML private TableColumn<Supplier, Supplier> colAction;

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final ObservableList<Supplier> allSuppliers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadSuppliers();

        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            searchSuppliers(newValue);
        });
    }

    private void setupTable() {
        supplierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        supplierTable.setFixedCellSize(50);

        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplierCode()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colAddress.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress()));
        colImportCount.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getImportCount()).asObject());

        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Supplier supplier, boolean empty) {
                super.updateItem(supplier, empty);
                if (empty || supplier == null) {
                    setGraphic(null);
                    return;
                }

                Button edit = new Button("✎");
                Button delete = new Button("⌫");
                edit.getStyleClass().add("icon-action");
                delete.getStyleClass().add("icon-action");

                edit.setOnAction(event -> openSupplierDialog(supplier));
                delete.setOnAction(event -> handleDeleteSupplier(supplier));

                HBox actions = new HBox(8, edit, delete);
                actions.setAlignment(Pos.CENTER_LEFT);
                setGraphic(actions);
            }
        });
    }

    private void loadSuppliers() {
        List<Supplier> list = supplierDAO.getAll();
        allSuppliers.setAll(list);
        supplierTable.setItems(allSuppliers);
        lblShowing.setText("Hiển thị " + allSuppliers.size() + " nhà cung cấp");
    }

    private void searchSuppliers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadSuppliers();
            return;
        }
        List<Supplier> list = supplierDAO.search(keyword);
        supplierTable.setItems(FXCollections.observableArrayList(list));
        lblShowing.setText("Hiển thị " + list.size() + " nhà cung cấp");
    }

    @FXML
    void handleNewSupplier(ActionEvent event) {
        openSupplierDialog(null);
    }

    private void openSupplierDialog(Supplier supplier) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(supplier == null ? "Thêm nhà cung cấp mới" : "Sửa thông tin nhà cung cấp");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #0b111e; -fx-text-fill: white;");

        // Styling method for modern labels
        String labelStyle = "-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px;";
        String inputStyle = "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-border-color: #334155; -fx-border-radius: 4; -fx-padding: 8; -fx-width: 250px;";

        // Fields
        Label lblCode = new Label("MÃ NHÀ CUNG CẤP");
        lblCode.setStyle(labelStyle);
        TextField txtCode = new TextField();
        txtCode.setStyle(inputStyle);
        if (supplier != null) {
            txtCode.setText(supplier.getSupplierCode());
            txtCode.setEditable(false);
            txtCode.setDisable(true);
        } else {
            txtCode.setText("NCC" + (System.currentTimeMillis() % 100000));
        }

        Label lblName = new Label("TÊN NHÀ CUNG CẤP (*)");
        lblName.setStyle(labelStyle);
        TextField txtName = new TextField();
        txtName.setStyle(inputStyle);
        txtName.setPromptText("Nhập tên nhà cung cấp...");
        if (supplier != null) txtName.setText(supplier.getName());

        Label lblPhone = new Label("SỐ ĐIỆN THOẠI (*)");
        lblPhone.setStyle(labelStyle);
        TextField txtPhone = new TextField();
        txtPhone.setStyle(inputStyle);
        txtPhone.setPromptText("Ví dụ: 0987654321");
        if (supplier != null) txtPhone.setText(supplier.getPhone());

        Label lblEmail = new Label("EMAIL");
        lblEmail.setStyle(labelStyle);
        TextField txtEmail = new TextField();
        txtEmail.setStyle(inputStyle);
        txtEmail.setPromptText("Ví dụ: ncc@gmail.com");
        if (supplier != null) txtEmail.setText(supplier.getEmail());

        Label lblAddress = new Label("ĐỊA CHỈ");
        lblAddress.setStyle(labelStyle);
        TextField txtAddress = new TextField();
        txtAddress.setStyle(inputStyle);
        txtAddress.setPromptText("Nhập địa chỉ nhà cung cấp...");
        if (supplier != null) txtAddress.setText(supplier.getAddress());

        grid.add(lblCode, 0, 0); grid.add(txtCode, 1, 0);
        grid.add(lblName, 0, 1); grid.add(txtName, 1, 1);
        grid.add(lblPhone, 0, 2); grid.add(txtPhone, 1, 2);
        grid.add(lblEmail, 0, 3); grid.add(txtEmail, 1, 3);
        grid.add(lblAddress, 0, 4); grid.add(txtAddress, 1, 4);

        Button btnSave = new Button("Lưu lại");
        btnSave.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #A855F7); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");

        Button btnCancel = new Button("Hủy");
        btnCancel.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 4; -fx-cursor: hand;");

        HBox buttonBox = new HBox(10, btnSave, btnCancel);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 5);

        btnSave.setOnAction(e -> {
            String code = txtCode.getText().trim();
            String name = txtName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String address = txtAddress.getText().trim();

            // Validation
            if (name.isEmpty() || phone.isEmpty()) {
                showError("Tên nhà cung cấp và Số điện thoại không được để trống!");
                return;
            }

            if (!phone.matches("^0[0-9]{9}$")) {
                showError("Số điện thoại không đúng định dạng! Phải bắt đầu bằng số 0 và gồm 10 chữ số.");
                return;
            }

            if (!email.isEmpty() && !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                showError("Email không đúng định dạng!");
                return;
            }

            if (supplier == null) {
                Supplier newSupplier = new Supplier(0, code, name, phone, email, address);
                if (supplierDAO.insertSupplier(newSupplier)) {
                    dialog.close();
                    loadSuppliers();
                    showInfo("Thêm nhà cung cấp mới thành công!");
                } else {
                    showError("Thêm nhà cung cấp mới thất bại!");
                }
            } else {
                supplier.setName(name);
                supplier.setPhone(phone);
                supplier.setEmail(email);
                supplier.setAddress(address);

                if (supplierDAO.updateSupplier(supplier)) {
                    dialog.close();
                    loadSuppliers();
                    showInfo("Cập nhật thông tin nhà cung cấp thành công!");
                } else {
                    showError("Cập nhật thông tin nhà cung cấp thất bại!");
                }
            }
        });

        btnCancel.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid, 420, 320);
        utils.ThemeManager.applyTheme(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void handleDeleteSupplier(Supplier supplier) {
        // Validation: Không cho xóa nếu đang có phiếu nhập liên kết
        if (supplierDAO.hasImportOrders(supplier.getId())) {
            showError("Không thể xóa nhà cung cấp này vì đã có phiếu nhập kho liên kết!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa nhà cung cấp");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn xóa nhà cung cấp \"" + supplier.getName() + "\"?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (supplierDAO.deleteSupplier(supplier.getId())) {
                    loadSuppliers();
                    showInfo("Xóa nhà cung cấp thành công!");
                } else {
                    showError("Xóa nhà cung cấp thất bại!");
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
