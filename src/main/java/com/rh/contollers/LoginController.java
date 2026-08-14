package com.rh.contollers;

import com.rh.models.User;
import com.rh.services.UserService;
import com.rh.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField tfEmail;
    @FXML private PasswordField pfPassword;
    @FXML private CheckBox cbSouvenir;
    @FXML private Label lblErreur;
    @FXML private Region errSpacer;
    @FXML private Button btnLogin;

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
        String mdp = pfPassword.getText();

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

    // ── 🔥 NOUVEAU : Réinitialisation par SMS ──────────────────────────────

    @FXML
    private void handleMdpOublie() {
        // Demander l'email
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("🔐 Réinitialisation du mot de passe");
        emailDialog.setHeaderText("Réinitialisation par SMS");
        emailDialog.setContentText("Entrez votre adresse email :");
        emailDialog.getDialogPane().setPrefWidth(400);

        Optional<String> emailResult = emailDialog.showAndWait();

        emailResult.ifPresent(email -> {
            String emailTrim = email.trim();

            if (!service.emailExists(emailTrim)) {
                showError("❌ Aucun compte trouvé avec cet email.");
                return;
            }

            // Envoyer le code par SMS
            boolean envoye = service.envoyerCodeReinitialisation(emailTrim);

            if (envoye) {
                // Ouvrir la fenêtre de validation du code
                ouvrirDialogueValidation(emailTrim);
            } else {
                showError("❌ Impossible d'envoyer le SMS. Vérifiez votre numéro de téléphone.");
            }
        });
    }

    /**
     * Dialogue pour saisir le code reçu par SMS
     */
    private void ouvrirDialogueValidation(String email) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("🔐 Validation du code");
        dialog.setHeaderText("Un code de validation a été envoyé par SMS.");

        // Boutons
        ButtonType btnValider = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, btnAnnuler);

        // Contenu
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10 0 10 0;");

        Label info = new Label("📱 Entrez le code à 6 chiffres reçu par SMS :");
        info.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-text-fill: #555555;");

        TextField codeField = new TextField();
        codeField.setPromptText("Code à 6 chiffres");
        codeField.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-padding: 8 12;");
        codeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                codeField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (codeField.getText().length() > 6) {
                codeField.setText(codeField.getText().substring(0, 6));
            }
        });

        // Champ pour le nouveau mot de passe
        Label pwdLabel = new Label("Nouveau mot de passe :");
        pwdLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-text-fill: #555555;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe (min 6 caractères)");
        newPasswordField.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-padding: 8 12;");

        Label pwdConfirmLabel = new Label("Confirmer le mot de passe :");
        pwdConfirmLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-text-fill: #555555;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");
        confirmPasswordField.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-padding: 8 12;");

        // Message d'erreur
        Label erreurLabel = new Label();
        erreurLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 11px; -fx-text-fill: #75070C;");
        erreurLabel.setManaged(false);
        erreurLabel.setVisible(false);

        content.getChildren().addAll(info, codeField, pwdLabel, newPasswordField, pwdConfirmLabel, confirmPasswordField, erreurLabel);
        dialog.getDialogPane().setContent(content);

        // Désactiver le bouton Valider tant que les champs ne sont pas remplis
        Button validerButton = (Button) dialog.getDialogPane().lookupButton(btnValider);
        validerButton.setDisable(true);

        // Écouter les changements
        codeField.textProperty().addListener((obs, old, newVal) -> {
            updateButtonState(validerButton, codeField, newPasswordField, confirmPasswordField, erreurLabel);
        });
        newPasswordField.textProperty().addListener((obs, old, newVal) -> {
            updateButtonState(validerButton, codeField, newPasswordField, confirmPasswordField, erreurLabel);
        });
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> {
            updateButtonState(validerButton, codeField, newPasswordField, confirmPasswordField, erreurLabel);
        });

        // Résultat
        dialog.setResultConverter(button -> {
            if (button == btnValider) {
                return codeField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(code -> {
            String nouveauMdp = newPasswordField.getText();
            String confirmMdp = confirmPasswordField.getText();

            // Vérifier les mots de passe
            if (!nouveauMdp.equals(confirmMdp)) {
                showError("❌ Les mots de passe ne correspondent pas.");
                return;
            }

            if (nouveauMdp.length() < 6) {
                showError("❌ Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }

            // Réinitialiser le mot de passe
            boolean reinitialise = service.reinitialiserMotDePasse(email, code, nouveauMdp);

            if (reinitialise) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("✅ Succès");
                success.setHeaderText("Mot de passe réinitialisé !");
                success.setContentText("Votre mot de passe a été modifié avec succès.\nVous pouvez maintenant vous connecter.");
                success.showAndWait();
            } else {
                showError("❌ Code invalide ou expiré. Veuillez réessayer.");
            }
        });
    }

    private void updateButtonState(Button button, TextField codeField, PasswordField pwd1, PasswordField pwd2, Label error) {
        String code = codeField.getText();
        String pwd = pwd1.getText();
        String confirm = pwd2.getText();

        boolean codeOk = code.length() == 6;
        boolean pwdOk = pwd.length() >= 6;
        boolean match = pwd.equals(confirm) && pwdOk;

        button.setDisable(!(codeOk && pwdOk && match));

        // Afficher les erreurs
        error.setManaged(false);
        error.setVisible(false);
        error.setText("");

        if (pwdOk && !match && !confirm.isEmpty()) {
            error.setText("⚠️ Les mots de passe ne correspondent pas.");
            error.setManaged(true);
            error.setVisible(true);
        } else if (!pwdOk && !pwd.isEmpty()) {
            error.setText("⚠️ Le mot de passe doit contenir au moins 6 caractères.");
            error.setManaged(true);
            error.setVisible(true);
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
}