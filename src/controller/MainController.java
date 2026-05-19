package controller;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MainController {

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

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
    private Button btnStaff;

    @FXML
    private Button btnImportHistory;

    @FXML
    private Button btnReports;

    @FXML
    private Button btnSales;

    @FXML
    private Button btnLogout;

    @FXML
    private StackPane contentPane;

    @FXML
    private javafx.scene.control.Label lblUsername;

    @FXML
    private javafx.scene.control.Label lblRole;

    @FXML
    public void initialize() {
        instance = this;
        if (utils.SessionManager.isLoggedIn()) {
            updateSessionInfo();

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

                btnStaff.setVisible(false);
                btnStaff.setManaged(false);

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

    public void updateSessionInfo() {
        if (utils.SessionManager.isLoggedIn()) {
            models.User user = utils.SessionManager.getCurrentUser();
            lblUsername.setText(user.getName());
            
            String roleText = "ADMIN".equalsIgnoreCase(user.getRole()) ? "Quản trị viên" : "Nhân viên bán hàng";
            
            if ("STAFF".equalsIgnoreCase(user.getRole())) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String loginTimeStr = utils.SessionManager.getLoginTime() != null 
                        ? utils.SessionManager.getLoginTime().format(dtf) 
                        : "—";
                
                String revenueStr = formatCurrency(utils.SessionManager.getSessionRevenue());
                
                lblRole.setText(roleText + " | Phiên: " + loginTimeStr + " | Doanh thu: " + revenueStr);
            } else {
                lblRole.setText(roleText);
            }
        }
    }

    private String formatCurrency(double value) {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(value).replace(",", ".") + "đ";
    }

    @FXML
    void switchTab(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        // Chặn quyền truy cập nếu là STAFF nhưng cố ý click hoặc dùng cơ chế khác
        if (!utils.SessionManager.isAdmin()) {
            if (clickedButton == btnDashboard || clickedButton == btnProducts || clickedButton == btnCategories
                || clickedButton == btnSuppliers || clickedButton == btnImportHistory || clickedButton == btnReports
                || clickedButton == btnStaff) {
                System.out.println("Từ chối truy cập: Tài khoản không có quyền Admin.");
                return;
            }
        }
        
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnProducts.getStyleClass().remove("nav-button-active");
        if (btnCategories != null) btnCategories.getStyleClass().remove("nav-button-active");
        if (btnSuppliers != null) btnSuppliers.getStyleClass().remove("nav-button-active");
        if (btnStaff != null) btnStaff.getStyleClass().remove("nav-button-active");
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
        } else if (clickedButton == btnStaff) {
            loadPane("/view/Staff.fxml");
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

    @FXML
    void handleLogout(ActionEvent event) {
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận kết ca & đăng xuất");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có muốn kết ca và đăng xuất không?");

        // Custom buttons in Vietnamese
        javafx.scene.control.ButtonType btnYes = new javafx.scene.control.ButtonType("Có", javafx.scene.control.ButtonBar.ButtonData.YES);
        javafx.scene.control.ButtonType btnNo = new javafx.scene.control.ButtonType("Không", javafx.scene.control.ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(btnYes, btnNo);

        confirm.showAndWait().ifPresent(result -> {
            if (result == btnYes) {
                // Clear session
                utils.SessionManager.clearSession();
                
                try {
                    // Load Login.fxml
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
                    Parent root = loader.load();
                    javafx.scene.Scene scene = new javafx.scene.Scene(root);
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnLogout.getScene().getWindow();
                    stage.setScene(scene);
                    stage.centerOnScreen();
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
