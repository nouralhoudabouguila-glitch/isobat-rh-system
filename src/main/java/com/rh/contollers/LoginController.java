package com.rh.contollers;

import com.rh.models.User;
import com.rh.services.UserService;
import com.rh.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField     tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private CheckBox      cbSouvenir;
    @FXML private Label         lblErreur;
    @FXML private Region        errSpacer;
    @FXML private Button        btnLogin;

    private final UserService service = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pfPassword.setOnAction(e -> handleLogin());
        tfEmail.requestFocus();
        clearError();
    }

    @FXML
    private void handleLogin() {
        clearError();

        String email = tfEmail.getText().trim();
        String mdp   = pfPassword.getText();

        if (email.isEmpty()) {
            showError("Veuillez saisir votre adresse email.");
            tfEmail.requestFocus();
            return;
        }
        if (mdp.isEmpty()) {
            showError("Veuillez saisir votre mot de passe.");
            pfPassword.requestFocus();
            return;
        }

        User user = service.login(email, mdp);
        if (user == null) {
            showError("Email ou mot de passe incorrect. Vérifiez vos identifiants.");
            pfPassword.clear();
            pfPassword.requestFocus();
            return;
        }

        SessionManager.getInstance().setCurrentUser(user);
        navigateToMain();
    }

    @FXML
    private void handleSignup() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/rh/views/Signup.fxml"));
            Stage stage = (Stage) tfEmail.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("ISOBAT — Inscription");
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMdpOublie() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Mot de passe oublié");
        dlg.setHeaderText("Réinitialisation du mot de passe");
        dlg.setContentText("Entrez votre adresse email :");
        dlg.showAndWait().ifPresent(email -> {
            if (service.emailExists(email.trim())) {
                showInfo("Un email de réinitialisation a été envoyé à " + email);
            } else {
                showError("Aucun compte trouvé avec cet email.");
            }
        });
    }

    private void navigateToMain() {
        try {
            String fxmlFile = "/com/rh/views/Main.fxml";
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlFile));

            Parent root = loader.load();

            String cssPath = "/com/rh/styles/main.css";
            if (getClass().getResource(cssPath) != null) {
                root.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }

            MainController ctrl = loader.getController();
            ctrl.updateUserInfo();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 760));
            stage.setTitle("ISOBAT — Gestion RH & Finance");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'application: " + e.getMessage());
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void showError(String msg) {
        lblErreur.setText("⚠  " + msg);
        lblErreur.setVisible(true);
        lblErreur.setManaged(true);
        errSpacer.setVisible(true);
        errSpacer.setManaged(true);

        tfEmail.getStyleClass().add("login-field-error");
        pfPassword.getStyleClass().add("login-field-error");
    }

    private void clearError() {
        lblErreur.setVisible(false);
        lblErreur.setManaged(false);
        errSpacer.setVisible(false);
        errSpacer.setManaged(false);

        tfEmail.getStyleClass().remove("login-field-error");
        pfPassword.getStyleClass().remove("login-field-error");
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Information");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}