package com.rh.contollers;

import com.rh.models.User;
import com.rh.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SignupController implements Initializable {

    @FXML private TextField tfNom;
    @FXML private TextField tfPrenom;
    @FXML private TextField tfEmail;
    @FXML private TextField tfTelephone;
    @FXML private PasswordField pfPassword;
    @FXML private PasswordField pfPasswordConfirm;
    @FXML private Button btnSignup;
    @FXML private Label lblErreur;
    @FXML private Label errNom;
    @FXML private Label errPrenom;
    @FXML private Label errEmail;
    @FXML private Label errTelephone;
    @FXML private Label errPassword;
    @FXML private Label errPasswordConfirm;

    private final UserService service = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Appuyer sur Entrée pour s'inscrire
        pfPasswordConfirm.setOnAction(e -> handleSignup());
        // Focus sur le premier champ
        tfNom.requestFocus();
    }

    @FXML
    private void handleSignup() {
        clearErrors();

        boolean ok = true;

        // Validation du Nom
        if (tfNom.getText().trim().isEmpty()) {
            errNom.setText("Le nom est obligatoire");
            errNom.setManaged(true);
            tfNom.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        // Validation du Prénom
        if (tfPrenom.getText().trim().isEmpty()) {
            errPrenom.setText("Le prénom est obligatoire");
            errPrenom.setManaged(true);
            tfPrenom.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        // Validation de l'Email
        String email = tfEmail.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errEmail.setText("Email invalide (ex: nom@domaine.com)");
            errEmail.setManaged(true);
            tfEmail.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        // Validation du Téléphone (optionnel)
        String telephone = tfTelephone.getText().trim();
        if (!telephone.isEmpty() && !telephone.matches("^[0-9\\s\\+\\-\\.]{8,15}$")) {
            errTelephone.setText("Numéro invalide");
            errTelephone.setManaged(true);
            tfTelephone.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        // Validation du Mot de passe
        String mdp = pfPassword.getText();
        if (mdp.length() < 6) {
            errPassword.setText("Minimum 6 caractères");
            errPassword.setManaged(true);
            pfPassword.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        // Validation de la confirmation
        if (!mdp.equals(pfPasswordConfirm.getText())) {
            errPasswordConfirm.setText("Les mots de passe ne correspondent pas");
            errPasswordConfirm.setManaged(true);
            pfPasswordConfirm.setStyle("-fx-border-color:#75070C;-fx-border-width:1.5px;");
            ok = false;
        }

        if (!ok) return;

        // ── Création de l'utilisateur ──────────────────────────────────────
        User user = new User();
        user.setNom(tfNom.getText().trim());
        user.setPrenom(tfPrenom.getText().trim());
        user.setEmail(email);
        user.setMotDePasse(mdp);
        user.setTelephone(telephone);
        user.setRole(User.Role.RESPONSABLE_RH); // Par défaut

        if (service.add(user)) {
            // Succès
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText("✅ Compte créé avec succès !");
            success.setContentText("Bienvenue " + user.getNomComplet() + " !\nVous pouvez maintenant vous connecter.");
            success.showAndWait();
            goToLogin();
        } else {
            showError("❌ Cet email est déjà utilisé. Veuillez vous connecter ou utiliser un autre email.");
        }
    }

    @FXML
    private void handleLogin() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/rh/views/Login.fxml"));
            Stage stage = (Stage) btnSignup.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("ISOBAT — Connexion");
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearErrors() {
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);

        errNom.setText("");
        errNom.setManaged(false);
        errPrenom.setText("");
        errPrenom.setManaged(false);
        errEmail.setText("");
        errEmail.setManaged(false);
        errTelephone.setText("");
        errTelephone.setManaged(false);
        errPassword.setText("");
        errPassword.setManaged(false);
        errPasswordConfirm.setText("");
        errPasswordConfirm.setManaged(false);

        String base = "-fx-font-family:'Poppins';-fx-font-size:13;-fx-padding:9 12 9 12;" +
                "-fx-background-color:#FFFFFF;-fx-border-color:#E0D0BE;" +
                "-fx-border-radius:8;-fx-background-radius:8;-fx-border-width:1.5;";
        tfNom.setStyle(base);
        tfPrenom.setStyle(base);
        tfEmail.setStyle(base);
        tfTelephone.setStyle(base);
        pfPassword.setStyle(base);
        pfPasswordConfirm.setStyle(base);
    }

    private void showError(String msg) {
        lblErreur.setText("⚠  " + msg);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
    }
}