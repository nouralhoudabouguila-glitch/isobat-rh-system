package com.rh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/rh/views/Login.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);

        // 🔥 Charger le CSS - IMPORTANT !
        String cssPath = "/com/rh/styles/main.css";
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            System.out.println("✅ CSS chargé : " + cssPath);
        } else {
            System.err.println("❌ CSS non trouvé : " + cssPath);
        }

        primaryStage.setTitle("ISOBAT — Connexion");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}