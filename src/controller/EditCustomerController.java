package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import DAO.CustomerDAO;
import models.Customer;

public class EditCustomerController {

    @FXML
    private TextField txtHoTen;
    @FXML
    private TextField txtSDT;
    @FXML
    private TextField txtEmail;

    private Customer customer;

    private CustomerDAO customerDAO = new CustomerDAO();

    // Nhận dự liệu Từ bảng truyền qua
    public void setCustomerData(Customer customer) {
        this.customer = customer;
        txtHoTen.setText(customer.getFullName());
        txtSDT.setText(customer.getPhone());
        txtEmail.setText(customer.getEmail());
    }

    private void closeStage() {
        Stage stage = (Stage) txtHoTen.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void buttonSave() {
        customer.setFullName(txtHoTen.getText());
        customer.setPhone(txtSDT.getText());
        customer.setEmail(txtEmail.getText());

        if (customerDAO.updateCustomer(customer)) {
            // Đóng cửa sổ sau khi lưu thành công
            closeStage();
        }
    }

    @FXML
    private void buttonCancel() {
        closeStage();
    }
}
