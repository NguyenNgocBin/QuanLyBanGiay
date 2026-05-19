package controller;

import DAO.UserDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import models.User;

import java.util.List;

public class StaffController {

    @FXML
    private TextField txtSearch;
    @FXML
    private Label lblShowing;
    @FXML
    private TableView<User> staffTable;
    @FXML
    private TableColumn<User, Integer> colId;
    @FXML
    private TableColumn<User, String> colName;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colLastLogin;
    @FXML
    private TableColumn<User, String> colSessionRevenue;
    @FXML
    private TableColumn<User, User> colAction;

    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<User> allStaff = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadStaff();

        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> {
            filterStaff(newValue);
        });
    }

    private void setupTable() {
        staffTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        staffTable.setFixedCellSize(50);

        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(
                "STAFF".equalsIgnoreCase(data.getValue().getRole()) ? "Nhân viên" : "Quản trị"));
        colLastLogin.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getLastLogin() != null ? data.getValue().getLastLogin() : "—"
        ));
        colSessionRevenue.setCellValueFactory(data -> new SimpleStringProperty(
            formatCurrency(data.getValue().getSessionRevenue())
        ));

        colAction.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        colAction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }

                Button delete = new Button("⌫");
                delete.getStyleClass().add("icon-action");
                delete.setStyle("-fx-text-fill: #ef4444;"); // RED color for delete

                delete.setOnAction(event -> handleDeleteStaff(user));

                HBox actions = new HBox(8, delete);
                actions.setAlignment(Pos.CENTER);
                setGraphic(actions);
            }
        });
    }

    private String formatCurrency(double value) {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(value).replace(",", ".") + "đ";
    }

    private void loadStaff() {
        List<User> list = userDAO.getAllStaff();
        allStaff.setAll(list);
        staffTable.setItems(allStaff);
        lblShowing.setText("Hiển thị " + allStaff.size() + " nhân viên");
    }

    private void filterStaff(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            staffTable.setItems(allStaff);
            lblShowing.setText("Hiển thị " + allStaff.size() + " nhân viên");
            return;
        }

        String searchKey = keyword.toLowerCase().trim();
        ObservableList<User> filtered = allStaff
                .filtered(u -> (u.getName() != null && u.getName().toLowerCase().contains(searchKey)) ||
                        (u.getUserName() != null && u.getUserName().toLowerCase().contains(searchKey)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchKey)));
        staffTable.setItems(filtered);
        lblShowing.setText("Hiển thị " + filtered.size() + " nhân viên");
    }

    private void handleDeleteStaff(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa nhân viên");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn xóa nhân viên \"" + user.getName() + "\" không?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (userDAO.deleteUser(user.getId())) {
                    loadStaff();
                    showInfo("Xóa tài khoản nhân viên thành công!");
                } else {
                    showError("Xóa tài khoản nhân viên thất bại!");
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
