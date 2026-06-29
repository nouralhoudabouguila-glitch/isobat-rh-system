package com.rh.contollers;

import com.rh.models.Fournisseur;
import com.rh.services.ServiceFournisseur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AjouterFournisseurController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextArea txtAdresse;  // Changé de TextField à TextArea
    @FXML
    private TextField txtVille;
    @FXML
    private TextField txtPays;
    @FXML
    private TextField txtMatriculeFiscal;

    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnAnnuler;

    private ServiceFournisseur service;
    private Runnable onAjoutReussi;

    @FXML
    public void initialize() {
        service = new ServiceFournisseur();
        ajouterValidateurs();
    }

    private void ajouterValidateurs() {
        txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.startsWith(" ")) {
                txtNom.setText(newVal.trim());
            }
        });
    }

    @FXML
    private void onAjouter() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            Fournisseur fournisseur = new Fournisseur();
            fournisseur.setNom(txtNom.getText().trim());
            fournisseur.setEmail(txtEmail.getText().trim());
            fournisseur.setTelephone(txtTelephone.getText().trim());
            fournisseur.setAdresse(txtAdresse.getText().trim());
            fournisseur.setVille(txtVille.getText().trim());
            fournisseur.setPays(txtPays.getText().trim());
            fournisseur.setMatriculeFiscal(txtMatriculeFiscal.getText().trim());

            service.add(fournisseur);

            showAlert("Succès", "Fournisseur ajouté avec succès !", Alert.AlertType.INFORMATION);

            if (onAjoutReussi != null) {
                onAjoutReussi.run();
            }

            fermerFenetre();

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        txtNom.setStyle("");
        txtEmail.setStyle("");
        txtTelephone.setStyle("");

        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            erreurs.append("• Le nom est obligatoire\n");
            txtNom.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
        }

        if (txtEmail.getText() != null && !txtEmail.getText().trim().isEmpty()) {
            if (!txtEmail.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                erreurs.append("• L'email n'est pas valide\n");
                txtEmail.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        }

        if (txtTelephone.getText() != null && !txtTelephone.getText().trim().isEmpty()) {
            if (!txtTelephone.getText().trim().matches("\\d*")) {
                erreurs.append("• Le téléphone ne doit contenir que des chiffres\n");
                txtTelephone.setStyle("-fx-border-color: #75070C; -fx-border-width: 2; -fx-border-radius: 8;");
            }
        }

        if (erreurs.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes :\n\n" + erreurs.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void onAnnuler() {
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOnAjoutReussi(Runnable callback) {
        this.onAjoutReussi = callback;
    }
}