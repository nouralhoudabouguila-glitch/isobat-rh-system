package com.rh.components;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NotificationToast {

    private static final int HEIGHT = 70;
    private static final int WIDTH = 380;

    /**
     * Affiche une notification toast dans l'application
     * @param root StackPane parent
     * @param titre Titre de la notification
     * @param message Message
     * @param type "urgent", "success", "info"
     */
    public static void show(StackPane root, String titre, String message, String type) {
        if (root == null) return;

        HBox notification = new HBox(15);
        notification.setAlignment(Pos.CENTER_LEFT);

        String bgColor = "#FFFFFF";
        String borderColor = "#E8DDD0";
        String icon = "🔔";

        switch (type) {
            case "urgent":
                bgColor = "#FFF0F0";
                borderColor = "#75070C";
                icon = "🔴";
                break;
            case "success":
                bgColor = "#F0F5E8";
                borderColor = "#4F6815";
                icon = "✅";
                break;
            case "info":
                bgColor = "#E6F1FB";
                borderColor = "#0C447C";
                icon = "ℹ️";
                break;
            default:
                bgColor = "#FFFFFF";
                borderColor = "#E8DDD0";
                icon = "🔔";
                break;
        }

        notification.setStyle(
                "-fx-background-color: " + bgColor + "; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-radius: 12; " +
                        "-fx-border-width: 2; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 5);"
        );
        notification.setPrefWidth(WIDTH);
        notification.setPrefHeight(HEIGHT);
        notification.setPadding(new javafx.geometry.Insets(12, 18, 12, 18));

        // Icône
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px;");

        // Texte
        VBox textBox = new VBox(3);
        Label titleLabel = new Label(titre);
        titleLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 11px; -fx-text-fill: #666666;");
        msgLabel.setWrapText(true);

        textBox.getChildren().addAll(titleLabel, msgLabel);

        // Bouton fermer
        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999; -fx-cursor: hand;");
        closeBtn.setOnMouseClicked(e -> {
            root.getChildren().remove(notification);
        });

        notification.getChildren().addAll(iconLabel, textBox, closeBtn);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        // Positionner en bas à droite
        double x = root.getWidth() - WIDTH - 30;
        double y = root.getHeight() - 100;

        notification.setTranslateX(root.getWidth());
        notification.setTranslateY(y);

        root.getChildren().add(notification);

        // Animation d'entrée
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), notification);
        slideIn.setFromX(root.getWidth());
        slideIn.setToX(x);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        // Animation de sortie après 6 secondes
        PauseTransition pause = new PauseTransition(Duration.seconds(6));
        pause.setOnFinished(e -> {
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), notification);
            slideOut.setFromX(x);
            slideOut.setToX(root.getWidth());
            slideOut.setInterpolator(Interpolator.EASE_IN);
            slideOut.setOnFinished(ev -> {
                root.getChildren().remove(notification);
            });
            slideOut.play();
        });

        slideIn.play();
        pause.play();
    }
}