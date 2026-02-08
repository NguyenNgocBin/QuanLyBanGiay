package controller;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import DAO.OderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Oder;
import javafx.scene.control.TableCell;
import javafx.geometry.Pos;

public class OderController {

    @FXML
    private TableView<Oder> tableOrders;

    @FXML
    private TableColumn<Oder, Integer> colId;

    @FXML
    private TableColumn<Oder, String> colCustomer;

    @FXML
    private TableColumn<Oder, Long> colTotal;

    @FXML
    private TableColumn<Oder, Date> colDate;

    @FXML
    private TableColumn<Oder, String> colStatus;

    @FXML
    private TextField txtSearch;

    // Tính đóng gói-bảo vệ dữ liệu
    // 2. Khai báo DAO và List chứa dữ liệu
    private OderDAO oderDAO = new OderDAO();
    private ObservableList<Oder> oderList = FXCollections.observableArrayList();

    // Tính trừu tượng
    @FXML
    public void initialize() {
        // 1. Cấu hình cột cơ bản
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colStatus.setCellFactory(column -> new TableCell<Oder, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER); // Căn giữa
                    // Xóa class cũ để tránh bị chồng màu
                    getStyleClass().removeAll("status-success", "status-warning", "status-danger");
                    switch (item) {
                        case "Đã thanh toán":
                            getStyleClass().add("status-success");
                            break;
                        case "Chờ xử lý":
                            getStyleClass().add("status-danger");
                            break;
                        case "Đang giao hàng":
                            getStyleClass().add("status-warning");
                            break;
                        default:
                            setStyle("-fx-text-fill: black;");
                            break;
                    }
                }
            }
        });

        loadData();
        setUpContextMenu();
        setupSearch();
    }

    private void loadData() {
        try {
            // Lấy dữ liệu từ Database
            List<Oder> dataFromDB = oderDAO.getAllOder();
            oderList.clear();
            oderList.addAll(dataFromDB);

            // Đổ dữ liệu vào bảng
            tableOrders.setItems(oderList);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi load dữ liệu lên bảng.");
        }
    }

    private void setUpContextMenu() {
        // Tao cac item cho menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Chỉnh Sửa");
        MenuItem deleteItem = new MenuItem("Xóa");

        // Hiện thị xoá, sửa
        contextMenu.getItems().addAll(editItem, deleteItem);
        tableOrders.setRowFactory(tv -> {
            TableRow<Oder> row = new TableRow<>();
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        });

        // Xử lý sự kiện cho item "Chỉnh Sửa"
        editItem.setOnAction(event -> {
            Oder selected = tableOrders.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEditOder(selected);
            }
        });
        // Xử lý sự kiệN cho item "Xoá"
        deleteItem.setOnAction(event -> {
            Oder selected = tableOrders.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteOder(selected);
            }
        });
    }

    private void handleEditOder(Oder oder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditOder.fxml"));
            Parent root = loader.load();

            // Lấy controller và truyền dữ liệu
            EditOderController controller = loader.getController();
            controller.setOderDate(oder);

            // Hiển thị cửa sổ
            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa khách hàng");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Cập nhật lại bảng sau khi đóng cửa sổ
            tableOrders.refresh();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Lỗi");
            alert.show();
        }
    }

    private void handleDeleteOder(Oder oder) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa khách hàng: " + oder.getCustomerName() + "?");
        alert.setContentText("Hành động này không thể hoàn tác.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            // Bước 1: Gọi DAO để xóa trong Cơ sở dữ liệu
            boolean success = new OderDAO().deleteOder(oder.getId());

            if (success) {
                oderList.remove(oder);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Thông báo");
                info.setHeaderText(null);
                info.setContentText("Đã xóa thành công!");
                info.showAndWait();
            }
        }
    }

    // Chức năng tìm kiếm
    private void setupSearch() {
        // 1. Tạo bộ lọc bao quanh danh sách gốc (oderList)
        FilteredList<Oder> filteredData = new FilteredList<>(oderList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(oder -> {
                // Nếu ô tìm kiếm trống thì Hiện tất cả
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // không phân biệt hoa/thường
                String lowerCaseFilter = newValue.toLowerCase();

                // 1. Tìm theo Tên Khách Hàng
                if (oder.getCustomerName() != null && oder.getCustomerName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // 2. Tìm theo Trạng Thái
                else if (oder.getStatus() != null && oder.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                // 3. Tìm theo ID
                else if (String.valueOf(oder.getId()).contains(lowerCaseFilter)) {
                    return true;
                }
                // Nếu không trùng cái nào ở trên -> Ẩn dòng đó đi
                return false;
            });
        });

        // 3. Bọc trong SortedList để giữ chức năng sắp xếp của bảng
        SortedList<Oder> sortedData = new SortedList<>(filteredData);
        // Kết nối việc sắp xếp của SortedList với TableView
        sortedData.comparatorProperty().bind(tableOrders.comparatorProperty());
        // 4. Đổ dữ liệu đã lọc vào bảng
        tableOrders.setItems(sortedData);
    }
}
