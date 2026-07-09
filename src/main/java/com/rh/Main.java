package com.rh;

import com.rh.scheduler.NotificationScheduler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/rh/styles/main.css").toExternalForm());

        primaryStage.setTitle("IsotBat - Gestion RH");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Démarrer le scheduler de notifications
        NotificationScheduler.getInstance().demarrer();

        // Arrêter le scheduler à la fermeture
        primaryStage.setOnCloseRequest(e -> {
            NotificationScheduler.getInstance().arreter();
            Platform.exit();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}