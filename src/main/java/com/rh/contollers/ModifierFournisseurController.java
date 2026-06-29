package com.rh.contollers;

import com.rh.models.Fournisseur;
import com.rh.services.ServiceFournisseur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ModifierFournisseurController {

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
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private ServiceFournisseur service;
    private Fournisseur fournisseurActuel;
    private Runnable onModificationReussie;

    @FXML
    public void initialize() {
        service = new ServiceFournisseur();
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseurActuel = fournisseur;

        txtNom.setText(fournisseur.getNom());
        txtEmail.setText(fournisseur.getEmail());
        txtTelephone.setText(fournisseur.getTelephone());
        txtAdresse.setText(fournisseur.getAdresse());
        txtVille.setText(fournisseur.getVille());
        txtPays.setText(fournisseur.getPays());
        txtMatriculeFiscal.setText(fournisseur.getMatriculeFiscal());
    }

    @FXML
    private void onModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            fournisseurActuel.setNom(txtNom.getText().trim());
            fournisseurActuel.setEmail(txtEmail.getText().trim());
            fournisseurActuel.setTelephone(txtTelephone.getText().trim());
            fournisseurActuel.setAdresse(txtAdresse.getText().trim());
            fournisseurActuel.setVille(txtVille.getText().trim());
            fournisseurActuel.setPays(txtPays.getText().trim());
            fournisseurActuel.setMatriculeFiscal(txtMatriculeFiscal.getText().trim());

            service.update(fournisseurActuel);

            showAlert("Succès", "Fournisseur modifié avec succès !", Alert.AlertType.INFORMATION);

            if (onModificationReussie != null) {
                onModificationReussie.run();
            }

            fermerFenetre();

        } catch (Exception e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
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

    public void setOnModificationReussie(Runnable callback) {
        this.onModificationReussie = callback;
    }
}