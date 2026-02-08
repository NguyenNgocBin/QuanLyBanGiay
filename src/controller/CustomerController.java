package controller;

import DAO.CustomerDAO;
import models.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import javafx.scene.control.TableRow;

public class CustomerController {

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Customer> tableKhachHang;
    @FXML
    private TableColumn<Customer, String> colMaKH;
    @FXML
    private TableColumn<Customer, String> colHoTen;
    @FXML
    private TableColumn<Customer, String> colSdt;
    @FXML
    private TableColumn<Customer, String> colEmail;
    @FXML
    private TableColumn<Customer, Double> colTongChiTieu;

    private CustomerDAO customerDAO = new CustomerDAO();

    // Tạo một danh sách gốc để giữ toàn bộ dữ liệu từ DB
    private ObservableList<Customer> masterData = FXCollections.observableArrayList();

    @FXML
    private void themMoiCustomer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AddCustomer.fxml"));

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm khách hàng");
            stage.showAndWait();

            // Sau khi đóng cửa sổ thêm, gọi loadData để bảng tự cập nhật
            loadData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {

        colMaKH.setCellValueFactory(new PropertyValueFactory<>("MaKH"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("HoTen"));
        colSdt.setCellValueFactory(new PropertyValueFactory<>("Sdt"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("Email"));

        colTongChiTieu.setCellValueFactory(new PropertyValueFactory<>("TongchiTieu"));

        loadData();
        setupSearch();
        setUpContextMenu();
    }

    public void loadData() {
        List<Customer> customers = customerDAO.getAllCustomers();
        masterData.setAll(customers);
    }

    private void setupSearch() {
        FilteredList<Customer> filteredData = new FilteredList<>(masterData, p -> true);
        // thay đổi trên ô tìm kiếm
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(customer -> {

                // Nếu ô tìm kiếm trống, hiện tất cả
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (customer.getMaKH().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getHoTen().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (customer.getSdt().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        SortedList<Customer> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableKhachHang.comparatorProperty());

        tableKhachHang.setItems(sortedData);
    }

    private void setUpContextMenu() {
        // tạo các item cho menu
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Chỉnh sửa");
        MenuItem deleteItem = new MenuItem("Xóa");

        // xử lý sự kiện cho sửa
        editItem.setOnAction(event -> {
            Customer selected = tableKhachHang.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEditCustomer(selected);
            }
        });

        // xử lý sự kiện cho xóa
        deleteItem.setOnAction(event -> {
            Customer selected = tableKhachHang.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteCustomer(selected);
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem);

        // Gán ContextMenu vào từng dòng của TableView
        tableKhachHang.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        });
    }

    private void handleDeleteCustomer(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa khách hàng: " + customer.getHoTen() + "?");
        alert.setContentText("Hành động này không thể hoàn tác.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Bước 1: Gọi DAO để xóa trong Cơ sở dữ liệu
            boolean success = customerDAO.deleteCustomer(customer.getMaKH());

            if (success) {
                masterData.remove(customer);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Thông báo");
                info.setHeaderText(null);
                info.setContentText("Đã xóa khách hàng thành công!");
                info.showAndWait();
            }
        }
    }

    private void handleEditCustomer(Customer customer) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditCustomer.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền dữ liệu
            EditCustomerController controller = loader.getController();
            controller.setCustomerData(customer);

            // Hiển thị cửa sổ
            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa khách hàng");
            stage.setScene(new Scene(root));

            stage.showAndWait();

            // Cập nhật lại bảng sau khi đóng cửa sổ
            tableKhachHang.refresh();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Lỗi");
            alert.show();
        }
    }
}