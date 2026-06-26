package com.rh.contollers;

import com.rh.models.User;
import com.rh.services.UserService;
import com.rh.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfilFormController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errEmail;

    private final UserService userService = new UserService();
    private User user;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Rien de spécial
    }

    public void setUser(User user) {
        this.user = user;
        tfNom.setText(user.getNom());
        tfPrenom.setText(user.getPrenom());
        tfEmail.setText(user.getEmail());
        tfTelephone.setText(user.getTelephone());
    }

    @FXML
    private void handleSave() {
        clearErrors();

        boolean ok = true;

        if (tfNom.getText().trim().isEmpty()) {
            errNom.setText("Le nom est obligatoire");
            errNom.setManaged(true);
            tfNom.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        if (tfPrenom.getText().trim().isEmpty()) {
            errPrenom.setText("Le prénom est obligatoire");
            errPrenom.setManaged(true);
            tfPrenom.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        String email = tfEmail.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errEmail.setText("Email invalide");
            errEmail.setManaged(true);
            tfEmail.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        if (!ok) return;

        // Mettre à jour l'utilisateur
        user.setNom(tfNom.getText().trim());
        user.setPrenom(tfPrenom.getText().trim());
        user.setEmail(email);
        user.setTelephone(tfTelephone.getText().trim());

        if (userService.update(user)) {
            // Mettre à jour la session
            SessionManager.getInstance().setCurrentUser(user);

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Succès");
            info.setHeaderText("Profil modifié");
            info.setContentText("Vos informations ont été mises à jour avec succès.");
            info.showAndWait();

            close();
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void clearErrors() {
        errNom.setText(""); errNom.setManaged(false);
        errPrenom.setText(""); errPrenom.setManaged(false);
        errEmail.setText(""); errEmail.setManaged(false);

        String base = "-fx-font-family:'Poppins';-fx-font-size:13;-fx-padding:10 12 10 12;" +
                "-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;" +
                "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;";
        tfNom.setStyle(base);
        tfPrenom.setStyle(base);
        tfEmail.setStyle(base);
    }

    private void close() {
        Stage stage = (Stage) tfNom.getScene().getWindow();
        stage.close();
    }
}