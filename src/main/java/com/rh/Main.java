package com.rh;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/rh/views/main.fxml"));
        Scene scene = new Scene(loader.load(), 1300, 820);
        String css = Main.class.getResource("/com/rh/styles/main.css").toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("ISOBAT — Gestion des Ressources Humaines");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}