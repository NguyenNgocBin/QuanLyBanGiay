package controller;

import DAO.OrderDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Order;

public class EditOrderController {
    @FXML
    private TextField txtTenKhach; // Should actually be customerId if we want to change customer, but let's keep UI simple for now

    @FXML
    private TextField txtTongTien;

    @FXML
    private TextField txtTrangThai;

    private Order order;

    private OrderDAO orderDAO = new OrderDAO();

    public void setOrder(Order order) {
        this.order = order;
        txtTenKhach.setText(order.getCustomerName());
        txtTenKhach.setDisable(true); // Disable changing name directly because it's a Foreign Key now
        txtTongTien.setText(String.valueOf(order.getTotalAmount()));
        txtTrangThai.setText(order.getStatus());
    }

    private void closeStage() {
        Stage stage = (Stage) txtTenKhach.getScene().getWindow();
        stage.close();
    }

    @FXML
    void buttonCancel(ActionEvent event) {
        closeStage();
    }

    @FXML
    void buttonSave(ActionEvent event) {
        order.setStatus(txtTrangThai.getText());
        try {
            double total = Double.parseDouble(txtTongTien.getText());
            order.setTotalAmount(total);
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Tiền phải nhập số!");
            return;
        }
        if (orderDAO.updateOrder(order)) {
            closeStage();
        }
    }
}
