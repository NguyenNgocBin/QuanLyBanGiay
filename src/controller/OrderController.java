package controller;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import DAO.OrderDAO;
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
import models.Order;
import javafx.scene.control.TableCell;
import javafx.geometry.Pos;

public class OrderController {

    @FXML
    private TableView<Order> tableOrders;

    @FXML
    private TableColumn<Order, Integer> colId;

    @FXML
    private TableColumn<Order, String> colCustomer;

    @FXML
    private TableColumn<Order, Double> colTotal; // Changed to Double

    @FXML
    private TableColumn<Order, Date> colDate;

    @FXML
    private TableColumn<Order, String> colStatus;

    @FXML
    private TextField txtSearch;

    private OrderDAO orderDAO = new OrderDAO();
    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount")); // changed from total
        
        colStatus.setCellFactory(column -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item);
                    setAlignment(Pos.CENTER);
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
            List<Order> dataFromDB = orderDAO.getAllOrders(); // renamed method
            orderList.clear();
            orderList.addAll(dataFromDB);
            tableOrders.setItems(orderList);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi khi load dữ liệu lên bảng.");
        }
    }

    private void setUpContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Chỉnh Sửa");
        MenuItem deleteItem = new MenuItem("Xóa");

        contextMenu.getItems().addAll(editItem, deleteItem);
        tableOrders.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(contextMenu);
                }
            });
            return row;
        });

        editItem.setOnAction(event -> {
            Order selected = tableOrders.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEditOrder(selected);
            }
        });

        deleteItem.setOnAction(event -> {
            Order selected = tableOrders.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteOrder(selected);
            }
        });
    }

    private void handleEditOrder(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditOrder.fxml")); // renamed FXML
            Parent root = loader.load();

            EditOrderController controller = loader.getController();
            controller.setOrder(order); // renamed method

            Stage stage = new Stage();
            stage.setTitle("Chỉnh sửa đơn hàng");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadData(); // reload from DB

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Lỗi");
            alert.show();
        }
    }

    private void handleDeleteOrder(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa đơn hàng của khách: " + order.getCustomerName() + "?");
        alert.setContentText("Hành động này không thể hoàn tác.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            boolean success = orderDAO.deleteOrder(order.getId());

            if (success) {
                orderList.remove(order);
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Thông báo");
                info.setHeaderText(null);
                info.setContentText("Đã xóa thành công!");
                info.showAndWait();
            }
        }
    }

    private void setupSearch() {
        FilteredList<Order> filteredData = new FilteredList<>(orderList, p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(order -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();

                if (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (order.getStatus() != null && order.getStatus().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(order.getId()).contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Order> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableOrders.comparatorProperty());
        tableOrders.setItems(sortedData);
    }
}
