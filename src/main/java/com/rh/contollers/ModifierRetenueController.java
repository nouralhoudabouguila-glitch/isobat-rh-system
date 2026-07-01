package com.rh.contollers;

import com.rh.models.Retenue;
import com.rh.services.ServiceRetenue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class ModifierRetenueController {

    @FXML
    private TextField txtEmploye;
    @FXML
    private TextField txtDepartement;
    @FXML
    private TextField txtMontant;
    @FXML
    private TextField txtMotif;
    @FXML
    private DatePicker datePickerRetenue;
    @FXML
    private TextArea txtCommentaire;

    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;

    private ServiceRetenue service;
    private Retenue retenueActuelle;
    private Runnable onModificationReussie;

    @FXML
    public void initialize() {
        service = new ServiceRetenue();
    }

    public void setRetenue(Retenue retenue) {
        this.retenueActuelle = retenue;

        // Remplir les champs
        if (retenue.getEmploye() != null) {
            txtEmploye.setText(retenue.getEmploye().getNom() + " " + retenue.getEmploye().getPrenom());
            if (retenue.getEmploye().getDepartement() != null) {
                txtDepartement.setText(retenue.getEmploye().getDepartement().toString());
            }
        }

        txtMontant.setText(String.valueOf(retenue.getMontant()));
        txtMotif.setText(retenue.getMotif());
        datePickerRetenue.setValue(retenue.getDateRetenue());
        txtCommentaire.setText(retenue.getCommentaire());
    }

    @FXML
    private void onModifier() {
        if (!validerFormulaire()) {
            return;
        }

        try {
            retenueActuelle.setMontant(Double.parseDouble(txtMontant.getText().replace(",", ".")));
            retenueActuelle.setMotif(txtMotif.getText().trim());
            retenueActuelle.setDateRetenue(datePickerRetenue.getValue());
            retenueActuelle.setCommentaire(txtCommentaire.getText().trim());

            service.update(retenueActuelle);

            showAlert("Succès", "Retenue modifiée avec succès !", Alert.AlertType.INFORMATION);

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

        if (txtMontant.getText() == null || txtMontant.getText().trim().isEmpty()) {
            erreurs.append("• Le montant est obligatoire\n");
        } else {
            try {
                double montant = Double.parseDouble(txtMontant.getText().replace(",", "."));
                if (montant <= 0) {
                    erreurs.append("• Le montant doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("• Le montant n'est pas valide\n");
            }
        }

        if (txtMotif.getText() == null || txtMotif.getText().trim().isEmpty()) {
            erreurs.append("• Le motif est obligatoire\n");
        }

        if (datePickerRetenue.getValue() == null) {
            erreurs.append("• La date est obligatoire\n");
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