package controller;

import DAO.CustomerDAO;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AddCustomerController {
    @FXML
    private TextField txtHoTen;

    @FXML
    private TextField txtSdt;

    @FXML
    private TextField txtEmail;

    @FXML
    private Text thongBao;

    private CustomerDAO CustomerDAO = new CustomerDAO(); // Khởi tạo DAO

    @FXML
    private void buttonsave() {
        // Thu thập từ UI
        String HoTen = txtHoTen.getText();
        String Sdt = txtSdt.getText();
        String Email = txtEmail.getText();

        if (HoTen.isEmpty() || Sdt.isEmpty() || Email.isEmpty()) {
            thongBao.setText("Vui lòng điền đầy đủ thông tin!");
            return;

        }
        // 2. Gọi DAO để xử lý logic (Lấy mã và Lưu)
        String maKH = CustomerDAO.getNextMaKH();
        CustomerDAO.insertCustomer(maKH, HoTen, Sdt, Email);
        thongBao.setText("Thêm khách hàng thành công!");
    }

    @FXML
    private void buttonHuy() {
        ((Stage) txtHoTen.getScene().getWindow()).close();
    }
}
