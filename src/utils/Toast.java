package utils;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Toast {

    public static void show(Stage ownerStage, String message) {
        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.setStyle("-fx-background-color: rgba(16, 185, 129, 0.95); " +
                       "-fx-text-fill: white; " +
                       "-fx-font-weight: bold; " +
                       "-fx-font-size: 14px; " +
                       "-fx-padding: 10px 20px; " +
                       "-fx-background-radius: 8px;");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        // Position at bottom center of the owner stage
        if (ownerStage != null) {
            toastStage.setX(ownerStage.getX() + ownerStage.getWidth() / 2 - 150);
            toastStage.setY(ownerStage.getY() + ownerStage.getHeight() - 120);
        }

        toastStage.show();

        // Fade in & out transition
        FadeTransition fade = new FadeTransition(Duration.millis(300), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        fade.setOnFinished(e -> {
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                javafx.application.Platform.runLater(() -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(evt -> toastStage.close());
                    fadeOut.play();
                });
            }).start();
        });
    }

    public static void showError(Stage ownerStage, String message) {
        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.setStyle("-fx-background-color: rgba(239, 68, 68, 0.95); " +
                       "-fx-text-fill: white; " +
                       "-fx-font-weight: bold; " +
                       "-fx-font-size: 14px; " +
                       "-fx-padding: 10px 20px; " +
                       "-fx-background-radius: 8px;");

        StackPane root = new StackPane(label);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        if (ownerStage != null) {
            toastStage.setX(ownerStage.getX() + ownerStage.getWidth() / 2 - 150);
            toastStage.setY(ownerStage.getY() + ownerStage.getHeight() - 120);
        }

        toastStage.show();

        FadeTransition fade = new FadeTransition(Duration.millis(300), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        fade.setOnFinished(e -> {
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                javafx.application.Platform.runLater(() -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(evt -> toastStage.close());
                    fadeOut.play();
                });
            }).start();
        });
    }
}
