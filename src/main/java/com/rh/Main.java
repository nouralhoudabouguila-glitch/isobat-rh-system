package com.rh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Charger la page de connexion en premier
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/rh/views/Login.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 600);

        // 🔥 CORRECTION : Utiliser "/com/rh/styles/main.css" au lieu de "/com.rh/styles/main.css"
        String cssPath = "/com/rh/styles/main.css";
        // Vérifier si le fichier existe avant de l'ajouter
        if (getClass().getResource(cssPath) != null) {
            scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } else {
            System.err.println("CSS non trouvé: " + cssPath);
        }

        primaryStage.setTitle("ISOBAT — Connexion");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}