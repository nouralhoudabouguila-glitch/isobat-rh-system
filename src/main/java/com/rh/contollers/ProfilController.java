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
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfilController implements Initializable {

    @FXML private Label avatarBig;
    @FXML private Label lblNomComplet;
    @FXML private Label lblRole;
    @FXML private Label lblEmail;
    @FXML private Label lblTelephone;
    @FXML private Label lblStatut;

    private final UserService userService = new UserService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadProfil();
    }

    private void loadProfil() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Rediriger vers login si non connecté
            try {
                Parent root = FXMLLoader.load(
                        getClass().getResource("/com/rh/views/Login.fxml"));
                Stage stage = (Stage) avatarBig.getScene().getWindow();
                stage.setScene(new Scene(root, 800, 600));
                stage.setTitle("ISOBAT — Connexion");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // Afficher les informations
        avatarBig.setText(currentUser.getInitiales());
        lblNomComplet.setText(currentUser.getNomComplet());
        lblRole.setText(currentUser.getRole().getLabel());
        lblEmail.setText(currentUser.getEmail());
        lblTelephone.setText(currentUser.getTelephone() != null && !currentUser.getTelephone().isEmpty()
                ? currentUser.getTelephone() : "Non renseigné");

        // Statut
        if (currentUser.isActif()) {
            lblStatut.setText("🟢 Actif");
            lblStatut.setStyle("-fx-font-family:'Poppins';-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#4F6815;");
        } else {
            lblStatut.setText("🔴 Inactif");
            lblStatut.setStyle("-fx-font-family:'Poppins';-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:#75070C;");
        }

        // Style du rôle
        if (currentUser.getRole() == User.Role.ADMIN) {
            lblRole.setStyle("-fx-font-family:'Poppins';-fx-font-size:13;-fx-text-fill:#FFFFFF;-fx-background-color:#75070C;-fx-background-radius:12px;-fx-padding:4 16 4 16;");
        } else {
            lblRole.setStyle("-fx-font-family:'Poppins';-fx-font-size:13;-fx-text-fill:#FFFFFF;-fx-background-color:#4F6815;-fx-background-radius:12px;-fx-padding:4 16 4 16;");
        }
    }

    @FXML
    private void handleModifier() {
        // Ouvrir le formulaire de modification du profil
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/rh/views/profil_form.fxml"));
            Parent root = loader.load();

            ProfilFormController ctrl = loader.getController();
            ctrl.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Modifier mon profil");
            stage.setScene(new Scene(root, 500, 550));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Recharger le profil après modification
            loadProfil();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer le compte");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        confirm.setContentText("Cette action est irréversible. Toutes vos données seront supprimées.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Supprimer l'utilisateur
                userService.delete(currentUser.getId());

                // Déconnecter
                SessionManager.getInstance().logout();

                try {
                    Parent root = FXMLLoader.load(
                            getClass().getResource("/com/rh/views/Login.fxml"));
                    Stage stage = (Stage) avatarBig.getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 600));
                    stage.setTitle("ISOBAT — Connexion");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}