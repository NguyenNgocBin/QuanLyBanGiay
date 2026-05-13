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
    private Button btnReports;

    @FXML
    private Button btnSales;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        // Load Dashboard by default
        loadPane("/view/Dashboard.fxml");
    }

    @FXML
    void switchTab(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnProducts.getStyleClass().remove("nav-button-active");
        btnSales.getStyleClass().remove("nav-button-active");
        btnOrders.getStyleClass().remove("nav-button-active");
        btnReports.getStyleClass().remove("nav-button-active");

        clickedButton.getStyleClass().add("nav-button-active");

        if (clickedButton == btnDashboard) {
            loadPane("/view/Dashboard.fxml");
        } else if (clickedButton == btnProducts) {
            loadPane("/view/Product.fxml");
        } else if (clickedButton == btnSales) {
            loadPane("/view/Sale.fxml");
        } else if (clickedButton == btnOrders) {
            loadPane("/view/Order.fxml");
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
