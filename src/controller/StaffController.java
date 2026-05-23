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
        
        // Cấu hình hiển thị cột Vai trò bằng ComboBox để thay đổi trực tiếp
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        colRole.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList("Nhân viên", "Quản trị"));
            private boolean isUpdating = false;

            {
                comboBox.setOnAction(event -> {
                    if (isUpdating) return;

                    User user = getTableView().getItems().get(getIndex());
                    if (user == null) return;

                    String selected = comboBox.getValue();
                    String newRole = "Nhân viên".equals(selected) ? "STAFF" : "ADMIN";

                    if (!newRole.equalsIgnoreCase(user.getRole())) {
                        // Chặn tự thay đổi quyền của chính mình
                        if (utils.SessionManager.getCurrentUser() != null &&
                            user.getId() == utils.SessionManager.getCurrentUser().getId()) {
                            showError("Bạn không thể tự thay đổi quyền của chính mình!");
                            isUpdating = true;
                            comboBox.setValue("ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản trị" : "Nhân viên");
                            isUpdating = false;
                            return;
                        }

                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Thay đổi quyền hạn");
                        confirm.setHeaderText(null);
                        confirm.setContentText("Bạn có chắc chắn muốn thay đổi vai trò của \"" + user.getName() + "\" thành \"" + selected + "\"?");

                        confirm.showAndWait().ifPresent(result -> {
                            if (result == ButtonType.OK) {
                                if (userDAO.updateUserRole(user.getId(), newRole)) {
                                    user.setRole(newRole);
                                    showInfo("Thay đổi quyền hạn thành công!");
                                    loadStaff();
                                } else {
                                    showError("Thay đổi quyền hạn thất bại!");
                                    isUpdating = true;
                                    comboBox.setValue("ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản trị" : "Nhân viên");
                                    isUpdating = false;
                                }
                            } else {
                                isUpdating = true;
                                comboBox.setValue("ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản trị" : "Nhân viên");
                                isUpdating = false;
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                    return;
                }

                isUpdating = true;
                comboBox.setValue("ADMIN".equalsIgnoreCase(role) ? "Quản trị" : "Nhân viên");

                User user = getTableView().getItems().get(getIndex());
                if (user != null && utils.SessionManager.getCurrentUser() != null &&
                    user.getId() == utils.SessionManager.getCurrentUser().getId()) {
                    comboBox.setDisable(true);
                } else {
                    comboBox.setDisable(false);
                }
                isUpdating = false;

                setGraphic(comboBox);
                setAlignment(Pos.CENTER);
            }
        });

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
        // Chặn tự xóa chính mình
        if (utils.SessionManager.getCurrentUser() != null &&
            user.getId() == utils.SessionManager.getCurrentUser().getId()) {
            showError("Bạn không thể tự xóa tài khoản của chính mình!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa tài khoản");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc chắn muốn xóa tài khoản \"" + user.getName() + "\" không?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (userDAO.deleteUser(user.getId())) {
                    loadStaff();
                    showInfo("Xóa tài khoản thành công!");
                } else {
                    showError("Xóa tài khoản thất bại!");
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
