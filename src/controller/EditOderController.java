package controller;

import DAO.OderDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Oder;

public class EditOderController {
    @FXML
    private TextField txtTenKhach;

    @FXML
    private TextField txtTongTien;

    @FXML
    private TextField txtTrangThai;

    private Oder oder; // Biến giữ đơn hàng đang sửa

    private OderDAO oderDAO = new OderDAO();

    // Nhan du lieu tu bang truyen qua
    public void setOderDate(Oder oder) {
        this.oder = oder;
        txtTenKhach.setText(oder.getCustomerName());
        txtTongTien.setText(String.valueOf(oder.getTotal()));
        txtTrangThai.setText(oder.getStatus());
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
        oder.setCustomerName(txtTenKhach.getText());

        oder.setStatus(txtTrangThai.getText());
        try {
            long total = Long.parseLong(txtTongTien.getText());
            oder.setTotal(total);
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: Tiền phải nhập số!");
            return;
        }
        if (oderDAO.updateOder(oder)) {
            closeStage();
        }
    }
}
