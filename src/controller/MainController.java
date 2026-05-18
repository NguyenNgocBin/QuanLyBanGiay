package controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnOrders;

    @FXML
    private Button btnProducts;

    @FXML
    private Button btnCategories;

    @FXML
    private Button btnSuppliers;

    @FXML
    private Button btnImportHistory;

    @FXML
    private Button btnReports;

    @FXML
    private Button btnSales;

    @FXML
    private StackPane contentPane;

    @FXML
    private javafx.scene.control.Label lblUsername;

    @FXML
    private javafx.scene.control.Label lblRole;

    @FXML
    public void initialize() {
        if (utils.SessionManager.isLoggedIn()) {
            models.User user = utils.SessionManager.getCurrentUser();
            lblUsername.setText(user.getName());
            lblRole.setText("ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản trị viên" : "Nhân viên bán hàng");

            if (!utils.SessionManager.isAdmin()) {
                // Hạn chế giao diện đối với STAFF
                btnDashboard.setVisible(false);
                btnDashboard.setManaged(false);

                btnProducts.setVisible(false);
                btnProducts.setManaged(false);

                btnCategories.setVisible(false);
                btnCategories.setManaged(false);

                btnSuppliers.setVisible(false);
                btnSuppliers.setManaged(false);

                btnImportHistory.setVisible(false);
                btnImportHistory.setManaged(false);

                btnReports.setVisible(false);
                btnReports.setManaged(false);

                // Mặc định cho Nhân viên là màn hình Bán hàng POS
                loadPane("/view/Sale.fxml");
                btnDashboard.getStyleClass().remove("nav-button-active");
                btnSales.getStyleClass().add("nav-button-active");
            } else {
                loadPane("/view/Dashboard.fxml");
            }
        } else {
            loadPane("/view/Dashboard.fxml");
        }
    }

    @FXML
    void switchTab(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Chặn quyền truy cập nếu là STAFF nhưng cố ý click hoặc dùng cơ chế khác
        if (!utils.SessionManager.isAdmin()) {
            if (clickedButton == btnDashboard || clickedButton == btnProducts || clickedButton == btnCategories
                || clickedButton == btnSuppliers || clickedButton == btnImportHistory || clickedButton == btnReports) {
                System.out.println("Từ chối truy cập: Tài khoản không có quyền Admin.");
                return;
            }
        }
        
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnProducts.getStyleClass().remove("nav-button-active");
        if (btnCategories != null) btnCategories.getStyleClass().remove("nav-button-active");
        if (btnSuppliers != null) btnSuppliers.getStyleClass().remove("nav-button-active");
        if (btnImportHistory != null) btnImportHistory.getStyleClass().remove("nav-button-active");
        btnSales.getStyleClass().remove("nav-button-active");
        btnOrders.getStyleClass().remove("nav-button-active");
        btnReports.getStyleClass().remove("nav-button-active");

        clickedButton.getStyleClass().add("nav-button-active");

        if (clickedButton == btnDashboard) {
            loadPane("/view/Dashboard.fxml");
        } else if (clickedButton == btnProducts) {
            loadPane("/view/Product.fxml");
        } else if (clickedButton == btnCategories) {
            loadPane("/view/Category.fxml");
        } else if (clickedButton == btnSuppliers) {
            loadPane("/view/Supplier.fxml");
        } else if (clickedButton == btnImportHistory) {
            loadPane("/view/ImportHistory.fxml");
        } else if (clickedButton == btnSales) {
            loadPane("/view/Sale.fxml");
        } else if (clickedButton == btnOrders) {
            loadPane("/view/Order.fxml");
        } else if (clickedButton == btnReports) {
            loadPane("/view/Reports.fxml");
        } else {
            System.out.println("Tab not implemented yet: " + clickedButton.getText());
        }
    }

    private void loadPane(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentPane.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể load file FXML: " + fxmlPath);
        }
    }
}
