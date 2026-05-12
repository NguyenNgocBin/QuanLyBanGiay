package controller;

import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class MainController {

    @FXML
    private BorderPane contentArea;

    @FXML
    public void initialize() {
        loadPage("Dashboard");
    }

    @FXML
    private void hienThiTrangChu(MouseEvent event) {
        loadPage("Dashboard");
    }

    @FXML
    private void hienThiSanPham(MouseEvent event) {
        loadPage("Product");
    }

    @FXML
    private void hienThiKhachHang(MouseEvent event) {
        loadPage("Customer");
    }

    @FXML
    private void hienThiBanHang(MouseEvent event) {
        loadPage("Sale");
    }

    @FXML
    private void hienThiDonHang(MouseEvent event) {
        loadPage("Order");
    }

    @FXML
    private void xuLyDangXuat(MouseEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Đăng xuất");
            alert.setHeaderText(null);
            alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");
            // Chờ người dùng bấm nút
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Node source = (Node) event.getSource();
                Stage currentStage = (Stage) source.getScene().getWindow();
                currentStage.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
                Parent root = loader.load();
                // Tạo Stage mới cho trang Login
                Stage loginStage = new Stage();
                loginStage.setTitle("Đăng nhập hệ thống");
                loginStage.setScene(new Scene(root));
                loginStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Nếu lỗi, hiện thông báo lỗi đường dẫn file
            System.err.println("Lỗi: Không tìm thấy file Login.fxml hoặc lỗi load file.");
        }
    }

    @FXML
    private void loadPage(String page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + page + ".fxml"));
            Parent root = loader.load();
            contentArea.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}